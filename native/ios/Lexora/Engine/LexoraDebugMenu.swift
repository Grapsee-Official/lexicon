//  LexoraDebugMenu.swift
//  Lexora
//
//  Per spec (P4-09, Week 23-24 — Beta testing, OOM monitoring, App Store /
//  Play Store submission prep):
//    * Hidden debug menu (activate by tapping version number 7 times —
//      like Android developer options).
//    * Toggle feature flags
//    * View engine memory usage
//    * Run benchmarks (1M lookups, show timing)
//    * Force sync / force crash / force OOM
//    * View crash logs
//    * Clear cache / reset database
//    * Only available in DEBUG builds (#if DEBUG)
//
//  Wiring (in MoreScreen.swift):
//  ```swift
//  @StateObject private var debugTap = LexoraDebugTapCounter()
//  // ... existing About row:
//  SettingsRow(icon: "info.circle", label: "About Lexora",
//              value: "v\(LexoraBetaConfig.versionName)",
//              action: { debugTap.onTap() })
//  // ... at the top of the body:
//  .lexoraDebugMenu(deps: .init(engine: engineOrNil,
//                               onForceSync: { /* DeltaSyncClient */ },
//                               onClearCache: { /* clear caches */ },
//                               onResetDatabase: { /* delete *.bin */ },
//                               crashLogProvider: LexoraDebugMenuDeps.defaultCrashLogProvider),
//                   isPresented: $debugTap.revealed)
//  ```
//
//  ---------------------------------------------------------------------------
//  KMM interop note
//  ---------------------------------------------------------------------------
//  The Kotlin common module `com.lexora.engine` defines `FeatureFlags`,
//  `FeatureFlag`, `BetaConfig`, and `BetaChannel`. When the KMM iOS framework
//  is linked, those types are visible to Swift and the mirrors below should
//  be replaced with `import <KMMFramework>` + the KMM types directly.
//  Until then, this file ships self-contained Swift mirrors so the debug
//  menu compiles standalone. The mirror API surface is identical to the
//  KMM one, so the swap is mechanical.
//  ---------------------------------------------------------------------------

#if DEBUG

import SwiftUI
import Foundation
import os.log

// MARK: - Feature flag mirrors (KMM common: FeatureFlag / FeatureFlags)

/// Mirror of `com.lexora.engine.FeatureFlag`.
public enum LexoraFeatureFlag: String, CaseIterable, Identifiable {
    case fuzzySearchV2 = "FUZZY_SEARCH_V2"
    case cloudSync = "CLOUD_SYNC"
    case crashReporting = "CRASH_REPORTING"
    case analytics = "ANALYTICS"
    case dawgEnabledByDefault = "DAWG_ENABLED_BY_DEFAULT"
    case watercolorIllustrations = "WATERCOLOR_ILLUSTRATIONS"
    case darkMode = "DARK_MODE"

    public var id: String { rawValue }

    /// Stable string identifier — matches the Kotlin `key` property and the
    /// remote-config JSON keys.
    public var key: String { rawValue }

    /// Hard-coded base default (STABLE channel, no remote config).
    public var defaultValue: Bool {
        switch self {
        case .fuzzySearchV2:              return false
        case .cloudSync:                  return false
        case .crashReporting:             return true
        case .analytics:                  return false
        case .dawgEnabledByDefault:       return true
        case .watercolorIllustrations:    return true
        case .darkMode:                   return true
        }
    }

    public var title: String {
        switch self {
        case .fuzzySearchV2:              return "Fuzzy search v2"
        case .cloudSync:                  return "Cloud sync"
        case .crashReporting:             return "Crash reporting"
        case .analytics:                  return "Anonymous analytics"
        case .dawgEnabledByDefault:       return "DAWG on by default"
        case .watercolorIllustrations:    return "Watercolour illustrations"
        case .darkMode:                   return "Dark mode"
        }
    }

