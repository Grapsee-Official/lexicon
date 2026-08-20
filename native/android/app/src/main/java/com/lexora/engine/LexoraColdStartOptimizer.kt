package com.lexora.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

/**
 * LexoraColdStartOptimizer — orchestrates a sub-500ms cold start.
 *
 * Spec hard gate: `<500ms cold start on iPhone SE.` The Android equivalent
 * low-end target is a Moto G / Android Go class device; the same budget
 * applies (the work done is identical, only the kernel paging cost differs).
 *
 * # Strategy — 3 phases
 *
 * 1. **Synchronous hash table load** (fast via `mmap`).
 *    `LexoraEngine.loadDatabase(dbDir)` opens `hash_table.bin` and `dawg.bin`
 *    and `mmap`s them. `mmap` itself is O(1) — the kernel only records the
 *    VMA and demand-pages on first access, so this step costs ~1-3 ms even
 *    for a 50 MB dictionary. We do this synchronously inside the init
 *    coroutine on [Dispatchers.IO]; the calling thread (typically
 *    [com.lexora.LexoraApplication.onCreate]) is NOT blocked because
 *    [warmEngine] returns immediately with a [Deferred].
 *
 * 2. **Defer DAWG enable until after first frame.**
 *    The DAWG file is `mmap`'d in phase 1, but its pages are not faulted
 *    into the page cache until something actually reads them (a prefix
 *    search, a fuzzy search). We keep `enableDawg(false)` during phase 1 so
 *    the first-frame render path can't accidentally touch DAWG memory.
 *    A [Handler.postDelayed] on the main [Looper] schedules the enable
 *    ~1 frame (16 ms) after [warmEngine] returns — by then Compose has
 *    painted the home screen and the user perceives an instant launch.
 *
 * 3. **Async DAWG enable on [Dispatchers.IO].**
 *    Inside the postDelayed callback we hop to a coroutine on
 *    [Dispatchers.IO] and call `engine.enableDawg(true)`. Subsequent
 *    prefix/fuzzy searches will fault in DAWG pages on the IO thread pool,
 *    never on the main thread.
 *
 * # What "engine ready" means
 *
 * The [Deferred] returned by [warmEngine] completes as soon as the hash
 * table load returns successfully. At that point `engine.lookup(word)` is
 * fully functional. Prefix search / fuzzy search will silently no-op
 * (returning empty results) until the deferred DAWG enable completes — the
 * native engine handles this gracefully.
 *
 * # Measurement contract
 *
 * Cold start is measured from the moment this singleton is constructed
 * (intended to be the first statement of
 * [com.lexora.LexoraApplication.onCreate]) to the moment the hash table
 * load returns. Both timestamps are captured with [System.nanoTime] and
 * reported via [Log.d] under tag `LexoraColdStart`. A breach of the 500 ms
 * gate is logged as a [Log.w] warning.
 *
 * # Wiring (future change to LexoraApplication — NOT in scope of this file)
 *
 * ```kotlin
 * class LexoraApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         val optimizer = LexoraColdStartOptimizer.get(this) // <- T0 captured here
 *         optimizer.warmEngine(this)                          // <- returns immediately
 *     }
 * }
 * ```
 */
