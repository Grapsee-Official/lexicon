//  LexiconFrameMonitor.swift
//  Lexicon
//
//  Per spec hard gates:
//    * "95th percentile frame render time <16ms (60fps) during list scrolling"
//      (Week 16)
//    * "Zero frame drops scrolling 1000 items on Moto G" (Week 16)
//
//  # How it works
//
//  [CADisplayLink] is a CoreAnimation timer object that fires once per
//  display refresh, synchronized to the vsync. When it fires, the
//  `timestamp` property is the time of the first frame the link fired for,
//  and `targetTimestamp` (iOS 10+) is the time of the last frame. The
//  **inter-frame interval** = `targetTimestamp(current) −
//  targetTimestamp(previous)` is the wall-clock duration allotted to the
//  previous frame's render pass. If that interval exceeds the per-screen
//  budget (default 16.67 ms @ 60 Hz), the frame is counted as **jank**.
//
//  This is the iOS equivalent of Android's [Choreographer.FrameCallback]
//  (see native/android/app/src/main/java/com/lexicon/engine/FrameMonitor.kt).
//  Both platforms report identical [ScreenStats] JSON shapes so the ETL
//  perf-collector can parse them with the same code path.
//
//  # Per-screen tracking
//
//  Call `startScreen(_:)` when a screen becomes visible (e.g. inside
//  `View.onAppear`) and `stopScreen(_:)` when it leaves the foreground.
//  Each screen maintains its own accumulator; `snapshot(_:)` reads the
//  live counters without stopping.
//
//  Multiple screens can be tracked concurrently (e.g. during a navigation
//  push transition where two screens are partially visible). The
//  CADisplayLink callback feeds every active screen — each screen sees
//  the same frame interval but attributes it to its own accumulator.
//
//  # JSON output
//
//  When `stopScreen(_:)` is called, a `frame_stats` event is emitted in
//  the same NDJSON format as the C++ `lexicon::PerfConfig` perf log:
//
//    {"ts": <epoch_ms>, "type": "frame_stats", "data": {
//      "screen": "<name>", "total_frames": N, "dropped_frames": M,
//      "jank_percent": <pct>, "budget_ms": 16.67,
//      "min_ms": <ms>, "max_ms": <ms>, "mean_ms": <ms>,
//      "p50_ms": <ms>, "p95_ms": <ms>, "p99_ms": <ms>
//    }}
//
//  The log path defaults to `<cachesDirectory>/lexicon_perf.log` (matching
//  the C++ side's intent of writing to the app's cache dir on iOS).
//  Override via `setLogPath(_:)` if you want to co-locate with the
//  engine's perf log.
//
//  # Verbosity
//
//  Verbose mode (set via `setVerbose(true)`) emits a per-jank `frame_jank`
//  event for every dropped frame. This is high-volume — use only during
//  profiling sessions, never in production.
//
//  # Overhead
//
//  Per-frame cost on the main thread:
//    * 1 CFTimeInterval subtraction (no clock read — CADisplayLink
//      provides the timestamp).
//    * 1 NSMapTable lookup per active screen (≈ 50 ns).
//    * 1 array push into a pre-sized ring buffer (amortized O(1)).
//    * A handful of Double arithmetic ops.
//
//  Total: < 1 μs per frame per active screen. At 60 fps with one active
//  screen that's < 60 μs/sec of main-thread overhead — well below the
//  16 ms frame budget.
//
//  # Wiring
//
//  ```swift
//  struct HomeScreen: View {
//      var body: some View {
//          ContentView()
//              .onAppear { LexiconFrameMonitor.shared.startScreen("home") }
//              .onDisappear { LexiconFrameMonitor.shared.stopScreen("home") }
//      }
//  }
//  ```
//
//  The monitor is a process-wide singleton — the CADisplayLink is installed
//  on `RunLoop.main` the first time a screen is started and torn down
//  when the last screen is stopped (to save battery).

import Foundation
import QuartzCore
import os.log

/// LexiconFrameMonitor — singleton that tracks per-screen frame render
/// times via CADisplayLink and enforces the spec's jank hard gates.
public final class LexiconFrameMonitor {

    // MARK: - Singleton

    public static let shared = LexiconFrameMonitor()

    // MARK: - Public data types

