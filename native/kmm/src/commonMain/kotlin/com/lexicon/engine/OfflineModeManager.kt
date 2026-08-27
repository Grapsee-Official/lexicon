// OfflineModeManager.kt — KMM commonMain
//
// Per spec (P4-01): "Offline mode fallback logic and delta sync resilience."
//
// Responsibilities:
//   - Detects if the device is offline (expect fun isOnline()).
//   - When offline: all lookups use local hash table + DAWG only (no cloud).
//   - Queues sync requests for when connection returns.
//   - On reconnect: flushes queue, triggers delta sync.
//   - Handles corrupt local DB: per spec "If corrupt, delete local DB and
//     download full .db.zst package from cloud."
//   - SQLite integrity_check on cold start → if fails, delete + re-download.
//   - Tracks sync state: lastSyncedVersion, lastSyncTimestamp.
//   - Exponential backoff on sync failures (3 retries: 1s, 2s, 4s).
//
// This file lives in commonMain. The `expect fun`s declared below are
// implemented per-platform in:
//   androidMain/.../OfflineModeManager.android.kt
//   iosMain/.../OfflineModeManager.ios.kt
// (Follow-up task — see Next Actions in worklog.)
//
// The class reuses the existing [DeltaSyncClient] (DeltaSyncClient.kt) for
// cloud sync — no second HttpClient is constructed. The integrity / file /
// connectivity primitives that DeltaSyncClient does NOT already provide are
// declared here as new expect funs and intentionally kept disjoint from
// DeltaSyncClient's surface so the two files can evolve independently.

package com.lexicon.engine

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

// ===========================================================================
// expect declarations — platform-specific bits
// ===========================================================================

/**
 * Returns true iff the device currently has at least one usable network
 * transport (WiFi, cellular, ethernet). A transport that is technically "up"
 * but captive-portal-blocked is treated as offline.
 *
 *   Android: ConnectivityManager.activeNetwork + NetworkCapabilities
 *            with NET_CAPABILITY_INTERNET and NET_CAPABILITY_VALIDATED.
 *   iOS:     NWPathMonitor current path status == .satisfied.
 *
 * Must be cheap — [OfflineModeManager.lookupWithOfflineFallback] may invoke
 * this on every lookup. Platform actuals should cache the last-known
 * reachability state and refresh it asynchronously via the platform's
 * connectivity-change listener (ConnectivityManager.NetworkCallback on
 * Android; NWPathMonitor on iOS).
 */
expect fun isOnline(): Boolean

/**
 * Coarse classification of the current default network transport.
 *
 * Used by [OfflineModeManager] (and by DeltaSyncClient's retry loop) to pick
 * a backoff strategy:
 *   - "wifi"     → small backoff, allow larger delta batches (unmetered).
 *   - "cellular" → larger backoff, smaller batches (metered connection).
 *   - "none"     → defer the sync entirely; the caller should not retry.
 *
 *   Android: ConnectivityManager + NetworkCapabilities.TRANSPORT_WIFI /
 *            TRANSPORT_CELLULAR.
 *   iOS:     NWPath.usesInterfaceType(NWInterfaceTypeWifi /
 *            NWInterfaceTypeCellular).
 *
 * @return one of `"wifi"`, `"cellular"`, `"none"`.
 */
expect fun getNetworkType(): String

/**
 * Returns the current wall-clock time in milliseconds since the Unix epoch.
 *
 * Common Kotlin has no `System.currentTimeMillis()` equivalent; this expect
 * fun delegates to:
 *   Android: `System.currentTimeMillis()`
 *   iOS:     `clock_gettime_nsec_np(CLOCK_REALTIME)` converted to ms.
 *
 * Used for [QueuedSyncRequest.queuedAtMs] and
 * [SyncStateSnapshot.lastSyncTimestampMs].
 */
expect fun nowMillis(): Long

