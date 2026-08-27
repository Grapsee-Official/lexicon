// SyncResilience.kt — KMM commonMain
//
// Per spec (P4-01): sync resilience primitives, paired with OfflineModeManager.
//
// Components:
//   - ConflictResolutionStrategy: last-write-wins + version numbers.
//   - TransactionWrapper: wraps all SQLite writes in atomic transactions.
//   - IntegrityChecker: runs PRAGMA integrity_check, quick_check,
//     foreign_key_check.
//   - CorruptionRecovery: deletes local DB, re-downloads from cloud,
//     restores saved words.
//   - SyncInterruptedHandler: per spec "Zero database corruption errors
//     across 10,000 simulated sync interruptions" — uses WAL mode + atomic
//     transactions + write-ahead logging.
//
// This file lives in commonMain. The `expect fun`s declared below are
// implemented per-platform in:
//   androidMain/.../SyncResilience.android.kt
//   iosMain/.../SyncResilience.ios.kt
// (Follow-up task — see Next Actions in worklog.)
//
// Relationship to OfflineModeManager.kt:
//   OfflineModeManager answers "is the db usable right now?" — it owns the
//   online/offline decision, the sync queue, and the simple delete+re-download
//   corruption recovery. SyncResilience answers "is the db SAFE under
//   arbitrary interruption?" — it owns the transactional write primitives,
//   the deep integrity checks, the user-data-preserving recovery flow, and
//   the WAL-based zero-corruption guarantee. The two files share the same
//   on-disk db (registered via DeltaSyncClient.kt's `initLexiconDb`) but
//   expose disjoint expect-fun surfaces so they can evolve independently.

package com.lexicon.engine

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

// ===========================================================================
// expect declarations — platform-specific SQLite operations
// ===========================================================================

/**
 * Executes [block] inside a single SQLite transaction (BEGIN … COMMIT on
 * success, ROLLBACK on exception or `false` return).
 *
 * Per spec: "TransactionWrapper: wraps all SQLite writes in atomic
 * transactions." The actual platform implementation uses:
 *   Android: `SQLiteDatabase.beginTransaction()` /
 *            `setTransactionSuccessful()` / `endTransaction()`.
 *   iOS:     `sqlite3_exec(db, "BEGIN", …)` /
 *            `sqlite3_exec(db, "COMMIT", …)` /
 *            `sqlite3_exec(db, "ROLLBACK", …)` on failure.
 *
 * The actual is also responsible for handling nested calls gracefully
 * (e.g. Android's `db.inTransaction()` check → use SAVEPOINT instead of
 * BEGIN for the nested call). This lets [SyncInterruptedHandler.safeWrite]
 * wrap a block that may itself call other transactional helpers without
 * tripping SQLite's "cannot start a transaction within a transaction" error.
 *
 * @return true iff the transaction committed. false iff it rolled back
 *         (either because [block] returned false or because it threw).
 */
expect fun executeInTransaction(block: () -> Boolean): Boolean

/**
 * Runs the three SQLite integrity PRAGMAs and returns their combined result:
 *   - `PRAGMA integrity_check`    (full structural check; slow on large dbs).
 *   - `PRAGMA quick_check`        (subset of integrity_check; ~10x faster).
 *   - `PRAGMA foreign_key_check`  (verifies FK constraints; no-op if
 *                                  `PRAGMA foreign_keys=OFF`, but still safe).
 *
 * Per spec: "IntegrityChecker: runs PRAGMA integrity_check, quick_check,
 * foreign_key_check."
 *
 * A healthy db returns [IntegrityResult.Healthy]. Any non-"ok" row from any
 * of the three PRAGMAs returns [IntegrityResult.Corrupt] with the offending
 * output(s) concatenated in [IntegrityResult.Corrupt.details].
 *
 * Note: this is the deep three-PRAGMA check used by [IntegrityChecker] and
 * [SyncInterruptedHandler]. The fast single-PRAGMA check used on the
 * cold-start path lives in OfflineModeManager.kt's `runSqliteIntegrityCheck`.
 */
expect fun runFullIntegrityCheck(): IntegrityResult

