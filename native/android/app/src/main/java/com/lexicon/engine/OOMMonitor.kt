package com.lexicon.engine

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * OOMMonitor — heap-pressure watchdog for the Lexicon Android app.
 *
 * Spec risk matrix hard gate:
 *   "App remains under 200MB peak heap on Moto G during intensive scrolling."
 *
 * This monitor is the safety net under that gate. It does three things:
 *
 *  1. Hooks [ComponentCallbacks2.onTrimMemory] at the Application level and
 *     translates each OS trim level into a graduated recovery action:
 *
 *       TRIM_MEMORY_COMPLETE  -> unload DAWG + flush caches + log to crash reporter
 *       TRIM_MEMORY_MODERATE  -> unload DAWG
 *       TRIM_MEMORY_BACKGROUND-> release non-critical resources
 *
 *  2. Samples the JVM heap on demand (and optionally on a periodic cadence)
 *     and tracks the peak. The peak is exposed via [peakHeapBytes] so the
 *     instrumentation harness can assert `<200MB` after an intensive scroll
 *     session. If the live heap crosses 180MB (90% of the 200MB budget) we
 *     proactively unload the DAWG and emit a warning — this is the "soft
 *     OOM" threshold intended to prevent a hard [OutOfMemoryError] from
 *     ever being thrown.
 *
 *  3. Detects low-RAM devices via [ActivityManager.isLowRamDevice] and, on
 *     those devices, advertises [watercoloursDisabled] = true. The UI layer
 *     reads this flag and suppresses decorative watercolour illustrations
 *     entirely (per the spec's "disable decorative watercolours on low-RAM
 *     devices" requirement). This is a static, device-class decision — it
 *     is NOT toggled by transient pressure, because flipping it
 *     mid-session would cause jarring recompose-and-redraw cycles.
 *
 * # Memory budget math
 *
 * The 200MB target is JVM heap only. Native heap (the engine's mmap'd hash
 * table + DAWG) is reported separately by [LexiconEngine.getMemoryUsage]
 * and is NOT counted against the JVM budget — mmap'd file-backed pages
 * are evictable by the kernel under pressure and do not contribute to the
 * LMK (Low Memory Killer) score the way anonymous JVM heap does.
 *
 * The 180MB proactive threshold = 90% of 200MB. The remaining 10% (20MB)
 * is reserved headroom for the allocation burst that typically precedes a
 * real OOM (a large bitmap decode, a Compose recomposition storm). Acting
 * at 90% gives the GC + this monitor enough time to shed load before the
 * remaining 10% is exhausted.
 *
 * # Threading
 *
 * [ComponentCallbacks2.onTrimMemory] is always delivered on the main
 * thread. The cache flushers registered via [registerCacheFlusher] are
 * invoked inline on the main thread — they MUST be non-blocking. Anything
 * heavier (e.g. Bitmap.recycle on a large pool) should be dispatched to
 * a background coroutine inside the flusher.
 *
 * # Wiring (host application — NOT in scope of this file)
 *
 * ```kotlin
 * class LexiconApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         val optimizer = LexiconColdStartOptimizer.get(this)
 *         optimizer.warmEngine(this)  // returns Deferred<LexiconEngine>
 *
 *         val oom = OOMMonitor.get(this)
 *         oom.register()
 *         // Bind the engine as soon as it's ready. The provider returns
 *         // null until then — the monitor tolerates that.
 *         oom.setEngineProvider { warmedEngineRef.get() }
 *         oom.setCrashReporter { tag, msg, t -> /* Firebase / Bugsnag */ }
 *         oom.startSampling()  // optional: 5s periodic soft-threshold check
 *     }
 * }
 * ```
 *
 * The monitor is a singleton because there is exactly one Application per
 * process and exactly one [ComponentCallbacks2] registration needed.
 */
class OOMMonitor private constructor(
    appContext: Context,
) : ComponentCallbacks2 {

    private val appContext: Context = appContext.applicationContext

    /**
     * Snapshot of [ActivityManager.isLowRamDevice], captured once at
     * construction. Static for the lifetime of the process — the OS does
     * not change this value at runtime.
     */
    private val lowRamDevice: Boolean = run {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.isLowRamDevice ?: false
    }

    /**
     * Engine provider — invoked when the monitor needs to unload the DAWG.
     * Lazy because the engine is constructed asynchronously by
     * [LexiconColdStartOptimizer.warmEngine]; we cannot dereference it at
     * registration time. Returns null if the engine is not yet ready — the
     * monitor's recovery paths tolerate this gracefully.
     */
    private val engineProvider = AtomicReference<(() -> LexiconEngine?)?>()

    /**
     * Crash reporter sink. Optional — defaults to logcat-only. Invoked
     * with `(tag, message, throwable)`; the throwable is null for
     * proactive soft-threshold reports (which are not exceptions).
     */
    private val crashReporter =
        AtomicReference<((tag: String, message: String, throwable: Throwable?) -> Unit)?>()

    /**
     * Peak observed JVM heap (used = totalMemory - freeMemory) in bytes,
     * sampled on every trim event and every [checkSoftThreshold] call.
     * Monotonically non-decreasing for the lifetime of the process —
     * callers that want a "session peak" should reset it via [resetPeak]
     * at the start of the measured session.
     */
    private val peakHeapBytes = AtomicLong(0L)

    /** Whether the monitor has been registered with the Application. */
    @Volatile private var registered: Boolean = false

    /** Main-thread [Handler] used for the periodic soft-threshold sampler. */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Whether periodic sampling is currently running. */
    @Volatile private var sampling: Boolean = false

    private val sampleRunnable = object : Runnable {
        override fun run() {
            checkSoftThreshold(reason = "periodic sample")
            if (sampling) {
                mainHandler.postDelayed(this, SAMPLE_INTERVAL_MS)
            }
        }
    }

    // --------------------------------------------------------------
    // Public configuration
    // --------------------------------------------------------------

    /**
     * Bind an engine provider. The provider will be invoked from main
     * thread (during trim events) or from any thread that calls
     * [checkSoftThreshold] — it MUST be cheap (a field read, not a
     * blocking call).
     */
    fun setEngineProvider(provider: () -> LexiconEngine?) {
        engineProvider.set(provider)
        Log.d(TAG, "engine provider bound")
    }

    /**
     * Bind a crash reporter. Optional — when unset, all reports fall back
     * to [Log.w] / [Log.e] only.
     */
    fun setCrashReporter(reporter: (tag: String, message: String, throwable: Throwable?) -> Unit) {
        crashReporter.set(reporter)
        Log.d(TAG, "crash reporter bound")
    }

    // --------------------------------------------------------------
    // Public state — read by UI / tests
    // --------------------------------------------------------------

    /**
     * Peak JVM heap observed since registration (or last [resetPeak]),
     * in bytes. Use this in instrumentation tests to assert `<200MB`
     * after an intensive scroll session.
     */
    fun peakHeapBytes(): Long = peakHeapBytes.get()

    /**
     * Current JVM heap usage in bytes = [Runtime.totalMemory] -
     * [Runtime.freeMemory]. This is the same metric the Runtime exposes;
     * centralised here so callers don't sprinkle `Runtime.getRuntime()`
     * calls across the app.
     */
    fun currentHeapBytes(): Long {
        val rt = Runtime.getRuntime()
        return rt.totalMemory() - rt.freeMemory()
    }

    /**
     * True iff [ActivityManager.isLowRamDevice] returned true at
     * construction. Static for the process lifetime.
     */
    fun isLowRamDevice(): Boolean = lowRamDevice

    /**
     * Effective "should the UI render decorative watercolours?" flag.
     *
     * Spec: on low-RAM devices, disable decorative watercolours entirely.
     * This is a static device-class decision (NOT toggled by transient
     * pressure — see class KDoc for rationale).
     *
     * UI code reads this once at composition time and conditionally mounts
     * the watercolour illustrations in [com.lexicon.ui.components.Hero],
     * [com.lexicon.ui.components.WordOfTheDayCard], and
     * [com.lexicon.ui.screens.CollectionsScreen]'s empty state.
     */
    val watercoloursDisabled: Boolean
        get() = lowRamDevice

    /** Reset [peakHeapBytes] to zero. Call from a test harness between sessions. */
    fun resetPeak() {
        peakHeapBytes.set(0L)
    }

    // --------------------------------------------------------------
    // Lifecycle
    // --------------------------------------------------------------

    /**
     * Register this monitor as a [ComponentCallbacks2] on the Application
     * context. Idempotent — second call is a no-op.
     *
     * Captures [lowRamDevice] (already done in the constructor) and emits
     * a single INFO log line so the device class can be correlated with
     * the rest of the cold-start trace.
     */
    fun register() {
        if (registered) return
        appContext.registerComponentCallbacks(this)
        registered = true
        Log.i(
            TAG,
            "registered — lowRamDevice=$lowRamDevice, " +
                "watercoloursDisabled=$watercoloursDisabled, " +
                "budget=${HARD_BUDGET_BYTES / MIB}MiB, " +
                "softThreshold=${SOFT_THRESHOLD_BYTES / MIB}MiB"
        )
    }

    /** Unregister. Call from Application.onTerminate (or tests). */
    fun unregister() {
        if (!registered) return
        appContext.unregisterComponentCallbacks(this)
        registered = false
        stopSampling()
        Log.d(TAG, "unregistered")
    }

    /**
     * Start periodic heap sampling on the main thread. Each tick invokes
     * [checkSoftThreshold], which updates [peakHeapBytes] and triggers a
     * proactive DAWG unload if the heap crosses 90% of the budget.
     *
     * [Runtime.totalMemory] and [Runtime.freeMemory] are essentially free
     * (two long field reads), so a 5-second cadence costs nothing
     * measurable. Idempotent.
     */
    fun startSampling(intervalMs: Long = SAMPLE_INTERVAL_MS) {
        if (sampling) return
        sampling = true
        mainHandler.postDelayed(sampleRunnable, intervalMs)
        Log.d(TAG, "sampling started — interval=${intervalMs}ms")
    }

    /** Stop periodic sampling. Idempotent. */
    fun stopSampling() {
        if (!sampling) return
        sampling = false
        mainHandler.removeCallbacks(sampleRunnable)
        Log.d(TAG, "sampling stopped")
    }

    // --------------------------------------------------------------
    // ComponentCallbacks2
    // --------------------------------------------------------------

    override fun onConfigurationChanged(newConfig: Configuration) {
        // No-op — config changes are handled by the Activity / ViewModel.
    }

    /**
     * Legacy [ComponentCallbacks.onLowMemory] callback. The framework only
     * fires this on very old API levels or as a last resort; modern
     * devices deliver graduated events via [onTrimMemory]. Treat it as
     * CRITICAL (equivalent to [ComponentCallbacks2.TRIM_MEMORY_COMPLETE]).
     */
    override fun onLowMemory() {
        handleTrim(ComponentCallbacks2.TRIM_MEMORY_COMPLETE, source = "onLowMemory")
    }

    /**
     * Graduated memory pressure events from the framework. Translated into
     * a recovery action via [handleTrim]. Always delivered on the main
     * thread.
     */
    override fun onTrimMemory(level: Int) {
        handleTrim(level, source = "onTrimMemory")
    }

    // --------------------------------------------------------------
    // Internal — pressure dispatch
    // --------------------------------------------------------------

    /**
     * Central dispatch for all memory pressure events. The [source] tag is
     * included in log lines so we can distinguish legacy [onLowMemory]
     * callbacks from modern [onTrimMemory] ones when reading logcat.
     */
    private fun handleTrim(level: Int, source: String) {
        val heapBefore = currentHeapBytes()
        val peakBefore = peakHeapBytes.get()

        // Update peak first — we want the pre-action reading so we can
        // report the high-water mark that triggered the recovery.
        updatePeak(heapBefore)

        val levelName = trimLevelName(level)
        Log.w(
            TAG,
            "memory pressure: level=$levelName ($level) source=$source " +
                "heap=${heapBefore / MIB}MiB peak=${peakBefore / MIB}MiB"
        )

        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // CRITICAL — the system is about to start killing background
                // processes. Shed everything discretionary.
                unloadDawg(reason = "TRIM_MEMORY_COMPLETE")
                flushCaches(reason = "TRIM_MEMORY_COMPLETE")
                reportCrash(
                    tag = "oom_trim_complete",
                    message = buildOomReport(level = level, source = source, heapBytes = heapBefore, peakBytes = peakBefore),
                    throwable = null,
                )
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // MODERATE — DAWG is our single biggest discretionary
                // allocation. Dropping it back to the page cache is cheap
                // (it's mmap'd) and frees the resident anonymous overhead
                // the DAWG search path accumulates.
                unloadDawg(reason = "TRIM_MEMORY_MODERATE")
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // BACKGROUND — release anything we can rebuild cheaply.
                // The DAWG stays loaded (it's expensive to reload and the
                // user may return to a search within seconds).
                releaseNonCritical(reason = "TRIM_MEMORY_BACKGROUND")
            }
            else -> {
                // UI_HIDDEN / RUNNING_CRITICAL / RUNNING_LOW / RUNNING_MODERATE
                // — informational. We log but do not act; the proactive
                // heap sampler is responsible for the 180MB soft threshold.
                Log.d(TAG, "informational trim level $levelName ($level) — no action")
            }
        }

        // Re-sample after the recovery action — the unload may have freed
        // enough to pull us back under the soft threshold. If not, escalate.
        val heapAfter = currentHeapBytes()
        updatePeak(heapAfter)
        if (heapAfter > SOFT_THRESHOLD_BYTES) {
            Log.w(
                TAG,
                "heap still above soft threshold after trim: " +
                    "${heapAfter / MIB}MiB > ${SOFT_THRESHOLD_BYTES / MIB}MiB — escalating"
            )
            unloadDawg(reason = "post-trim soft threshold")
            reportCrash(
                tag = "oom_soft_threshold_post_trim",
                message = buildOomReport(level = level, source = source, heapBytes = heapAfter, peakBytes = peakHeapBytes.get()),
                throwable = null,
            )
        }
    }

    /**
     * Proactive heap check — called externally (by the periodic sampler,
     * by the scroll-end listener, by tests) OR internally after a trim
     * event. Implements the spec rule: "If heap > 180MB (90% of 200MB
     * budget): proactively unload DAWG + warn."
     *
     * Returns true iff the soft threshold was breached AND an unload was
     * attempted. Callers may use this to gate further work (e.g. postpone
     * a bitmap decode until after the next GC).
     */
    fun checkSoftThreshold(reason: String): Boolean {
        val heap = currentHeapBytes()
        updatePeak(heap)
        if (heap <= SOFT_THRESHOLD_BYTES) return false

        Log.w(
            TAG,
            "SOFT THRESHOLD BREACH: heap=${heap / MIB}MiB > " +
                "${SOFT_THRESHOLD_BYTES / MIB}MiB (90% of ${HARD_BUDGET_BYTES / MIB}MiB budget) — reason=$reason"
        )
        unloadDawg(reason = "soft threshold ($reason)")
        reportCrash(
            tag = "oom_soft_threshold",
            message = buildOomReport(level = -1, source = "proactive:$reason", heapBytes = heap, peakBytes = peakHeapBytes.get()),
            throwable = null,
        )
        return true
    }

    // --------------------------------------------------------------
    // Internal — recovery actions
    // --------------------------------------------------------------

    /**
     * Idempotent DAWG unload. Logs and no-ops if the engine is not yet
     * ready or the DAWG is already disabled. Never throws — a failure
     * here would mask the original pressure event.
     */
    private fun unloadDawg(reason: String) {
        val engine = try {
            engineProvider.get()?.invoke()
        } catch (t: Throwable) {
            Log.e(TAG, "engine provider threw during unloadDawg ($reason)", t)
            null
        } ?: run {
            Log.d(TAG, "unloadDawg skipped — engine not yet available ($reason)")
            return
        }

        try {
            if (!engine.isDawgEnabled()) {
                Log.d(TAG, "unloadDawg no-op — DAWG already disabled ($reason)")
                return
            }
            val before = engine.getMemoryUsage()
            engine.enableDawg(false)
            val after = engine.getMemoryUsage()
            val freed = before.dawgBytes - after.dawgBytes
            Log.i(
                TAG,
                "DAWG unloaded — reason=$reason, freed=${freed / MIB}MiB " +
                    "(dawg ${before.dawgBytes / MIB}MiB -> ${after.dawgBytes / MIB}MiB)"
            )
        } catch (t: Throwable) {
            // The native engine should never throw here, but if it does
            // we must not let the exception escape onTrimMemory.
            Log.e(TAG, "enableDawg(false) threw during $reason", t)
        }
    }

    /**
     * Flush all registered caches. The flushers are invoked inline on the
     * calling thread (main thread for trim events) — they MUST be
     * non-blocking. Heavier work should be dispatched to a background
     * coroutine inside the flusher.
     */
    private fun flushCaches(reason: String) {
        val flushers = cacheFlushers.toList()
        if (flushers.isEmpty()) {
            Log.d(TAG, "flushCaches — no flushers registered ($reason)")
            return
        }
        flushers.forEach { f ->
            try {
                f.invoke()
            } catch (t: Throwable) {
                Log.e(TAG, "cache flusher threw during $reason", t)
            }
        }
        Log.i(TAG, "caches flushed — reason=$reason, ${flushers.size} flushers invoked")
    }

    /**
     * Release non-critical resources for BACKGROUND-level pressure.
     *
     * Distinct from [flushCaches] in intent — this targets resources that
     * are cheap to release but slow to rebuild (e.g. cached decoded
     * bitmaps), not the hot caches. The current implementation delegates
     * to the same flusher list; as the app grows we can split into a
     * separate registration list.
     */
    private fun releaseNonCritical(reason: String) {
        flushCaches(reason)
    }

    // --------------------------------------------------------------
    // Cache flusher registration (future-facing API)
    // --------------------------------------------------------------

    private val cacheFlushers = CopyOnWriteArrayList<() -> Unit>()

    /**
     * Register a cache-flushing callback. The callback will be invoked on
     * CRITICAL trim events, BACKGROUND trim events, and on
     * soft-threshold breach. The callback MUST be idempotent and MUST NOT
     * block (it runs on the main thread for trim events).
     */
    fun registerCacheFlusher(flusher: () -> Unit) {
        cacheFlushers.addIfAbsent(flusher)
        Log.d(TAG, "cache flusher registered (${cacheFlushers.size} total)")
    }

    /** Remove a previously registered flusher. */
    fun unregisterCacheFlusher(flusher: () -> Unit) {
        cacheFlushers.remove(flusher)
    }

    // --------------------------------------------------------------
    // Peak tracking + reporting
    // --------------------------------------------------------------

    /**
     * Update [peakHeapBytes] if [heap] exceeds the current peak. Uses
     * compare-and-set in a loop so concurrent trim events (which can
     * fire on binder threads in some edge cases) don't clobber each other.
     */
    private fun updatePeak(heap: Long) {
        var prev = peakHeapBytes.get()
        while (heap > prev) {
            if (peakHeapBytes.compareAndSet(prev, heap)) break
            prev = peakHeapBytes.get()
        }
    }

    /**
     * Construct a single-line OOM report suitable for crash reporters.
     * Includes the triggering trim level, source, current heap, peak
     * heap, native engine memory (if available), device class, and a
     * short stack-trace snippet for attribution.
     */
    private fun buildOomReport(level: Int, source: String, heapBytes: Long, peakBytes: Long): String {
        val native = try {
            engineProvider.get()?.invoke()?.getMemoryUsage()
        } catch (t: Throwable) {
            Log.e(TAG, "getMemoryUsage threw during report build", t)
            null
        }

        val nativeStr = native?.let {
            "ht=${it.hashTableBytes / MIB}MiB " +
                "dawg=${it.dawgBytes / MIB}MiB " +
                "total=${it.totalBytes / MIB}MiB"
        } ?: "native=n/a"

        return buildString {
            append("OOM event: ")
            append("level=${trimLevelName(level)}($level) ")
            append("source=$source ")
            append("heap=${heapBytes / MIB}MiB ")
            append("peak=${peakBytes / MIB}MiB ")
            append("budget=${HARD_BUDGET_BYTES / MIB}MiB ")
            append("soft=${SOFT_THRESHOLD_BYTES / MIB}MiB ")
            append("lowRamDevice=$lowRamDevice ")
            append("watercoloursDisabled=$watercoloursDisabled ")
            append(nativeStr)
            append(" stack=${currentStackSnippet()}")
        }
    }

    /**
     * Hand the report (and an optional [Throwable]) to the registered
     * crash reporter. If no reporter is bound, falls back to logcat.
     */
    private fun reportCrash(tag: String, message: String, throwable: Throwable?) {
        val reporter = crashReporter.get()
        if (reporter == null) {
            Log.w(TAG, "[$tag] $message (no crash reporter bound)")
            return
        }
        try {
            reporter.invoke(tag, message, throwable)
        } catch (t: Throwable) {
            // The crash reporter itself crashed — never let this escape
            // onTrimMemory, or we'd mask the original pressure event.
            Log.e(TAG, "crash reporter threw for tag=$tag", t)
        }
    }

    /**
     * Best-effort stack-trace snippet for OOM reports (top 8 frames).
     * Captured via a fresh [Throwable] so we get the *current* call stack
     * at the moment of the pressure event — useful for attributing which
     * allocation path crossed the threshold.
     */
    private fun currentStackSnippet(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        Throwable("oom-stack-snapshot").printStackTrace(pw)
        val trace = sw.toString()
            .lines()
            .filter { it.isNotBlank() }
            .take(8)
            .joinToString(" | ") { it.trim() }
        return trace.ifBlank { "<no stack>" }
    }

    /** Human-readable name for a trim level constant. */
    private fun trimLevelName(level: Int): String = when (level) {
        ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
        ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
        ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
        else -> "UNKNOWN"
    }

    // --------------------------------------------------------------
    // Companion
    // --------------------------------------------------------------

    companion object {
        private const val TAG = "LexiconOOM"

        /**
         * Hard heap budget from the spec risk matrix:
         * "App remains under 200MB peak heap on Moto G during intensive scrolling."
         */
        private const val HARD_BUDGET_BYTES = 200L * 1024 * 1024

        /**
         * Soft threshold = 90% of the hard budget = 180 MiB. Crossing this
         * triggers a proactive DAWG unload + warning, intended to prevent
         * the hard budget from ever being reached.
         *
         * Computed via integer math (`* 9 / 10`) so it stays exact at
         * 188,743,680 bytes — no floating-point drift in tests.
         */
        private val SOFT_THRESHOLD_BYTES = (HARD_BUDGET_BYTES * 9) / 10 // 180 MiB

        /** MiB constant for log formatting. */
        private const val MIB = 1024L * 1024L

        /**
         * Periodic sampling cadence for [startSampling]. 5 seconds is
         * frequent enough to catch a heap ramp before it hits the hard
         * budget, and rare enough that the main-thread cost is negligible
         * (two long field reads per tick).
         */
        private const val SAMPLE_INTERVAL_MS = 5_000L

        @Volatile private var instance: OOMMonitor? = null

        /**
         * Process-wide singleton. The monitor owns its own
         * [ComponentCallbacks2] registration — never register two. First
         * call captures [lowRamDevice] from the supplied [context].
         */
        fun get(context: Context): OOMMonitor =
            instance ?: synchronized(this) {
                instance ?: OOMMonitor(context.applicationContext).also { instance = it }
            }
    }
}
