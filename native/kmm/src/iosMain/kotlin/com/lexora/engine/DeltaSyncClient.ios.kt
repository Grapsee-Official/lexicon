// DeltaSyncClient.ios.kt — iOS `actual` implementations for the three
// `expect fun`s declared in DeltaSyncClient.kt (commonMain).
//
// Per task P3-FIX-21:
//   - createHttpClient        → ktor-client-darwin (NSURLSession engine).
//   - zstdDecompress          → libzstd via @SymbolName external funs
//                                (ZSTD_decompress / ZSTD_getFrameContentSize /
//                                ZSTD_isError). libzstd.a is bundled into
//                                the LexoraEngine.framework at link time —
//                                the @SymbolName declarations compile without
//                                a cinterop def file because they only need
//                                the C ABI signatures, not the struct types.
//   - applyDeltaChangesNative → sqlite3 C API via @SymbolName external funs.
//                                iOS ships libsqlite3.tbd as a system
//                                framework; no cinterop def needed for the
//                                function declarations to compile.
//
// Why @SymbolName instead of cinterop:
//   The existing LexoraEngine.ios.kt already uses this pattern (see lines
//   45-81) for the C++ engine's C ABI. It avoids adding a second cinterop
//   def file (zstd.def / sqlite3.def) just to get top-level stubs that we'd
//   ignore anyway. The @SymbolName annotations resolve at link time when
//   the .framework is built — the host iOS app is responsible for linking
//   libzstd.a and the system libsqlite3.
package com.lexora.engine

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.*
import kotlinx.serialization.json.Json

// ---------------------------------------------------------------------------
// libzstd C API bindings
// ---------------------------------------------------------------------------
//
// Signatures (from zstd.h):
//   unsigned long long ZSTD_getFrameContentSize(const void* src, size_t srcSize);
//   size_t ZSTD_decompress(void* dst, size_t dstCapacity,
//                          const void* src, size_t srcSize);
//   unsigned ZSTD_isError(size_t code);
//
// On 64-bit iOS (all our targets: iosX64, iosArm64, iosSimulatorArm64),
// `size_t` and `unsigned long long` are both 8 bytes → Kotlin `ULong`.
// `unsigned` is 4 bytes → Kotlin `UInt`.

@SymbolName("ZSTD_getFrameContentSize")
private external fun zstdGetFrameContentSize(
    src: CPointer<ByteVar>?,
    srcSize: ULong
): ULong

@SymbolName("ZSTD_decompress")
private external fun zstdDecompressC(
    dst: CPointer<ByteVar>?,
    dstCapacity: ULong,
    src: CPointer<ByteVar>?,
    srcSize: ULong
): ULong

@SymbolName("ZSTD_isError")
private external fun zstdIsError(code: ULong): UInt

// Sentinel return values from ZSTD_getFrameContentSize (zstd.h):
//   #define ZSTD_CONTENTSIZE_UNKNOWN  (0ULL - 1)   // size not embedded in frame
//   #define ZSTD_CONTENTSIZE_ERROR     (0ULL - 2)   // invalid / corrupt frame
// Kotlin unsigned arithmetic wraps on underflow, mirroring the C macros.
private val ZSTD_CONTENTSIZE_UNKNOWN: ULong = 0UL - 1UL
private val ZSTD_CONTENTSIZE_ERROR: ULong = 0UL - 2UL

// ---------------------------------------------------------------------------
// sqlite3 C API bindings
// ---------------------------------------------------------------------------
//
// Signatures (from sqlite3.h), treating `sqlite3*` and `sqlite3_stmt*` as
// opaque pointers (COpaquePointer?):
//   int sqlite3_open(const char* filename, sqlite3** ppDb);
//   int sqlite3_close(sqlite3* db);
//   int sqlite3_exec(sqlite3* db, const char* sql,
//                    int (*callback)(void*, int, char**, char**),
//                    void* arg, char** errmsg);
//   int sqlite3_prepare_v2(sqlite3* db, const char* sql, int nByte,
//                          sqlite3_stmt** ppStmt, const char** pzTail);
//   int sqlite3_bind_text(sqlite3_stmt*, int, const char*, int n,
//                         void(*)(void*));
//   int sqlite3_bind_null(sqlite3_stmt*, int);
//   int sqlite3_step(sqlite3_stmt*);
//   int sqlite3_reset(sqlite3_stmt*);
//   int sqlite3_finalize(sqlite3_stmt*);
//   const unsigned char* sqlite3_column_text(sqlite3_stmt*, int iCol);

@SymbolName("sqlite3_open")
private external fun sqlite3Open(
    filename: CPointer<ByteVar>?,
    db: CPointer<COpaquePointerVar>?
): Int

@SymbolName("sqlite3_close")
private external fun sqlite3Close(db: COpaquePointer?): Int