/**
 * Runs `PRAGMA integrity_check` on the local Lexicon SQLite db and returns
 * the raw result. The platform actual concatenates all result rows with
 * newlines (a healthy db returns a single "ok" row; a corrupt db returns
 * one or more descriptive error rows).
 *
 * Per spec: "SQLite integrity_check on cold start → if fails, delete local
 * DB and download full .db.zst package from cloud."
 *
 * Note: this is the *fast* single-PRAGMA check used on the cold-start path.
 * The deeper three-PRAGMA battery (integrity_check + quick_check +
 * foreign_key_check) lives in SyncResilience.kt's `runFullIntegrityCheck`.
 *
 * @return the integrity_check output. Equal to "ok" (case-insensitive) iff
 *         the db is structurally healthy.
 */
expect fun runSqliteIntegrityCheck(): String

/**
 * Physically deletes the on-disk SQLite db file plus its `-wal` and
 * `-shm` siblings. Used by the corruption-recovery path: the local file
 * is destroyed, then re-downloaded from the cloud as a fresh `.db.zst`
 * package via [downloadAndReplaceLanguagePack].
 *
 * Per spec: "If corrupt, delete local DB and download full .db.zst package
 * from cloud."
 *
 * @return true iff the delete succeeded OR the file did not exist.
 *         false signals the caller that re-download cannot proceed.
 */
expect fun deleteLocalDbFile(): Boolean

/**
 * Downloads a `.db.zst` language pack from [url], decompresses it with
 * Zstd (via [zstdDecompress] from DeltaSyncClient.kt), and atomically
 * replaces the local SQLite db file.
 *
 * "Atomically" here means: the new bytes are written to a temp file,
 * fsynced, then renamed over the existing db path. A crash mid-replace
 * leaves either the old (intact) db or the new (intact) db — never a
 * half-written hybrid.
 *
 * The platform actual is also responsible for clearing any stale
 * `-wal` / `-shm` siblings left over from the previous db (a fresh db
 * starts with no WAL).
 *
 * @return true iff the new db file is in place AND opens cleanly (i.e. a
 *         follow-up `PRAGMA integrity_check` returns "ok").
 */
expect suspend fun downloadAndReplaceLanguagePack(
    languageCode: String,
    url: String
): Boolean

// --- Sync state persistence (host app's SharedPreferences / UserDefaults) ---

/**
 * Reads the last successfully-applied cloud version for [languageCode], or 0
 * if no sync has ever completed for that language. Persisted across app
 * launches by the platform (SharedPreferences on Android; UserDefaults on iOS).
 *
 * Used as the `since` cursor for the next delta sync request.
 */
expect fun readLastSyncedVersion(languageCode: String): Int

/** Persists [version] as the last-applied cloud version for [languageCode]. */
expect fun writeLastSyncedVersion(languageCode: String, version: Int)

/**
 * Reads the wall-clock timestamp (ms since epoch) of the last successful
 * sync across ALL languages, or 0 if no sync has ever completed.
 */
expect fun readLastSyncTimestamp(): Long

/** Persists [timestampMs] as the last-sync timestamp. */
expect fun writeLastSyncTimestamp(timestampMs: Long)

// ===========================================================================
// Data classes
// ===========================================================================

/**
 * One queued sync request — recorded while the device is offline, replayed
 * in FIFO order on reconnect. Only the (languageCode, fromVersion) pair is
 * needed because the cloud delta endpoint is idempotent and self-cursors
 * from the device's last-known version.
 *
 * @property queuedAtMs  Wall-clock time the request was enqueued. Used to
 *                       prioritise the oldest request first and to age out
 *                       stale entries (older than 7 days are dropped by the
 *                       host app's periodic cleanup, not by this class).
 */
data class QueuedSyncRequest(
    val languageCode: String,
    val fromVersion: Int,
    val queuedAtMs: Long
)

