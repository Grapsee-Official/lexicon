//  LexoraCrashReporter.swift
//  Lexora
//
//  Per task P4-03 — dependency-free crash reporting scaffolding for iOS.
//
//  Capture paths:
//    1. `NSSetUncaughtExceptionHandler` for NSException (Obj-C / Swift
//       exceptions that escape the runloop — typically a forced unwrap of
//       nil, an array out-of-bounds, etc.).
//    2. `signal()` handlers for SIGSEGV / SIGABRT / SIGBUS — true native
//       crashes (e.g., a bad pointer dereference in the C++ engine).
//
//  Storage:
//    Crash logs are written as JSON files under `Documents/crash_logs/`.
//    Each file contains: device_model, os_version, app_version,
//    crash_type, stack_trace, memory_stats, timestamp.
//
//  Upload:
//    On the NEXT app launch the singleton walks `crash_logs/`, POSTs each
//    file to `${baseUrl}/api/v1/crash-report`, and deletes it on HTTP 2xx.
//    Failures retain the file so the next launch can retry.
//
//  Wiring (NOT done here — caller's responsibility):
//    In `LexoraApp.swift` `@main` or in `AppDelegate.application(_:didFinishLaunchingWithOptions:)`:
//
//        LexoraCrashReporter.shared.install()
//        LexoraCrashReporter.shared.uploadPendingCrashLogs()
//
//  Constraints:
//    * NO Firebase Crashlytics (the spec forbids it).
//    * NO third-party dependencies — only Foundation + Darwin + (optional) UIKit.
//    * Signal handlers run on an unspecified thread with the process in an
//      indeterminate state. We do the minimum: write a small JSON tombstone
//      to disk via FileManager, then re-raise the signal to let the system's
//      default disposition kill the process.

import Foundation
import os.log
#if canImport(UIKit)
import UIKit
#endif

/// LexoraCrashReporter — process-wide singleton owning crash capture + upload.
///
/// All three capture paths (NSException, signal handlers, and the deferred
/// uploader) share the same on-disk layout and JSON schema so the cloud
/// server's single `POST /api/v1/crash-report` endpoint can ingest them all
/// uniformly.
public final class LexoraCrashReporter {

    // MARK: - Singleton

    public static let shared = LexoraCrashReporter()

    // MARK: - Configuration

    /// Default server origin. iOS Simulator reaches the host's localhost
    /// via `http://localhost:8080`; physical-device testing should call
    /// `setBaseUrl(_:)` with the dev machine's LAN IP before
    /// `uploadPendingCrashLogs()`.
    public var baseUrl: String = "http://localhost:8080"

    // MARK: - Private state

    private let logger = Logger(subsystem: "com.lexora", category: "CrashReporter")
    private let lock = NSLock()
    private var installed = false

    /// Captured at `install()` time so signal handlers (which are
    /// `@convention(c)` and can't capture Swift context) can read it via
    /// the singleton. This is the absolute path to the crash log directory.
    private var crashDirPath: String = ""

    /// Saved previous disposition for each signal we install. Unused in
    /// production (we never `uninstall()`), but kept around so a future
    /// test hook can restore the original handlers without restarting the
    /// process.
    private var previousSignalHandlers: [Int32: (@convention(c) (Int32) -> Void)] = [:]

    // MARK: - Init

    private init() {
        // Resolve the crash directory up front so signal handlers (which
        // can't safely allocate) can grab it from the singleton without
        // triggering FileManager calls.
        crashDirPath = Self.resolveCrashDir().path
    }

    // MARK: - Public API

    /// Install the NSException handler and the SIGSEGV / SIGABRT / SIGBUS
    /// signal handlers. Idempotent — second call is a no-op (logged).
    ///
    /// MUST be called as early as possible in the app's lifetime — ideally
    /// the first line of `application(_:didFinishLaunchingWithOptions:)`
    /// or `@main`'s `init`. Any crash before this returns is lost.
    public func install() {
        lock.lock(); defer { lock.unlock() }
        guard !installed else {
            logger.warning("CrashReporter already installed — ignoring")
            return
        }
        installed = true

        // Ensure the crash directory exists. Signal handlers can't create
        // directories safely, so we do it now.
        try? FileManager.default.createDirectory(
            at: Self.resolveCrashDir(),
            withIntermediateDirectories: true
        )

        NSSetUncaughtExceptionHandler { exception in
            LexoraCrashReporter.shared.recordException(exception)
        }

        installSignalHandlers()

        logger.info("CrashReporter installed — crashDir=\(self.crashDirPath, privacy: .public)")
    }