    /// Immutable snapshot of one screen's accumulated frame stats.
    public struct ScreenStats: Codable, Equatable {
        public let screenName: String
        public let totalFrames: Int
        public let droppedFrames: Int
        public let jankPercent: Double
        public let frameBudgetMs: Double
        public let minMs: Double
        public let maxMs: Double
        public let meanMs: Double
        public let p50Ms: Double
        public let p95Ms: Double
        public let p99Ms: Double

        /// True iff the spec's 95th-percentile gate is met:
        /// `p95Ms < frameBudgetMs`.
        public var meetsP95Gate: Bool { p95Ms < frameBudgetMs }

        /// True iff the spec's zero-drops gate is met:
        /// `droppedFrames == 0`.
        public var meetsZeroDropsGate: Bool { droppedFrames == 0 }

        private enum CodingKeys: String, CodingKey {
            case screenName = "screen"
            case totalFrames = "total_frames"
            case droppedFrames = "dropped_frames"
            case jankPercent = "jank_percent"
            case frameBudgetMs = "budget_ms"
            case minMs = "min_ms"
            case maxMs = "max_ms"
            case meanMs = "mean_ms"
            case p50Ms = "p50_ms"
            case p95Ms = "p95_ms"
            case p99Ms = "p99_ms"
        }
    }

    // MARK: - Public configuration

    /// The hard 60 fps frame budget (ms). Frames exceeding this are
    /// counted as drops. Override per-screen via `startScreen(_:frameBudgetMs:)`.
    public static let defaultFrameBudgetMs: Double = 16.67

    /// Override the JSON perf log path. Defaults to
    /// `<cachesDirectory>/lexicon_perf.log`. Pass nil to disable file
    /// logging (events still go to os.Logger).
    public func setLogPath(_ path: String?) {
        lock.lock(); defer { lock.unlock() }
        logPath = path
        logger.info("setLogPath('\(path ?? "<nil>")')")
    }

    /// Enable verbose per-jank logging. When true, every dropped frame
    /// emits a `frame_jank` JSON event (high volume — use only during
    /// profiling). Default false.
    public func setVerbose(_ enabled: Bool) {
        verbose = enabled
        logger.info("setVerbose(\(enabled))")
    }

    // MARK: - Public API — screen lifecycle

    /// Start accumulating frame stats for `screenName`. Idempotent: if
    /// the screen is already tracked, the existing accumulator is reset
    /// (so the new window starts fresh).
    ///
    /// - Parameters:
    ///   - screenName: logical screen label (e.g. "home", "dictionary").
    ///   - frameBudgetMs: per-frame budget. Frames whose render time
    ///     exceeds this are counted as drops. Default 16.67 ms (60 fps).
    ///     Use 8.33 for 120 Hz ProMotion displays, 11.11 for 90 Hz.
    public func startScreen(_ screenName: String,
                            frameBudgetMs: Double = LexiconFrameMonitor.defaultFrameBudgetMs) {
        lock.lock()
        if screens[screenName] == nil {
            screens[screenName] = ScreenAccumulator(screenName: screenName,
                                                     frameBudgetMs: frameBudgetMs)
        } else {
            screens[screenName]?.reset(frameBudgetMs: frameBudgetMs)
        }
        let activeCount = screens.count
        lock.unlock()

        ensureRunning()
        logger.debug("startScreen('\(screenName)') — budget=\(frameBudgetMs)ms, activeScreens=\(activeCount)")
    }

    /// Stop accumulating for `screenName`, emit a JSON `frame_stats`
    /// event to the perf log, and return the final stats. Returns nil
    /// if the screen wasn't being tracked (no-op).
    @discardableResult
    public func stopScreen(_ screenName: String) -> ScreenStats? {
        lock.lock()
        let acc = screens.removeValue(forKey: screenName)
        let remaining = screens.count
        lock.unlock()

        guard let acc = acc else {
            logger.warning("stopScreen('\(screenName)') — not tracked, no-op")
            return nil
        }
        let stats = acc.snapshot()
        emitFrameStats(stats)
        logger.debug("stopScreen('\(screenName)') — total=\(stats.totalFrames), " +
                     "dropped=\(stats.droppedFrames), jankPct=\(stats.jankPercent)%, " +
                     "p95=\(stats.p95Ms)ms")

        if remaining == 0 {
            stopRunning()
        }
        return stats
    }