    public var description: String {
        switch self {
        case .fuzzySearchV2:
            return "New Levenshtein algorithm with early-exit pruning. Off until parity benchmarks pass."
        case .cloudSync:
            return "Enable delta sync with the Lexora cloud server. Requires a logged-in account."
        case .crashReporting:
            return "Upload crash stack traces to the Lexora crash endpoint. PII stripped before upload."
        case .analytics:
            return "Aggregate, non-PII usage events. Off by default; user must opt in from Settings."
        case .dawgEnabledByDefault:
            return "Load the Directed Acyclic Word Graph at engine init for O(k) prefix search."
        case .watercolorIllustrations:
            return "Render the hand-painted hero / category / WotD artwork. Auto-disabled on low-RAM devices."
        case .darkMode:
            return "Expose the in-app dark theme toggle. The toggle itself is always present."
        }
    }
}

/// Mirror of `com.lexora.engine.BetaChannel`.
public enum LexoraBetaChannel: String, CaseIterable {
    case stable, beta, alpha
}

/// Mirror of `com.lexora.engine.BetaConfig`.
public enum LexoraBetaConfig {

    /// Monotonically increasing across all channels and platforms. CI-injected in production.
    public static let buildNumber: Int = 1

    /// Human-readable SemVer.
    public static let versionName: String = "1.0.0"

    /// Bumped whenever a flag is added, removed, or has its default changed.
    public static let featureFlagVersion: Int = 1

    /// Base URL of the Lexora remote-config endpoint.
    public static let remoteConfigURL: String =
        "https://config.lexora.app/api/v1/feature-flags"

    /// Active channel for this build. Overwritten at app startup from a
    /// build-config-derived value.
    public static var activeChannel: LexoraBetaChannel = .stable {
        didSet { LexoraFeatureFlagStore.shared.setChannel(activeChannel) }
    }

    /// True if the Debug Menu should be visible. On iOS, DEBUG builds are
    /// the only ones that ship the LexoraDebugMenu type at all (the whole
    /// file is wrapped in `#if DEBUG`), so this always returns true here
    /// when called — but we keep the policy logic for parity with Android.
    public static func isDebugMenuVisible(isDebugBuild: Bool) -> Bool {
        switch activeChannel {
        case .stable: return false
        case .beta:   return isDebugBuild
        case .alpha:  return isDebugBuild
        }
    }

    public static func isVerboseLogging() -> Bool {
        activeChannel == .alpha
    }

    /// Channel-derived flag defaults. STABLE = base; BETA = +analytics +cloudSync;
    /// ALPHA = +fuzzySearchV2 +cloudSync +analytics.
    public static func defaultFlags(for channel: LexoraBetaChannel) -> [String: Bool] {
        var base: [String: Bool] = [:]
        for f in LexoraFeatureFlag.allCases { base[f.key] = f.defaultValue }
        switch channel {
        case .stable:
            return base
        case .beta:
            base[LexoraFeatureFlag.analytics.key] = true
            base[LexoraFeatureFlag.cloudSync.key] = true
            return base
        case .alpha:
            base[LexoraFeatureFlag.fuzzySearchV2.key] = true
            base[LexoraFeatureFlag.cloudSync.key] = true
            base[LexoraFeatureFlag.analytics.key] = true
            return base
        }
    }
}

/// Mirror of `com.lexora.engine.FeatureFlags` — singleton flag store with
/// the same resolution chain (debug > remote > channel > base default) and
/// the same thread-safety contract (lock-free reads via volatile-ish
/// `DispatchQueue.sync`-free snapshot).
///
/// Implementation note: uses a `DispatchQueue` serial queue for writes
/// and an atomic-ish `@Published` snapshot for reads. SwiftUI `@Published`
/// is thread-safe for reads but emits on the main runloop; for hot-path
/// reads use `currentValue` (one dictionary lookup, ~10 ns).
public final class LexoraFeatureFlagStore: ObservableObject {

    public static let shared = LexoraFeatureFlagStore()

    private let writeQueue = DispatchQueue(label: "com.lexora.featureflags.write")
    private let logger = Logger(subsystem: "com.lexora", category: "FeatureFlags")

    /// Layers — mutated only on `writeQueue`.
    private var channelDefaults: [String: Bool] = LexoraBetaConfig.defaultFlags(for: .stable)
    private var remoteOverrides: [String: Bool] = [:]
    private var debugOverrides: [String: Bool] = [:]
    private var _remoteConfigVersion: Int = 0
    private var _remoteConfigEtag: String? = nil

    /// Flattened, read-only snapshot — published for SwiftUI reactivity.
    /// Reads consult this dictionary directly (one lookup, ~10 ns).
    @Published public private(set) var currentValue: [String: Bool] = LexoraBetaConfig.defaultFlags(for: .stable)

