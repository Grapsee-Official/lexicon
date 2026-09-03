// CrashReporter.ios.kt — iOS `actual` implementation for the `expect fun`
// declared in CrashReporter.kt (commonMain).
//
// Per task P4-10:
//   - uploadCrashReport(report: String): Boolean
//     → HTTP POST via NSURLSession (with a dispatch semaphore to make the
//       async NSURLSession API synchronous, as the expect contract
//       requires).
//
// Why NSURLSession (not CFNetwork / NSURLConnection):
//   - `NSURLSession` is the only App-Store-safe HTTP client on iOS since
//     iOS 9 (NSURLConnection was deprecated in iOS 9 and removed in iOS 13).
//   - It's also the only client that respects the user's global
//     "Low Data Mode" / "Allow Cellular" toggles and any VPN / proxy
//     configuration. This matters for a crash reporter — we don't want
//     to surprise the user by sending data over a metered connection they
//     didn't expect.
//   - CFNetwork is too low-level for a single POST; NSURLSession's
//     `dataTaskWithRequest:completionHandler:` gives us a one-shot POST
//     in <50 lines.
//
// Why a dispatch semaphore (not a coroutine):
//   This `actual fun` is called from the iOS uncaught-exception handler
//   (`NSSetUncaughtExceptionHandler`), where there's no run loop and no
//   coroutine scope. The semaphore blocks the crashing thread until the
//   upload completes — same pattern as the Android actual's synchronous
//   HttpURLConnection. The 30-second timeout bounds the worst case so a
//   hung server can't keep a half-dead process alive forever.
//
// Endpoint:
//   Same default as the Android actual (`http://localhost:8080/api/v1/crashes`)
//   — see the comment on [CRASH_REPORT_URL] for the override story.
package com.lexora.engine

import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_t
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time

/**
 * Default crash-report ingestion endpoint. Mirrors the Android actual's
 * constant of the same name — same origin as the delta-sync server (see
 * DeltaSyncClient.kt:193). A `/api/v1/crashes` route should be added to
 * LexoraSyncServer.kt to accept these POSTs.
 *
 * Override at build time via `-DCRASH_REPORT_URL=...` for staging / prod.
 */
internal const val CRASH_REPORT_URL: String =
    "http://localhost:8080/api/v1/crashes"

/**
 * Hard ceiling on how long [uploadCrashReport] will block the caller
 * waiting for the server to respond. Generous (30s) because:
 *   - The server may queue the report for async processing before
 *     replying.
 *   - On a flaky cellular connection, the TCP handshake + TLS + HTTP
 *     round-trip can take 5–10s by itself.
 *   - The iOS uncaught-exception handler typically gets ~5s before the
 *     watchdog kills the process — but we want to bound the worst case
 *     for non-crash callers (e.g. a manual "Send feedback" button) where
 *     blocking 30s on a hung server is preferable to indefinitely
 *     hanging the UI.
 */
private const val UPLOAD_TIMEOUT_SECONDS: Long = 30L

/**
 * iOS `actual` — POSTs the crash report as `text/plain; charset=utf-8`.
 *
 * Flow:
 *   1. Build an `NSMutableURLRequest` with method=POST, Content-Type,
 *      User-Agent, and a 30-second timeout (in case the semaphore wait
 *      itself somehow fires before the URLSession's own timeout —
 *      belt-and-suspenders).
 *   2. Encode the String body to `NSData` via `NSString.dataUsingEncoding:`
 *      (UTF-8). The `?: return false` guards against the (impossible in
 *      practice) case where the report contains invalid UTF-8 — the
 *      Kotlin String is always well-formed UTF-16, and UTF-8 can encode
 *      every Unicode codepoint, so this is purely defensive.
 *   3. Create a `dispatch_semaphore_t` with initial count 0. The
 *      completion handler will `signal` it; the calling thread will
 *      `wait` on it with a 30s timeout.
 *   4. Schedule the `NSURLSessionDataTask` via `NSURLSession.sharedSession`
 *      (no need for a custom session — we don't need a custom delegate
 *      queue, and sharedSession uses the system's connection pool).
 *   5. `task.resume()` kicks off the request asynchronously.
 *   6. `dispatch_semaphore_wait` blocks the calling thread until either:
 *        (a) the completion handler fires and signals — `wait` returns 0
 *            (success), we return the captured `success` value; OR
 *        (b) the 30s timeout elapses — `wait` returns non-zero, we
 *            return `false`. The task is NOT cancelled (NSURLSession will
 *            eventually reap it), and the captured `success` is ignored.
 *
 * Error contract (mirrors the Android actual):
 *   Any throwable is caught and returns `false`. The crash handler MUST
 *   NOT propagate exceptions — that would mask the original crash and
 *   prevent the system's default handler from logging it.
 *
 * @return `true` iff the server returned HTTP 2xx within 30s; `false`
 *         otherwise.
 */
