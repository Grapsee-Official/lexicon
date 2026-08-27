//  LexiconOOMMonitor.swift
//  Lexicon
//
//  Per spec risk matrix: "iOS background app suspension — milliseconds to
//  respond before killing." When iOS posts
//  UIApplication.didReceiveMemoryWarningNotification, the app has a very
//  short window to shed memory before the jetsam watchdog kills it. If we
//  don't unload the DAWG and flush caches promptly, the process is
//  terminated and the user's next text-selection action in another app
//  (the iOS edit-menu hook) lands on a dead listener.
//
//  Strategy:
//    1. Subscribe to UIApplication.didReceiveMemoryWarningNotification on
//       the main queue. On fire: unload DAWG, flush caches, log to
//       os.Logger, and emit a crash-reporter record.
//    2. Sample mach_task_basic_info.resident_size on demand and track the
//       peak. If resident > 180 MiB (90% of the 200 MiB budget that
//       mirrors the Android Moto G gate), proactively unload the DAWG.
//    3. Provide a background-task wrapper (UIApplication.beginBackgroundTask)
//       so the engine can flush cleanly when the app is suspended; on
//       expiration we synchronously force-unload the DAWG to shed as much
//       memory as possible before the system reclaims us.
//
//  This monitor is a singleton — there is exactly one UIApplication per
//  process and exactly one didReceiveMemoryWarningNotification stream.

import Foundation
import UIKit
import os.log

/// LexiconOOMMonitor — singleton watchdog that listens for iOS memory
/// pressure and the background-task expiration events, and translates
/// them into DAWG-unload + cache-flush actions on the shared LexiconEngine.
public final class LexiconOOMMonitor {

    // MARK: - Singleton

    public static let shared = LexiconOOMMonitor()

    // MARK: - Public types

    /// Snapshot of the memory state at a point in time. All sizes in bytes.
    public struct MemoryStats {
        /// mach_task_basic_info.resident_size — physical pages currently
        /// resident for this process. This is the metric the jetsam
        /// watchdog uses to decide who to kill.
        public let residentBytes: UInt64
        /// mach_task_basic_info.virtual_size — total virtual address space
        /// reserved (mostly mmap'd files + guard pages). Logged for
        /// diagnostics; not used for thresholds.
        public let virtualBytes: UInt64
        /// Peak resident size observed since [start] was called.
        public let peakResidentBytes: UInt64
        /// 180 MiB — the proactive DAWG-unload trigger.
        public let softThresholdBytes: UInt64
        /// 200 MiB — the hard budget (mirrors Android's Moto G gate).
        public let hardBudgetBytes: UInt64
        /// True iff `residentBytes > softThresholdBytes`.
        public let isAboveSoftThreshold: Bool
    }

    /// Crash reporter sink. `(tag, message)` — there is no throwable
    /// because all OOM reports are proactive (not exception-driven).
    public typealias CrashReporter = (_ tag: String, _ message: String) -> Void

    // MARK: - Thresholds (public constants — tests assert against these)

    /// Hard memory budget from the spec: 200 MiB peak. Mirrors the Android
    /// Moto G gate so both platforms enforce the same ceiling.
    public static let hardBudgetBytes: UInt64 = 200 * 1024 * 1024

    /// 90% of the hard budget = 180 MiB. Crossing this triggers a
    /// proactive DAWG unload + warning. Computed via integer math so it
    /// stays exact (no floating-point drift in tests).
    public static let softThresholdBytes: UInt64 = (hardBudgetBytes * 9) / 10 // 180 MiB

    // MARK: - Private state

    private let logger = Logger(subsystem: "com.lexicon", category: "OOM")
    private let lock = NSLock()

    /// The engine. Set via [setEngine] once the cold-start optimizer has
    /// finished warming. Nil until then — recovery paths tolerate nil.
    private var engine: LexiconEngine?

    /// Optional crash reporter. Defaults to os.Logger only.
    private var crashReporter: CrashReporter?

    /// Peak resident size observed since [start]. Updated under [lock].
    private var peakResident: UInt64 = 0

    /// Observer token for the memory-warning notification. Cleared on [stop].
    private var memoryWarningObserver: NSObjectProtocol?

    /// Background task identifier for the suspension-grace window.
    /// `.invalid` when no task is outstanding.
    private var backgroundTask: UIBackgroundTaskIdentifier = .invalid

    /// Whether [start] has been called. Guarded by [lock].
    private var started: Bool = false

    // MARK: - Init

    private init() {
        // No work here — singleton init is lazy and we don't want to
        // touch UIApplication from init (it may not be available yet
        // during early app launch).
    }

    // MARK: - Public configuration

