package com.lexicon.engine

import android.os.Choreographer
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.ceil

/**
 * FrameMonitor — Android frame-timing instrumentation that enforces the
 * spec's hard gates for scroll jank:
 *
 *   * "95th percentile frame render time <16ms (60fps) during list scrolling"
 *     (Week 16)
 *   * "Zero frame drops scrolling 1000 items on Moto G" (Week 16)
 *
 * # How it works
 *
 * [Choreographer.FrameCallback.doFrame] fires once per vsync on the main
 * [Looper], with `frameTimeNanos` being the timestamp of the vsync that
 * triggered this frame (in the same time base as [System.nanoTime]).
 *
 * The **inter-frame interval** = `frameTimeNanos(current) −
 * frameTimeNanos(previous)` is the wall-clock duration the platform
 * allotted to the previous frame's render pass. If that interval exceeds
 * the per-screen budget (default 16.67 ms @ 60 Hz), the frame is counted
 * as **jank** (a dropped frame).
 *
 * This is more accurate than timing individual `onDraw` calls because it
 * captures the *full* frame cost including measure / layout / draw /
 * GPU submit / compositor handoff — anything that pushed the frame past
 * the vsync deadline.
 *
 * # Per-screen tracking
 *
 * Call [startScreen] when a screen becomes visible (e.g. inside
 * `Composable.LaunchedEffect` or `Activity.onResume`) and [stopScreen]
 * when it leaves the foreground. Each screen maintains its own
 * accumulator; [snapshot] reads the live counters without stopping.
 *
 * Multiple screens can be tracked concurrently (e.g. during a transition
 * where two screens are partially visible). The Choreographer callback
 * feeds every active screen — each screen sees the same frame interval,
 * but attributes it to its own accumulator.
 *
 * # JSON output
 *
 * When [stopScreen] is called, a `frame_stats` event is emitted in the
 * same NDJSON format as the C++ [lexicon::PerfConfig] perf log:
 *
 *   {"ts": <epoch_ms>, "type": "frame_stats", "data": {
 *     "screen": "<name>", "total_frames": N, "dropped_frames": M,
 *     "jank_percent": <pct>, "budget_ms": 16.67,
 *     "min_ms": <ms>, "max_ms": <ms>, "mean_ms": <ms>,
 *     "p50_ms": <ms>, "p95_ms": <ms>, "p99_ms": <ms>
 *   }}
 *
 * The log path defaults to `<cacheDir>/lexicon_perf.log` (matching the
 * C++ side's intent of writing to the app's cache dir on Android).
 * Override via [setLogPath] if you want to co-locate with the engine's
 * perf log (the C++ side appends to the same file safely when both
 * processes open it in append mode).
 *
 * # Verbosity
 *
 * Verbose mode (set via [setVerbose] = true, or via the
 * `LEXICON_PERF_LEVEL=2` env var on debuggable builds) emits a per-jank
 * `frame_jank` event for every dropped frame. This is high-volume — use
 * only during profiling sessions, never in production.
 *
 * # Overhead
 *
 * Per-frame cost on the main thread:
 *   * 1 `System.nanoTime` comparison (already provided by Choreographer's
 *     `frameTimeNanos` — no extra clock read needed).
 *   * 1 ConcurrentHashMap lookup per active screen (≈ 50 ns).
 *   * 1 array push into a pre-sized ring buffer (amortized O(1)).
 *   * A handful of long/double arithmetic ops.
 *
 * Total: < 1 μs per frame per active screen. At 60 fps with one active
 * screen that's < 60 μs/sec of main-thread overhead — well below the
 * 16 ms frame budget.
 *
 * # Wiring
 *
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onResume() {
 *         super.onResume()
 *         FrameMonitor.get(this).startScreen("home")
 *     }
 *     override fun onPause() {
 *         super.onPause()
 *         FrameMonitor.get(this).stopScreen("home")
 *     }
 * }
 * ```
 *
 * For Compose, use `DisposableEffect(Unit) { onDispose { ... } }` inside
 * the screen's top-level composable.
 */