    private init() {}

    // MARK: - Reads

    public func isEnabled(_ flag: LexoraFeatureFlag) -> Bool {
        currentValue[flag.key] ?? flag.defaultValue
    }

    public func isEnabled(key: String) -> Bool {
        if let v = currentValue[key] { return v }
        return LexoraFeatureFlag(rawValue: key)?.defaultValue ?? false
    }

    public var remoteConfigVersion: Int {
        writeQueue.sync { _remoteConfigVersion }
    }

    public var remoteConfigEtag: String? {
        writeQueue.sync { _remoteConfigEtag }
    }

    public var debugOverrideCount: Int {
        writeQueue.sync { debugOverrides.count }
    }

    // MARK: - Writes

    public func setChannel(_ channel: LexoraBetaChannel) {
        mutate { layer in
            layer.channelDefaults = LexoraBetaConfig.defaultFlags(for: channel)
        }
    }

    /// Apply a remote-config JSON document. Throws on malformed JSON.
    @discardableResult
    public func applyRemoteConfig(body: String, etag: String? = nil) -> Int {
        guard let data = body.data(using: .utf8),
              let parsed = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            logger.error("Remote config: failed to parse JSON")
            return 0
        }
        let version = (parsed["version"] as? Int) ?? 0
        var overrides: [String: Bool] = [:]
        if let flags = parsed["flags"] as? [String: Any] {
            for (k, v) in flags {
                if let b = v as? Bool { overrides[k] = b }
            }
        }
        applyRemoteOverrides(overrides, version: version, etag: etag)
        return version
    }

    public func applyRemoteOverrides(_ overrides: [String: Bool], version: Int = 0, etag: String? = nil) {
        mutate { layer in
            layer.remoteOverrides = overrides
            layer._remoteConfigVersion = version
            layer._remoteConfigEtag = etag
        }
    }

    public func setDebugOverride(_ flag: LexoraFeatureFlag, value: Bool?) {
        mutate { layer in
            if let v = value { layer.debugOverrides[flag.key] = v }
            else { layer.debugOverrides.removeValue(forKey: flag.key) }
        }
    }

    public func clearDebugOverrides() {
        mutate { layer in
            layer.debugOverrides.removeAll()
        }
    }

    public func clearRemoteOverrides() {
        mutate { layer in
            layer.remoteOverrides.removeAll()
            layer._remoteConfigVersion = 0
            layer._remoteConfigEtag = nil
        }
    }

    public func reset() {
        mutate { layer in
            layer.remoteOverrides.removeAll()
            layer.debugOverrides.removeAll()
            layer._remoteConfigVersion = 0
            layer._remoteConfigEtag = nil
        }
    }

    // MARK: - Internal

    /// Mutable scratch layer so the mutate helper can update multiple fields
    /// atomically before publishing.
    private final class Layer {
        var channelDefaults: [String: Bool]
        var remoteOverrides: [String: Bool]
        var debugOverrides: [String: Bool]
        var _remoteConfigVersion: Int
        var _remoteConfigEtag: String?

        init(_ owner: LexoraFeatureFlagStore) {
            self.channelDefaults = owner.channelDefaults
            self.remoteOverrides = owner.remoteOverrides
            self.debugOverrides = owner.debugOverrides
            self._remoteConfigVersion = owner._remoteConfigVersion
            self._remoteConfigEtag = owner._remoteConfigEtag
        }

        func commit(to owner: LexoraFeatureFlagStore) {
            owner.channelDefaults = channelDefaults
            owner.remoteOverrides = remoteOverrides
            owner.debugOverrides = debugOverrides
            owner._remoteConfigVersion = _remoteConfigVersion
            owner._remoteConfigEtag = _remoteConfigEtag
        }
    }

    private func mutate(_ block: (Layer) -> Void) {
        let resolved: [String: Bool] = writeQueue.sync {
            let layer = Layer(self)
            block(layer)
            layer.commit(to: self)
            // Flatten: channel < remote < debug
            var resolved = self.channelDefaults
            for (k, v) in self.remoteOverrides { resolved[k] = v }
            for (k, v) in self.debugOverrides { resolved[k] = v }
            for f in LexoraFeatureFlag.allCases where resolved[f.key] == nil {
                resolved[f.key] = f.defaultValue
            }
            return resolved
        }
        DispatchQueue.main.async { [weak self] in
            self?.currentValue = resolved
        }
    }
}