    /// Bind the engine. Called once the cold-start optimizer has finished
    /// warming. The monitor uses the engine to (a) unload the DAWG on
    /// pressure and (b) read native memory usage for OOM reports.
    public func setEngine(_ engine: LexiconEngine) {
        lock.lock(); defer { lock.unlock() }
        self.engine = engine
        logger.info("OOM: engine bound")
    }

    /// Bind a crash reporter sink. Optional — when unset, all reports
    /// fall back to os.Logger only.
    public func setCrashReporter(_ reporter: @escaping CrashReporter) {
        lock.lock(); defer { lock.unlock() }
        self.crashReporter = reporter
        logger.info("OOM: crash reporter bound")
    }

    // MARK: - Public state

    /// Current memory snapshot. Always safe to call (returns zeros if the
    /// mach call fails).
    public var stats: MemoryStats {
        let (res, virt) = machMemoryInfo()
        let peak = peakResidentBytes
        return MemoryStats(
            residentBytes: res,
            virtualBytes: virt,
            peakResidentBytes: peak,
            softThresholdBytes: Self.softThresholdBytes,
            hardBudgetBytes: Self.hardBudgetBytes,
            isAboveSoftThreshold: res > Self.softThresholdBytes
        )
    }

    /// Peak resident size observed since [start] was called.
    public var peakResidentBytes: UInt64 {
        lock.lock(); defer { lock.unlock() }
        return peakResident
    }

    /// Reset the peak to zero. Call from a test harness between sessions.
    public func resetPeak() {
        lock.lock(); defer { lock.unlock() }
        peakResident = 0
    }

    // MARK: - Lifecycle

    /// Start listening for memory warnings. Idempotent.
    ///
    /// Call from `AppDelegate.application(_:didFinishLaunchingWithOptions:)`
    /// AFTER the cold-start optimizer has begun warming — the engine
    /// itself may still be nil at this point (it's bound later via
    /// [setEngine]), but the notification subscription is cheap and safe
    /// to install early.
    public func start() {
        lock.lock(); defer { lock.unlock() }
        guard !started else { return }
        started = true

        memoryWarningObserver = NotificationCenter.default.addObserver(
            forName: UIApplication.didReceiveMemoryWarningNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            // Delivered on the main queue (we requested .main) — jetsam
            // gives us very little wall-clock time, so the recovery path
            // must be synchronous and non-blocking.
            self?.handleMemoryWarning()
        }

        logger.info(
            "OOM: monitor started — budget=\(Self.hardBudgetBytes / (1024*1024))MiB, softThreshold=\(Self.softThresholdBytes / (1024*1024))MiB"
        )
    }

    /// Stop listening. Call from tests or app teardown. Idempotent.
    public func stop() {
        lock.lock(); defer { lock.unlock() }
        guard started else { return }
        started = false
        if let obs = memoryWarningObserver {
            NotificationCenter.default.removeObserver(obs)
            memoryWarningObserver = nil
        }
        // Note: endBackgroundTask also takes the lock via the public API,
        // so we release our lock first to avoid a self-deadlock.
        // The simplest correct pattern here is to NOT hold the lock while
        // calling endBackgroundTask — instead we read the identifier under
        // the lock and end it outside.
        // (This is the only place we do this; in normal use the caller
        // invokes endBackgroundTask directly.)
        let taskToKill = backgroundTask
        backgroundTask = .invalid
        if taskToKill != .invalid {
            DispatchQueue.main.async {
                UIApplication.shared.endBackgroundTask(taskToKill)
            }
        }
        logger.info("OOM: monitor stopped")
    }

    // MARK: - Memory warning handling

    private func handleMemoryWarning() {
        let (resident, virtual) = machMemoryInfo()
        let peakBefore = updatePeak(resident)

        logger.warning(
            "OOM: didReceiveMemoryWarning — resident=\(resident / (1024*1024))MiB virtual=\(virtual / (1024*1024))MiB peak=\(peakBefore / (1024*1024))MiB"
        )

        unloadDawg(reason: "didReceiveMemoryWarning")
        flushCaches(reason: "didReceiveMemoryWarning")
        reportCrash(
            tag: "oom_memory_warning",
            message: buildReport(source: "memoryWarning", resident: resident, virtual: virtual, peak: peakBefore)
        )

        // Re-check after recovery — jetsam is unforgiving. If we're still
        // over the soft threshold we escalate (the DAWG unload above was
        // already a no-op if it was already disabled, but we log the
        // post-recovery reading for diagnostics).
        let (res2, virt2) = machMemoryInfo()
        let peak2 = updatePeak(res2)
        if res2 > Self.softThresholdBytes {
            logger.error(
                "OOM: resident still above soft threshold after warning: \(res2 / (1024*1024))MiB > \(Self.softThresholdBytes / (1024*1024))MiB"
            )
            unloadDawg(reason: "post-warning soft threshold")
            reportCrash(
                tag: "oom_soft_post_warning",
                message: buildReport(source: "postWarning", resident: res2, virtual: virt2, peak: peak2)
            )
        }
    }