class FrameMonitor private constructor(
    private val cacheDir: File,
) {

    // ------------------------------------------------------------------------
    // Public API — screen lifecycle
    // ------------------------------------------------------------------------

    /**
     * Start accumulating frame stats for [screenName]. Idempotent: if the
     * screen is already being tracked, the existing accumulator is reset
     * (so the new window starts fresh).
     *
     * Safe to call from any thread. The first call to [startScreen]
     * (or [ensureRunning]) installs the [Choreographer.FrameCallback]
     * on the main [Looper].
     *
     * @param screenName logical screen label (e.g. "home", "dictionary",
     *   "word_detail"). Goes into the JSON event's "screen" field.
     * @param frameBudgetMs per-frame budget. Frames whose render time
     *   exceeds this are counted as drops. Default 16.67 ms (60 fps).
     *   Use 8.33 for 120 Hz displays, 11.11 for 90 Hz.
     */
    fun startScreen(screenName: String, frameBudgetMs: Double = DEFAULT_FRAME_BUDGET_MS) {
        ensureRunning()
        val acc = screens.computeIfAbsent(screenName) {
            ScreenAccumulator(screenName, frameBudgetMs)
        }
        acc.reset(frameBudgetMs)
        Log.d(TAG, "startScreen('$screenName') — budget=${frameBudgetMs}ms, " +
            "activeScreens=${screens.size}")
    }

    /**
     * Stop accumulating for [screenName], emit a JSON `frame_stats` event
     * to the perf log, and return the final [ScreenStats]. Returns null
     * if the screen wasn't being tracked (no-op).
     *
     * Safe to call from any thread. The Choreographer callback keeps
     * running as long as at least one screen is active; it's torn down
     * only when the last screen is stopped.
     */
    fun stopScreen(screenName: String): ScreenStats? {
        val acc = screens.remove(screenName) ?: run {
            Log.w(TAG, "stopScreen('$screenName') — not tracked, no-op")
            return null
        }
        val stats = acc.snapshot()
        emitFrameStats(stats)
        Log.d(TAG, "stopScreen('$screenName') — total=${stats.totalFrames}, " +
            "dropped=${stats.droppedFrames}, jankPct=${stats.jankPercent}%, " +
            "p95=${stats.p95Ms}ms")
        // If no screens remain, stop the Choreographer callback to save battery.
        if (screens.isEmpty()) {
            stopRunning()
        }
        return stats
    }

    /**
     * Read the current stats for [screenName] without stopping accumulation.
     * Returns null if the screen isn't tracked.
     *
     * Safe to call from any thread. The returned [ScreenStats] is a snapshot
     * — it does NOT continue to update.
     */
    fun snapshot(screenName: String): ScreenStats? {
        val acc = screens[screenName] ?: return null
        return acc.snapshot()
    }

    /**
     * Snapshot every active screen. Returns a map keyed by screen name.
     * Useful for periodic background reporting (e.g. emit all screens
     * every 2 seconds via a [Handler.postDelayed] loop).
     */
    fun snapshotAll(): Map<String, ScreenStats> {
        return screens.mapValues { (_, acc) -> acc.snapshot() }
    }

    // ------------------------------------------------------------------------
    // Public API — configuration
    // ------------------------------------------------------------------------

    /**
     * Override the JSON perf log path. Defaults to `<cacheDir>/lexicon_perf.log`.
     * Pass null to disable file logging (events still go to logcat).
     *
     * The C++ [lexicon::PerfConfig] writes to the same path by default
     * (set via `LEXICON_PERF_LOG` env var on the native side). Opening
     * the same file in append mode from both languages is safe on
     * Android's POSIX filesystem — each write is a single `write(2)`
     * syscall, which is atomic for sizes ≤ PIPE_BUF (4 KB on Linux).
     * Our JSON events are well under 4 KB.
     */
    fun setLogPath(path: String?) {
        logPath = path
        Log.d(TAG, "setLogPath('${path ?: "<null>"}')")
    }

    /**
     * Enable verbose per-jank logging. When true, every dropped frame
     * emits a `frame_jank` JSON event (high volume — use only during
     * profiling). Default false.
     */
    fun setVerbose(enabled: Boolean) {
        verbose = enabled
        Log.d(TAG, "setVerbose($enabled)")
    }

    /**
     * Stop all screen accumulators, emit final reports, and tear down the
     * Choreographer callback. After shutdown, [startScreen] can still be
     * called (it will re-install the callback).
     */
    fun shutdown() {
        Log.d(TAG, "shutdown() — stopping ${screens.size} screen(s)")
        for (name in screens.keys.toList()) {
            stopScreen(name)
        }
        stopRunning()
    }

    // ------------------------------------------------------------------------
    // Public API — data types
    // ------------------------------------------------------------------------

    /** Immutable snapshot of one screen's accumulated frame stats. */
    data class ScreenStats(
        val screenName: String,
        val totalFrames: Long,
        val droppedFrames: Long,
        val jankPercent: Double,
        val frameBudgetMs: Double,
        val minMs: Double,
        val maxMs: Double,
        val meanMs: Double,
        val p50Ms: Double,
        val p95Ms: Double,
        val p99Ms: Double,
    ) {
        /**
         * True iff the spec's 95th-percentile gate is met:
         * `p95Ms < frameBudgetMs`.
         */
        val meetsP95Gate: Boolean get() = p95Ms < frameBudgetMs

        /**
         * True iff the spec's zero-drops gate is met:
         * `droppedFrames == 0`.
         */
        val meetsZeroDropsGate: Boolean get() = droppedFrames == 0L
    }

    // ------------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------------

    private val screens = ConcurrentHashMap<String, ScreenAccumulator>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val callbackInstalled = AtomicBoolean(false)

    // The Choreographer callback — installed on the main thread, fires
    // once per vsync. We hold a strong reference so the Choreographer
    // doesn't GC it (Choreographer holds a weak ref).
    @Volatile private var frameCallback: Choreographer.FrameCallback? = null

    // Last frameTimeNanos observed — used to compute the inter-frame
    // interval. Volatile because it's written on the main thread (inside
    // doFrame) and read by snapshot() on any thread (where we read the
    // accumulator's own buffer, not this field directly — this field is
    // only used inside doFrame).
    @Volatile private var lastFrameNanos: Long = 0L

    @Volatile private var verbose: Boolean = false
    @Volatile private var logPath: String? = File(cacheDir, "lexicon_perf.log").absolutePath

    private fun ensureRunning() {
        if (callbackInstalled.getAndSet(true)) return
        // Post the callback on the main Looper. Choreographer is
        // thread-local to the main Looper, so we MUST install it from
        // the main thread.
        mainHandler.post {
            val cb = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    onFrame(frameTimeNanos)
                    // Re-post for the next vsync. If stopRunning() was
                    // called between this doFrame and the re-post, the
                    // callback won't be re-installed.
                    if (callbackInstalled.get()) {
                        Choreographer.getInstance().postFrameCallback(this)
                    }
                }
            }
            frameCallback = cb
            Choreographer.getInstance().postFrameCallback(cb)
            Log.d(TAG, "Choreographer.FrameCallback installed on main Looper")
        }
    }

    private fun stopRunning() {
        if (!callbackInstalled.getAndSet(false)) return
        mainHandler.post {
            frameCallback?.let {
                Choreographer.getInstance().removeFrameCallback(it)
            }
            frameCallback = null
            lastFrameNanos = 0L
            Log.d(TAG, "Choreographer.FrameCallback removed (no active screens)")
        }
    }

    /**
     * Called on the main thread inside doFrame(). Computes the inter-frame
     * interval and feeds it to every active screen accumulator.
     */
    private fun onFrame(frameTimeNanos: Long) {
        val prev = lastFrameNanos
        lastFrameNanos = frameTimeNanos
        if (prev == 0L) {
            // First frame after install — no interval to compute.
            return
        }
        // Inter-frame interval in nanoseconds. Always positive (Choreographer
        // guarantees monotonic frameTimeNanos).
        val intervalNs = frameTimeNanos - prev
        val renderMs = intervalNs / NANOS_PER_MS

        // Feed every active screen. ConcurrentHashMap iteration is weakly
        // consistent — screens added/removed during this iteration will
        // or won't be seen, but no ConcurrentModificationException.
        for ((_, acc) in screens) {
            acc.recordFrame(renderMs)
            if (renderMs > acc.frameBudgetMs && verbose) {
                emitFrameJank(acc.screenName, renderMs, acc.frameBudgetMs)
            }
        }
    }

    // ------------------------------------------------------------------------
    // JSON event emission
    // ------------------------------------------------------------------------

    private fun emitFrameStats(stats: ScreenStats) {
        val data = JSONObject().apply {
            put("screen", stats.screenName)
            put("total_frames", stats.totalFrames)
            put("dropped_frames", stats.droppedFrames)
            put("jank_percent", round4(stats.jankPercent))
            put("budget_ms", round3(stats.frameBudgetMs))
            put("min_ms", round3(stats.minMs))
            put("max_ms", round3(stats.maxMs))
            put("mean_ms", round3(stats.meanMs))
            put("p50_ms", round3(stats.p50Ms))
            put("p95_ms", round3(stats.p95Ms))
            put("p99_ms", round3(stats.p99Ms))
        }
        emitEvent("frame_stats", data)
    }

    private fun emitFrameJank(screenName: String, renderMs: Double, budgetMs: Double) {
        val data = JSONObject().apply {
            put("screen", screenName)
            put("render_ms", round3(renderMs))
            put("budget_ms", round3(budgetMs))
        }
        emitEvent("frame_jank", data)
    }

    private fun emitEvent(type: String, data: JSONObject) {
        val event = JSONObject().apply {
            put("ts", System.currentTimeMillis())
            put("type", type)
            put("data", data)
        }
        val line = event.toString()
        // Always log to logcat (cheap, discoverable via adb logcat).
        Log.d(TAG, line)
        // Optionally append to the perf log file (for ETL collection).
        logPath?.let { path ->
            try {
                File(path).appendText(line + "\n")
            } catch (t: Throwable) {
                // Don't let file IO failures crash the main thread.
                Log.w(TAG, "Failed to append perf event to '$path': ${t.message}")
            }
        }
    }

    // ------------------------------------------------------------------------
    // Per-screen accumulator
    // ------------------------------------------------------------------------

    /**
     * Per-screen accumulator. Updated only on the main thread (inside
     * doFrame), but snapshotted from any thread. Synchronization:
     *   * The ring buffer is guarded by `bufferLock` (a single short-lived
     *     mutex per snapshot call — never contended because doFrame writes
     *     and snapshot reads are rare relative to frame rate).
     *   * The atomic counters (totalFrames, droppedFrames) are updated
     *     via AtomicLong to allow lock-free reads from snapshot().
     */
    private class ScreenAccumulator(
        val screenName: String,
        @Volatile var frameBudgetMs: Double,
    ) {
        private val totalFrames = AtomicLong(0L)
        private val droppedFrames = AtomicLong(0L)

        // Ring buffer for percentile computation. Power-of-two for cheap
        // modulo. Capacity = 4096 frames ≈ 68 seconds @ 60 fps — plenty
        // for a scroll burst.
        private val buffer = DoubleArray(BUFFER_CAPACITY)
        private var bufferHead = 0
        private var bufferFull = false
        private val bufferLock = Any()

        fun reset(frameBudgetMs: Double) {
            this.frameBudgetMs = frameBudgetMs
            totalFrames.set(0L)
            droppedFrames.set(0L)
            synchronized(bufferLock) {
                bufferHead = 0
                bufferFull = false
                // No need to zero the buffer — we track occupancy via
                // bufferFull + bufferHead.
            }
        }

        fun recordFrame(renderMs: Double) {
            totalFrames.incrementAndGet()
            if (renderMs > frameBudgetMs) {
                droppedFrames.incrementAndGet()
            }
            synchronized(bufferLock) {
                buffer[bufferHead] = renderMs
                bufferHead = (bufferHead + 1) and (BUFFER_CAPACITY - 1)
                if (bufferHead == 0) bufferFull = true
            }
        }

        fun snapshot(): ScreenStats {
            val total = totalFrames.get()
            val dropped = droppedFrames.get()
            if (total == 0L) {
                return ScreenStats(
                    screenName = screenName,
                    totalFrames = 0L,
                    droppedFrames = 0L,
                    jankPercent = 0.0,
                    frameBudgetMs = frameBudgetMs,
                    minMs = 0.0, maxMs = 0.0, meanMs = 0.0,
                    p50Ms = 0.0, p95Ms = 0.0, p99Ms = 0.0,
                )
            }
            // Copy the ring buffer out under the lock.
            val samples: DoubleArray
            synchronized(bufferLock) {
                val size = if (bufferFull) BUFFER_CAPACITY else bufferHead
                samples = DoubleArray(size)
                if (bufferFull) {
                    // Wrap-around: reconstruct in-order. After sort the
                    // order doesn't matter, but we keep chronological
                    // for potential timeline use.
                    val tailLen = BUFFER_CAPACITY - bufferHead
                    System.arraycopy(buffer, bufferHead, samples, 0, tailLen)
                    System.arraycopy(buffer, 0, samples, tailLen, bufferHead)
                } else {
                    System.arraycopy(buffer, 0, samples, 0, bufferHead)
                }
            }
            samples.sort()
            val min = samples.first()
            val max = samples.last()
            val mean = samples.average()
            val p50 = percentile(samples, 50.0)
            val p95 = percentile(samples, 95.0)
            val p99 = percentile(samples, 99.0)
            val jankPct = 100.0 * dropped.toDouble() / total.toDouble()
            return ScreenStats(
                screenName = screenName,
                totalFrames = total,
                droppedFrames = dropped,
                jankPercent = jankPct,
                frameBudgetMs = frameBudgetMs,
                minMs = min,
                maxMs = max,
                meanMs = mean,
                p50Ms = p50,
                p95Ms = p95,
                p99Ms = p99,
            )
        }

        private fun percentile(sorted: DoubleArray, p: Double): Double {
            if (sorted.isEmpty()) return 0.0
            val n = sorted.size
            // Nearest-rank: rank = ceil(p/100 * n) - 1, clamped to [0, n-1].
            var rank = ceil(p / 100.0 * n).toInt()
            if (rank == 0) rank = 1
            rank -= 1
            if (rank >= n) rank = n - 1
            if (rank < 0) rank = 0
            return sorted[rank]
        }

        companion object {
            private const val BUFFER_CAPACITY = 4096  // power of two
        }
    }

    // ------------------------------------------------------------------------
    // Numeric formatting helpers (avoid String.format locale issues).
    // ------------------------------------------------------------------------

    private fun round3(v: Double): Double {
        return Math.round(v * 1000.0) / 1000.0
    }

    private fun round4(v: Double): Double {
        return Math.round(v * 10000.0) / 10000.0
    }

    // ------------------------------------------------------------------------
    // Singleton
    // ------------------------------------------------------------------------

    companion object {
        private const val TAG = "LexiconFrameMonitor"

        /** Default frame budget: 16.67 ms (60 fps). */
        private const val DEFAULT_FRAME_BUDGET_MS = 16.67

        private const val NANOS_PER_MS = 1_000_000.0

        @Volatile private var instance: FrameMonitor? = null

        /**
         * Process-wide singleton. The monitor owns a [Choreographer.FrameCallback]
         * that's installed on the main [Looper] the first time a screen is
         * started. Subsequent [startScreen] calls reuse the same callback.
         *
         * @param context any context — the [android.content.Context.getCacheDir]
         *   is used to default the perf log path to
         *   `<cacheDir>/lexicon_perf.log`.
         */
        fun get(context: android.content.Context): FrameMonitor =
            instance ?: synchronized(this) {
                instance ?: FrameMonitor(context.applicationContext.cacheDir)
                    .also { instance = it }
            }

        /**
         * Test-only: reset the singleton. Call from `@After` in instrumented
         * tests to ensure a clean state between test cases. Not for
         * production use.
         */
        @JvmStatic
        fun resetForTesting() {
            instance?.shutdown()
            instance = null
        }
    }
}