// MARK: - 7-tap activation

/// State holder for the "tap version number 7 times to reveal debug menu"
/// gesture. Mirrors Android's "tap Build Number 7 times" developer-options
/// activation.
///
/// In release builds the whole `LexoraDebugMenu.swift` file is compiled out
/// via `#if DEBUG`, so this type doesn't exist there — the host app must
/// also wrap its usage in `#if DEBUG`.
public final class LexoraDebugTapCounter: ObservableObject {

    @Published public private(set) var taps: Int = 0
    @Published public private(set) var revealed: Bool = false

    private static let revealThreshold = 7
    private static let idleResetSeconds: TimeInterval = 3.0

    private var idleTimer: Timer?
    private let logger = Logger(subsystem: "com.lexora", category: "DebugMenu")

    public init() {}

    /// Call from any "About" / version-number tap. Returns the number of
    /// taps remaining before reveal (0 once revealed).
    @discardableResult
    public func onTap() -> Int {
        if revealed { return 0 }
        taps += 1
        resetIdleTimer()

        if taps >= Self.revealThreshold {
            revealed = true
            logger.info("Debug menu revealed after \(self.taps) taps")
            return 0
        }
        return Self.revealThreshold - taps
    }

    public func hide() {
        revealed = false
        taps = 0
        idleTimer?.invalidate()
        idleTimer = nil
    }

    private func resetIdleTimer() {
        idleTimer?.invalidate()
        idleTimer = Timer.scheduledTimer(withTimeInterval: Self.idleResetSeconds, repeats: false) { [weak self] _ in
            guard let self = self, !self.revealed else { return }
            self.taps = 0
        }
    }
}

// MARK: - Dependencies

/// Dependencies the `LexoraDebugMenu` view needs from the host app.
public struct LexoraDebugMenuDeps {
    public var engine: LexoraEngine?
    public var onForceSync: () -> Void
    public var onClearCache: () -> Void
    public var onResetDatabase: () -> Void
    public var crashLogProvider: () -> String

    public init(
        engine: LexoraEngine? = nil,
        onForceSync: @escaping () -> Void = {},
        onClearCache: @escaping () -> Void = {},
        onResetDatabase: @escaping () -> Void = {},
        crashLogProvider: @escaping () -> String = LexoraDebugMenuDeps.defaultCrashLogProvider
    ) {
        self.engine = engine
        self.onForceSync = onForceSync
        self.onClearCache = onClearCache
        self.onResetDatabase = onResetDatabase
        self.crashLogProvider = crashLogProvider
    }

    /// Reads all JSON crash-log files written by `LexoraCrashReporter` and
    /// concatenates them (newest first). Returns "(no crashes recorded)"
    /// if the directory is empty or missing.
    public static let defaultCrashLogProvider: () -> String = {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
            ?? URL(fileURLWithPath: NSTemporaryDirectory())
        let dir = docs.appendingPathComponent("crash_logs", isDirectory: true)
        guard let entries = try? FileManager.default.contentsOfDirectory(
            at: dir, includingPropertiesForKeys: [.contentModificationDateKey],
            options: [.skipsHiddenFiles]
        ) else {
            return "(no crashes recorded)"
        }
        let jsons = entries.filter { $0.pathExtension == "json" }
            .sorted { a, b in
                let da = (try? a.resourceValues(forKeys: [.contentModificationDateKey]).contentModificationDate) ?? .distantPast
                let db = (try? b.resourceValues(forKeys: [.contentModificationDateKey]).contentModificationDate) ?? .distantPast
                return da > db
            }
        if jsons.isEmpty { return "(no crashes recorded)" }
        return jsons.compactMap { try? String(contentsOf: $0, encoding: .utf8) }
            .joined(separator: "\n\n———\n\n")
    }
}

// MARK: - Benchmark result

public struct LexoraBenchmarkResult: CustomStringConvertible {
    public let iterations: Int
    public let elapsedMillis: Double
    public let perLookupNanos: Double
    public let hits: Int

