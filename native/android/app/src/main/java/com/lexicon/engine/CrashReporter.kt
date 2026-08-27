package com.lexicon.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import org.json.JSONObject
import sun.misc.Signal
import sun.misc.SignalHandler
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CrashReporter — dependency-free crash reporting scaffolding for Android.
 *
 * Per task P4-03 the reporter must:
 *   * Capture stack trace + device info (model, OS version, RAM, app version).
 *   * Write crash log to internal storage (`crash_logs/` directory).
 *   * On next app launch: check for crash logs, upload to cloud sync server.
 *   * NOT use Firebase Crashlytics (keep it dependency-free).
 *   * Detect ANRs via a watchdog thread.
 *   * Detect native crashes via signal handlers (SIGSEGV / SIGABRT / SIGBUS).
 *   * Upload endpoint: `POST /api/v1/crash-report`.
 *
 * # Architecture
 *
 * The reporter is a process-wide singleton (see [companion] getter). It owns
 * three independent capture paths, all of which write JSON files to the same
 * `crash_logs/` directory under [Context.getFilesDir]:
 *
 *   1. **JVM crashes** — registered via [Thread.setDefaultUncaughtExceptionHandler].
 *      Catches any `Throwable` that escapes a thread's `run()`. Writes the
 *      full stack trace + device snapshot to disk, then delegates to the
 *      previously-installed handler (default = process termination).
 *
 *   2. **ANR detection** — an [AnrWatchdog] daemon thread ticks every
 *      [ANR_POLL_INTERVAL_MS]; each tick posts a one-line `Runnable` to the
 *      main [Looper] that flips a `tick` flag. If the flag hasn't flipped
 *      after [ANR_TIMEOUT_MS] (== 2 × poll interval, to avoid false positives
 *      on legitimate slow tasks), we record an ANR crash log. The process is
 *      NOT killed — the system's own ANR dialog handles that.
 *
 *   3. **Native crashes** — uses `sun.misc.Signal` (shipped with the Android
 *      runtime since API 1, no third-party dependency) to install Java-side
 *      handlers for `SIGSEGV`, `SIGABRT`, `SIGBUS`. These reliably catch:
 *        * Explicit `raise()` / `abort()` calls from native code (e.g.,
 *          failed asserts in the C++ engine).
 *        * Stack-overflow-induced signals raised by the JVM.
 *      They may NOT reliably catch true native segfaults (e.g., dereferencing
 *      an invalid pointer in C++) because the JVM's own internal signal
 *      handler runs first. For production-grade native crash capture, a
 *      `sigaction()` shim should be added to `lexicon_jni.cpp` (see Follow-up
 *      #1 in the worklog).
 *
 * # Upload model — "write now, upload next launch"
 *
 * Crash capture happens synchronously on the crashing thread, so it must be
 * fast and allocation-light. The crash record is written to disk and the
 * process is then allowed to die. Upload is deferred to the NEXT app launch
 * via [uploadPendingCrashLogs], which:
 *   * Iterates every `.json` file in `crash_logs/` (oldest first).
 *   * POSTs each to `${baseUrl}/api/v1/crash-report`.
 *   * Deletes the file on HTTP 2xx; retains it on any other outcome so the
 *     next launch can retry.
 *
 * Upload runs on a dedicated single-thread [Executors] daemon so it does not
 * compete with engine init or Compose frame work.
 *
 * # Wiring (NOT done here — caller's responsibility)
 *
 * The intended call site is [com.lexicon.LexiconApplication.onCreate]:
 *
 * ```kotlin
 * class LexiconApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         CrashReporter.get(this).install()
 *         CrashReporter.get(this).uploadPendingCrashLogs()
 *     }
 * }
 * ```
 *
 * # Why dependency-free
 *
 * The spec explicitly forbids Firebase Crashlytics. Everything here uses
 * only the Android SDK + JDK stdlib:
 *   * `org.json.JSONObject` for JSON serialisation (built into Android).
 *   * `java.net.HttpURLConnection` for the upload (no OkHttp).
 *   * `sun.misc.Signal` for signal handling (shipped with the Android runtime).
 *
 * No Gradle dependency additions are required for this file to compile.
 */
class CrashReporter private constructor(appContext: Context) {

    private val appContext: Context = appContext.applicationContext
    private val crashDir: File = File(appContext.filesDir, CRASH_DIR).apply { mkdirs() }
    private val uploadExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "lexicon-crash-upload").apply { isDaemon = true }
    }
    private val installed = AtomicBoolean(false)
    private val anrWatchdog = AnrWatchdog()

    @Volatile private var previousHandler: Thread.UncaughtExceptionHandler? = null
    @Volatile private var baseUrl: String = DEFAULT_BASE_URL

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Install the JVM exception handler, native signal handlers, and ANR
     * watchdog. Idempotent — second call is a no-op (logged at WARN).
     *
     * MUST be called as early as possible in the process lifetime — ideally
     * the first statement of [com.lexicon.LexiconApplication.onCreate]. Any
     * crash before this returns is lost.
     */
    fun install() {
        if (!installed.compareAndSet(false, true)) {
            Log.w(TAG, "install() called twice — ignoring")
            return
        }
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(LexiconExceptionHandler())
        installSignalHandlers()
        anrWatchdog.start()
        Log.d(TAG, "CrashReporter installed — crashDir=$crashDir, baseUrl=$baseUrl")
    }

    /**
     * Override the cloud sync server origin. Default `http://10.0.2.2:8080`
     * reaches the host machine from the Android emulator (10.0.2.2 is the
     * emulator's alias for 127.0.0.1 on the host). For physical-device
     * testing, set this to your dev machine's LAN IP.
     *
     * Call BEFORE [uploadPendingCrashLogs]. Trailing slash is trimmed.
     */
    fun setBaseUrl(url: String) {
        this.baseUrl = url.trimEnd('/')
    }

    /**
     * Walk the `crash_logs/` directory and POST each `.json` file to the
     * cloud sync server, oldest first. Successful uploads (HTTP 2xx) delete
     * the file; failures leave it on disk for the next launch.
     *
     * Safe to call from the main thread — the actual work happens on a
     * daemon single-thread executor. Multiple calls queue up; they do not
     * race each other.
     */
    fun uploadPendingCrashLogs() {
        uploadExecutor.execute {
            val files = crashDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
                ?: return@execute
            if (files.isEmpty()) return@execute
            Log.d(TAG, "Found ${files.size} pending crash log(s) — uploading…")
            for (file in files.sortedBy { it.lastModified() }) {
                try {
                    val payload = file.readText()
                    if (uploadCrashLog(payload)) {
                        if (file.delete()) {
                            Log.d(TAG, "Uploaded + deleted ${file.name}")
                        } else {
                            Log.w(TAG, "Uploaded ${file.name} but could not delete — will re-upload next launch")
                        }
                    } else {
                        Log.w(TAG, "Upload failed for ${file.name} — retained for next launch")
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Error processing ${file.name}", t)
                }
            }
        }
    }

    /**
     * Stop the ANR watchdog and shut down the upload executor. After this
     * returns, no further crash logs will be captured or uploaded. Intended
     * for unit tests and [android.app.Application.onTerminate].
     */
    fun shutdown() {
        anrWatchdog.cancel()
        uploadExecutor.shutdown()
        Log.d(TAG, "shutdown() — ANR watchdog cancelled, upload executor shut down")
    }

    // ------------------------------------------------------------------
    // JVM exception handler
    // ------------------------------------------------------------------

    /**
     * The [Thread.UncaughtExceptionHandler] installed process-wide by
     * [install]. Catches every uncaught `Throwable` from every thread,
     * writes a JSON crash record to disk, then delegates to whatever
     * handler was installed before us (typically the system default which
     * terminates the process and shows the "App keeps stopping" dialog).
     */
    private inner class LexiconExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread, e: Throwable) {
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val payload = buildPayload(
                    crashType = "java",
                    signalName = null,
                    stackTrace = sw.toString(),
                    threadName = t.name,
                )
                writeCrashFile(payload, prefix = "java")
                Log.e(TAG, "Recorded JVM crash on thread '${t.name}': ${e.javaClass.simpleName}: ${e.message}")
            } catch (recordingFailure: Throwable) {
                // Recording itself failed — log loudly but DON'T swallow the
                // original crash. We must still delegate to the previous handler.
                Log.e(TAG, "Failed to record JVM crash", recordingFailure)
            }
            // Always delegate so the process terminates normally and the
            // system's "app crashed" UI is shown.
            previousHandler?.uncaughtException(t, e)
        }
    }

    // ------------------------------------------------------------------
    // Native signal handlers
    // ------------------------------------------------------------------

    /**
     * Install Java-side signal handlers for SIGSEGV, SIGABRT, SIGBUS via
     * `sun.misc.Signal`. Each handler writes a JSON tombstone to disk and
     * then kills the process (the JVM's default disposition for these
     * signals is also termination, but we re-kill explicitly because
     * returning from a SignalHandler does NOT auto-terminate the process).
     *
     * Signals are named WITHOUT the `SIG` prefix when passed to the
     * [Signal] constructor (i.e., `"SEGV"`, not `"SIGSEGV"`).
     */
    private fun installSignalHandlers() {
        for (name in SIGNAL_NAMES) {
            try {
                val handler = SignalHandler { sig ->
                    onNativeSignal(sig.getName())
                }
                Signal.handle(Signal(name), handler)
                Log.d(TAG, "Signal handler installed for SIG$name")
            } catch (t: Throwable) {
                // Signal.handle throws IllegalArgumentException for signals
                // the JVM refuses to chain (e.g. SIGKILL, SIGSTOP). Our
                // three targets (SEGV/ABRT/BUS) are all chainable on
                // Android, but we still guard against ART/JVM differences
                // across vendors.
                Log.w(TAG, "Could not install handler for SIG$name: ${t.message}")
            }
        }
    }

    /**
     * Invoked synchronously on the JVM's signal-handling thread when one of
     * our three target signals fires. The JVM may be in a partially
     * corrupted state — keep work minimal, avoid allocation, and never
     * throw (a throw out of a signal handler is undefined behaviour).
     */
    private fun onNativeSignal(signalName: String) {
        try {
            val payload = buildPayload(
                crashType = "native",
                signalName = "SIG$signalName",
                stackTrace = "Native signal SIG$signalName received — no Java stack available.",
                threadName = Thread.currentThread().name,
            )
            writeCrashFile(payload, prefix = "native_${signalName}")
        } catch (t: Throwable) {
            // Best-effort — do not recurse, do not log (logging may also crash).
        } finally {
            // Re-raise the default disposition so the process dies and the
            // system's native-crash tombstone is written. We do NOT restore
            // the previous handler via Signal.handle (the JVM is in an
            // indeterminate state); a hard kill is safer.
            Process.killProcess(Process.myPid())
        }
    }

    // ------------------------------------------------------------------
    // ANR watchdog
    // ------------------------------------------------------------------

    /**
     * Daemon thread that detects Application Not Responding (ANR) by polling
     * the main [Looper].
     *
     * Algorithm (one tick):
     *   1. Reset the [tick] flag to `false`.
     *   2. Post a `Runnable` to the main [Handler] that sets [tick] = `true`.
     *   3. Sleep [ANR_POLL_INTERVAL_MS].
     *   4. If [tick] is STILL `false`, the main thread has not processed our
     *      `Runnable` within the poll window — the main Looper is blocked.
     *   5. Give it one more poll window of grace (avoids false positives on
     *      legitimate but slow frame work). If still unresponsive, record
     *      an ANR crash log and enter a [ANR_COOLDOWN_MS] cooldown so we
     *      don't flood the disk with duplicates.
     *
     * The watchdog does NOT kill the process — Android's system ANR dialog
     * (triggered when the system's own 5-second input-dispatch timeout fires)
     * handles that. Our job is just to record a stack trace from the main
     * thread's current (blocked) state.
     */
    private inner class AnrWatchdog : Thread("lexicon-anr-watchdog") {

        private val running = AtomicBoolean(true)
        private val mainHandler = Handler(Looper.getMainLooper())

        /** Flipped by the main-thread Runnable; reset by the watchdog. */
        @Volatile private var tick = false

        init {
            isDaemon = true
            priority = MIN_PRIORITY  // Don't compete with the engine or UI.
        }

        override fun run() {
            Log.d(TAG, "ANR watchdog started — poll=${ANR_POLL_INTERVAL_MS}ms, timeout=${ANR_TIMEOUT_MS}ms")
            while (running.get()) {
                tick = false
                // Post a zero-allocation lambda — if the main Looper is alive,
                // this runs within a few ms and flips `tick` to true.
                mainHandler.post { tick = true }
                try {
                    sleep(ANR_POLL_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    if (!running.get()) return
                }
                if (!tick) {
                    // First miss — give the Looper one more poll window of
                    // grace before escalating. Genuine ANRs persist; a one-off
                    // GC pause or a slow frame doesn't.
                    try {
                        sleep(ANR_POLL_INTERVAL_MS)
                    } catch (e: InterruptedException) {
                        if (!running.get()) return
                    }
                    if (!tick) {
                        handleAnr()
                        // Cooldown — avoid recording the same ANR every tick.
                        try {
                            sleep(ANR_COOLDOWN_MS)
                        } catch (e: InterruptedException) {
                            if (!running.get()) return
                        }
                    }
                }
            }
        }

        /**
         * Capture the main thread's current stack trace and write an ANR
         * crash record. The main thread is still alive (just blocked), so
         * we can safely inspect its stack via [Thread.getAllStackTraces].
         */
        private fun handleAnr() {
            try {
                val mainStack = dumpMainThreadStack()
                val payload = buildPayload(
                    crashType = "anr",
                    signalName = null,
                    stackTrace = mainStack,
                    threadName = "main",
                )
                writeCrashFile(payload, prefix = "anr")
                Log.w(TAG, "ANR detected — main thread unresponsive for >= ${ANR_TIMEOUT_MS}ms")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to record ANR", t)
            }
        }

        /** Signal the watchdog loop to exit on its next iteration. */
        fun cancel() {
            running.set(false)
            interrupt()
        }
    }

    // ------------------------------------------------------------------
    // Payload construction & file I/O
    // ------------------------------------------------------------------

    /**
     * Build the JSON crash payload that the server endpoint expects.
     *
     * Matches the server-side `CrashReport` data class in
     * `CrashReportEndpoint.kt` (snake_case field names). The required seven
     * fields per the spec are: `device_model`, `os_version`, `app_version`,
     * `crash_type`, `stack_trace`, `memory_stats`, `timestamp`. We also
     * emit two optional extras (`signal`, `thread`) — the server stores
     * these in its `details` JSONB column and they are not part of the
     * spec contract.
     */
    private fun buildPayload(
        crashType: String,
        signalName: String?,
        stackTrace: String,
        threadName: String,
    ): String {
        val (totalMb, availMb, lowMem) = memoryStats()
        val memStats = JSONObject().apply {
            put("total_mb", totalMb)
            put("available_mb", availMb)
            put("low_memory", lowMem)
        }
        val payload = JSONObject().apply {
            put("device_model", Build.MODEL ?: "unknown")
            put("os_version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            put("app_version", appVersion())
            put("crash_type", crashType)
            if (signalName != null) put("signal", signalName)
            put("thread", threadName)
            put("stack_trace", stackTrace)
            put("memory_stats", memStats)
            put("timestamp", System.currentTimeMillis())
        }
        return payload.toString()
    }

    /**
     * Write [payload] to a uniquely-named JSON file in [crashDir]. Filename
     * format: `<prefix>_<UTC-timestamp>_pid<pid>.json` — sortable by name
     * equals sortable by time, which is what [uploadPendingCrashLogs] needs.
     */
    private fun writeCrashFile(payload: String, prefix: String) {
        val ts = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        val pid = Process.myPid()
        val file = File(crashDir, "${prefix}_${ts}_pid${pid}.json")
        file.writeText(payload)
    }

    /**
     * Read [ActivityManager.MemoryInfo] and convert to MB. Returns (-1, -1,
     * false) if the system service is unavailable — the JSON still gets
     * written, just with sentinel values.
     */
    private fun memoryStats(): Triple<Long, Long, Boolean> {
        return try {
            val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            Triple(
                mi.totalMem / BYTES_PER_MB,
                mi.availMem / BYTES_PER_MB,
                mi.lowMemory,
            )
        } catch (t: Throwable) {
            Triple(-1L, -1L, false)
        }
    }

    /**
     * Resolve the app's version name + version code via PackageManager.
     * Returns `"unknown (0)"` if the lookup fails (shouldn't happen on a
     * production install, but defensive — never throw from a crash reporter).
     */
    private fun appVersion(): String {
        return try {
            val pi = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            "${pi.versionName ?: "unknown"} (${pi.longVersionCode})"
        } catch (t: Throwable) {
            "unknown (0)"
        }
    }

    /**
     * Snapshot the main thread's current stack trace. Used by [AnrWatchdog]
     * — the main thread is blocked but still alive, so we can introspect it
     * via [Thread.getAllStackTraces]. If the main thread has been renamed
     * (rare, but possible via debugging tools), we fall back to a sentinel.
     */
    private fun dumpMainThreadStack(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        val mainEntry = Thread.getAllStackTraces().entries.firstOrNull { it.key.name == "main" }
        if (mainEntry != null) {
            pw.println("Thread: main (state=${mainEntry.key.state})")
            for (frame in mainEntry.value) {
                pw.println("    at $frame")
            }
        } else {
            pw.println("Main thread stack trace unavailable — thread not found.")
        }
        return sw.toString()
    }

    // ------------------------------------------------------------------
    // Upload
    // ------------------------------------------------------------------

    /**
     * POST [payload] (a JSON string) to `${baseUrl}/api/v1/crash-report`.
     *
     * Returns `true` only for HTTP 2xx. Any other outcome (network failure,
     * non-2xx response, timeout) returns `false` so the caller retains the
     * crash log for the next launch attempt. Timeouts are 15 s for both
     * connect and read — the server should be FAST (one INSERT + COMMIT).
     */
    private fun uploadCrashLog(payload: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL("${baseUrl}${UPLOAD_ENDPOINT}").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = UPLOAD_TIMEOUT_MS.toInt()
                readTimeout = UPLOAD_TIMEOUT_MS.toInt()
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("User-Agent", "Lexicon-CrashReporter/1.0 (Android)")
                // Crash payloads are small (a few KB) — let HttpURLConnection
                // buffer the whole body and set Content-Length. Chunked
                // streaming is not needed and avoids edge cases with servers
                // that mishandle Transfer-Encoding: chunked.
            }
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code in 200..299) {
                Log.d(TAG, "Upload OK: HTTP $code")
                true
            } else {
                Log.w(TAG, "Upload failed: HTTP $code — body retained")
                false
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Upload error: ${t.javaClass.simpleName}: ${t.message}")
            false
        } finally {
            conn?.disconnect()
        }
    }

    // ------------------------------------------------------------------
    // Companion — singleton
    // ------------------------------------------------------------------

    companion object {
        private const val TAG = "LexiconCrash"
        private const val CRASH_DIR = "crash_logs"
        private const val UPLOAD_ENDPOINT = "/api/v1/crash-report"

        /**
         * Default server origin. `10.0.2.2` is the Android emulator's alias
         * for 127.0.0.1 on the host machine, so this reaches a Ktor server
         * running locally during development. Override via [setBaseUrl]
         * before calling [uploadPendingCrashLogs] if your server is
         * elsewhere (e.g. a physical device hitting a LAN IP).
         */
        private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"

        /** Main-thread poll interval — how often the watchdog wakes to ping. */
        private const val ANR_POLL_INTERVAL_MS = 5_000L

        /** Total unresponsiveness threshold before an ANR is recorded. */
        private const val ANR_TIMEOUT_MS = 2 * ANR_POLL_INTERVAL_MS  // 10 s

        /** Cooldown after recording an ANR, to avoid duplicate records. */
        private const val ANR_COOLDOWN_MS = 60_000L

        /** HTTP connect + read timeout for the upload. */
        private const val UPLOAD_TIMEOUT_MS = 15_000L

        private const val BYTES_PER_MB = 1024L * 1024L

        /** Signal names (WITHOUT the `SIG` prefix) we install handlers for. */
        private val SIGNAL_NAMES = listOf("SEGV", "ABRT", "BUS")

        @Volatile private var instance: CrashReporter? = null

        /**
         * Process-wide singleton. First call constructs the reporter and
         * resolves [crashDir] from [Context.getFilesDir]. Does NOT install
         * handlers — call [install] explicitly from
         * [com.lexicon.LexiconApplication.onCreate].
         */
        fun get(context: Context): CrashReporter =
            instance ?: synchronized(this) {
                instance ?: CrashReporter(context.applicationContext).also { instance = it }
            }
    }
}