    /// Read the current stats for `screenName` without stopping
    /// accumulation. Returns nil if the screen isn't tracked.
    public func snapshot(_ screenName: String) -> ScreenStats? {
        lock.lock(); defer { lock.unlock() }
        return screens[screenName]?.snapshot()
    }

    /// Snapshot every active screen. Returns a dictionary keyed by
    /// screen name. Useful for periodic background reporting.
    public func snapshotAll() -> [String: ScreenStats] {
        lock.lock(); defer { lock.unlock() }
        return screens.mapValues { $0.snapshot() }
    }

    /// Stop all screen accumulators, emit final reports, and tear down
    /// the CADisplayLink. After shutdown, `startScreen(_:)` can still
    /// be called (it will re-install the display link).
    public func shutdown() {
        logger.debug("shutdown() — stopping all screens")
        var toStop: [String] = []
        lock.lock()
        toStop = Array(screens.keys)
        lock.unlock()
        for name in toStop {
            _ = stopScreen(name)
        }
        stopRunning()
    }

    // MARK: - Private state

    private let logger = Logger(subsystem: "com.lexicon", category: "FrameMonitor")
    private let lock = NSLock()

    /// Per-screen accumulators. Modified under `lock`. The accumulator
    /// itself is thread-safe for `recordFrame` (main-thread only) and
    /// `snapshot` (any thread).
    private var screens: [String: ScreenAccumulator] = [:]

    /// The CADisplayLink — installed on RunLoop.main the first time a
    /// screen is started. Held as a strong reference so the run loop
    /// doesn't dealloc it (the run loop holds a weak ref by default).
    private var displayLink: CADisplayLink?

    /// Last targetTimestamp observed — used to compute the inter-frame
    /// interval. Written on the main thread (inside the display link
    /// callback). Marked volatile-equivalent via NSLock around the
    /// displayLink install/teardown.
    private var lastTargetTimestamp: CFTimeInterval = 0.0

    private var verbose: Bool = false
    private var logPath: String? = defaultLogPath()

    private init() {
        logger.info("LexiconFrameMonitor initialized — logPath=\(self.logPath ?? "<nil>")")
    }

    // MARK: - CADisplayLink lifecycle

    private func ensureRunning() {
        lock.lock()
        if displayLink != nil {
            lock.unlock()
            return
        }
        // Install the display link on the main run loop. The callback
        // fires once per vsync, on the main thread.
        let link = CADisplayLink(target: self, selector: #selector(onDisplayLink(_:)))
        // Add to .common modes so it fires during scrolling (which runs
        // in UITrackingRunLoopMode, a member of .common).
        link.add(to: .main, forMode: .common)
        displayLink = link
        lock.unlock()
        logger.debug("CADisplayLink installed on RunLoop.main (.common modes)")
    }

    private func stopRunning() {
        lock.lock()
        if let link = displayLink {
            link.invalidate()
            displayLink = nil
        }
        lastTargetTimestamp = 0.0
        lock.unlock()
        logger.debug("CADisplayLink invalidated (no active screens)")
    }

    /// CADisplayLink callback. Fires once per vsync on the main thread.
    @objc private func onDisplayLink(_ link: CADisplayLink) {
        // targetTimestamp is iOS 10+; fallback to timestamp on older
        // (we target iOS 13+ per the spec, but the guard is cheap).
        let ts: CFTimeInterval
        if #available(iOS 10.0, *) {
            ts = link.targetTimestamp
        } else {
            ts = link.timestamp
        }

        lock.lock()
        let prev = lastTargetTimestamp
        lastTargetTimestamp = ts
        let activeScreens = Array(screens.values)
        let isVerbose = verbose
        lock.unlock()

        if prev <= 0.0 {
            // First frame after install — no interval to compute.
            return
        }

        // Inter-frame interval in milliseconds.
        let renderMs = (ts - prev) * 1000.0

        // Feed every active screen.
        for acc in activeScreens {
            acc.recordFrame(renderMs: renderMs)
            if renderMs > acc.frameBudgetMs && isVerbose {
                emitFrameJank(screenName: acc.screenName,
                              renderMs: renderMs,
                              budgetMs: acc.frameBudgetMs)
            }
        }
    }

    // MARK: - JSON event emission