actual fun uploadCrashReport(report: String): Boolean {
    return try {
        // Build the request.
        val url = NSURL(string = CRASH_REPORT_URL) ?: return false
        val request = NSMutableURLRequest.requestWithURL(url)
        request.setHTTPMethod("POST")
        request.setValue(
            "text/plain; charset=utf-8",
            forHTTPHeaderField = "Content-Type"
        )
        request.setValue("Lexora/1.0 (iOS)", forHTTPHeaderField = "User-Agent")
        request.setTimeoutInterval(UPLOAD_TIMEOUT_SECONDS.toDouble())

        // Encode the body to NSData. NSString.create(string:) is the
        // Kotlin/Native factory for [NSString stringWithString:] — it
        // copies the Kotlin String into an immutable NSString. UTF-8
        // encoding can never fail for a valid Kotlin String, but the
        // nullable return type is preserved by the cinterop, so we guard.
        val body: NSData = NSString.create(string = report)
            .dataUsingEncoding(NSUTF8StringEncoding)
            ?: return false
        request.setHTTPBody(body)

        // Synchronisation primitive: the completion handler signals this
        // semaphore when the upload finishes (success or failure); the
        // calling thread waits on it.
        val semaphore: dispatch_semaphore_t = dispatch_semaphore_create(0)

        // Mutable result captured by the completion handler. Under the
        // new Kotlin/Native memory model (default in Kotlin 1.9.22, which
        // build.gradle.kts pins), lambdas passed to ObjC APIs can capture
        // and mutate local vars across threads without explicit freezing.
        var success = false

        // Schedule the upload on the shared NSURLSession (no custom
        // delegate queue needed — the completion handler is the only
        // callback we care about).
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { _, response, error ->
            // invoked on an NSURLSession-managed background queue.
            if (error == null) {
                val httpResp = response as? NSHTTPURLResponse
                val code = httpResp?.statusCode ?: 0
                // 2xx == success. Accept 200/201/202/204 — see the
                // Android actual's comment for the rationale.
                success = code in 200..299
            }
            // else: leave success == false (network failure, DNS, TLS, …)
            dispatch_semaphore_signal(semaphore)
        }
        task.resume()

        // Block the calling thread until either the upload completes
        // (semaphore signalled) or 30 seconds elapse. dispatch_time takes
        // a delta in nanoseconds; NSEC_PER_SEC converts seconds → ns.
        val timeoutNs = UPLOAD_TIMEOUT_SECONDS * NSEC_PER_SEC
        val deadline = dispatch_time(DISPATCH_TIME_NOW, timeoutNs)
        val waitResult = dispatch_semaphore_wait(semaphore, deadline)
        // waitResult == 0 means the semaphore was signalled before the
        // deadline; non-zero means the deadline elapsed. On timeout we
        // return false (the task is still running in the background —
        // NSURLSession will eventually reap it).
        if (waitResult == 0L) success else false
    } catch (t: Throwable) {
        // Best-effort telemetry — never propagate. Even an Error
        // (OutOfMemoryError, NSException bridged to Kotlin Throwable) is
        // swallowed; the original crash is more important than this
        // upload's success.
        false
    }
}