@SymbolName("sqlite3_exec")
private external fun sqlite3Exec(
    db: COpaquePointer?,
    sql: CPointer<ByteVar>?,
    callback: COpaquePointer?,
    arg: COpaquePointer?,
    errmsg: CPointer<CPointerVar<ByteVar>>?
): Int

@SymbolName("sqlite3_prepare_v2")
private external fun sqlite3PrepareV2(
    db: COpaquePointer?,
    sql: CPointer<ByteVar>?,
    nByte: Int,
    stmt: CPointer<COpaquePointerVar>?,
    tail: CPointer<CPointerVar<ByteVar>>?
): Int

@SymbolName("sqlite3_bind_text")
private external fun sqlite3BindText(
    stmt: COpaquePointer?,
    idx: Int,
    value: CPointer<ByteVar>?,
    nByte: Int,
    destructor: COpaquePointer?
): Int

@SymbolName("sqlite3_bind_null")
private external fun sqlite3BindNull(stmt: COpaquePointer?, idx: Int): Int

@SymbolName("sqlite3_step")
private external fun sqlite3Step(stmt: COpaquePointer?): Int

@SymbolName("sqlite3_reset")
private external fun sqlite3Reset(stmt: COpaquePointer?): Int

@SymbolName("sqlite3_finalize")
private external fun sqlite3Finalize(stmt: COpaquePointer?): Int

@SymbolName("sqlite3_column_text")
private external fun sqlite3ColumnText(
    stmt: COpaquePointer?,
    col: Int
): CPointer<ByteVar>?

// SQLite result codes (sqlite3.h).
private const val SQLITE_OK: Int = 0
private const val SQLITE_ROW: Int = 100
private const val SQLITE_DONE: Int = 101

// ---------------------------------------------------------------------------
// DB path singleton — registered by the host iOS app at startup.
// ---------------------------------------------------------------------------

/**
 * Internal holder for the on-disk Lexora SQLite path. The iOS app must call
 * [initLexoraDb] once at launch (typically from `application(_:didFinishLaunchingWithOptions:)`
 * with the URL from `FileManager.default.urls(for:in:)`) before any sync
 * runs. A missing registration makes [applyDeltaChangesNative] return false.
 *
 * Mirrors the Android `LexoraDbProvider` in DeltaSyncClient.android.kt so
 * common calling code can rely on the same `initLexoraDb(path)` entry point.
 */
internal object LexoraDbProvider {
    @Volatile
    var dbPath: String? = null
}

/**
 * Register the on-disk SQLite database path that [applyDeltaChangesNative]
 * will write delta changes to. Must be called once at app launch:
 *
 *   initLexoraDb(dbURL.path)
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
 * iOS `actual` — builds a Darwin-backed Ktor HttpClient.
 *
 * Darwin is ktor's NSURLSession-based engine for iOS/macOS/tvOS. It hooks
 * into the platform's connection pooling, TLS stack, and reachability
 * monitoring — no manual OkHttp-equivalent config needed.
 *
 * Timeouts: we install the cross-engine [HttpTimeout] plugin (request=30s,
 * connect=10s, socket=30s) so the values match the Android actual exactly
 * and stay engine-agnostic in commonMain.
 *
 * UserAgent: "Lexora/1.0 (iOS)" — distinguishes iOS traffic in server logs.
 */
actual fun createHttpClient(): HttpClient {
    return HttpClient(Darwin) {
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
            agent = "Lexora/1.0 (iOS)"
        }
    }
}

// ---------------------------------------------------------------------------
// actual fun zstdDecompress(compressed: ByteArray): ByteArray
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — decompresses a Zstd frame via libzstd.
 *
 * The compressed [ByteArray] is pinned with [usePinned] so the GC won't move
 * it while C code reads from it; the same is done for the output buffer. The
 * decompressed size is read from the frame header via
 * [zstdGetFrameContentSize]; if absent (returns ZSTD_CONTENTSIZE_UNKNOWN),
 * we fall back to a 64 MiB cap. Any error (bad magic, truncation, size
 * mismatch) is wrapped in [ZstdDecompressException].
 *
 * libzstd is expected to be statically linked into LexoraEngine.framework by
 * the iOS app's build — the @SymbolName declarations resolve at link time.
 */