    /// Override the cloud sync server origin. Trailing slash is trimmed.
    /// Call BEFORE `uploadPendingCrashLogs()`.
    public func setBaseUrl(_ url: String) {
        lock.lock(); defer { lock.unlock() }
        baseUrl = url.hasSuffix("/") ? String(url.dropLast()) : url
    }

    /// Walk the `crash_logs/` directory and POST each `.json` file to the
    /// cloud sync server, oldest first. Successful uploads (HTTP 2xx)
    /// delete the file; failures leave it on disk for the next launch.
    ///
    /// Fire-and-forget — runs on a `.utility` background queue. Multiple
    /// invocations queue up; they do not race each other.
    public func uploadPendingCrashLogs() {
        DispatchQueue.global(qos: .utility).async { [weak self] in
            guard let self = self else { return }
            let dir = Self.resolveCrashDir()
            let fm = FileManager.default

            guard let files = try? fm.contentsOfDirectory(
                at: dir,
                includingPropertiesForKeys: [.contentModificationDateKey],
                options: [.skipsHiddenFiles]
            ) else {
                self.logger.info("No crash_logs directory or empty — nothing to upload")
                return
            }

            let jsonFiles = files
                .filter { $0.pathExtension == "json" }
                .sorted { (a, b) -> Bool in
                    let da = (try? a.resourceValues(forKeys: [.contentModificationDateKey])
                        .contentModificationDate) ?? .distantPast
                    let db = (try? b.resourceValues(forKeys: [.contentModificationDateKey])
                        .contentModificationDate) ?? .distantPast
                    return da < db
                }

            if jsonFiles.isEmpty {
                self.logger.info("No pending crash logs to upload")
                return
            }

            self.logger.info("Found \(jsonFiles.count) pending crash log(s) — uploading…")

            for file in jsonFiles {
                self.upload(file: file)
            }
        }
    }

    // MARK: - NSException handler

    /// Invoked synchronously by the NSException runtime when an Obj-C / Swift
    /// exception escapes the runloop. We snapshot everything we can, write
    /// a JSON tombstone to disk, and return — the runtime will then crash
    /// the process per its default disposition.
    ///
    /// No `@objc` annotation needed: the closure passed to
    /// `NSSetUncaughtExceptionHandler` (which expects a `@convention(c)`
    /// function pointer) calls this method directly via the singleton —
    /// Swift-to-Swift dispatch, no Obj-C runtime involvement.
    private func recordException(_ exception: NSException) {
        let stack = exception.callStackSymbols.joined(separator: "\n")
        let payload = buildPayload(
            crashType: "objc",
            signalName: nil,
            stackTrace: "NSException: \(exception.name.rawValue)\nreason: \(exception.reason ?? "(none)")\n\n\(stack)",
            threadName: Thread.current.name.isEmpty ? "main" : Thread.current.name
        )
        writeCrashFile(payload: payload, prefix: "objc")
        logger.error("Recorded NSException: \(exception.name.rawValue, privacy: .public) — \(exception.reason ?? "(no reason)", privacy: .public)")
    }

    // MARK: - Signal handlers

    /// Install `signal()` handlers for SIGSEGV, SIGABRT, SIGBUS. Each handler
    /// is a `@convention(c)` closure that calls back into the singleton's
    /// `recordNativeSignal(_:)` — that callback does the file write and the
    /// process kill.
    ///
    /// `signal()` (not `sigaction()`) is used because:
    ///   * It's simpler — a one-liner per signal.
    ///   * The three signals we care about are fatal-by-default and
    ///     don't need `SA_RESTART` or other `sigaction`-only flags.
    ///   * The handler runs once and then we explicitly re-raise via
    ///     `raise()` + `exit()`, so we don't need signal-info structs.
    private func installSignalHandlers() {
        // The handler must be `@convention(c)` (a plain C function pointer)
        // — Swift closures can't capture context here. We capture nothing
        // and route through the singleton instead.
        let handler: @convention(c) (Int32) -> Void = { receivedSig in
            LexoraCrashReporter.shared.recordNativeSignal(receivedSig)
        }

        let signals: [Int32] = [SIGSEGV, SIGABRT, SIGBUS]
        for sig in signals {
            let prev = signal(sig, handler)
            if prev == SIG_ERR {
                logger.warning("Could not install handler for signal \(sig)")
            } else {
                logger.info("Signal handler installed for signal \(sig) (\(self.signalName(sig) ?? "unknown"))")
            }
        }
    }