    /// Proactive check — call from a periodic sampler or after a heavy
    /// operation. Implements the spec rule: "If resident > 180MB:
    /// proactive DAWG unload."
    ///
    /// Returns true iff the soft threshold was breached AND an unload was
    /// attempted. Callers may use this to gate further work (e.g. postpone
    /// a large allocation until after the next sample).
    @discardableResult
    public func checkSoftThreshold(reason: String) -> Bool {
        let (resident, _) = machMemoryInfo()
        _ = updatePeak(resident)
        if resident <= Self.softThresholdBytes { return false }

        logger.warning(
            "OOM: SOFT THRESHOLD BREACH — resident=\(resident / (1024*1024))MiB > \(Self.softThresholdBytes / (1024*1024))MiB reason=\(reason, privacy: .public)"
        )
        unloadDawg(reason: "soft threshold (\(reason))")
        reportCrash(
            tag: "oom_soft_threshold",
            message: buildReport(source: "proactive:\(reason)", resident: resident, virtual: 0, peak: peakResidentBytes)
        )
        return true
    }

    // MARK: - Recovery actions

    /// Idempotent DAWG unload. Logs and no-ops if the engine is not yet
    /// bound or the DAWG is already disabled. Never throws — a failure
    /// here would mask the original pressure event.
    private func unloadDawg(reason: String) {
        guard let engine = engine else {
            logger.debug("OOM: unloadDawg skipped — no engine (\(reason, privacy: .public))")
            return
        }
        if !engine.isDawgEnabled() {
            logger.debug("OOM: unloadDawg no-op — DAWG already disabled (\(reason, privacy: .public))")
            return
        }
        let before = engine.getMemoryUsage()
        engine.enableDawg(false)
        let after = engine.getMemoryUsage()
        let freed = before.dawgBytes - after.dawgBytes
        logger.info(
            "OOM: DAWG unloaded — reason=\(reason, privacy: .public) freed=\(freed / (1024*1024))MiB (dawg \(before.dawgBytes / (1024*1024))MiB -> \(after.dawgBytes / (1024*1024))MiB)"
        )
    }

    /// Flush caches. Currently a placeholder — wire to LexiconViewModel
    /// cache-flush hooks when those are added (mirror the Android
    /// OOMMonitor.registerCacheFlusher API).
    private func flushCaches(reason: String) {
        logger.debug("OOM: flushCaches (\(reason, privacy: .public)) — no flushers registered yet")
    }

    // MARK: - Background task expiration handling
    //
    // Per spec: "iOS background app suspension — milliseconds to respond
    // before killing." When the app moves to the background we request a
    // short UIApplication background task to allow the engine to flush +
    // the DAWG to be unloaded cleanly. If the system is about to expire
    // the task (typically ~30s on iOS, but can be much shorter on
    // subsequent launches) the expiration handler fires and we force-
    // unload the DAWG synchronously so the next suspension snapshot shows
    // minimal resident memory.
    //

    /// Begin a named background task to keep the app alive while we
    /// perform cleanup. Call from `applicationDidEnterBackground`.
    /// Idempotent — if a task is already outstanding, returns it.
    public func beginBackgroundTask() {
        // endBackgroundTask and the expiration handler are called on the
        // main thread, so we don't need the lock here — but we use it for
        // atomicity against the expiration handler which may fire on any
        // queue the system chooses (typically the main queue).
        lock.lock()
        if backgroundTask != .invalid {
            lock.unlock()
            logger.debug("OOM: beginBackgroundTask — task already outstanding")
            return
        }
        lock.unlock()

        let taskId = UIApplication.shared.beginBackgroundTask(withName: "LexiconOOM") { [weak self] in
            // Expiration — the system is about to suspend us. Force-shed.
            self?.handleBackgroundExpiration()
        }
        lock.lock()
        backgroundTask = taskId
        let id = taskId
        lock.unlock()
        if id != .invalid {
            logger.info("OOM: background task begun (id=\(id.rawValue))")
        } else {
            logger.warning("OOM: beginBackgroundTask returned .invalid — no grace window granted")
        }
    }

    /// End the outstanding background task. Safe to call when no task is
    /// outstanding (no-op).
    public func endBackgroundTask() {
        lock.lock()
        let taskToKill = backgroundTask
        backgroundTask = .invalid
        lock.unlock()
        guard taskToKill != .invalid else { return }
        UIApplication.shared.endBackgroundTask(taskToKill)
        logger.info("OOM: background task ended (id=\(taskToKill.rawValue))")
    }