/**
 * Snapshot of the manager's sync state — returned by
 * [OfflineModeManager.snapshot] for UI/telemetry consumption.
 *
 * @property online                Result of the latest [isOnline] check.
 * @property queueDepth            Number of pending [QueuedSyncRequest]s.
 * @property lastSyncedVersion     Per-language version cursors (one entry
 *                                  per registered language pack).
 * @property lastSyncTimestampMs   Wall-clock time of the last successful sync.
 * @property coldStartIntegrityOk  Result of the most recent cold-start
 *                                  integrity check. false signals the db
 *                                  was corrupt at cold start and recovery
 *                                  may still be in progress.
 */
data class SyncStateSnapshot(
    val online: Boolean,
    val queueDepth: Int,
    val lastSyncedVersion: Map<String, Int>,
    val lastSyncTimestampMs: Long,
    val coldStartIntegrityOk: Boolean
)

/**
 * Raised when the corruption-recovery path fails — i.e. the local db is
 * corrupt AND the cloud re-download also failed. The host app should
 * surface a "download failed, retry?" UI when this happens.
 *
 * Not raised by [OfflineModeManager.triggerDeltaSync] itself (which swallows
 * recovery failures into a `false` return); exposed as a typed exception for
 * callers that want to distinguish "sync failed transiently" from "sync
 * failed AND the db is unrecoverable without user action".
 */
class CorruptionRecoveryException(message: String, cause: Throwable? = null) : Exception(message, cause)

// ===========================================================================
// OfflineModeManager
// ===========================================================================

/**
 * OfflineModeManager — orchestrates offline fallback, queued sync, and
 * corruption recovery for the Lexicon KMM client.
 *
 * Lifecycle (host-app integration):
 *   1. Construct an instance at startup, passing the [DeltaSyncClient]
 *      the app already uses for online sync (no second HttpClient).
 *   2. Register language pack URLs via [registerLanguagePackUrl] (one per
 *      installed language) so corruption recovery can re-download without
 *      a server round-trip to look up the URL.
 *   3. Call [runColdStartIntegrityCheck] once before any user-facing
 *      lookup runs.
 *   4. The host app's connectivity listener calls [onConnectivityRestored]
 *      whenever the OS signals the device came back online.
 *   5. Per-lookup: call [lookupWithOfflineFallback] instead of hitting
 *      [LexiconEngine] directly.
 *
 * Threading:
 *   - All public `suspend` functions are main-safe; they delegate I/O to
 *     the injected DeltaSyncClient's HttpClient dispatcher.
 *   - The sync queue is guarded by a [Mutex] so concurrent enqueue/flush
 *     calls from different coroutines are safe.
 *   - [queueDepth] is `@Volatile` so [snapshot] can read it without the
 *     mutex (used for telemetry where an off-by-one is acceptable).
 *
 * @param deltaSyncClient  The cloud sync client (reused — no second HttpClient).
 * @param maxRetries       Number of RETRY attempts after the initial attempt.
 *                          Default 3 → 4 total attempts (1 initial + 3 retries)
 *                          with backoffs 1s, 2s, 4s, per spec.
 */