actual fun zstdDecompress(compressed: ByteArray): ByteArray {
    if (compressed.isEmpty()) {
        throw ZstdDecompressException("zstd: empty input")
    }
    // Read the embedded decompressed size from the frame header.
    val expected: ULong = compressed.usePinned { src ->
        zstdGetFrameContentSize(src.addressOf(0), compressed.size.toULong())
    }
    val targetSize: ULong = when (expected) {
        ZSTD_CONTENTSIZE_ERROR ->
            throw ZstdDecompressException("ZSTD_getFrameContentSize: invalid frame")
        ZSTD_CONTENTSIZE_UNKNOWN ->
            // ETL always embeds the size; this is purely defensive.
            (64L * 1024 * 1024).toULong()
        else -> {
            if (expected > Int.MAX_VALUE.toULong()) {
                throw ZstdDecompressException(
                    "zstd: decompressed size > 2 GiB ($expected)"
                )
            }
            expected
        }
    }
    val out = ByteArray(targetSize.toInt())
    val written: ULong = out.usePinned { dst ->
        compressed.usePinned { src ->
            zstdDecompressC(
                dst.addressOf(0),
                targetSize,
                src.addressOf(0),
                compressed.size.toULong()
            )
        }
    }
    if (zstdIsError(written) != 0u) {
        throw ZstdDecompressException(
            "ZSTD_decompress failed (code=$written)"
        )
    }
    // The decompressor may have written fewer bytes than the target cap
    // (when the frame's content size was unknown and the real size < 64 MiB).
    // Truncate to the actual byte count.
    val writtenInt = written.toInt()
    return if (writtenInt == out.size) out else out.copyOf(writtenInt)
}

// ---------------------------------------------------------------------------
// actual fun applyDeltaChangesNative(changes: List<WordChange>): Boolean
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — applies a batch of [WordChange]s to the local SQLite
 * database via the sqlite3 C API.
 *
 * Flow (mirrors the Android actual):
 *   1. sqlite3_open(path, &db)
 *   2. sqlite3_exec("BEGIN TRANSACTION")
 *   3. For each change: prepare / bind / step / reset a statement
 *      (INSERT OR REPLACE / UPDATE / DELETE on the `words` table).
 *   4. sqlite3_exec("COMMIT") on success, or "ROLLBACK" on any failure.
 *   5. PRAGMA integrity_check → must return "ok" for true.
 *   6. sqlite3_close(db)
 *
 * The FTS5 triggers in `lexora_sqlite.sql:39-54` fire on each row mutation,
 * keeping `words_fts` in sync — we never touch the FTS table directly.
 *
 * @return true iff every change applied AND `PRAGMA integrity_check` = "ok".
 */