    private func handleBackgroundExpiration() {
        logger.warning("OOM: background task expiring — force-unloading DAWG")
        unloadDawg(reason: "backgroundTaskExpiration")
        endBackgroundTask()
    }

    // MARK: - mach memory info

    /// Returns `(resident_size, virtual_size)` from `mach_task_basic_info`.
    ///
    /// `resident_size` is the count of physical pages currently resident
    /// for this process — this is the number jetsam uses to decide who to
    /// kill under memory pressure. `virtual_size` is the total virtual
    /// address space (mostly mmap'd files + guard pages) — logged for
    /// diagnostics, never used for thresholds.
    ///
    /// Returns `(0, 0)` if the mach call fails (which should never happen
    /// in practice — `task_info(mach_task_self_, …)` is always callable).
    private func machMemoryInfo() -> (resident: UInt64, virtual: UInt64) {
        var info = mach_task_basic_info()
        var count = mach_msg_type_number_t(
            MemoryLayout<mach_task_basic_info>.size / MemoryLayout<integer_t>.size
        )

        let kerr: kern_return_t = withUnsafeMutablePointer(to: &info) { ptr -> kern_return_t in
            ptr.withMemoryRebound(to: integer_t.self, capacity: Int(count)) { intPtr in
                task_info(
                    mach_task_self_,
                    task_flavor_t(MACH_TASK_BASIC_INFO),
                    intPtr,
                    &count
                )
            }
        }

        guard kerr == KERN_SUCCESS else {
            logger.error("OOM: task_info(MACH_TASK_BASIC_INFO) failed — kerr=\(kerr)")
            return (0, 0)
        }

        // `resident_size` is `integer_t` (Int32) in the mach header — a
        // historical quirk. For our threshold (180 MiB = 188,743,680) it
        // fits comfortably in Int32 (max ~2.1 billion). We widen to UInt64
        // for safe arithmetic against the threshold constants.
        // `virtual_size` is `mach_vm_size_t` (UInt on 64-bit) — already wide.
        let resident = UInt64(UInt64(bitPattern: Int64(info.resident_size)))
        let virtual = UInt64(info.virtual_size)
        return (resident, virtual)
    }

    /// Update `peakResident` if `resident` exceeds the current peak.
    /// Returns the new peak (== resident if resident > old peak, else old
    /// peak). Thread-safe via [lock].
    @discardableResult
    private func updatePeak(_ resident: UInt64) -> UInt64 {
        lock.lock(); defer { lock.unlock() }
        if resident > peakResident { peakResident = resident }
        return peakResident
    }

    // MARK: - Report building

    /// Construct a single-line OOM report suitable for crash reporters.
    /// Includes the source (memoryWarning / proactive:<reason> /
    /// backgroundTaskExpiration), resident + virtual + peak memory, the
    /// native engine's memory breakdown (if available), and a short
    /// stack-trace snippet for attribution.
    private func buildReport(source: String, resident: UInt64, virtual: UInt64, peak: UInt64) -> String {
        let native: MemoryInfo?
        if let engine = engine {
            native = engine.getMemoryUsage()
        } else {
            native = nil
        }
        let nativeStr: String
        if let n = native {
            nativeStr = "ht=\(n.hashTableBytes / (1024*1024))MiB " +
                "dawg=\(n.dawgBytes / (1024*1024))MiB " +
                "total=\(n.totalBytes / (1024*1024))MiB"
        } else {
            nativeStr = "native=n/a"
        }

        let stack = Thread.callStackSymbols
            .prefix(8)
            .joined(separator: " | ")

        return "OOM event: " +
            "source=\(source) " +
            "resident=\(resident / (1024*1024))MiB " +
            "virtual=\(virtual / (1024*1024))MiB " +
            "peak=\(peak / (1024*1024))MiB " +
            "budget=\(Self.hardBudgetBytes / (1024*1024))MiB " +
            "soft=\(Self.softThresholdBytes / (1024*1024))MiB " +
            "\(nativeStr) " +
            "stack=\(stack)"
    }

    /// Hand the report to the registered crash reporter. If no reporter
    /// is bound, falls back to os.Logger only.
    private func reportCrash(tag: String, message: String) {
        guard let reporter = crashReporter else {
            logger.warning("OOM: [\(tag, privacy: .public)] \(message, privacy: .public) (no crash reporter bound)")
            return
        }
        // The crash reporter sink may do its own I/O (e.g. Firebase
        // exception handler). We call it synchronously because the
        // memory-warning path is already on the main queue and we want
        // the report captured before any potential suspension.
        reporter(tag, message)
    }
}