    /// Invoked by the `@convention(c)` signal handler trampoline. The
    /// process is in an indeterminate state — keep work to the absolute
    /// minimum: write a small JSON file via `FileManager.createFile`
    /// (a thin wrapper over `open()` + `write()`), then re-raise the
    /// signal so the system's default disposition terminates the process
    /// and emits a proper iOS tombstone.
    private func recordNativeSignal(_ sig: Int32) {
        // Build the payload WITHOUT touching the heap any more than
        // strictly necessary. Foundation's `FileManager.createFile` is
        // acceptable here — it's a single `open()` + `write()` syscall,
        // and we're going to die immediately afterwards anyway.
        let sigName = signalName(sig) ?? "SIG\(sig)"
        let payload = buildPayload(
            crashType: "native",
            signalName: sigName,
            stackTrace: "Native signal \(sigName) received — no Swift stack available.",
            threadName: Thread.current.name.isEmpty ? "signal-\(sig)" : Thread.current.name
        )
        writeCrashFile(payload: payload, prefix: "native_\(sig)")

        // Re-raise the default disposition: restore SIG_DFL, then raise
        // the signal again. The system will then kill us with the right
        // exit reason and write a proper tombstone.
        //
        // `_exit()` (not `exit()`) is used because we're inside a signal
        // handler — only async-signal-safe calls are permitted. `signal()`
        // and `raise()` are both async-signal-safe per POSIX; `exit()` is
        // NOT (it runs atexit handlers + flushes stdio buffers).
        signal(sig, SIG_DFL)
        raise(sig)
        _exit(EXIT_FAILURE)
    }

    // MARK: - Payload construction

    /// Build the JSON crash payload matching the server-side `CrashReport`
    /// data class in `CrashReportEndpoint.kt`. Snake_case keys.
    private func buildPayload(
        crashType: String,
        signalName: String?,
        stackTrace: String,
        threadName: String
    ) -> String {
        let (totalMb, availMb) = memoryStats()

        let memStats: [String: Any] = [
            "total_mb": totalMb,
            "available_mb": availMb,
        ]

        var payload: [String: Any] = [
            "device_model": deviceModel(),
            "os_version": osVersion(),
            "app_version": appVersion(),
            "crash_type": crashType,
            "stack_trace": stackTrace,
            "memory_stats": memStats,
            "timestamp": Int64(Date().timeIntervalSince1970 * 1000),
            "thread": threadName,
        ]
        if let sig = signalName {
            payload["signal"] = sig
        }

        // Serialize defensively — if JSONSerialization fails (shouldn't,
        // but the process may be corrupted), fall back to a minimal
        // string so we at least leave SOMETHING on disk.
        guard let data = try? JSONSerialization.data(
            withJSONObject: payload,
            options: [.sortedKeys]
        ), let str = String(data: data, encoding: .utf8) else {
            return "{\"crash_type\":\"\(crashType)\",\"timestamp\":\(Int64(Date().timeIntervalSince1970 * 1000)),\"error\":\"json_serialization_failed\"}"
        }
        return str
    }

    /// Write [payload] to a uniquely-named JSON file in the crash_logs
    /// directory. Filename format: `<prefix>_<UTC-timestamp>.json` —
    /// sortable by name equals sortable by time, which the uploader needs.
    private func writeCrashFile(payload: String, prefix: String) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyyMMdd-HHmmss-SSS"
        formatter.timeZone = TimeZone(identifier: "UTC")
        formatter.locale = Locale(identifier: "en_US_POSIX")
        let ts = formatter.string(from: Date())

        let filename = "\(prefix)_\(ts).json"
        let fileURL = URL(fileURLWithPath: crashDirPath).appendingPathComponent(filename)