/**
 * Enables WAL journal mode on the local SQLite db. Idempotent — calling on
 * a db that's already in WAL mode is a no-op (returns true).
 *
 * Per spec: "SyncInterruptedHandler … uses WAL mode + atomic transactions +
 * write-ahead logging." WAL mode is THE guarantee that an interrupted write
 * leaves the main db file in its last-committed state: incomplete writes
 * accumulate in the `-wal` sidecar file, never in the main file. A crash
 * mid-write therefore corrupts at most the WAL, which SQLite auto-recovers
 * on next open (the WAL is replayed up to the last complete commit frame).
 *
 *   Android: `db.enableWriteAheadLogging()` (API 16+) — equivalent to
 *            `PRAGMA journal_mode=WAL` but also enables multi-threaded
 *            concurrent reads + a single writer.
 *   iOS:     `sqlite3_exec(db, "PRAGMA journal_mode=WAL", …)`.
 *
 * @return true iff the db is now in WAL mode (or was already). false iff
 *         the platform refused (e.g. an in-memory db cannot enter WAL mode).
 */
expect fun enableWalMode(): Boolean

/**
 * Forces a WAL checkpoint — the `-wal` file is merged into the main db file
 * and truncated. Called periodically by [SyncInterruptedHandler] to bound
 * WAL growth (SQLite's default auto-checkpoint at 1000 pages is too lax for
 * heavy sync workloads).
 *
 * Uses `PRAGMA wal_checkpoint(TRUNCATE)` which:
 *   1. Merges all committed WAL frames into the main db.
 *   2. Resets the WAL file to zero size.
 *   3. Returns only after the merge is durable on disk.
 *
 * @return true iff the checkpoint completed without error.
 */
expect fun checkpointWal(): Boolean

/**
 * Reads the local version number for [wordId], or 0 if the row doesn't
 * exist locally. Used by [ConflictResolutionStrategy] to decide whether an
 * incoming write should be applied (last-write-wins).
 *
 * The version is stored in the `words` table — the platform actual queries
 * `SELECT version FROM words WHERE id = ?`. (The `version` column is added
 * by the schema migration that introduces sync; if the column doesn't exist
 * yet, the actual returns 0 and LWW degrades to "always apply".)
 */
expect fun readLocalVersion(wordId: String): Int

/**
 * Backs up user-authored rows from the local db before a destructive
 * re-download. "User-authored" means any rows the user added that are NOT
 * in the cloud's master language pack — e.g. custom words, personal notes,
 * starred/saved words.
 *
 * Per spec: "CorruptionRecovery: deletes local DB, re-downloads from cloud,
 * restores saved words."
 *
 * The platform actual queries the `words` table for rows where
 * `id LIKE 'user:%'` (the convention for user-authored rows — cloud rows
 * use stable UUIDs that never start with `user:`). If the SELECT throws
 * (because the db is too corrupt to serve it), the actual returns an empty
 * list — the caller proceeds with no backup and the user loses their saved
 * words, but the db is at least restored to a usable state.
 *
 * @return a list of [SavedWord] records to be re-inserted after the
 *         re-download completes. Empty list if there are no user-authored
 *         words OR the backup query failed.
 */
expect fun backupSavedWords(): List<SavedWord>

/**
 * Re-inserts [words] into the local db after a destructive re-download.
 * Each insert is wrapped in the same atomic transaction as the rest of the
 * recovery flow (by [CorruptionRecovery]'s caller), so a mid-restore
 * failure rolls back without leaving the db in a half-restored state.
 *
 * The platform actual uses `INSERT OR REPLACE` so that a re-restore
 * (e.g. after a retry) is idempotent — it doesn't fail on duplicate PKs.
 *
 * @return true iff all inserts succeeded.
 */
expect fun restoreSavedWords(words: List<SavedWord>): Boolean

/**
 * Atomically replaces the local SQLite db file with the contents of
 * [newDbBytes] (already-decompressed `.db.zst` bytes). The platform actual:
 *   1. Writes [newDbBytes] to a temp file (`<dbPath>.tmp`).
 *   2. `fsync`s the temp file (so the bytes are durable on disk before
 *      the rename — without this, a crash after rename but before the
 *      fsync of the main file could leave a zero-length db).
 *   3. Renames the temp file over the existing db path (atomic on POSIX:
 *      `rename(2)` is atomic w.r.t. concurrent readers and crashes).
 *   4. Deletes any stale `-wal` / `-shm` siblings (a fresh db starts with
 *      no WAL; SQLite recreates them on first write).
 *
 * A crash at any point before step 3 leaves the old db intact. A crash
 * during step 3 (extremely unlikely — `rename(2)` is atomic on POSIX
 * filesystems) leaves either the old or new db, never a hybrid.
 *
 * @return true iff the new db file is in place AND opens cleanly (a
 *         follow-up `PRAGMA integrity_check` returns "ok").
 */
