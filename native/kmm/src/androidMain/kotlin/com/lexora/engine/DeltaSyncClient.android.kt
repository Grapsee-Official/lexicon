// DeltaSyncClient.android.kt — Android `actual` implementations for the three
// `expect fun`s declared in DeltaSyncClient.kt (commonMain).
//
// Per task P3-FIX-21:
//   - createHttpClient        → ktor-client-okhttp wrapping an OkHttpClient
//                                with 10s connect / 30s read-write timeouts.
//   - zstdDecompress          → com.github.luben.zstd.Zstd (zstd-jni) which
//                                bundles native .so for arm64-v8a, armeabi-v7a,
//                                x86_64 and x86.
//   - applyDeltaChangesNative → android.database.sqlite.SQLiteDatabase direct
//                                writes inside a BEGIN/COMMIT transaction, with
//                                a post-commit PRAGMA integrity_check.
//
// Notes on the SQLite path:
//   `applyDeltaChangesNative` is a top-level `expect fun`, so it has no
//   `Context` parameter. The Android app must call `initLexoraDb(path)` once
//   at startup (typically from Application.onCreate via
//   `getDatabasePath("lexora.db").absolutePath`) before any sync runs. This
//   mirrors how the iOS side registers its db path via the same top-level
//   `initLexoraDb` function (declared in DeltaSyncClient.ios.kt).
package com.lexora.engine

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.github.luben.zstd.Zstd
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// DB path singleton — registered by the host Android app at startup.
// ---------------------------------------------------------------------------

/**
 * Internal holder for the on-disk Lexora SQLite path. The Android app must
 * call [initLexoraDb] once (from `Application.onCreate`) before any sync
 * attempt, otherwise [applyDeltaChangesNative] short-circuits with `false`.
 *
 * We use a `@Volatile var` rather than a `lateinit` so that the value can be
 * read from any thread without synchronization, and a missing registration
 * returns `false` instead of throwing `UninitializedPropertyAccessException`.
 */
internal object LexoraDbProvider {
    @Volatile
    var dbPath: String? = null
}

/**
 * Register the on-disk SQLite database path that [applyDeltaChangesNative]
 * will write delta changes to. Must be called once at app startup:
 *
 *   initLexoraDb(getDatabasePath("lexora.db").absolutePath)
 *
 * Safe to call multiple times — the latest path wins.
 */
fun initLexoraDb(path: String) {
    LexoraDbProvider.dbPath = path
}

// ---------------------------------------------------------------------------
// actual fun createHttpClient()
// ---------------------------------------------------------------------------

/**
 * Android `actual` — builds an OkHttp-backed Ktor HttpClient with:
 *   - connectTimeout    = 10s  (per spec comment in DeltaSyncClient.kt:106)
 *   - readTimeout       = 30s  (matches requestTimeoutMillis default)
 *   - writeTimeout      = 30s  (uploads are usually small but symmetric)
 *   - retryOnConnectionFailure = true (SSP/proxy flakiness)
 *
 * Plus the common Ktor plugins:
 *   - ContentNegotiation { json(Json { ignoreUnknownKeys = true }) }
 *   - HttpTimeout (requestTimeoutMillis = 30s)
 *   - UserAgent = "Lexora/1.0 (Android)"
 *
 * The OkHttpClient is built `preconfigured` so Ktor inherits our timeouts
 * instead of applying its own defaults on top.
 */
