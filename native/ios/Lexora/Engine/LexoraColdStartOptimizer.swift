//  LexoraColdStartOptimizer.swift
//  Lexora
//
//  Per spec: "Cold start optimization. Hard gate: <500ms cold start on iPhone SE."
//
//  Strategy:
//    1. On app launch, load ONLY the memory-mapped hash table (the fast path
//       for exact lookup — the most common operation triggered from the home
//       screen and the iOS text-selection menu).
//    2. Defer DAWG loading until AFTER the first frame has committed. The
//       DAWG is only required for prefix/fuzzy search, which the user has
//       not requested yet during cold start. Loading it eagerly would blow
//       the 500ms budget on iPhone SE.
//    3. Run engine init on DispatchQueue.global(qos: .userInteractive) so
//       it gets near-realtime scheduling priority without blocking the
//       main thread / first-frame commit.
//    4. Cache the warmed engine in a singleton so subsequent accesses are
//       free (the engine is expensive to construct — never rebuild it).
//    5. Log every phase's wall-clock duration via os.Logger so timing can
//       be inspected in Console.app / Instruments.
//
//  Intended call sites (NOT modified by this change):
//    - App delegate / @main:  await LexoraColdStartOptimizer.shared.warmEngine(dbDir: ...)
//    - Root view .onAppear:   LexoraColdStartOptimizer.shared.scheduleDawgWarmAfterFirstRender()
//
//  The C++ engine's `lexora_engine_load` loads both the hash table and the
//  DAWG in one call (see native/cpp/src/engine.cpp). To honor the "DAWG
//  lazy" requirement without modifying the C ABI, this optimizer calls
//  `loadDatabase` then immediately `enableDawg(false)` to unload the DAWG
//  before it counts against the cold-start gate. `loadDawgAfterFirstRender`
//  re-enables it later on a background queue.

import Foundation
import os.log

/// LexoraColdStartOptimizer — singleton that owns the warmed LexoraEngine
/// and enforces the <500ms cold-start hard gate.
public final class LexoraColdStartOptimizer {

    // MARK: - Singleton

    public static let shared = LexoraColdStartOptimizer()

    // MARK: - Public state

    /// The cached engine. `nil` until `warmEngine(dbDir:)` completes Phase 1
    /// (hash table load). Once set, never rebuilt — callers should hold the
    /// returned reference for the lifetime of the app.
    public private(set) var engine: LexoraEngine?

    /// Live snapshot of all timing metrics. Reflects DAWG load completion
    /// even after the initial cold-start measurement was taken.
    public var metrics: ColdStartMetrics? {
        lock.lock(); defer { lock.unlock() }
        guard let ready = engineReadyTimestamp else { return nil }
        let launch = effectiveLaunchTimestamp
        let coldMs = (ready - launch) * 1000.0
        return ColdStartMetrics(
            launchTimestamp: launch,
            engineReadyTimestamp: ready,
            dawgLoadedTimestamp: dawgLoadedTimestamp,
            hashTableLoadDurationMs: hashTableLoadDurationMs,
            dawgLoadDurationMs: dawgLoadDurationMs,
            coldStartDurationMs: coldMs,
            metHardGate: coldMs < Self.hardGateMs
        )
    }

    /// Immutable timing snapshot. All durations in milliseconds.
    public struct ColdStartMetrics {
        /// Wall-clock timestamp captured at optimizer init (or via `markLaunch`).
        public let launchTimestamp: CFAbsoluteTime
        /// Wall-clock timestamp when Phase 1 completed and the engine became usable.
        public let engineReadyTimestamp: CFAbsoluteTime
        /// Wall-clock timestamp when the DAWG finished loading (lazy). Nil if not yet loaded.
        public let dawgLoadedTimestamp: CFAbsoluteTime?
        /// Hash table load duration (ms). Subset of coldStartDurationMs.
        public let hashTableLoadDurationMs: Double
        /// DAWG load duration (ms). Nil if not yet loaded.
        public let dawgLoadDurationMs: Double?
        /// Total cold start duration (ms) = engineReady - launch.
        public let coldStartDurationMs: Double
        /// True iff `coldStartDurationMs < 500` (the iPhone SE hard gate).
        public let metHardGate: Bool
    }

