// CrashReporter.android.kt — Android `actual` implementation for the
// `expect fun` declared in CrashReporter.kt (commonMain).
//
// Per task P4-10:
//   - uploadCrashReport(report: String): Boolean
//     → HTTP POST to the cloud crash-ingestion endpoint.
//
// Why HttpURLConnection (not OkHttp / Ktor):
//   This `actual fun` is called from the uncaught-exception handler
//   (`Thread.setDefaultUncaughtExceptionHandler`). At that point:
//     - The crashing thread is about to terminate; the JVM may be in a
//       partially-torn-down state.
//     - Looper/Handler queues may be draining abnormally.
//     - OkHttp's connection pool and dispatcher use background threads
//       that may have died with the crash; relying on them risks a
//       deadlock where the upload task is queued but never executes.
//   `HttpURLConnection` is part of the JDK / Android core library and
//   makes NO assumptions about thread-pool availability — every call
//   owns its own socket from `URL.openConnection()` through to
//   `disconnect()`. It's the lowest-friction HTTP API available in a
//   crash handler.
//
// Why a synchronous call (not a `suspend fun`):
//   Crash handlers don't run on a coroutine. The thread is about to die;
//   blocking for ~1–2 seconds to upload the report is the only way to
//   ensure delivery before process death. The Android runtime's
//   `UncaughtExceptionHandler` contract gives us ~5 seconds before
//   `killProcess` is called by the framework; our 30s read timeout will
//   never fire in practice, but it bounds the worst case so a hung
//   server can't keep a half-dead process alive forever.
//
// Endpoint:
//   Defaults to `http://localhost:8080/api/v1/crashes` — same origin as
//   the LexoraSyncServer (used by DeltaSyncClient). A `/api/v1/crashes`
//   POST handler should be added to the server in a follow-up; the
//   existing `/api/v1/integrity/{userId}` handler is the closest analog
//   (it also accepts a `text/plain` body).
package com.lexora.engine

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Default crash-report ingestion endpoint. Same origin as the delta-sync
 * server (see DeltaSyncClient.kt:193); a new `/api/v1/crashes` route
 * should be added to LexoraSyncServer.kt to accept these POSTs.
 *
 * Override at build time via `-DCRASH_REPORT_URL=...` for staging / prod.
 */
internal const val CRASH_REPORT_URL: String =
    "http://localhost:8080/api/v1/crashes"

/**
 * Android `actual` — POSTs the crash report as `text/plain; charset=utf-8`.
 *
 * Flow:
 *   1. Open the URL → cast to HttpURLConnection (HTTP-specific subclass
 *      ofURLConnection; will throw ClassCastException on a non-http(s)
 *      URL — caught by the outer try and returns false).
 *   2. Configure: POST method, Content-Type, Content-Length (so the
 *      server can pre-allocate the body reader), doOutput=true (so
 *      getOutputStream() is allowed).
 *   3. Set timeouts: connect=10s, read=30s. The 30s read timeout is
 *      generous because the server may queue the report for async
 *      processing before responding; in normal operation it replies
 *      within 100ms.
 *   4. Write the body, flush, then read `responseCode`. Reading
 *      `responseCode` forces HttpURLConnection to actually send the
 *      request and wait for the response — without this call, the
 *      request might be buffered and never sent before `disconnect()`.
 *   5. Disconnect (returns the underlying socket to the keep-alive pool
 *      if the server sent `Connection: keep-alive`; closes it otherwise).
 *
 * Error contract:
 *   Any throwable (IOException from the network, ClassCastException from
 *   a malformed URL, IllegalStateException from a torn-down JVM, OOM,
 *   SecurityException from a missing INTERNET permission, …) is caught
 *   and returns `false`. The crash handler MUST NOT propagate exceptions
 *   — that would mask the original crash and prevent the framework's
 *   default handler from logging it.
 *
 * @return `true` iff the server returned HTTP 2xx; `false` otherwise.
 */
actual fun uploadCrashReport(report: String): Boolean {
    return try {
        val url = URL(CRASH_REPORT_URL)
        val conn = url.openConnection() as? HttpURLConnection ?: return false
        try {
            val body = report.toByteArray(Charsets.UTF_8)
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            conn.setRequestProperty("Content-Length", body.size.toString())
            conn.setRequestProperty("User-Agent", "Lexora/1.0 (Android)")
            conn.doOutput = true
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.useCaches = false

            // use {} closes the stream in a finally, which forces
            // HttpURLConnection to flush any buffered bytes to the socket
            // before we read the response code.
            conn.outputStream.use { os: OutputStream ->
                os.write(body)
                os.flush()
            }

            val code = conn.responseCode
            // 2xx == success. We deliberately accept 200/201/202/204 —
            // the server may reply 201 (Created) if it persists the report
            // synchronously, or 202 (Accepted) if it queues for async
            // processing. Both are "the server has the report".
            code in 200..299
        } finally {
            conn.disconnect()
        }
    } catch (t: Throwable) {
        // Best-effort telemetry — never propagate. Even an Error (OOM,
        // StackOverflowError) is swallowed; the original crash is more
        // important than this upload's success.
        false
    }
}