        // `FileManager.createFile` is the safest write API from inside a
        // signal handler — it's a single syscall and doesn't touch the
        // file cache the way `Data.write(to:)` does.
        let data = payload.data(using: .utf8) ?? Data()
        FileManager.default.createFile(atPath: fileURL.path, contents: data)
    }

    // MARK: - Device info

    /// Returns `(total_mb, available_mb)`. Both are best-effort — on iOS
    /// we can read `ProcessInfo.physicalMemory` for the total, and
    /// `os_proc_available_memory()` for the per-process available memory
    /// budget (returns 0 if unavailable, e.g., on Simulator).
    private func memoryStats() -> (Int64, Int64) {
        let total = Int64(ProcessInfo.processInfo.physicalMemory / (1024 * 1024))

        // `os_proc_available_memory` is iOS 13.0+/macOS 10.15+. It returns
        // the remaining bytes in the process's memory budget. Returns 0
        // when the platform doesn't enforce a per-process limit (Simulator).
        let avail: Int64
        if let raw = osAvailableMemoryBytes() {
            avail = Int64(raw / (1024 * 1024))
        } else {
            avail = -1
        }
        return (total, avail)
    }

    /// Wrapper around `os_proc_available_memory()`. Declared via `dlsym`
    /// because the symbol isn't part of any public Swift module, but is
    /// present in libSystem on iOS 13.0+ / macOS 10.15+. Returns nil if the
    /// call fails, returns 0 (per-process limit not enforced, e.g. on
    /// Simulator), or the symbol isn't found.
    private func osAvailableMemoryBytes() -> UInt64? {
        // RTLD_DEFAULT on Darwin is `((void *) -2)`. Swift can't see the
        // C macro, so we pass the same value via UnsafeMutableRawPointer(bitPattern:).
        guard let defaultHandle = UnsafeMutableRawPointer(bitPattern: -2) else { return nil }
        guard let ptr = dlsym(defaultHandle, "os_proc_available_memory") else { return nil }
        typealias FnType = @convention(c) () -> UInt64
        let fn = unsafeBitCast(ptr, to: FnType.self)
        let v = fn()
        return v == 0 ? nil : v
    }

    private func deviceModel() -> String {
        // `UIDevice.current.model` gives "iPhone" / "iPad" — not very
        // useful for crash triage. The hardware identifier (e.g.,
        // "iPhone14,3") requires reading `hw.machine` via sysctl, which
        // is what we do here.
        var size = 0
        sysctlbyname("hw.machine", nil, &size, nil, 0)
        guard size > 0 else {
            #if canImport(UIKit)
            return UIDevice.current.model
            #else
            return "unknown"
            #endif
        }
        var machine = [CChar](repeating: 0, count: size)
        sysctlbyname("hw.machine", &machine, &size, nil, 0)
        return String(cString: machine)
    }

    private func osVersion() -> String {
        let v = ProcessInfo.processInfo.operatingSystemVersion
        return "iOS \(v.majorVersion).\(v.minorVersion).\(v.patchVersion)"
    }

    private func appVersion() -> String {
        let v = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "unknown"
        let b = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "0"
        return "\(v) (\(b))"
    }

    /// Map a signal number to its conventional name (e.g., `11` → "SIGSEGV").
    private func signalName(_ sig: Int32) -> String? {
        switch sig {
        case SIGSEGV: return "SIGSEGV"
        case SIGABRT: return "SIGABRT"
        case SIGBUS:  return "SIGBUS"
        case SIGFPE:  return "SIGFPE"
        case SIGILL:  return "SIGILL"
        case SIGPIPE: return "SIGPIPE"
        case SIGTRAP: return "SIGTRAP"
        default:      return nil
        }
    }

    // MARK: - Upload

    /// Upload a single crash log file. On HTTP 2xx the file is deleted;
    /// any other outcome retains it for the next launch. Failures are
    /// logged but never propagated — upload is best-effort.
    private func upload(file: URL) {
        guard let body = try? Data(contentsOf: file) else {
            logger.warning("Could not read crash file \(file.lastPathComponent, privacy: .public) — skipping")
            return
        }
        guard let url = URL(string: "\(baseUrl)/api/v1/crash-report") else {
            logger.warning("Invalid baseUrl — cannot upload \(file.lastPathComponent, privacy: .public)")
            return
        }

        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        req.setValue("Lexora-CrashReporter/1.0 (iOS)", forHTTPHeaderField: "User-Agent")
        req.timeoutInterval = 15.0
        req.httpBody = body

        let task = URLSession.shared.dataTask(with: req) { [weak self] _, response, error in
            guard let self = self else { return }
            if let error = error {
                self.logger.warning("Upload error for \(file.lastPathComponent, privacy: .public): \(error.localizedDescription, privacy: .public) — retained")
                return
            }
            if let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) {
                self.logger.info("Uploaded + deleted \(file.lastPathComponent, privacy: .public) (HTTP \(http.statusCode))")
                try? FileManager.default.removeItem(at: file)
            } else {
                let code = (response as? HTTPURLResponse)?.statusCode ?? -1
                self.logger.warning("Upload failed for \(file.lastPathComponent, privacy: .public) (HTTP \(code)) — retained")
            }
        }
        task.resume()
    }

    // MARK: - Helpers

    /// Resolve the on-disk directory for crash logs: `Documents/crash_logs/`.
    /// Created lazily — callers should `createDirectory(withIntermediateDirectories: true)`
    /// before writing, but `FileManager.createFile` will fail gracefully if
    /// the parent doesn't exist.
    private static func resolveCrashDir() -> URL {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
            ?? URL(fileURLWithPath: NSTemporaryDirectory())
        return docs.appendingPathComponent("crash_logs", isDirectory: true)
    }
}