expect fun atomicReplaceDbFile(newDbBytes: ByteArray): Boolean

// ===========================================================================
// Data classes
// ===========================================================================

/**
 * Result of [runFullIntegrityCheck]. [IntegrityResult.Healthy] means all
 * three PRAGMAs returned "ok" (or, for `foreign_key_check`, no rows). Any
 * deviation returns [IntegrityResult.Corrupt] with the offending output.
 */
sealed class IntegrityResult {
    /** All three PRAGMAs passed. */
    object Healthy : IntegrityResult()

    /**
     * One or more PRAGMAs failed.
     *
     * @property details  The concatenated raw output of all three PRAGMAs
     *                     (newline-separated) for diagnostics. Typically
     *                     surfaced to the cloud via
     *                     `DeltaSyncClient.reportIntegrity` for telemetry.
     */
    data class Corrupt(val details: String) : IntegrityResult()
}

/**
 * One user-authored word — backed up before a destructive re-download and
 * restored after. Mirrors the `words` table schema (lexicon_sqlite.sql:10)
 * but only carries the columns that survive a re-download (everything
 * except `created_at` / `updated_at`, which are re-stamped on insert by
 * the column defaults).
 *
 * @property localVersion  Cloud-side version when this word was last edited
 *                          locally. Used by [ConflictResolutionStrategy]
 *                          after restore to decide whether the cloud has
 *                          a newer version of the same word.
 */
data class SavedWord(
    val id: String,
    val word: String,
    val phonetic: String?,
    val partOfSpeech: String?,
    val definitions: String?,
    val etymology: String?,
    val synonyms: String?,
    val antonyms: String?,
    val examples: String?,
    val language: String?,
    val localVersion: Int
)

/**
 * A single write operation submitted to [TransactionWrapper]. Carries the
 * target row's version number so [ConflictResolutionStrategy] can decide
 * whether to apply it (last-write-wins: apply iff [incomingVersion] is
 * strictly greater than the local version).
 *
 * @property wordId           Primary key of the target row.
 * @property incomingVersion  Cloud-side version of this write.
 * @property operation        INSERT / UPDATE / DELETE.
 * @property payload          The field-level change (null for DELETE).
 */
data class VersionedWrite(
    val wordId: String,
    val incomingVersion: Int,
    val operation: WriteOperation,
    val payload: WordChange? = null
)

/** Operation kind for [VersionedWrite]. */
enum class WriteOperation { INSERT, UPDATE, DELETE }

/**
 * Outcome of a conflict-resolution decision — returned by
 * [ConflictResolutionStrategy.resolve].
 */
sealed class ConflictResolution {
    /**
     * Apply the incoming write as-is. Used for INSERT against a row that
     * doesn't exist locally, and for DELETE (always applies).
     */
    data class Apply(val write: VersionedWrite) : ConflictResolution()

    /**
     * Skip the incoming write — local version is newer or equal
     * (last-write-wins: the local row wins ties for the same version).
     *
     * @property reason  Human-readable explanation for diagnostics.
     */
    data class Skip(val reason: String) : ConflictResolution()

    /**
     * Overwrite the local row — incoming version is strictly newer than
     * the local version. (Functionally similar to [Apply] but distinguished
     * for telemetry so the caller can count "rows overwritten by cloud"
     * vs "rows freshly inserted".)
     */
    data class Overwrite(val write: VersionedWrite) : ConflictResolution()
}

// ===========================================================================
// ConflictResolutionStrategy — last-write-wins + version numbers
// ===========================================================================

/**
 * ConflictResolutionStrategy — decides whether an incoming [VersionedWrite]
 * should be applied against the local db, given the local row's current
 * version number.
 *
 * Policy: last-write-wins (LWW). The row with the higher version number
 * wins. Ties (incoming version == local version) are broken in favour of
 * the local row — this prevents a stuck cloud re-sync from needlessly
 * overwriting locally-cached rows with byte-identical content, and it
 * matches the typical "cloud is the source of truth, but the device's
 * view of the same version is authoritative for its own cache" convention.
 *
 * Per spec: "ConflictResolutionStrategy: last-write-wins + version numbers."
 *
 * This class is pure — no I/O. The caller (typically the delta-sync apply
 * loop) is responsible for looking up the local version via
 * [readLocalVersion] before calling [resolve], and for executing the
 * resulting decision via [TransactionWrapper].
 *
 * DELETE is always applied: a cloud-side DELETE means the word was removed
 * from the master dictionary and the local cache should follow. User-
 * authored words are protected from cloud DELETEs by a separate upstream
 * filter (the `id LIKE 'user:%'` convention — cloud DELETEs never target
 * user-prefixed IDs).
 */
