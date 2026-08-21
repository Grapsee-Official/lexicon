// CrashReporter.kt — KMM commonMain expect declaration for uploading crash
// report payloads to the Lexora cloud ingestion endpoint.
//
// Phase 4 (P4-10):
//   - `uploadCrashReport(report: String): Boolean`
//
// Why a top-level `expect fun` (not an `expect class`):
//   - Crash reports are often generated on a background thread at process-
//     shutdown time, after the Kotlin runtime has already begun tearing down
//     class loaders. A free function with no captured object state is the
//     lowest-overhead way to ship a single payload — no `expect class`
//     allocation, no DI lookup.
//   - The signature is intentionally synchronous (returns `Boolean`, not a
//     `suspend fun`) so the actual can be called from a non-coroutine crash
//     handler (e.g. `Thread.setDefaultUncaughtExceptionHandler` on Android
//     or `NSSetUncaughtExceptionHandler` on iOS). Both actuals block the
//     caller until the upload completes or times out — see the per-platform
//     docs for the timeout values.
//
// The cloud endpoint URL is defined as a `const val` in the platform
// actuals (`CRASH_REPORT_URL`). Override at build time via
// `-DCRASH_REPORT_URL=...` if a staging endpoint is needed; the default
// points at the same Ktor server host that DeltaSyncClient.kt uses for
// delta sync (`http://localhost:8080/api/v1/crashes`), so a single dev
// server can ingest both sync traffic and crash payloads.
//
// Return contract:
//   - `true`  iff the server returned HTTP 2xx for the POST.
//   - `false` on any I/O failure, non-2xx response, timeout, or
//              interruption. Crash uploads are best-effort telemetry — a
//              `false` return MUST NOT block the user or interrupt the
//              crash-shutdown flow. The host app may queue a retry on next
//              launch via a local pending-reports directory; that policy is
//              out of scope for this `expect fun`.

package com.lexora.engine

/**
 * Best-effort synchronous upload of a crash report payload to the Lexora
 * cloud ingestion endpoint.
 *
 * @param report  The crash report body. The platform actuals POST this as
 *                `Content-Type: text/plain` (UTF-8); the server stores it
 *                verbatim for later triage. Callers that have a structured
 *                payload (JSON, protobuf) should serialise it before
 *                calling — this function does not transform [report].
 *
 * @return `true` iff the server acknowledged with HTTP 2xx; `false` on any
 *         failure (see file header for the full contract).
 */
expect fun uploadCrashReport(report: String): Boolean