class OfflineModeManager(
    private val deltaSyncClient: DeltaSyncClient,
    private val maxRetries: Int = 3
) {
    /**
     * Backoff schedule per spec: "3 retries: 1s, 2s, 4s".
     *
     * Applied BEFORE each retry (not before the initial attempt, not after
     * the final attempt). With [maxRetries] = 3:
     *   attempt 0 (initial) fails  → delay 1s
     *   attempt 1 (retry 1) fails  → delay 2s
     *   attempt 2 (retry 2) fails  → delay 4s
     *   attempt 3 (retry 3) fails  → no delay, return false
     */
    private val backoffScheduleMs = longArrayOf(1_000L, 2_000L, 4_000L)

    private val queueMutex = Mutex()
    private val syncQueue = mutableListOf<QueuedSyncRequest>()

    /**
     * languageCode → CDN download URL, registered by the host app at startup.
     * Guarded by the object's own monitor (synchronized block) — cheap and
     * the registration window is tiny (a handful of puts at app launch).
     */
    private val languagePackUrls = mutableMapOf<String, String>()

    @Volatile
    private var coldStartIntegrityOk: Boolean = true

    @Volatile
    private var flushing: Boolean = false

    /**
     * Telemetry-only mirror of `syncQueue.size` — updated under
     * [queueMutex] but read without it by [snapshot] so the snapshot call
     * stays non-suspending. Worst case off-by-one during a concurrent
     * flush, which is acceptable for UI display.
     */
    @Volatile
    private var queueDepth: Int = 0

    // -------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------

    /**
     * Registers the CDN download URL for a language pack. Must be called
     * once per installed language at app startup so [recoverFromCorruption]
     * can re-download the pack without a server round-trip to look it up.
     *
     * The URL is typically the `downloadUrl` field of [LanguagePackInfo]
     * returned by `GET /api/v1/languages` (DeltaSyncClient.kt:88-96).
     */
    fun registerLanguagePackUrl(languageCode: String, downloadUrl: String) {
        synchronized(languagePackUrls) {
            languagePackUrls[languageCode] = downloadUrl
        }
    }

    // -------------------------------------------------------------------
    // Cold-start integrity check
    // -------------------------------------------------------------------

    /**
     * Cold-start integrity check — call once from app launch BEFORE any
     * user-facing lookup runs.
     *
     * Per spec: "SQLite integrity_check on cold start → if fails, delete
     * local DB and download full .db.zst package from cloud."
     *
     * @param languageCode  The primary language to re-download on failure.
     *                      (Multi-language recovery: call this once per
     *                      installed language in sequence.)
     * @return true iff the db is healthy (either was already healthy OR
     *         was successfully recovered via re-download).
     */
    suspend fun runColdStartIntegrityCheck(languageCode: String): Boolean {
        val result = runSqliteIntegrityCheck()
        if (result.equals("ok", ignoreCase = true)) {
            coldStartIntegrityOk = true
            return true
        }
        coldStartIntegrityOk = false
        return recoverFromCorruption(languageCode)
    }

    // -------------------------------------------------------------------
    // Offline-aware lookup
    // -------------------------------------------------------------------

    /**
     * Lookup dispatcher — always tries the local engine first (the local
     * hash table + DAWG are the fast path regardless of online state).
     * Only on a local miss do we consider the cloud:
     *   - Online:  trigger a delta sync, then retry the local lookup.
     *   - Offline: enqueue a sync request and return null.
     *
     * Per spec: "When offline: all lookups use local hash table + DAWG only
     * (no cloud)."
     *
     * @param languageCode  The language being looked up (drives which delta
     *                      sync cursor is used).
     * @param word          The query string.
     * @param localLookup   Lambda that delegates to `LexiconEngine.lookup`
     *                      (so this class stays decoupled from the JNI /
     *                      Swift engine bindings).
     */
    suspend fun lookupWithOfflineFallback(
        languageCode: String,
        word: String,
        localLookup: (String) -> WordEntry?
    ): WordEntry? {
        // Fast path — local hit. Works identically online and offline.
        val local = localLookup(word)
        if (local != null) return local

        // Local miss + offline → queue + return null.
        // Per spec: "Queues sync requests for when connection returns."
        if (!isOnline()) {
            enqueueSync(languageCode)
            return null
        }

        // Local miss + online → delta sync now, then retry local.
        // Sync failures are non-fatal for the lookup path — the user
        // simply sees "no result" for this word. The next lookup will
        // retry the sync.
        try {
            triggerDeltaSync(languageCode)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Swallow — the lookup path must never throw due to a sync issue.
        }
        return localLookup(word)
    }

    // -------------------------------------------------------------------
    // Sync queue
    // -------------------------------------------------------------------

    /**
     * Enqueues a sync request for [languageCode]. Idempotent — if a
     * pending request for the same language already exists, it's replaced
     * (we don't want unbounded queue growth from repeated lookups during
     * a long offline window).
     */
    suspend fun enqueueSync(languageCode: String) {
        val fromVersion = readLastSyncedVersion(languageCode)
        val request = QueuedSyncRequest(
            languageCode = languageCode,
            fromVersion = fromVersion,
            queuedAtMs = nowMillis()
        )
        queueMutex.withLock {
            // Replace any existing request for the same language — the
            // latest cursor supersedes the older one.
            syncQueue.removeAll { it.languageCode == languageCode }
            syncQueue.add(request)
            queueDepth = syncQueue.size
        }
    }

    /**
     * Called by the host app's connectivity listener when the OS signals
     * the device came back online.
     *
     * Per spec: "On reconnect: flushes queue, triggers delta sync."
     *
     * Safe to call repeatedly — concurrent invocations are serialised by
     * the [flushing] flag; the second call is a no-op while the first is
     * running.
     */
    suspend fun onConnectivityRestored() {
        if (flushing) return
        flushing = true
        try {
            flushQueue()
        } finally {
            flushing = false
        }
    }

    /**
     * Drains the pending sync queue in FIFO order. Each request triggers
     * a [triggerDeltaSync] which has its own retry/backoff; this function
     * does NOT double-retry. A failed item short-circuits the flush (the
     * device may have gone offline again mid-flush) and is left in the
     * queue for the next [onConnectivityRestored].
     *
     * @return the number of requests that completed successfully.
     */
    suspend fun flushQueue(): Int {
        // Snapshot the queue under the lock; mutate it as items succeed.
        val drained: List<QueuedSyncRequest> = queueMutex.withLock {
            syncQueue.toList()
        }
        var succeeded = 0
        for (req in drained) {
            val ok = try {
                triggerDeltaSync(req.languageCode)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                false
            }
            if (ok) {
                queueMutex.withLock {
                    syncQueue.removeAll { it.languageCode == req.languageCode }
                    queueDepth = syncQueue.size
                }
                succeeded++
            } else {
                // Stop on first failure — the device likely went offline
                // again mid-flush. Remaining items stay queued.
                break
            }
        }
        return succeeded
    }

    // -------------------------------------------------------------------
    // Delta sync with exponential backoff
    // -------------------------------------------------------------------

    /**
     * Triggers a delta sync for [languageCode] with exponential backoff
     * (3 retries: 1s, 2s, 4s per spec).
     *
     * Flow:
     *   1. Read the last-applied cloud version for this language.
     *   2. Ask the server for changes since that version
     *      ([DeltaSyncClient.checkForUpdates]).
     *   3. Apply the changes locally via [DeltaSyncClient.applyDeltaChanges]
     *      (atomic transaction + post-apply integrity check).
     *   4. On apply failure (returns false → likely local corruption):
     *      bail out of the retry loop and trigger [recoverFromCorruption]
     *      instead of pointlessly retrying the same broken apply.
     *   5. On any OTHER failure (network, timeout, parse): back off per the
     *      schedule and retry, up to [maxRetries] retries.
     *
     * @return true iff the sync completed (including the "no updates"
     *         case). false iff all retries were exhausted OR recovery
     *         failed.
     */
    suspend fun triggerDeltaSync(languageCode: String): Boolean {
        val currentVersion = readLastSyncedVersion(languageCode)
        var lastError: Throwable? = null

        // 1 initial attempt + maxRetries retries = (maxRetries + 1) total.
        val totalAttempts = maxRetries + 1
        repeat(totalAttempts) { attempt ->
            try {
                val resp = deltaSyncClient.checkForUpdates(languageCode, currentVersion)
                if (resp == null) {
                    // No updates available OR server unreachable (DeltaSyncClient
                    // swallows network failures into a null — see
                    // DeltaSyncClient.kt:224-231). Record the timestamp and
                    // return success: there's nothing to retry.
                    writeLastSyncTimestamp(nowMillis())
                    return true
                }
                val applied = deltaSyncClient.applyDeltaChanges(resp.changes)
                if (!applied) {
                    // Apply failed → likely local corruption. Don't retry the
                    // same broken apply; recover instead. Recovery is itself
                    // best-effort — if it succeeds, fast-forward the cursor
                    // to the server's current version (the fresh db is at
                    // least as new as `resp.newVersion`).
                    val recovered = recoverFromCorruption(languageCode)
                    if (recovered) {
                        writeLastSyncedVersion(languageCode, resp.newVersion)
                        writeLastSyncTimestamp(nowMillis())
                        coldStartIntegrityOk = true
                        return true
                    }
                    // Recovery failed — surface as a hard failure.
                    throw CorruptionRecoveryException(
                        "applyDeltaChanges failed AND recovery failed for $languageCode"
                    )
                }
                writeLastSyncedVersion(languageCode, resp.newVersion)
                writeLastSyncTimestamp(nowMillis())
                return true
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: CorruptionRecoveryException) {
                // Don't retry a recovery failure — surface it.
                lastError = t
                return false
            } catch (t: Throwable) {
                lastError = t
                // Apply backoff before each RETRY (not after the final attempt).
                if (attempt < maxRetries) {
                    delay(backoffScheduleMs[attempt])
                }
            }
        }
        // All retries exhausted.
        return false
    }

    // -------------------------------------------------------------------
    // Corruption recovery
    // -------------------------------------------------------------------

    /**
     * Recovers from local-db corruption by:
     *   1. Deleting the on-disk db file (and `-wal` / `-shm` siblings).
     *   2. Downloading a fresh `.db.zst` from the registered language pack URL.
     *   3. Atomically replacing the local db file.
     *
     * Per spec: "If corrupt, delete local DB and download full .db.zst
     * package from cloud."
     *
     * Note: this method does NOT back up / restore user-authored words.
     * That richer flow lives in `SyncResilience.kt`'s `CorruptionRecovery`
     * class, which wraps this method's primitives with a backup+restore
     * layer. The two-layer split keeps OfflineModeManager focused on the
     * "is the db usable?" question, while SyncResilience handles the
     * "did we lose user data?" question.
     *
     * @return true iff the new db is in place AND opens cleanly.
     */
    suspend fun recoverFromCorruption(languageCode: String): Boolean {
        val url = synchronized(languagePackUrls) { languagePackUrls[languageCode] }
        if (url == null) {
            // No registered URL → cannot recover. Host app should call
            // registerLanguagePackUrl at startup.
            coldStartIntegrityOk = false
            return false
        }
        val deleted = try {
            deleteLocalDbFile()
        } catch (t: Throwable) {
            coldStartIntegrityOk = false
            return false
        }
        if (!deleted) {
            coldStartIntegrityOk = false
            return false
        }
        val replaced = try {
            downloadAndReplaceLanguagePack(languageCode, url)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            coldStartIntegrityOk = false
            return false
        }
        coldStartIntegrityOk = replaced
        if (replaced) {
            // The fresh db is at whatever version the cloud baked into the
            // .db.zst. We deliberately do NOT zero out `lastSyncedVersion`
            // here — the server dedupes by word_id (DeltaSyncClient.kt:333),
            // so re-applying already-present changes on the next delta sync
            // is a no-op INSERT OR REPLACE. Recording the timestamp keeps
            // the UI's "last synced" indicator honest.
            writeLastSyncTimestamp(nowMillis())
        }
        return replaced
    }

    // -------------------------------------------------------------------
    // Telemetry
    // -------------------------------------------------------------------

    /**
     * Returns a point-in-time snapshot of the manager's state for UI or
     * telemetry. Cheap to call — no I/O on the queue, just in-memory reads
     * + the platform's fast persisted-state reads (SharedPreferences /
     * UserDefaults are loaded into memory at first access).
     */
    fun snapshot(): SyncStateSnapshot {
        val versions = mutableMapOf<String, Int>()
        synchronized(languagePackUrls) {
            for (code in languagePackUrls.keys) {
                versions[code] = readLastSyncedVersion(code)
            }
        }
        return SyncStateSnapshot(
            online = isOnline(),
            queueDepth = queueDepth,
            lastSyncedVersion = versions.toMap(),
            lastSyncTimestampMs = readLastSyncTimestamp(),
            coldStartIntegrityOk = coldStartIntegrityOk
        )
    }
}