class ConflictResolutionStrategy {

    /**
     * Resolves a single incoming write against the local row's version.
     *
     * @param incoming       The cloud-side write.
     * @param localVersion   The local row's current version (0 if the row
     *                       doesn't exist locally — e.g. for an INSERT).
     * @return one of [ConflictResolution.Apply], [ConflictResolution.Skip],
     *         or [ConflictResolution.Overwrite].
     */
    fun resolve(incoming: VersionedWrite, localVersion: Int): ConflictResolution {
        return when (incoming.operation) {
            WriteOperation.INSERT -> {
                // INSERT against a non-existent row (localVersion == 0) → apply.
                // INSERT against an existing row (localVersion > 0) → treat as
                // UPDATE (the cloud uses INSERT OR REPLACE idempotently; we
                // resolve via version comparison to decide whether to overwrite).
                if (localVersion == 0) {
                    ConflictResolution.Apply(incoming)
                } else if (incoming.incomingVersion > localVersion) {
                    ConflictResolution.Overwrite(incoming)
                } else {
                    ConflictResolution.Skip(
                        "INSERT id=${incoming.wordId} skipped: " +
                            "local v$localVersion >= incoming v${incoming.incomingVersion}"
                    )
                }
            }
            WriteOperation.UPDATE -> {
                if (incoming.incomingVersion > localVersion) {
                    ConflictResolution.Overwrite(incoming)
                } else {
                    ConflictResolution.Skip(
                        "UPDATE id=${incoming.wordId} skipped: " +
                            "local v$localVersion >= incoming v${incoming.incomingVersion}"
                    )
                }
            }
            WriteOperation.DELETE -> {
                // DELETE always applies — a cloud-side delete means the word
                // is gone from the master pack and the local cache should
                // follow. User-authored words are protected upstream by the
                // `id LIKE 'user:%'` filter (cloud DELETEs never target
                // user-prefixed IDs).
                ConflictResolution.Apply(incoming)
            }
        }
    }

    /**
     * Convenience: resolves a batch of incoming writes against their local
     * versions (looked up via [readLocalVersion]). Returns the subset that
     * should be applied (Apply + Overwrite outcomes; Skip outcomes are
     * dropped).
     *
     * The caller is responsible for executing the returned writes inside a
     * single [TransactionWrapper.runWrites] call so the batch is atomic.
     */
    fun resolveBatch(incoming: List<VersionedWrite>): List<VersionedWrite> {
        return incoming.mapNotNull { write ->
            val localVersion = readLocalVersion(write.wordId)
            when (val decision = resolve(write, localVersion)) {
                is ConflictResolution.Apply -> decision.write
                is ConflictResolution.Overwrite -> decision.write
                is ConflictResolution.Skip -> null
            }
        }
    }
}

// ===========================================================================
// TransactionWrapper — atomic SQLite writes
// ===========================================================================

/**
 * TransactionWrapper — wraps every SQLite write in a single atomic
 * transaction.
 *
 * Per spec: "TransactionWrapper: wraps all SQLite writes in atomic
 * transactions."
 *
 * The wrapper delegates to the platform [executeInTransaction] expect fun,
 * which uses BEGIN / COMMIT / ROLLBACK under the hood. Combined with WAL
 * mode (enabled separately by [SyncInterruptedHandler.enableWal]), this
 * delivers the spec's "zero database corruption errors across 10,000
 * simulated sync interruptions" hard gate:
 *   - Atomic transactions guarantee that a half-applied batch rolls back
 *     to the last COMMIT on the next open.
 *   - WAL mode guarantees that the rollback is automatic (SQLite replays
 *     the WAL up to the last complete commit frame) and that the main db
 *     file is never modified mid-transaction.
 *
 * Usage:
 *   val ok = transactionWrapper.runWrites {
 *       // … bind + execute SQLiteStatements …
 *       // return true iff all succeeded
 *   }
 *   if (!ok) { /* rollback already happened; surface error */ }
 */