actual fun applyDeltaChangesNative(changes: List<WordChange>): Boolean {
    val path = LexoraDbProvider.dbPath ?: return false
    return memScoped {
        val dbVar = alloc<COpaquePointerVar>()
        val dbPathC = path.cstr.getPointer(this)
        if (sqlite3Open(dbPathC, dbVar.ptr) != SQLITE_OK) {
            // Even on failure, sqlite3_open may have allocated a handle —
            // close it defensively.
            sqlite3Close(dbVar.value)
            return@memScoped false
        }
        val db: COpaquePointer? = dbVar.value
        try {
            if (execC(db, "BEGIN TRANSACTION", this) != SQLITE_OK) {
                return@memScoped false
            }
            val ok = try {
                applyAllChangesC(db, changes, this)
            } catch (t: Throwable) {
                false
            }
            if (ok) {
                if (execC(db, "COMMIT", this) != SQLITE_OK) {
                    execC(db, "ROLLBACK", this)
                    return@memScoped false
                }
            } else {
                execC(db, "ROLLBACK", this)
                return@memScoped false
            }
            // Post-commit integrity check.
            integrityCheckOkC(db, this)
        } finally {
            sqlite3Close(db)
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Runs a no-arg SQL statement (BEGIN / COMMIT / ROLLBACK / PRAGMA) via
 * sqlite3_exec. The callback and errmsg out-param are left NULL — we don't
 * need row data or detailed error messages here.
 */
private fun execC(
    db: COpaquePointer?,
    sql: String,
    scope: AutofreeScope
): Int {
    val sqlC = sql.cstr.getPointer(scope)
    return sqlite3Exec(db, sqlC, null, null, null)
}

/**
 * Compiles + binds + steps each WordChange. Statements are prepared once and
 * re-used across the batch (sqlite3_reset between rows) to amortize the
 * parse cost. Returns false on any bind/step failure — the caller will
 * issue ROLLBACK.
 *
 * SQL column mapping (words table, per `native/cloud/schema/lexora_sqlite.sql:10`):
 *   id, word, phonetic, part_of_speech, definitions, language
 * (etymology/synonyms/antonyms/examples are left at column defaults — they
 *  come from a different sync stream.)
 */
private fun applyAllChangesC(
    db: COpaquePointer?,
    changes: List<WordChange>,
    scope: AutofreeScope
): Boolean {
    val insertSql =
        "INSERT OR REPLACE INTO words (id, word, phonetic, part_of_speech, " +
        "definitions, language) VALUES (?, ?, ?, ?, ?, ?)"
    val updateSql =
        "UPDATE words SET word=?, phonetic=?, part_of_speech=?, " +
        "definitions=?, language=? WHERE id=?"
    val deleteSql = "DELETE FROM words WHERE id=?"

    val insertStmt = prepareC(db, insertSql, scope) ?: return false
    val updateStmt = prepareC(db, updateSql, scope) ?: run {
        sqlite3Finalize(insertStmt)
        return false
    }
    val deleteStmt = prepareC(db, deleteSql, scope) ?: run {
        sqlite3Finalize(insertStmt)
        sqlite3Finalize(updateStmt)
        return false
    }
    try {
        for (c in changes) {
            val stmt: COpaquePointer? = when (c.operation.uppercase()) {
                "INSERT" -> insertStmt
                "UPDATE" -> updateStmt
                "DELETE" -> deleteStmt
                else -> return false  // unknown op → abort batch
            }
            // Bind parameters. sqlite3_bind indices are 1-based. nByte=-1
            // tells SQLite to strlen() the value (strings are null-terminated
            // by cstr). destructor=NULL means SQLite does NOT copy the string
            // — safe because the memScoped allocation outlives the step() call.
            when (c.operation.uppercase()) {
                "INSERT" -> {
                    sqlite3BindText(stmt, 1, c.wordId.cstr.getPointer(scope), -1, null)
                    bindTextOrNull(stmt, 2, c.word, scope)
                    bindTextOrNull(stmt, 3, c.phonetic, scope)
                    bindTextOrNull(stmt, 4, c.partOfSpeech, scope)
                    bindTextOrNull(stmt, 5, c.definitions, scope)
                    bindTextOrNull(stmt, 6, c.language, scope)
                }
                "UPDATE" -> {
                    bindTextOrNull(stmt, 1, c.word, scope)
                    bindTextOrNull(stmt, 2, c.phonetic, scope)
                    bindTextOrNull(stmt, 3, c.partOfSpeech, scope)
                    bindTextOrNull(stmt, 4, c.definitions, scope)
                    bindTextOrNull(stmt, 5, c.language, scope)
                    sqlite3BindText(stmt, 6, c.wordId.cstr.getPointer(scope), -1, null)
                }
                "DELETE" -> {
                    sqlite3BindText(stmt, 1, c.wordId.cstr.getPointer(scope), -1, null)
                }
            }
            val rc = sqlite3Step(stmt)
            if (rc != SQLITE_DONE) return false
            // Reset so the statement is ready for the next bind/step cycle.
            sqlite3Reset(stmt)
        }
        return true
    } finally {
        sqlite3Finalize(insertStmt)
        sqlite3Finalize(updateStmt)
        sqlite3Finalize(deleteStmt)
    }
}

/**
 * Compiles a SQL string into a sqlite3_stmt*. Returns null on failure.
 * The statement must be finalized by the caller.
 */
private fun prepareC(
    db: COpaquePointer?,
    sql: String,
    scope: AutofreeScope
): COpaquePointer? {
    val stmtVar = scope.alloc<COpaquePointerVar>()
    val tailVar = scope.alloc<CPointerVar<ByteVar>>()
    val sqlC = sql.cstr.getPointer(scope)
    val rc = sqlite3PrepareV2(db, sqlC, -1, stmtVar.ptr, tailVar.ptr)
    if (rc != SQLITE_OK) return null
    return stmtVar.value
}

/**
 * Binds a nullable String to a statement parameter. Null values use
 * sqlite3_bind_null; non-null uses sqlite3_bind_text with nByte=-1
 * (let SQLite strlen the null-terminated C string).
 */
private fun bindTextOrNull(
    stmt: COpaquePointer?,
    idx: Int,
    value: String?,
    scope: AutofreeScope
): Int {
    return if (value == null) {
        sqlite3BindNull(stmt, idx)
    } else {
        sqlite3BindText(stmt, idx, value.cstr.getPointer(scope), -1, null)
    }
}

/**
 * Runs `PRAGMA integrity_check` and returns true iff the first row is "ok".
 * A corrupt database returns one or more rows describing the corruption;
 * we only need the first row to fail fast.
 */
private fun integrityCheckOkC(
    db: COpaquePointer?,
    scope: AutofreeScope
): Boolean {
    val stmt = prepareC(db, "PRAGMA integrity_check", scope) ?: return false
    try {
        if (sqlite3Step(stmt) != SQLITE_ROW) return false
        val textPtr = sqlite3ColumnText(stmt, 0) ?: return false
        // toKString() copies the C string into a Kotlin String, so the
        // returned value is safe to use after the statement is finalized.
        val firstRow = textPtr.toKString()
        return firstRow.equals("ok", ignoreCase = true)
    } finally {
        sqlite3Finalize(stmt)
    }
}
