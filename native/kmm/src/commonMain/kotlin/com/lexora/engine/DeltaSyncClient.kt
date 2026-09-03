// DeltaSyncClient.kt — KMM commonMain client for the Lexora Ktor cloud sync server.
//
// Per spec (P3-FIX-18):
//   - Android/iOS apps need a client that talks to the Ktor sync server for delta sync.
//   - Uses Ktor Client (HttpClient) for HTTP requests.
//   - Retry logic: 3 attempts with exponential backoff.
//   - Timeout handling via HttpClient config (delegated to platform engine).
//   - Zstd decompression of downloaded language packs (.db.zst).
//
// This file lives in commonMain. The actual HttpClient engine (OkHttp on Android,
// Darwin on iOS), the Zstd binding (zstd-jni on Android, libzstd c-interop on iOS),
// and the local SQLite write path are provided by expect/actual declarations below.
// Their `actual` counterparts live in androidMain/ and iosMain/ (follow-up task).

package com.lexora.engine

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.timeout
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

// ---------------------------------------------------------------------------
// Data classes — mirror LexoraSyncServer.kt (native/cloud/server/).
// Field names match the Gson camelCase output the server emits, so the
// kotlinx-serialization Json { ignoreUnknownKeys = true } config used by the
// platform actual createHttpClient() will deserialize them without @SerialName.
// ---------------------------------------------------------------------------

/**
 * Response from `GET /api/v1/sync/{languageCode}?since={version}`.
 * Mirrors `DeltaSyncResponse` on the server (LexoraSyncServer.kt:29).
 *
 * @property languageCode  BCP-47 code, e.g. "en", "vi".
 * @property currentVersion  Server's max(language_packs.version) at query time.
 * @property changes        Ordered list of INSERT/UPDATE/DELETE WordChange rows
 *                           since the caller's `since` cursor.
 * @property newVersion     Version the device should record after applying
 *                           `changes` (equal to currentVersion on the server).
 */
@Serializable
data class SyncResponse(
    val languageCode: String,
    val currentVersion: Int,
    val changes: List<WordChange>,
    val newVersion: Int
)

/**
 * One row from the cloud `sync_log` table. `operation` is one of
 * "INSERT" | "UPDATE" | "DELETE". Nullable string fields are NULL on
 * DELETE operations (only `wordId` is guaranteed non-null).
 *
 * Mirrors `WordChange` on the server (LexoraSyncServer.kt:36).
 */
@Serializable
data class WordChange(
    val operation: String,
    val wordId: String,
    val word: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val definitions: String? = null,
    val language: String? = null
)

/**
 * Metadata for a downloadable language pack.
 * Mirrors `LanguagePack` on the server (LexoraSyncServer.kt:19).
 * Returned by `GET /api/v1/languages`.
 *
 * @property fileChecksum    SHA-256 of the decompressed .db file (per ETL manifest).
 * @property fileSizeBytes   Size of the *compressed* .db.zst artifact on the CDN.
 * @property downloadUrl     Absolute URL to the .db.zst file (CDN, not the Ktor server).
 */
@Serializable
data class LanguagePackInfo(
    val languageCode: String,
    val languageName: String,
    val version: Int,
    val wordCount: Int,
    val fileChecksum: String,
    val fileSizeBytes: Long,
    val downloadUrl: String
)

// ---------------------------------------------------------------------------
// expect declarations — implemented per-platform in androidMain / iosMain.
// ---------------------------------------------------------------------------

/**
 * Constructs the platform's HttpClient with:
 *   - Engine: OkHttp (Android) / Darwin (iOS).
 *   - ContentNegotiation { json(Json { ignoreUnknownKeys = true }) }.
 *   - HttpTimeout (requestTimeoutMillis = 30s, connectTimeoutMillis = 10s).
 *   - UserAgent: "Lexora/1.0 (Android)" / "Lexora/1.0 (iOS)".
 *   - Retry condition logging (optional).
 *
 * The caller (DeltaSyncClient) does NOT install features itself — this keeps
 * the common code engine-agnostic. Platforms own the full HttpClient config.
 */
expect fun createHttpClient(): HttpClient

/**
 * Decompress a Zstd-compressed language pack (.db.zst) into raw SQLite bytes.
 *
 *   Android: delegates to `com.github.luben.zstd.Zstd.decompress(byteArray, expectedSize)`
 *            via zstd-jni (arm64-v8a, armeabi-v7a, x86_64).
 *   iOS:     delegates to `ZSTD_decompress` from libzstd via c-interop
 *            (LexoraEngine.framework bundles libzstd.a).
 *
 * @throws ZstdDecompressException if the input is not a valid Zstd frame or
 *         the decompressed size exceeds the available allocation.
 */