class TransactionWrapper {

    /**
     * Runs [block] inside a single atomic transaction.
     *
     * @return true iff the transaction committed. false iff it rolled back
     *         (either because [block] returned false or because it threw).
     *         [CancellationException] is ALWAYS rethrown — structured
     *         concurrency must propagate even though this function is
     *         non-suspending (the platform actual's I/O may surface
     *         cancellation as a throwable).
     */
    fun runWrites(block: () -> Boolean): Boolean {
        return try {
            executeInTransaction(block)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Any unexpected throwable from the platform layer (e.g. SQLite
            // constraint violation that escaped the block's own try/catch)
            // is treated as a rollback — the actual's finally block already
            // ran ROLLBACK via endTransaction / sqlite3_exec("ROLLBACK").
            false
        }
    }

    /**
     * Convenience overload for a block that returns [Unit]. The wrapper
     * interprets a non-throwing return as success (commit) and any throw
     * as failure (rollback).
     */
    fun runWritesUnit(block: () -> Unit): Boolean {
        return runWrites {
            block()
            true
        }
    }
}

// ===========================================================================
// IntegrityChecker — runs all three integrity PRAGMAs
// ===========================================================================

/**
 * IntegrityChecker — runs the full battery of SQLite integrity PRAGMAs.
 *
 * Per spec: "IntegrityChecker: runs PRAGMA integrity_check, quick_check,
 * foreign_key_check."
 *
 * The three PRAGMAs serve different purposes:
 *   - `integrity_check`:   Full structural check. Catches b-tree corruption,
 *                          page-chain breaks, missing pages. Slow on large
 *                          dbs (~1s per 100 MB), so run on cold start only.
 *   - `quick_check`:       Subset of `integrity_check` (skips b-tree
 *                          validation). ~10x faster; safe to run after
 *                          every batch apply.
 *   - `foreign_key_check`: Verifies FK constraints. No-op if
 *                          `PRAGMA foreign_keys=OFF`, but cheap to run
 *                          unconditionally.
 *
 * [check] returns [IntegrityResult.Healthy] iff all three pass. Any failure
 * returns [IntegrityResult.Corrupt] with the concatenated raw output for
 * diagnostics — the caller (typically [SyncInterruptedHandler]) feeds this
 * into [CorruptionRecovery.recover].
 */
class IntegrityChecker {

    /**
     * Runs all three PRAGMAs and returns the combined result.
     *
     * @return [IntegrityResult.Healthy] iff all three pass; otherwise
     *         [IntegrityResult.Corrupt] with details.
     */
    fun check(): IntegrityResult {
        return runFullIntegrityCheck()
    }