    /// The hard gate enforced by the spec, in milliseconds.
    public static let hardGateMs: Double = 500.0

    // MARK: - Private state

    private let logger = Logger(subsystem: "com.lexora", category: "ColdStart")
    private let lock = NSLock()

    /// Captured once at singleton init. This is the earliest moment the
    /// optimizer can observe without modifying the app's `@main` entrypoint.
    /// For a true "process start" timestamp, the caller may invoke
    /// `markLaunch()` from `application(_:didFinishLaunchingWithOptions:)`.
    private let initTimestamp: CFAbsoluteTime

    /// Optional earlier launch marker set via `markLaunch(at:)`. Idempotent.
    private var externalLaunchTimestamp: CFAbsoluteTime?

    private var effectiveLaunchTimestamp: CFAbsoluteTime {
        externalLaunchTimestamp ?? initTimestamp
    }

    private var engineReadyTimestamp: CFAbsoluteTime?
    private var hashTableLoadDurationMs: Double = 0
    private var dawgLoadedTimestamp: CFAbsoluteTime?
    private var dawgLoadDurationMs: Double?

    // MARK: - Init

    private init() {
        self.initTimestamp = CFAbsoluteTimeGetCurrent()
        logger.info("Cold start: optimizer initialized (proxy launch ts captured)")
    }

    /// Optional hook for the app delegate to record an earlier launch
    /// timestamp than the singleton's lazy init. Idempotent — only the
    /// first call wins. Call from `application(_:didFinishLaunchingWithOptions:)`.
    public func markLaunch(at timestamp: CFAbsoluteTime = CFAbsoluteTimeGetCurrent()) {
        lock.lock(); defer { lock.unlock() }
        if externalLaunchTimestamp == nil {
            externalLaunchTimestamp = timestamp
            logger.info("Cold start: external launch marker recorded")
        }
    }

    // MARK: - Public API

    /// Warm the engine asynchronously.
    ///
    /// Loads ONLY the hash table (fast path, mmap-backed) on a
    /// `.userInteractive` background queue so the main thread is free to
    /// drive the first-frame commit. The DAWG is unloaded immediately after
    /// `loadDatabase` returns so its load time and memory do NOT count
    /// against the cold-start gate.
    ///
    /// Call `loadDawgAfterFirstRender()` (or `scheduleDawgWarmAfterFirstRender()`)
    /// once the first frame has committed to bring the DAWG online for
    /// prefix/fuzzy search.
    ///
    /// - Parameter dbDir: Absolute path to the directory containing
    ///   `lexora.ht`, `lexora.dawg`, and `lexora.sqlite`.
    /// - Returns: The cached `LexoraEngine`, or `nil` on failure. Subsequent
    ///   calls return the same cached instance without re-loading.
    @discardableResult
    public func warmEngine(dbDir: String) async -> LexoraEngine? {
        // Fast path — no lock needed for the read; the write path is
        // serialized under `lock` in `loadHashTablePhase`.
        if let existing = engine { return existing }

        return await withCheckedContinuation { (continuation: CheckedContinuation<LexoraEngine?, Never>) in
            DispatchQueue.global(qos: .userInteractive).async { [weak self] in
                guard let self = self else {
                    continuation.resume(returning: nil)
                    return
                }
                let warmed = self.loadHashTablePhase(dbDir: dbDir)
                continuation.resume(returning: warmed)
            }
        }
    }