class LexoraColdStartOptimizer private constructor(
    appContext: Context,
) {

    private val appContext: Context = appContext.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Wall-clock anchor captured at singleton construction.
     *
     * Intended to be the very first thing that happens inside
     * [com.lexora.LexoraApplication.onCreate]. Measured with
     * [System.nanoTime] for monotonic nanosecond precision.
     */
    private val t0AppOnCreateNanos: Long = System.nanoTime()

    /** nanoTime when the hash-table load returned (engine "ready"). */
    @Volatile private var engineReadyNanos: Long = 0L

    /** nanoTime when the deferred DAWG enable completed. */
    @Volatile private var dawgReadyNanos: Long = 0L

    /**
     * Warm the engine: synchronously mmap the hash table inside an IO
     * coroutine, then schedule the DAWG enable one frame later.
     *
     * Returns immediately with a [Deferred] that resolves to a
     * ready-to-lookup [LexoraEngine] as soon as the hash table load
     * finishes. Safe to call from the main thread.
     *
     * Idempotent in the sense that calling it twice will start two
     * independent engine instances — callers should normally hold on to
     * the returned [Deferred] rather than re-invoking.
     */
    fun warmEngine(context: Context): Deferred<LexoraEngine> {
        val ctx = context.applicationContext
        Log.d(TAG, "warmEngine() invoked — scheduling engine init on Dispatchers.IO")

        // CoroutineStart.DEFAULT (async's default) starts eagerly — the
        // hash-table mmap kicks off immediately, not when the Deferred
        // is awaited. This is what makes "synchronous hash table load"
        // semantically correct: the load is a synchronous call *inside*
        // the coroutine, blocking the coroutine (not the caller).
        return scope.async(Dispatchers.IO) {
            val tConstruct = System.nanoTime()
            val engine = LexoraEngine()
            val constructMs = (System.nanoTime() - tConstruct) / NANOS_PER_MS
            Log.d(TAG, "LexoraEngine() constructed — ${constructMs}ms (native handle allocated)")

            val dbDir = resolveDbDir(ctx)
            val tLoad = System.nanoTime()
            val loaded = try {
                engine.loadDatabase(dbDir)
            } catch (t: Throwable) {
                Log.e(TAG, "loadDatabase threw — dir=$dbDir", t)
                throw t
            }
            val loadMs = (System.nanoTime() - tLoad) / NANOS_PER_MS

            if (!loaded) {
                Log.e(TAG, "loadDatabase returned false — dir=$dbDir (loadMs=$loadMs)")
                throw IllegalStateException("LexoraEngine.loadDatabase failed for dir=$dbDir")
            }

            // Defensive: keep DAWG OFF for first frame. The native side
            // may default-enable it; we explicitly disable so the first
            // Compose frame can't touch DAWG pages and trigger faults.
            engine.enableDawg(false)

            engineReadyNanos = System.nanoTime()
            val coldStartMs = (engineReadyNanos - t0AppOnCreateNanos) / NANOS_PER_MS
            Log.d(
                TAG,
                "engine READY — coldStart=${coldStartMs}ms " +
                    "(hashTableLoad=${loadMs}ms, dawg=disabled, dbDir=$dbDir)"
            )
            if (coldStartMs > HARD_GATE_MS) {
                Log.w(
                    TAG,
                    "COLD START GATE BREACH: ${coldStartMs}ms > ${HARD_GATE_MS}ms " +
                        "budget. Investigate DAWG pre-fault, DB path, or native lib load."
                )
            }

            // Phase 2 + 3: defer DAWG enable until after first frame.
            scheduleDawgEnable(engine)

            engine
        }
    }

    /**
     * Schedule `enableDawg(true)` [DAWG_DEFER_DELAY_MS] after now on the
     * main [Looper]. When the callback fires, hop to [Dispatchers.IO] so
     * any page faults triggered by the enable land on the IO pool, not
     * the main thread.
     */
    private fun scheduleDawgEnable(engine: LexoraEngine) {
        mainHandler.postDelayed({
            scope.launch(Dispatchers.IO) {
                val tEnable = System.nanoTime()
                try {
                    engine.enableDawg(true)
                } catch (t: Throwable) {
                    Log.e(TAG, "Deferred enableDawg(true) threw", t)
                    return@launch
                }
                dawgReadyNanos = System.nanoTime()
                val enableMs = (System.nanoTime() - tEnable) / NANOS_PER_MS
                val delaySinceReadyMs = (dawgReadyNanos - engineReadyNanos) / NANOS_PER_MS
                Log.d(
                    TAG,
                    "DAWG enabled (deferred) — " +
                        "enable=${enableMs}ms, " +
                        "delaySinceReady=${delaySinceReadyMs}ms, " +
                        "isDawgEnabled=${engine.isDawgEnabled()}"
                )
            }
        }, DAWG_DEFER_DELAY_MS)
        Log.d(TAG, "DAWG enable scheduled in ${DAWG_DEFER_DELAY_MS}ms (post first frame)")
    }

    /**
     * Resolve the on-disk directory holding `hash_table.bin` + `dawg.bin`.
     * Default location is `context.filesDir/lexora`. The directory is
     * created if missing — the engine will then fail loudly if the files
     * are absent, which is the desired behaviour (no silent empty DB).
     */
    private fun resolveDbDir(ctx: Context): String {
        val dir = File(ctx.filesDir, "lexora").apply { mkdirs() }
        return dir.absolutePath
    }

    /**
     * Measured cold start in milliseconds — valid only after the
     * [warmEngine] Deferred has completed. Returns -1 if not yet ready.
     * Useful for tests / analytics.
     */
    fun coldStartMillis(): Long {
        val ready = engineReadyNanos
        return if (ready == 0L) -1L else (ready - t0AppOnCreateNanos) / NANOS_PER_MS
    }

    /**
     * Cancel the optimizer's [CoroutineScope] and remove pending
     * [Handler] callbacks. Call from [android.app.Application.onTerminate]
     * (or tests) for clean teardown. After shutdown, [warmEngine] must
     * not be called again on this instance.
     */
    fun shutdown() {
        mainHandler.removeCallbacksAndMessages(null)
        scope.cancel()
        Log.d(TAG, "shutdown() — Handler callbacks cleared, coroutine scope cancelled")
    }

    companion object {
        private const val TAG = "LexoraColdStart"

        /** Hard gate from the spec: `<500ms cold start on iPhone SE`. */
        private const val HARD_GATE_MS = 500L

        /**
         * Defer DAWG enable by ~1 frame (16 ms @ 60 Hz) so the first
         * Compose frame paints without competing for page-cache
         * bandwidth. Tunable — bump to 32 ms on 30 Hz devices if needed.
         */
        private const val DAWG_DEFER_DELAY_MS = 16L

        private const val NANOS_PER_MS = 1_000_000L

        @Volatile private var instance: LexoraColdStartOptimizer? = null

        /**
         * Process-wide singleton. The optimizer owns its own
         * [CoroutineScope] and [Handler] — we never want two instances
         * racing to load the engine. First call captures the cold-start
         * T0 timestamp, so this should be invoked as early as possible
         * in [com.lexora.LexoraApplication.onCreate].
         */
        fun get(context: Context): LexoraColdStartOptimizer =
            instance ?: synchronized(this) {
                instance ?: LexoraColdStartOptimizer(context.applicationContext)
                    .also { instance = it }
            }
    }
}