    private func emitFrameStats(_ stats: ScreenStats) {
        let data: [String: Any] = [
            "screen": stats.screenName,
            "total_frames": stats.totalFrames,
            "dropped_frames": stats.droppedFrames,
            "jank_percent": round4(stats.jankPercent),
            "budget_ms": round3(stats.frameBudgetMs),
            "min_ms": round3(stats.minMs),
            "max_ms": round3(stats.maxMs),
            "mean_ms": round3(stats.meanMs),
            "p50_ms": round3(stats.p50Ms),
            "p95_ms": round3(stats.p95Ms),
            "p99_ms": round3(stats.p99Ms),
        ]
        emitEvent(type: "frame_stats", data: data)
    }

    private func emitFrameJank(screenName: String, renderMs: Double, budgetMs: Double) {
        let data: [String: Any] = [
            "screen": screenName,
            "render_ms": round3(renderMs),
            "budget_ms": round3(budgetMs),
        ]
        emitEvent(type: "frame_jank", data: data)
    }

    /// Emit one NDJSON event. Always logs to os.Logger (for live
    /// debugging via Console.app / Instruments). Optionally appends to
    /// the perf log file (for ETL collection).
    private func emitEvent(type: String, data: [String: Any]) {
        let event: [String: Any] = [
            "ts": Int64(Date().timeIntervalSince1970 * 1000.0),
            "type": type,
            "data": data,
        ]
        // Serialize to JSON.
        let line: String
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: event,
                                                       options: [.sortedKeys])
            guard let str = String(data: jsonData, encoding: .utf8) else {
                logger.warning("emitEvent('\(type)') — JSON serialization produced non-UTF8")
                return
            }
            line = str
        } catch {
            logger.warning("emitEvent('\(type)') — JSON serialization failed: \(error.localizedDescription)")
            return
        }

        // Always log to os.Logger (cheap, discoverable via Console.app).
        logger.debug("\(line, privacy: .public)")

        // Optionally append to the perf log file.
        guard let path = logPath else { return }
        DispatchQueue.global(qos: .utility).async { [weak self] in
            guard let self = self else { return }
            do {
                let url = URL(fileURLWithPath: path)
                // Try to create the parent directory if missing (first
                // call on a fresh install).
                try? FileManager.default.createDirectory(
                    at: url.deletingLastPathComponent(),
                    withIntermediateDirectories: true)
                let lineWithNewline = line + "\n"
                // append mode — creates the file if it doesn't exist.
                if let handle = try? FileHandle(forWritingTo: url) {
                    defer { try? handle.close() }
                    _ = try? handle.seekToEnd()
                    if let data = lineWithNewline.data(using: .utf8) {
                        try? handle.write(contentsOf: data)
                    }
                } else {
                    // File doesn't exist yet — write atomically.
                    try lineWithNewline.write(to: url, atomically: true,
                                              encoding: .utf8)
                }
            } catch {
                self.logger.warning("emitEvent('\(type)') — failed to append to '\(path)': \(error.localizedDescription, privacy: .public)")
            }
        }
    }

    // MARK: - Per-screen accumulator

    /// Per-screen accumulator. `recordFrame` is called ONLY on the main
    /// thread (inside the CADisplayLink callback). `snapshot` may be
    /// called from any thread. Both acquire `bufferLock` — the lock is
    /// per-screen and uncontended in practice (snapshot is rare relative
    /// to frame rate), so this is the simplest correct synchronization.
    /// Using the same lock for both writes and reads guarantees a
    /// happens-before relationship: a snapshot either sees a fully
    /// recorded frame or doesn't see it at all.
    private final class ScreenAccumulator {

        let screenName: String
        var frameBudgetMs: Double

        private var totalFrames: Int = 0
        private var droppedFrames: Int = 0

        // Ring buffer for percentile computation. Power-of-two for cheap
        // modulo. Capacity = 4096 frames ≈ 68 seconds @ 60 fps.
        private static let bufferCapacity: Int = 4096
        private var buffer: [Double] = Array(repeating: 0.0,
                                              count: bufferCapacity)
        private var bufferHead: Int = 0
        private var bufferFull: Bool = false

        /// Lock guarding totalFrames / droppedFrames / buffer / bufferHead /
        /// bufferFull. Held by both recordFrame (main thread) and snapshot
        /// (any thread).
        private let bufferLock = NSLock()

        init(screenName: String, frameBudgetMs: Double) {
            self.screenName = screenName
            self.frameBudgetMs = frameBudgetMs
        }

        func reset(frameBudgetMs: Double) {
            self.frameBudgetMs = frameBudgetMs
            bufferLock.lock()
            totalFrames = 0
            droppedFrames = 0
            bufferHead = 0
            bufferFull = false
            bufferLock.unlock()
        }

        /// Main-thread only. Updates the ring buffer + counters under
        /// `bufferLock` so that a concurrent `snapshot` sees a consistent
        /// view (either the pre-frame or post-frame state, never torn).
        func recordFrame(renderMs: Double) {
            bufferLock.lock(); defer { bufferLock.unlock() }
            totalFrames &+= 1
            if renderMs > frameBudgetMs {
                droppedFrames &+= 1
            }
            buffer[bufferHead] = renderMs
            bufferHead = (bufferHead &+ 1) & (ScreenAccumulator.bufferCapacity - 1)
            if bufferHead == 0 {
                bufferFull = true
            }
        }

        func snapshot() -> ScreenStats {
            bufferLock.lock(); defer { bufferLock.unlock() }
            let total = totalFrames
            let dropped = droppedFrames
            if total == 0 {
                return ScreenStats(
                    screenName: screenName,
                    totalFrames: 0,
                    droppedFrames: 0,
                    jankPercent: 0.0,
                    frameBudgetMs: frameBudgetMs,
                    minMs: 0.0, maxMs: 0.0, meanMs: 0.0,
                    p50Ms: 0.0, p95Ms: 0.0, p99Ms: 0.0)
            }
            // Copy the ring buffer out.
            let size = bufferFull ? ScreenAccumulator.bufferCapacity : bufferHead
            var samples = [Double](); samples.reserveCapacity(size)
            if bufferFull {
                // Wrap-around: reconstruct in-order.
                let tailLen = ScreenAccumulator.bufferCapacity - bufferHead
                samples.append(contentsOf: buffer[bufferHead..<(bufferHead + tailLen)])
                samples.append(contentsOf: buffer[0..<bufferHead])
            } else {
                samples.append(contentsOf: buffer[0..<bufferHead])
            }
            samples.sort()
            let minMs = samples.first ?? 0.0
            let maxMs = samples.last ?? 0.0
            let meanMs = samples.reduce(0.0, +) / Double(samples.count)
            let p50 = Self.percentile(sorted: samples, p: 50.0)
            let p95 = Self.percentile(sorted: samples, p: 95.0)
            let p99 = Self.percentile(sorted: samples, p: 99.0)
            let jankPct = Double(dropped) / Double(total) * 100.0
            return ScreenStats(
                screenName: screenName,
                totalFrames: total,
                droppedFrames: dropped,
                jankPercent: jankPct,
                frameBudgetMs: frameBudgetMs,
                minMs: minMs,
                maxMs: maxMs,
                meanMs: meanMs,
                p50Ms: p50,
                p95Ms: p95,
                p99Ms: p99)
        }

        /// Nearest-rank percentile. `sorted` MUST be sorted ascending.
        /// p in [0, 100]. rank = ceil(p/100 * n) - 1, clamped to [0, n-1].
        private static func percentile(sorted: [Double], p: Double) -> Double {
            guard !sorted.isEmpty else { return 0.0 }
            let n = sorted.count
            var rank = Int(ceil(p / 100.0 * Double(n)))
            if rank == 0 { rank = 1 }
            rank -= 1
            if rank >= n { rank = n - 1 }
            if rank < 0 { rank = 0 }
            return sorted[rank]
        }
    }

    // MARK: - Numeric formatting helpers

    private func round3(_ v: Double) -> Double {
        return (v * 1000.0).rounded() / 1000.0
    }

    private func round4(_ v: Double) -> Double {
        return (v * 10000.0).rounded() / 10000.0
    }

    /// Default perf log path: `<Caches>/lexicon_perf.log` (per Apple's
    /// file system guidelines, cache is the right place for diagnostic
    /// logs that can be deleted to reclaim space).
    private static func defaultLogPath() -> String? {
        let urls = FileManager.default.urls(for: .cachesDirectory,
                                            in: .userDomainMask)
        guard let cachesURL = urls.first else { return nil }
        return cachesURL.appendingPathComponent("lexicon_perf.log").path
    }

    // MARK: - Test-only hooks

    /// Test-only: reset the singleton state. Call from tearDown() in
    /// XCTest cases to ensure a clean state between tests. Not for
    /// production use.
    internal static func resetForTesting() {
        shared.shutdown()
    }
}