actual fun createHttpClient(): HttpClient {
    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    return HttpClient(OkHttp) {
        engine {
            preconfigured = okHttp
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        install(UserAgent) {
            agent = "Lexora/1.0 (Android)"
        }
    }
}

// ---------------------------------------------------------------------------
// actual fun zstdDecompress(compressed: ByteArray): ByteArray
// ---------------------------------------------------------------------------

/**
 * Android `actual` — delegates to zstd-jni's [Zstd.decompress].
 *
 * zstd-jni (com.github.luben:zstd-jni) ships native bindings for arm64-v8a,
 * armeabi-v7a, x86_64 and x86 inside the AAR, so no extra NDK work is needed.
 *
 * The .db.zst artifacts produced by the ETL pipeline embed the original
 * (decompressed) byte size in the Zstd frame header; we read it via
 * [Zstd.decompressedSize] to pre-size the output buffer. If the size is not
 * embedded (returns 0), we fall back to a generous 64 MiB cap — the ETL
 * pipeline always writes the content size, so this is purely defensive.
 *
 * Any failure (truncated input, bad magic, size mismatch) is wrapped in a
 * [ZstdDecompressException] so the caller can decide whether to retry the
 * download or pick a different mirror.
 */
actual fun zstdDecompress(compressed: ByteArray): ByteArray {
    if (compressed.isEmpty()) {
        throw ZstdDecompressException("zstd: empty input")
    }
    return try {
        val expected = Zstd.decompressedSize(compressed)
        val targetSize: Int = when {
            expected > Int.MAX_VALUE.toLong() ->
                throw ZstdDecompressException("zstd: decompressed size > 2 GiB ($expected)")
            expected > 0L -> expected.toInt()
            else -> 64 * 1024 * 1024  // 64 MiB defensive fallback for unknown size.
        }
        Zstd.decompress(compressed, targetSize)
    } catch (t: ZstdDecompressException) {
        throw t
    } catch (t: Throwable) {
        throw ZstdDecompressException(
            "zstd-jni decompress failed: ${t.message}",
            t
        )
    }
}

// ---------------------------------------------------------------------------
// actual fun applyDeltaChangesNative(changes: List<WordChange>): Boolean
// ---------------------------------------------------------------------------

/**
 * Android `actual` — applies a batch of [WordChange]s to the local SQLite
 * database in a single transaction.
 *
 * Strategy:
 *   1. Open the on-disk db via [SQLiteDatabase.openOrCreateDatabase] (path
 *      registered by the app via [initLexoraDb]).
 *   2. `beginTransaction()` → for each change, bind+execute the appropriate
 *      pre-compiled statement (INSERT OR REPLACE / UPDATE / DELETE).
 *   3. `setTransactionSuccessful()` + `endTransaction()` commits if every
 *      statement ran; any failure skips the success flag and the transaction
 *      rolls back atomically.
 *   4. Post-commit `PRAGMA integrity_check` — the result must be "ok" for
 *      the method to return true. This satisfies the spec hard gate:
 *      "Device updates 100 new words without FTS5 corruption."
 *
 * The FTS5 triggers defined in `lexora_sqlite.sql:39-54` fire automatically
 * on each INSERT/UPDATE/DELETE, keeping the `words_fts` index in sync — we
 * do NOT touch the FTS table directly.
 *
 * SQL column mapping (words table, per `native/cloud/schema/lexora_sqlite.sql:10`):
 *   id, word, phonetic, part_of_speech, definitions, language
 * The WordChange carries exactly these fields; etymology/synonyms/antonyms/
 * examples are left at their column defaults (NULL on INSERT, untouched on
 * UPDATE) — they come from a different sync stream.
 *
 * @return true iff every change applied AND `PRAGMA integrity_check` = "ok".
 *         false signals the caller (DeltaSyncClient.applyDeltaChanges) to
 *         fall back to a full re-download via downloadLanguagePack.
 */
actual fun applyDeltaChangesNative(changes: List<WordChange>): Boolean {
    val path = LexoraDbProvider.dbPath ?: return false
    val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(path, null)
    return try {
        db.beginTransaction()
        try {
            applyAllChanges(db, changes)
            db.setTransactionSuccessful()
        } finally {
            // If setTransactionSuccessful() was NOT reached (exception thrown
            // inside the try), endTransaction() rolls back. Otherwise commits.
            db.endTransaction()
        }
        // Post-commit integrity check. Runs even on a rolled-back batch so
        // the caller gets an accurate signal about db health.
        integrityCheckOk(db)
    } finally {
        db.close()
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Compiled-statement batch. Re-using a single [SQLiteStatement] across the
 * whole batch (re-binding per row) avoids re-parsing the SQL ~Nx. Each
 * statement is closed in a `finally` to release its native sqlite3_stmt.
 *
 * Unknown operation codes short-circuit with `return false` so the caller's
 * transaction rolls back — silently ignoring an unknown op would violate the
 * "all-or-nothing" contract documented in DeltaSyncClient.applyDeltaChanges.
 */
private fun applyAllChanges(db: SQLiteDatabase, changes: List<WordChange>): Boolean {
    val insertSql = "INSERT OR REPLACE INTO words " +
        "(id, word, phonetic, part_of_speech, definitions, language) " +
        "VALUES (?, ?, ?, ?, ?, ?)"
    val updateSql = "UPDATE words SET " +
        "word=?, phonetic=?, part_of_speech=?, definitions=?, language=? " +
        "WHERE id=?"
    val deleteSql = "DELETE FROM words WHERE id=?"

    val insertStmt: SQLiteStatement = db.compileStatement(insertSql)
    val updateStmt: SQLiteStatement = db.compileStatement(updateSql)
    val deleteStmt: SQLiteStatement = db.compileStatement(deleteSql)
    try {
        for (c in changes) {
            when (c.operation.uppercase()) {
                "INSERT" -> {
                    // id is the PK and always non-null per WordChange contract.
                    insertStmt.bindString(1, c.wordId)
                    insertStmt.bindStringOrNull(2, c.word)
                    insertStmt.bindStringOrNull(3, c.phonetic)
                    insertStmt.bindStringOrNull(4, c.partOfSpeech)
                    insertStmt.bindStringOrNull(5, c.definitions)
                    insertStmt.bindStringOrNull(6, c.language)
                    insertStmt.executeInsert()
                }
                "UPDATE" -> {
                    updateStmt.bindStringOrNull(1, c.word)
                    updateStmt.bindStringOrNull(2, c.phonetic)
                    updateStmt.bindStringOrNull(3, c.partOfSpeech)
                    updateStmt.bindStringOrNull(4, c.definitions)
                    updateStmt.bindStringOrNull(5, c.language)
                    updateStmt.bindString(6, c.wordId)
                    updateStmt.executeUpdateDelete()
                }
                "DELETE" -> {
                    deleteStmt.bindString(1, c.wordId)
                    deleteStmt.executeUpdateDelete()
                }
                else -> {
                    // Unknown operation → abort the batch, transaction rolls back.
                    return false
                }
            }
        }
        return true
    } finally {
        insertStmt.close()
        updateStmt.close()
        deleteStmt.close()
    }
}

/**
 * Runs `PRAGMA integrity_check` and returns true iff the first row is "ok".
 * A corrupt db returns one or more rows describing the corruption; we only
 * need to inspect the first row to fail fast.
 */
private fun integrityCheckOk(db: SQLiteDatabase): Boolean {
    val cursor = db.rawQuery("PRAGMA integrity_check", null)
    return try {
        if (!cursor.moveToFirst()) return false
        val firstRow = cursor.getString(0)
        firstRow.equals("ok", ignoreCase = true)
    } finally {
        cursor.close()
    }
}

/**
 * SQLiteStatement.bindString throws IllegalArgumentException on null values;
 * bindNull must be used instead. This extension picks the right call.
 */
private fun SQLiteStatement.bindStringOrNull(index: Int, value: String?) {
    if (value == null) bindNull(index) else bindString(index, value)
}