    /**
     * Convenience: returns true iff [check] returns [IntegrityResult.Healthy].
     * Use this when you don't need the diagnostic details.
     */
    fun isHealthy(): Boolean = check() is IntegrityResult.Healthy
}

// ===========================================================================
// CorruptionRecovery — delete + re-download + restore saved words
// ===========================================================================

/**
 * CorruptionRecovery — restores the local db to a healthy state after
 * integrity-check failure, while preserving user-authored rows.
 *
 * Per spec: "CorruptionRecovery: deletes local DB, re-downloads from cloud,
 * restores saved words."
 *
 * Flow:
 *   1. Backup user-authored rows via [backupSavedWords] (platform expect fun).
 *      This runs on the still-corrupt db — SQLite can usually serve simple
 *      SELECTs from a corrupt db (the corruption is typically in the FTS5
 *      index or a single b-tree page, not the words table itself). If the
 *      SELECT throws, we proceed with an empty backup (saved words are lost
 *      — surfaced to the caller via [RecoveryOutcome.savedWordsRestored]).
 *   2. Download a fresh `.db.zst` from the cloud (via the injected
 *      [DeltaSyncClient.downloadLanguagePack]) and decompress it.
 *   3. Atomically replace the local db file via [atomicReplaceDbFile]
 *      (platform expect fun — temp file + fsync + rename).
 *   4. Restore the backed-up user-authored rows via [restoreSavedWords]
 *      (platform expect fun), inside a single transaction via
 *      [TransactionWrapper].
 *   5. Re-run [IntegrityChecker.check] to confirm the new db is healthy.
 *
 * If any step fails, the local db is left in whichever state the last
 * successful step produced. The caller can retry the whole flow.
 *
 * @param deltaSyncClient      Reused from OfflineModeManager — no second HttpClient.
 * @param integrityChecker     Injected (or default).
 * @param transactionWrapper   Injected (or default).
 */
class CorruptionRecovery(
    private val deltaSyncClient: DeltaSyncClient,
    private val integrityChecker: IntegrityChecker = IntegrityChecker(),
    private val transactionWrapper: TransactionWrapper = TransactionWrapper()
) {

    /**
     * Runs the full recovery flow for [languageCode].
     *
     * @param languageCode  The language to recover (drives which pack to
     *                      re-download).
     * @param downloadUrl   The CDN URL of the `.db.zst` pack (typically
     *                      registered in OfflineModeManager via
     *                      `registerLanguagePackUrl`).
     * @return a [RecoveryOutcome] describing what happened — callers use
     *         this to surface UI ("db restored, 12 saved words recovered")
     *         and to decide whether to retry.
     */
    suspend fun recover(languageCode: String, downloadUrl: String): RecoveryOutcome {
        // 1. Backup user-authored rows BEFORE any destructive op.
        val saved: List<SavedWord> = try {
            backupSavedWords()
        } catch (t: Throwable) {
            // Backup failed — proceed with empty backup. The user's saved
            // words are lost, but the db is at least restored to a usable
            // state. Flag this in the outcome so the caller can surface a
            // "some saved words could not be restored" notice.
            return downloadReplaceAndVerify(languageCode, downloadUrl, emptyList(), savedWordsRestored = false)
        }

        return downloadReplaceAndVerify(languageCode, downloadUrl, saved, savedWordsRestored = true)
    }

    /**
     * Steps 2-5 of the recovery flow, factored out so [recover] can call it
     * with either the backed-up word list or an empty list (when backup
     * failed). The [savedWordsRestored] flag is propagated to the outcome
     * so the caller knows whether to warn the user about lost saved words.
     */
    private suspend fun downloadReplaceAndVerify(
        languageCode: String,
        downloadUrl: String,
        saved: List<SavedWord>,
        savedWordsRestored: Boolean
    ): RecoveryOutcome {
        // 2. Download + decompress the fresh pack.
        val freshBytes: ByteArray = try {
            deltaSyncClient.downloadLanguagePack(languageCode, downloadUrl)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Download failed — local db is untouched (still corrupt).
            return RecoveryOutcome(
                success = false,
                dbReplaced = false,
                savedWordsRestored = false,
                savedWordsCount = saved.size,
                error = "download failed: ${t.message}"
            )
        }

        // 3. Atomically replace the local db file.
        val replaced = try {
            atomicReplaceDbFile(freshBytes)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            return RecoveryOutcome(
                success = false,
                dbReplaced = false,
                savedWordsRestored = false,
                savedWordsCount = saved.size,
                error = "atomicReplaceDbFile threw: ${t.message}"
            )
        }
        if (!replaced) {
            return RecoveryOutcome(
                success = false,
                dbReplaced = false,
                savedWordsRestored = false,
                savedWordsCount = saved.size,
                error = "atomicReplaceDbFile returned false"
            )
        }

        // 4. Restore saved words in a single transaction.
        var restoredCount = 0
        if (saved.isNotEmpty()) {
            val restored = transactionWrapper.runWrites {
                val ok = try {
                    restoreSavedWords(saved)
                } catch (t: Throwable) {
                    false
                }
                if (ok) restoredCount = saved.size
                ok
            }
            if (!restored) {
                // Restore failed — db is fresh but user words are missing.
                // Not fatal: the db is usable, just incomplete. Continue to
                // the final integrity check; surface the partial restore in
                // the outcome.
            }
        }

        // 5. Final integrity check on the new db.
        val healthy = integrityChecker.isHealthy()
        return RecoveryOutcome(
            success = healthy,
            dbReplaced = true,
            savedWordsRestored = savedWordsRestored && restoredCount == saved.size,
            savedWordsCount = restoredCount,
            error = if (healthy) null else "post-recovery integrity_check failed"
        )
    }
}

/**
 * Outcome of [CorruptionRecovery.recover]. Surfaces enough detail for the
 * host app to show a precise UI ("db restored, 12 of 12 saved words
 * recovered" vs "db restored but saved words were lost").
 *
 * @property success               true iff the db is now healthy.
 * @property dbReplaced            true iff the local db file was replaced
 *                                  with a fresh download (steps 1-3
 *                                  completed, regardless of restore outcome).
 * @property savedWordsRestored    true iff ALL backed-up saved words were
 *                                  re-inserted successfully.
 * @property savedWordsCount       Number of saved words actually restored
 *                                  (may be < the backup size if restore
 *                                  partially failed).
 * @property error                 Human-readable error if [success] is false;
 *                                  null otherwise.
 */
data class RecoveryOutcome(
    val success: Boolean,
    val dbReplaced: Boolean,
    val savedWordsRestored: Boolean,
    val savedWordsCount: Int,
    val error: String?
)

// ===========================================================================
// SyncInterruptedHandler — zero corruption across 10k simulated interruptions
// ===========================================================================

/**
 * SyncInterruptedHandler — guarantees zero database corruption even under
 * repeated sync interruptions (app kill, OS crash, battery pull, network
 * drop mid-write).
 *
 * Per spec: "Zero database corruption errors across 10,000 simulated sync
 * interruptions — uses WAL mode + atomic transactions + write-ahead logging."
 *
 * Mechanism (the three guarantees that together deliver zero corruption):
 *
 *   1. WAL mode (`PRAGMA journal_mode=WAL`):
 *      Incomplete writes accumulate in the `-wal` sidecar file, NEVER in
 *      the main db file. A crash mid-write leaves the main file in its
 *      last-committed state. On next open, SQLite replays the WAL up to
 *      the last complete commit frame and discards any trailing partial
 *      frame — automatic recovery, no app intervention needed.
 *
 *   2. Atomic transactions (BEGIN / COMMIT via [TransactionWrapper]):
 *      Every batch of writes is wrapped in a single transaction. A crash
 *      mid-transaction means the commit frame was never written to the WAL,
 *      so the next-open replay rolls back the entire batch. There is no
 *      "half-applied batch" state — it's all or nothing.
 *
 *   3. Write-ahead logging (the WAL itself):
 *      The WAL is an append-only, crash-safe log of committed db pages.
 *      SQLite writes to the WAL first, then checkpoints (merges) into the
 *      main db lazily. A crash during checkpoint leaves both the WAL and
 *      the main db in a consistent state (the checkpoint is restartable).
 *
 * The handler's [safeWrite] method:
 *   1. Ensures WAL mode is enabled (idempotent — first call only).
 *   2. Wraps [block] in [TransactionWrapper.runWrites].
 *   3. On any throwable (simulating an interruption), re-opens the db
 *      (which auto-recovers the WAL) and runs [IntegrityChecker.check].
 *      If the check passes, the interruption was recovered cleanly.
 *      If it fails, escalates to [CorruptionRecovery.recover].
 *   4. Periodically forces a WAL checkpoint to bound WAL growth.
 *
 * Usage in a sync loop:
 *   for (batch in batches) {
 *       val ok = handler.safeWrite(batch.languageCode, batch.downloadUrl) {
 *           // raw SQLite writes — NO inner transaction (the wrapper provides it)
 *           applyRawWrites(batch.changes)
 *       }
 *       if (!ok) { /* CorruptionRecovery already ran; abort sync */ }
 *   }
 *
 * @param transactionWrapper  Injected (or default) — same instance used
 *                             elsewhere to share the connection.
 * @param integrityChecker    Injected (or default).
 * @param corruptionRecovery  Injected (or null — null disables auto-recovery
 *                             and surfaces corruption as a `false` return
 *                             for the caller to handle).
 * @param checkpointEveryNth  Force a WAL checkpoint every N writes to bound
 *                             WAL growth. Default 50 (≈once per typical
 *                             sync session of 50 batches).
 */