    public var description: String {
        let perLookup = String(format: "%.0f", perLookupNanos)
        return "\(iterations) lookups in \(String(format: "%.1f", elapsedMillis))ms (\(perLookup) ns/lookup, \(hits) hits)"
    }
}

// MARK: - Debug menu view

public struct LexoraDebugMenu: View {

    let deps: LexoraDebugMenuDeps
    let onDismiss: () -> Void

    @Environment(\.colorScheme) private var scheme
    @ObservedObject private var flags = LexoraFeatureFlagStore.shared
    @State private var memoryInfo: MemoryInfo?
    @State private var oomStats: String = ""
    @State private var benchmarkResult: LexoraBenchmarkResult?
    @State private var benchmarkRunning: Bool = false
    @State private var crashLogExpanded: Bool = false
    @State private var crashLogText: String = ""
    @State private var confirmDialog: ConfirmDialog?
    @State private var toast: String?

    public init(deps: LexoraDebugMenuDeps, onDismiss: @escaping () -> Void) {
        self.deps = deps
        self.onDismiss = onDismiss
    }

    public var body: some View {
        NavigationStack {
            List {
                headerSection
                buildInfoSection
                featureFlagsSection
                engineMemorySection
                benchmarkSection
                syncSection
                crashTestingSection
                crashLogsSection
                dataSection
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Debug Menu")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { onDismiss() }
                }
            }
            // `.alert` requires us to drive both the title and the
            // presentation via separate bindings; we derive the
            // isPresented flag from whether `confirmDialog` is non-nil.
            .alert(
                confirmDialog?.title ?? "",
                isPresented: Binding(
                    get: { confirmDialog != nil },
                    set: { if !$0 { confirmDialog = nil } }
                ),
                presenting: confirmDialog
            ) { dialog in
                Button("Cancel", role: .cancel) { }
                Button(dialog.confirmLabel, role: .destructive) {
                    handleConfirm(dialog)
                }
            } message: { dialog in
                Text(dialog.message)
            }
            .overlay(alignment: .bottom) {
                if let toast = toast {
                    Text(toast)
                        .font(.footnote)
                        .padding(.horizontal, 16).padding(.vertical, 10)
                        .background(.ultraThinMaterial)
                        .clipShape(Capsule())
                        .padding(.bottom, 24)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .animation(.easeOut(duration: 0.2), value: toast)
            .animation(.easeOut(duration: 0.2), value: crashLogExpanded)
        }
    }

    // MARK: - Sections

    private var headerSection: some View {
        Section {
            VStack(alignment: .leading, spacing: 4) {
                Text("Lexora Debug Menu")
                    .font(.headline)
                Text("Channel: \(LexoraBetaConfig.activeChannel.rawValue.uppercased())  ·  Build #\(LexoraBetaConfig.buildNumber)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .padding(.vertical, 4)
        }
    }

    private var buildInfoSection: some View {
        Section("Build") {
            InfoRow(label: "Channel", value: LexoraBetaConfig.activeChannel.rawValue.uppercased())
            InfoRow(label: "Version name", value: LexoraBetaConfig.versionName)
            InfoRow(label: "Build number", value: "\(LexoraBetaConfig.buildNumber)")
            InfoRow(label: "Feature-flag schema", value: "\(LexoraBetaConfig.featureFlagVersion)")
            InfoRow(label: "Remote config v", value: "\(flags.remoteConfigVersion)")
            InfoRow(label: "Debug overrides", value: "\(flags.debugOverrideCount)")
        }
    }

    private var featureFlagsSection: some View {
        Section("Feature Flags") {
            ForEach(LexoraFeatureFlag.allCases) { flag in
                Toggle(isOn: Binding(
                    get: { flags.isEnabled(flag) },
                    set: { newVal in
                        // Setting to the default value clears the override,
                        // so the resolution falls back to channel/remote.
                        flags.setDebugOverride(flag, value: newVal == flag.defaultValue ? nil : newVal)
                    }
                )) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(flag.title).font(.body)
                        Text(flag.description).font(.caption).foregroundStyle(.secondary)
                        Text(flag.key).font(.caption2).monospaced().foregroundStyle(.tertiary)
                    }
                }
            }
            if flags.debugOverrideCount > 0 {
                Button(role: .destructive) {
                    flags.clearDebugOverrides()
                    showToast("Debug overrides cleared")
                } label: {
                    Text("Clear \(flags.debugOverrideCount) debug override(s)")
                }
            }
        }
    }

    private var engineMemorySection: some View {
        Section("Engine Memory") {
            if let engine = deps.engine {
                if let info = memoryInfo {
                    InfoRow(label: "Hash table", value: formatBytes(info.hashTableBytes))
                    InfoRow(label: "DAWG", value: formatBytes(info.dawgBytes))
                    InfoRow(label: "Total engine", value: formatBytes(info.totalBytes))
                    InfoRow(label: "DAWG enabled", value: engine.isDawgEnabled() ? "true" : "false")
                    if !oomStats.isEmpty {
                        InfoRow(label: "Resident (jetsam)", value: oomStats)
                    }
                } else {
                    HStack {
                        ProgressView()
                        Text("Sampling memory…")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .onAppear {
                        memoryInfo = engine.getMemoryUsage()
                        refreshOomStats()
                    }
                }
                Button {
                    memoryInfo = engine.getMemoryUsage()
                    refreshOomStats()
                } label: {
                    Label("Refresh", systemImage: "arrow.clockwise")
                }
            } else {
                Text("Engine not ready.")
                    .foregroundStyle(.secondary)
                    .font(.caption)
            }
        }
    }

    private var benchmarkSection: some View {
        Section("Benchmark") {
            if deps.engine == nil {
                Text("Engine not ready — cannot benchmark.")
                    .foregroundStyle(.secondary)
                    .font(.caption)
            } else {
                Button {
                    runBenchmark()
                } label: {
                    HStack {
                        Image(systemName: "play.fill")
                        Text(benchmarkRunning ? "Running 1M lookups…" : "Run 1M lookups")
                    }
                }
                .disabled(benchmarkRunning)
                if let r = benchmarkResult {
                    Text(r.description)
                        .font(.caption.monospaced())
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private var syncSection: some View {
        Section("Cloud Sync") {
            Button {
                deps.onForceSync()
                showToast("Force sync triggered")
            } label: {
                Label("Force sync now", systemImage: "arrow.triangle.2.circlepath")
            }
        }
    }

    private var crashTestingSection: some View {
        Section {
            Text("These actions crash the app on purpose. The crash reporter will capture the stack trace.")
                .font(.caption)
                .foregroundStyle(.secondary)
            Button(role: .destructive) {
                confirmDialog = .forceCrash
            } label: {
                Label("Force crash", systemImage: "exclamationmark.triangle")
            }
            Button(role: .destructive) {
                confirmDialog = .forceOom
            } label: {
                Label("Force OOM", systemImage: "exclamationmark.triangle.fill")
            }
        } header: {
            Text("Crash Testing")
        }
    }

    private var crashLogsSection: some View {
        Section("Crash Logs") {
            Button {
                if crashLogExpanded {
                    crashLogExpanded = false
                } else {
                    crashLogText = deps.crashLogProvider()
                    crashLogExpanded = true
                }
            } label: {
                Text(crashLogExpanded ? "Hide crash logs" : "View crash logs")
            }
            if crashLogExpanded {
                ScrollView {
                    Text(crashLogText.isEmpty ? "(no crashes recorded)" : crashLogText)
                        .font(.caption.monospaced())
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(8)
                }
                .frame(minHeight: 160, maxHeight: 240)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
    }

    private var dataSection: some View {
        Section("Data") {
            Button {
                confirmDialog = .clearCache
            } label: {
                Label("Clear cache", systemImage: "trash")
            }
            Button(role: .destructive) {
                confirmDialog = .resetDb
            } label: {
                Label("Reset database", systemImage: "trash.fill")
            }
        }
    }

    // MARK: - Actions

    private func handleConfirm(_ dialog: ConfirmDialog) {
        confirmDialog = nil
        switch dialog {
        case .forceCrash:
            // Defer one runloop so the alert can dismiss cleanly first.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                fatalError("Debug menu: forced crash for crash-reporter testing")
            }
        case .forceOom:
            DispatchQueue.global(qos: .userInitiated).async {
                forceOom()
            }
        case .clearCache:
            deps.onClearCache()
            showToast("Cache cleared")
        case .resetDb:
            deps.onResetDatabase()
            showToast("Database reset — reload engine")
        }
    }

    private func runBenchmark() {
        guard let engine = deps.engine, !benchmarkRunning else { return }
        benchmarkRunning = true
        DispatchQueue.global(qos: .userInitiated).async {
            // Warm-up so the first lookup doesn't pay for cold cache + JNI.
            for _ in 0..<1_000 { _ = engine.lookup("lexora") }
            let tStart = DispatchTime.now()
            var hits = 0
            let key = "lexora"
            for _ in 0..<1_000_000 {
                if engine.lookup(key) != nil { hits += 1 }
            }
            let elapsed = DispatchTime.now().uptimeNanoseconds - tStart.uptimeNanoseconds
            let result = LexoraBenchmarkResult(
                iterations: 1_000_000,
                elapsedMillis: Double(elapsed) / 1_000_000.0,
                perLookupNanos: Double(elapsed) / 1_000_000.0,
                hits: hits
            )
            DispatchQueue.main.async {
                benchmarkResult = result
                benchmarkRunning = false
            }
        }
    }

    /// Allocate 100MB chunks until the process is jetsammed or OOMs.
    private func forceOom() {
        var sink: [Data] = []
        while true {
            // 100MB chunk.
            if let chunk = Data(count: 100 * 1024 * 1024) {
                sink.append(chunk)
            } else {
                break
            }
        }
        // If we somehow get here without jetsam, force a crash.
        fatalError("Debug menu: forced OOM (allocated \(sink.count) chunks without jetsam)")
    }

    private func refreshOomStats() {
        // LexoraOOMMonitor is part of the same Lexora iOS module — read
        // the current resident + peak bytes from its public `stats` getter.
        // `stats` is always safe to call (returns zeros if the mach call
        // fails), so we don't need to guard for "not started" here.
        let stats = LexoraOOMMonitor.shared.stats
        oomStats = "\(formatBytes(stats.residentBytes)) (peak \(formatBytes(stats.peakResidentBytes)))"
    }

    private func showToast(_ message: String) {
        toast = message
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) { [self] in
            if toast == message { toast = nil }
        }
    }

    private func formatBytes(_ bytes: UInt64) -> String {
        if bytes < 1024 { return "\(bytes) B" }
        let kb = Double(bytes) / 1024
        if kb < 1024 { return String(format: "%.1f KB", kb) }
        let mb = kb / 1024
        if mb < 1024 { return String(format: "%.1f MB", mb) }
        return String(format: "%.2f GB", mb / 1024)
    }
}

// MARK: - Subviews

private struct InfoRow: View {
    let label: String
    let value: String
    var body: some View {
        HStack {
            Text(label).foregroundStyle(.secondary)
            Spacer()
            Text(value).monospaced()
        }
    }
}

// MARK: - Confirm dialog model

private enum ConfirmDialog: Identifiable {
    case forceCrash, forceOom, clearCache, resetDb

    var id: Self { self }

    var title: String {
        switch self {
        case .forceCrash: return "Force crash?"
        case .forceOom:   return "Force OOM?"
        case .clearCache: return "Clear cache?"
        case .resetDb:    return "Reset database?"
        }
    }

    var message: String {
        switch self {
        case .forceCrash:
            return "This will call fatalError(). The crash reporter should capture the stack trace. The app will close."
        case .forceOom:
            return "This will allocate ~100MB chunks until jetsam kills the process. The crash reporter should capture the OOM record."
        case .clearCache:
            return "Clears the in-memory hash cache and image cache. The on-disk dictionary is preserved."
        case .resetDb:
            return "Deletes the on-disk hash_table.bin and dawg.bin. The engine must be re-loaded before lookups work again."
        }
    }

    var confirmLabel: String {
        switch self {
        case .forceCrash: return "Crash"
        case .forceOom:   return "OOM"
        case .clearCache: return "Clear"
        case .resetDb:    return "Reset"
        }
    }
}

// MARK: - View modifier

public extension View {
    /// Attach the Lexora debug menu as a sheet. `isPresented` should be
    /// bound to `LexoraDebugTapCounter.revealed`.
    func lexoraDebugMenu(deps: LexoraDebugMenuDeps, isPresented: Binding<Bool>) -> some View {
        sheet(isPresented: isPresented) {
            LexoraDebugMenu(deps: deps, onDismiss: { isPresented.wrappedValue = false })
        }
    }
}

#endif // DEBUG