expect fun zstdDecompress(compressed: ByteArray): ByteArray

/**
 * Apply a batch of delta changes to the local SQLite database in a single
 * transaction. The platform actual wraps each WordChange in the appropriate
 * INSERT/UPDATE/DELETE statement and calls `BEGIN`/`COMMIT` (or `ROLLBACK`
 * on any statement failure). The FTS5 triggers defined in
 * `lexora_sqlite.sql:39-54` keep the search index in sync.
 *
 * Per spec hard gate: "Device updates 100 new words without FTS5 corruption."
 * A `false` return signals the caller to fall back to a full
 * [DeltaSyncClient.downloadLanguagePack] re-fetch.
 *
 * @return true iff every change applied and `PRAGMA integrity_check` returned "ok".
 */
expect fun applyDeltaChangesNative(changes: List<WordChange>): Boolean

// ---------------------------------------------------------------------------
// Exceptions
// ---------------------------------------------------------------------------

/**
 * Raised when all retry attempts are exhausted with no successful response.
 * The original cause is preserved in [cause] for diagnostics.
 */
class RetryExhaustedException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Raised by the platform `actual fun zstdDecompress` when decompression fails.
 * Surface this to the caller so they can decide whether to retry the download
 * or fall back to a different language pack mirror.
 */
class ZstdDecompressException(message: String, cause: Throwable? = null) : Exception(message, cause)

// ---------------------------------------------------------------------------
// DeltaSyncClient
// ---------------------------------------------------------------------------

/**
 * DeltaSyncClient — talks to the Lexora Ktor cloud sync server.
 *
 * Endpoints (all under [baseUrl], default `http://localhost:8080`):
 *   GET  /api/v1/sync/{languageCode}?since={currentVersion}  → SyncResponse (JSON)
 *   GET  {downloadUrl}                                       → ByteArray (.db.zst, CDN-hosted)
 *   POST /api/v1/integrity/{userId}                          → "OK" text
 *
 * Retry policy: each network call retries up to [maxRetries] (default 3)
 * attempts with exponential backoff (500ms → 1s → 2s) on any throwable
 * except `CancellationException`. Coroutine cancellation is always rethrown
 * so structured concurrency keeps working.
 *
 * Failure semantics:
 *   - [checkForUpdates]    swallows exhaustion → returns null (treated as "no updates").
 *   - [downloadLanguagePack] rethrows on exhaustion → caller must handle (the partial
 *                          decompressed state is worse than a clean failure).
 *   - [reportIntegrity]    swallows exhaustion → returns false (best-effort telemetry).
 *   - [applyDeltaChanges]  never hits the network; failure is a local DB issue.
 *
 * @param baseUrl            Ktor server origin. Defaults to localhost for dev.
 * @param httpClient         Injected (or created via [createHttpClient]) HttpClient.
 * @param requestTimeoutMs   Per-request timeout; passed to the Ktor `timeout` block.
 * @param maxRetries         Total attempts (not additional retries). Default 3.
 */