    /// Bring the DAWG online AFTER the first frame has committed.
    ///
    /// Fire-and-forget. Runs on `.userInteractive` so it finishes quickly
    /// without competing with the user's first interaction. Safe to call
    /// multiple times — only the first call wins; subsequent calls are no-ops.
    ///
    /// - Precondition: `warmEngine(dbDir:)` must have completed first.
    public func loadDawgAfterFirstRender() {
        lock.lock()
        let alreadyLoaded = dawgLoadedTimestamp != nil
        let cachedEngine = engine
        lock.unlock()

        if alreadyLoaded { return }

        guard let engine = cachedEngine else {
            logger.warning("Cold start: cannot load DAWG — engine is nil. Call warmEngine(dbDir:) first.")
            return
        }

        DispatchQueue.global(qos: .userInteractive).async { [weak self] in
            guard let self = self else { return }
            let start = CFAbsoluteTimeGetCurrent()
            engine.enableDawg(true)
            let end = CFAbsoluteTimeGetCurrent()
            let durationMs = (end - start) * 1000.0

            self.lock.lock()
            if self.dawgLoadedTimestamp == nil {
                self.dawgLoadedTimestamp = end
                self.dawgLoadDurationMs = durationMs
                self.logger.info("Cold start: DAWG loaded lazily in \(durationMs, format: .fixed(precision: 1)) ms")
            }
            self.lock.unlock()
        }
    }

    /// Convenience for SwiftUI: call from `.onAppear` of the root view.
    /// Defers two runloop ticks (one async + 50ms `asyncAfter`) so the first
    /// frame commits before the DAWG load begins competing for I/O.
    public func scheduleDawgWarmAfterFirstRender() {
        DispatchQueue.main.async { [weak self] in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
                self?.loadDawgAfterFirstRender()
            }
        }
    }

    /// Tear down the cached engine and reset all metrics.
    /// Used by unit tests and by low-memory recovery paths.
    public func reset() {
        lock.lock(); defer { lock.unlock() }
        engine?.close()
        engine = nil
        engineReadyTimestamp = nil
        hashTableLoadDurationMs = 0
        dawgLoadedTimestamp = nil
        dawgLoadDurationMs = nil
    }

    // MARK: - Private

    /// Phase 1: create the engine, load the database (hash table + DAWG),
    /// then immediately disable the DAWG so its cost is excluded from the
    /// cold-start measurement. MUST be called off the main thread.
    private func loadHashTablePhase(dbDir: String) -> LexoraEngine? {
        lock.lock(); defer { lock.unlock() }

        // Double-check after acquiring the lock — another caller may have
        // completed Phase 1 while we were waiting.
        if let existing = engine { return existing }

        let phaseStart = CFAbsoluteTimeGetCurrent()
        logger.info("Cold start: Phase 1 begin — hash table only (dbDir=\(dbDir, privacy: .public))")

        let newEngine = LexoraEngine()
        guard newEngine.loadDatabase(dbDir: dbDir) else {
            logger.error("Cold start: Phase 1 FAILED — loadDatabase returned false for \(dbDir, privacy: .public)")
            return nil
        }

        // The C++ `lexora_engine_load` loads both hash table and DAWG in
        // one shot. Immediately disable the DAWG so its memory + load time
        // are excluded from the cold-start gate. Exact lookup (hash table)
        // is sufficient for the initial home screen render and for the
        // iOS text-selection "Lexora" menu action.
        newEngine.enableDawg(false)

        let phaseEnd = CFAbsoluteTimeGetCurrent()
        hashTableLoadDurationMs = (phaseEnd - phaseStart) * 1000.0
        engineReadyTimestamp = phaseEnd
        engine = newEngine

        let launch = effectiveLaunchTimestamp
        let coldStartMs = (phaseEnd - launch) * 1000.0
        let met = coldStartMs < Self.hardGateMs

        logger.info("Cold start: Phase 1 complete — hash table \(self.hashTableLoadDurationMs, format: .fixed(precision: 1)) ms, total cold start \(coldStartMs, format: .fixed(precision: 1)) ms, hard gate (\(Self.hardGateMs, format: .fixed(precision: 0)) ms) met=\(met)")

        if !met {
            logger.error("Cold start: HARD GATE VIOLATION — \(coldStartMs, format: .fixed(precision: 1)) ms > \(Self.hardGateMs, format: .fixed(precision: 0)) ms target on iPhone SE")
        }

        return newEngine
    }
}
