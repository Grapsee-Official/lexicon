// CrashReportEndpoint.kt — Ktor route for ingesting mobile crash reports.
//
// Per task P4-03:
//   * Add a new Ktor route: POST /api/v1/crash-report
//   * Accepts JSON: { device_model, os_version, app_version, crash_type,
//                     stack_trace, memory_stats, timestamp }
//   * Stores in PostgreSQL `crash_logs` table (CREATE TABLE added to
//     schema/lexora_postgres.sql).
//
// Wiring: this file declares an extension function on Ktor's `Route`
// receiver. It is mounted inside the existing `/api/v1` route block in
// `LexoraSyncServer.kt::lexoraRoutes()` via a single line:
//
//     route("/api/v1") {
//         …existing routes…
//         crashReportRoutes(dbUrl, dbUser, dbPass)   // <- added
//     }
//
// The data class uses snake_case field names so Gson (with default config,
// matching the rest of LexoraSyncServer.kt) deserialises the wire JSON
// directly without any @SerializedName annotations or FieldNamingPolicy
// tweaks. This keeps the server config consistent across all routes.

package com.lexora.cloud.server

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.DriverManager

/**
 * Wire-format model for the `POST /api/v1/crash-report` body.
 *
 * Field names mirror the JSON keys EXACTLY (snake_case) so Gson's default
 * field-name policy deserialises them without `@SerializedName`. All fields
 * except [crash_type] are nullable — the device may omit any of them if
 * collection fails (e.g., the device model is unavailable on a stripped
 * build, or `memory_stats` couldn't be sampled before the process died).
 *
 * @property deviceModel   e.g., "Pixel 7", "iPhone14,3".
 * @property osVersion     e.g., "Android 14 (API 34)", "iOS 17.2.1".
 * @property appVersion     e.g., "1.0.0 (1)" — versionName (versionCode).
 * @property crashType      One of: "java" | "anr" | "native" | "objc" | "swift".
 *                          The server stores this verbatim (NOT validated
 *                          against an enum) so we can add new crash types
 *                          without a schema migration.
 * @property stackTrace     Free-form stack-trace text. May be multi-line.
 *                          For native crashes, this is a tombstone string
 *                          (no Java/Swift frames available).
 * @property memoryStats    Nested map of memory metrics (total_mb,
 *                          available_mb, low_memory, …). Stored as JSONB.
 * @property timestamp      Epoch milliseconds when the crash happened on
 *                          the device. Stored as `client_timestamp BIGINT`
 *                          in the DB (column name avoids SQL reserved word).
 * @property signal         Optional signal name for native crashes
 *                          (e.g., "SIGSEGV"). Stored in the `details` JSONB.
 * @property thread         Optional thread name. Stored in `details` JSONB.
 */
data class CrashReport(
    val device_model: String? = null,
    val os_version: String? = null,
    val app_version: String? = null,
    val crash_type: String,
    val stack_trace: String? = null,
    val memory_stats: Map<String, Any?>? = null,
    val timestamp: Long? = null,
    val signal: String? = null,
    val thread: String? = null
)

/**
 * Install the `POST /api/v1/crash-report` route on this [Route] receiver.
 *
 * On success: HTTP 200 with a small JSON body `{"status":"ok","id":<rowId>}`.
 * On bad input (missing `crash_type` / unparseable JSON): HTTP 400.
 * On DB failure: HTTP 500 with `{"status":"error","error":"<message>"}`.
 *
 * The route is idempotent in the sense that re-POSTing the same crash log
 * creates a duplicate row — there is no client-supplied deduplication key
 * by design (crash logs are append-only telemetry). The caller is expected
 * to delete the local crash file on HTTP 2xx, which is the natural
 * deduplication boundary.
 */
fun Route.crashReportRoutes(
    dbUrl: String,
    dbUser: String,
    dbPass: String
) {
    post("/crash-report") {
        val raw = call.receiveText()
        val report = try {
            Gson().fromJson(raw, CrashReport::class.java)
        } catch (t: Throwable) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "status" to "error",
                "error" to "invalid JSON: ${t.message}"
            ))
            return@post
        }

        if (report.crash_type.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "status" to "error",
                "error" to "missing or empty 'crash_type'"
            ))
            return@post
        }

        // Re-serialize memory_stats + the extras (signal, thread) into JSON
        // strings for the JSONB columns. Gson serialises a Map<String,Any?>
        // to a nested JSON object; for `details` we build a fresh map so
        // unknown future extras survive the round-trip without a schema bump.
        val gson = Gson()
        val memoryStatsJson = report.memory_stats?.let { gson.toJson(it) }
        val detailsMap = mutableMapOf<String, Any?>()
        if (report.signal != null) detailsMap["signal"] = report.signal
        if (report.thread != null) detailsMap["thread"] = report.thread
        val detailsJson = if (detailsMap.isEmpty()) null else gson.toJson(detailsMap)

        var conn: java.sql.Connection? = null
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)
            val stmt = conn.prepareStatement("""
                INSERT INTO crash_logs
                    (device_model, os_version, app_version, crash_type,
                     stack_trace, memory_stats, client_timestamp, details)
                VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?::jsonb)
                RETURNING id
            """)
            stmt.setString(1, report.device_model)
            stmt.setString(2, report.os_version)
            stmt.setString(3, report.app_version)
            stmt.setString(4, report.crash_type)
            stmt.setString(5, report.stack_trace)
            stmt.setString(6, memoryStatsJson)
            if (report.timestamp != null) {
                stmt.setLong(7, report.timestamp)
            } else {
                stmt.setNull(7, java.sql.Types.BIGINT)
            }
            stmt.setString(8, detailsJson)

            val rs = stmt.executeQuery()
            val rowId = if (rs.next()) rs.getLong("id") else -1L
            conn.close()

            call.respond(mapOf(
                "status" to "ok",
                "id" to rowId
            ))
        } catch (t: Throwable) {
            conn?.let { runCatching { it.close() } }
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "status" to "error",
                "error" to (t.message ?: t.javaClass.simpleName)
            ))
        }
    }
}