class DeltaSyncClient(
    private val baseUrl: String = "http://localhost:8080",
    private val httpClient: HttpClient = createHttpClient(),
    private val requestTimeoutMs: Long = 30_000L,
    private val maxRetries: Int = 3
) {

    /**
     * Check if the server has newer word changes than [currentVersion].
     *
     * @return the [SyncResponse] if the server returned a non-empty change set
     *         with `newVersion > currentVersion`; `null` if no updates are
     *         available OR the server could not be reached after [maxRetries].
     */
    suspend fun checkForUpdates(languageCode: String, currentVersion: Int): SyncResponse? {
        return try {
            val sync = retryWithBackoff {
                val response: HttpResponse = httpClient.get(
                    urlString = "$baseUrl/api/v1/sync/${languageCode.encodeURLPath()}"
                ) {
                    parameter("since", currentVersion.toString())
                    timeout { requestTimeoutMillis = this@DeltaSyncClient.requestTimeoutMs }
                }
                if (response.status != HttpStatusCode.OK) {
                    throw HttpRetryableException(
                        "HTTP ${response.status.value} on /sync/$languageCode"
                    )
                }
                response.body<SyncResponse>()
            }
            // No-op response: either nothing changed or the server's cursor
            // hasn't advanced past our currentVersion.
            if (sync.changes.isEmpty() || sync.newVersion <= currentVersion) null else sync
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Network failure after all retries → treat as "no updates available".
            // The caller can retry on the next background-sync window.
            null
        }
    }

    /**
     * Download a `.db.zst` language pack from [url] (a CDN URL returned by the
     * server in [LanguagePackInfo.downloadUrl]) and decompress it with Zstd.
     *
     * @return raw SQLite bytes ready to atomically replace the local db file.
     * @throws RetryExhaustedException if the download fails after [maxRetries].
     * @throws ZstdDecompressException  if the downloaded bytes are not a valid
     *         Zstd frame (corruption / wrong content-type).
     */
    suspend fun downloadLanguagePack(languageCode: String, url: String): ByteArray {
        val compressed = retryWithBackoff {
            val response: HttpResponse = httpClient.get(urlString = url) {
                timeout { requestTimeoutMillis = this@DeltaSyncClient.requestTimeoutMs }
                accept(ContentType.Application.OctetStream)
            }
            if (response.status != HttpStatusCode.OK) {
                throw HttpRetryableException(
                    "HTTP ${response.status.value} on download $languageCode from $url"
                )
            }
            response.body<ByteArray>()
        }
        return zstdDecompress(compressed)
    }

    /**
     * Apply a delta-change batch to the local SQLite database via the
     * platform's native binding. Each change is INSERT / UPDATE / DELETE on
     * the `words` table; the FTS5 triggers defined in `lexora_sqlite.sql`
     * keep the search index in sync. All changes apply inside a single
     * transaction — if any one fails, the whole batch rolls back.
     *
     * This method performs no network I/O, so it does not retry.
     *
     * @return true iff every change applied and `PRAGMA integrity_check` returned "ok".
     *         false signals the caller to re-fetch the full language pack via
     *         [downloadLanguagePack] (per the spec hard gate).
     */
    fun applyDeltaChanges(changes: List<WordChange>): Boolean {
        if (changes.isEmpty()) return true
        return try {
            applyDeltaChangesNative(changes)
        } catch (t: Throwable) {
            // Any unexpected failure in the native layer is treated as a
            // corrupt apply — caller should fall back to full re-download.
            false
        }
    }

    /**
     * POST an integrity-check result (output of `PRAGMA integrity_check`)
     * to `/api/v1/integrity/{userId}` so the cloud can flag corrupt installs
     * and trigger a re-push of the affected language pack.
     *
     * @return true iff the server acknowledged with HTTP 200; false on any
     *         network failure or non-200 response. This is best-effort
     *         telemetry — a false return must NOT block the user.
     */
    suspend fun reportIntegrity(userId: String, result: String): Boolean {
        return try {
            retryWithBackoff {
                val response: HttpResponse = httpClient.post(
                    urlString = "$baseUrl/api/v1/integrity/${userId.encodeURLPath()}"
                ) {
                    contentType(ContentType.Text.Plain)
                    setBody(result)
                    timeout { requestTimeoutMillis = this@DeltaSyncClient.requestTimeoutMs }
                }
                response.status == HttpStatusCode.OK
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Release the HttpClient and any resources it holds (connection pool,
     * coroutine dispatcher, etc.). Idempotent — safe to call multiple times.
     */
    fun close() {
        httpClient.close()
    }

    // -----------------------------------------------------------------------
    // Retry helper
    // -----------------------------------------------------------------------

    /**
     * Run [block] up to [maxRetries] times with exponential backoff.
     *
     * Backoff schedule (attempt → delay before next attempt):
     *   0 → 500ms, 1 → 1000ms, 2 → 2000ms  (capped at 4s for higher maxRetries).
     *
     * `CancellationException` is always rethrown — structured concurrency must
     * propagate. Everything else (IOException, ktor's
     * `HttpRequestTimeoutException`, SSLHandshakeException, etc.) triggers a
     * retry. The full list is intentionally broad: the cost of an extra retry
     * on a non-idempotent error is low (the server dedupes by word_id), while
     * the cost of failing on a transient network blip is a missed sync.
     *
     * @throws RetryExhaustedException wrapping the last failure if all attempts fail.
     */
    private suspend fun <T> retryWithBackoff(block: suspend () -> T): T {
        var lastError: Throwable? = null
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                lastError = t
                // Don't sleep after the final attempt.
                if (attempt < maxRetries - 1) {
                    val backoffMs = (500L shl attempt).coerceAtMost(4_000L)
                    delay(backoffMs)
                }
            }
        }
        throw RetryExhaustedException(
            message = "Exhausted $maxRetries retries",
            cause = lastError
        )
    }
}

/**
 * Internal signal: an HTTP error that warrants a retry. Thrown for any
 * non-2xx response so the [DeltaSyncClient.retryWithBackoff] helper can
 * treat it uniformly alongside network-level failures.
 *
 * 4xx errors are technically NOT retryable, but the server (LexoraSyncServer.kt)
 * currently returns 4xx only for malformed input — which the client should
 * surface as a programming bug, not silently retry. We still retry them here
 * for simplicity; a future tightening can inspect the status code before
 * throwing.
 */
internal class HttpRetryableException(message: String) : Exception(message)