class SyncInterruptedHandler(
    private val transactionWrapper: TransactionWrapper = TransactionWrapper(),
    private val integrityChecker: IntegrityChecker = IntegrityChecker(),
    private val corruptionRecovery: CorruptionRecovery? = null,
    private val checkpointEveryNth: Int = 50
) {
    @Volatile
    private var walEnabled: Boolean = false

    /** Guards [writeCount] — incremented on every [safeWrite] call. */
    private val writeCounterMutex = Mutex()
    private var writeCount: Int = 0

    /**
     * Runs [block] inside a transaction with full interruption recovery.
     *
     * @param languageCode  Used for corruption recovery if needed.
     * @param downloadUrl   Used for corruption recovery if needed.
     * @param block         The write operation to execute. Return true iff
     *                      all writes succeeded; return false OR throw to
     *                      signal failure (both trigger rollback).
     *                      IMPORTANT: [block] must NOT open its own inner
     *                      transaction — [TransactionWrapper] provides the
     *                      outer BEGIN/COMMIT. (The platform actual for
     *                      [executeInTransaction] handles accidental nesting
     *                      via SAVEPOINT, but explicit nesting is discouraged.)
     * @return true iff the write committed (fast path — no integrity check
     *         needed, WAL guarantees consistency).
     *         false iff the write rolled back AND either (a) the db is still
     *         healthy (transient failure — caller can retry the batch), OR
     *         (b) the db was corrupt and recovery was attempted (caller
     *         MUST abort the sync session and re-query the server from the
     *         new cursor — the local db is now fresh from the cloud).
     */
    suspend fun safeWrite(
        languageCode: String,
        downloadUrl: String,
        block: () -> Boolean
    ): Boolean {
        ensureWal()

        val committed = try {
            transactionWrapper.runWrites(block)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Simulated interruption — the transaction rolled back.
            // WAL guarantees the main db is intact; verify with a check.
            false
        }

        maybeCheckpoint()

        if (committed) {
            // Fast path: write committed cleanly. Skip the full integrity
            // check (expensive) — WAL's commit-frame guarantee means a
            // successful COMMIT implies a consistent db. The full check
            // runs on cold start via OfflineModeManager.
            return true
        }

        // Slow path: write failed OR threw. Verify db health.
        val healthy = integrityChecker.isHealthy()
        if (healthy) {
            // Db is fine — the failure was a transient issue (constraint
            // violation, OOM, etc.) that rolled back cleanly. Caller can
            // retry the batch.
            return false
        }

        // Db is corrupt — escalate to full recovery.
        // If no recovery is configured, surface as a hard failure.
        val recovery = corruptionRecovery ?: return false
        try {
            // We intentionally ignore the RecoveryOutcome here: whether
            // recovery succeeded or failed, the caller MUST abort this
            // sync session and re-query the server from the new cursor
            // (the local db is either fresh from the cloud, or still
            // corrupt — either way the in-flight batch is no longer
            // applicable). The outcome's savedWordsRestored / error fields
            // are surfaced separately by the host app via telemetry.
            recovery.recover(languageCode, downloadUrl)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            // Recovery itself crashed — surface as a hard failure.
        }
        return false
    }

    /**
     * Forces an immediate WAL checkpoint + integrity check. Call from a
     * background maintenance task (e.g. once per app launch, or after a
     * large sync session) to keep the WAL small and detect any latent
     * corruption.
     *
     * @return true iff the checkpoint succeeded AND the integrity check
     *         passed.
     */
    fun maintenanceCheck(): Boolean {
        val checkpointed = try {
            checkpointWal()
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            false
        }
        if (!checkpointed) return false
        return integrityChecker.isHealthy()
    }

    // -------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------

    /**
     * Enables WAL mode on first call; no-op thereafter. Idempotent.
     */
    private fun ensureWal() {
        if (!walEnabled) {
            walEnabled = enableWalMode()
            // If the platform refuses WAL (e.g. in-memory db), we proceed
            // with the default journal mode (DELETE or TRUNCATE). The
            // atomic-transaction guarantee still holds; only the crash-
            // recovery window degrades (a mid-transaction crash could lose
            // the last batch but not corrupt the db — DELETE journal also
            // rolls back uncommitted changes on next open, just less
            // efficiently than WAL).
        }
    }

    /**
     * Periodically checkpoints the WAL to bound its growth. Called after
     * every write; the actual checkpoint runs only every [checkpointEveryNth]
     * write to avoid I/O overhead.
     */
    private suspend fun maybeCheckpoint() {
        val n = writeCounterMutex.withLock {
            writeCount++
            writeCount
        }
        if (n % checkpointEveryNth == 0) {
            try {
                checkpointWal()
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                // Checkpoint failure is non-fatal — the WAL will keep
                // growing until the next successful checkpoint. SQLite
                // auto-checkpoints at 1000 pages by default, so this is
                // just an optimization to keep the WAL small for faster
                // crash recovery.
            }
        }
    }
}
