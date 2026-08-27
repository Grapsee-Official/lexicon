// OfflineModeManager.ios.kt — iOS `actual` implementations for the two
// `expect fun`s declared in OfflineModeManager.kt (commonMain).
//
// Per task P4-10:
//   - isOnline()        → NWPathMonitor (Network framework), cached path
//                          status == .satisfied.
//   - getNetworkType()  → NWPath.usesInterfaceType: WiFi → "wifi",
//                          Cellular → "cellular", anything else → "none".
//
// Why NWPathMonitor (not the deprecated SCNetworkReachability):
//   - `SCNetworkReachability` is sync-only, doesn't see captive portals,
//     and reports "reachable" even when no interface is actually up. Apple
//     has recommended NWPathMonitor since iOS 12.
//   - `NWPath.status == .satisfied` is the *post-captive-portal* verdict
//     (mirrors Android's `NET_CAPABILITY_VALIDATED`). Matches the Android
//     actual 1:1 for free.
//
// Architecture:
//   NWPathMonitor is *asynchronous* — the path updates arrive via a block
//   callback on a dispatch queue. Both `expect fun`s are *synchronous*, so
//   we can't just wait for the next update. Instead:
//     1. The host iOS app calls `startNetworkMonitor()` once at launch
//        (typically from `application(_:didFinishLaunchingWithOptions:)`).
//     2. The singleton [LexiconNetworkMonitor] creates an NWPathMonitor,
//        retains it for the app lifetime, and updates two `@Volatile var`s
//        on every path update.
//     3. `isOnline()` / `getNetworkType()` read those cached vars
//        synchronously — sub-microsecond, no allocation.
//   Before the first path update arrives (the very first few hundred ms of
//   app launch), both reads return their conservative defaults
//   (`false` / `"none"`). This matches the contract documented in the
//   expect file: "unknown" is treated the same as "offline".
//
// cinterop requirement (BLOCKS iOS COMPILATION UNTIL DONE — see Next
// Actions in worklog):
//   The `Network` framework is NOT part of Kotlin/Native's default
//   `platform.darwin` cinterop. A `.def` file must be added under
//   `src/nativeInterop/cinterop/network.def`:
//
//     language = Objective-C
//     modules  = Network
//     linkerOpts = -framework Network
//
//   and registered in build.gradle.kts for each iosX64 / iosArm64 /
//   iosSimulatorArm64 target:
//
//     compilations.main.cinterops.create("network") {
//         defFile = file("src/nativeInterop/cinterop/network.def")
//     }
//
//   This is the same pattern that already exists for the `lexicon` cinterop
//   (see nativeInterop/cinterop/lexicon.def); the build.gradle.kts currently
//   doesn't register that cinterop either (documented gap in P3-FIX-26),
//   so a single follow-up task can add BOTH registrations.
package com.lexicon.engine

import platform.Network.NWPath
import platform.Network.NWPathMonitor
import platform.Network.NWPathStatus
import platform.Network.NWInterfaceType
import platform.darwin.dispatch_get_global_queue
import platform.darwin.QOS_CLASS_UTILITY

// ---------------------------------------------------------------------------
// Network monitor singleton
// ---------------------------------------------------------------------------

/**
 * Process-global cache of the most-recent NWPath state.
 *
 * The `@Volatile` annotations guarantee that updates from the dispatch
 * queue thread are visible to reads on the calling thread without explicit
 * locking. Kotlin/Native's `@Volatile` is the direct equivalent of the
 * JVM `@Volatile` annotation and emits the same memory-barrier instructions
 * as `std::atomic`'s load/store with `memory_order_seq_cst` on ARM64.
 *
 * The singleton itself is initialised lazily on first access (Kotlin
 * object semantics) and lives for the process lifetime — exactly what we
 * want for an NWPathMonitor that should never be torn down.
 */
internal object LexiconNetworkMonitor {

    @Volatile
    private var cachedIsOnline: Boolean = false

    @Volatile
    private var cachedNetworkType: String = "none"

    /**
     * Strong reference to the underlying NWPathMonitor. We MUST hold this
     * for the app lifetime — if the monitor were deallocated, path updates
     * would stop arriving and the cached values would go stale silently.
     */
    @Volatile
    private var monitor: NWPathMonitor? = null

    /**
     * Idempotent: starts the monitor on a global utility queue if it isn't
     * already running. Safe to call from any thread — the `monitor` check
     * is best-effort; the worst case is that two callers race and create
     * two monitors, which is harmless (the second one just overwrites the
     * first reference; ARC will release the first when its last strong
     * reference goes away, which is the local `m` below).
     */
    fun start() {
        if (monitor != null) return
        val m = NWPathMonitor()
        // pathUpdateHandler is an ObjC `copy`-semantic block property. The
        // Kotlin/Native compiler emits a freezing-aware thunk that captures
        // the singleton `this` reference (frozen by default for `object`
        // singletons in the new memory model), so no explicit StableRef
        // dance is needed.
        m.pathUpdateHandler = { path ->
            updateFromPath(path)
        }
        // Background queue — never the main queue. NWPathMonitor callbacks
        // are fast but we still don't want to risk blocking first-frame
        // commit. QOS_CLASS_UTILITY = "long-running, user-initiated" QoS,
        // perfect for telemetry-style work.
        val queue = dispatch_get_global_queue(QOS_CLASS_UTILITY.toLong(), 0u)
        m.startOnQueue(queue)
        monitor = m
    }

    /**
     * Translates an NWPath into (online, type) and atomically publishes
     * both values. The two writes happen on the same dispatch-queue
     * callback, so they're naturally ordered with respect to each other;
     * readers on another thread might see a torn read across the two vars
     * in theory, but in practice the worst case is "online=true,
     * networkType=none" for one read, which the caller handles gracefully
     * (treats as "online but unknown transport" → conservative sync).
     */
    private fun updateFromPath(path: NWPath) {
        when (path.status) {
            NWPathStatus.NWPathStatusSatisfied -> {
                // Order matters: write networkType FIRST, then isOnline,
                // so a reader that sees isOnline=true is guaranteed to
                // also see the up-to-date networkType.
                cachedNetworkType = computeNetworkType(path)
                cachedIsOnline = true
            }
            else -> {
                // Includes .invalid, .unsatisfied, .requiresConnection.
                cachedIsOnline = false
                cachedNetworkType = "none"
            }
        }
    }

    /**
     * Returns the coarse transport classification for the path.
     *
     * `usesInterfaceType` returns true if the path's *current* default
     * route uses the given interface type. On dual-stack devices (e.g. an
     * iPhone on WiFi that also has cellular as a fallback), WiFi wins
     * because we check it first — matches the Android actual's behaviour.
     */
    private fun computeNetworkType(path: NWPath): String {
        return when {
            path.usesInterfaceType(NWInterfaceType.NWInterfaceTypeWifi) -> "wifi"
            path.usesInterfaceType(NWInterfaceType.NWInterfaceTypeCellular) -> "cellular"
            else -> "none"
        }
    }

    fun isOnline(): Boolean = cachedIsOnline
    fun networkType(): String = cachedNetworkType
}

/**
 * Entry point for the host iOS app. Call once at launch:
 *
 *   func application(_:didFinishLaunchingWithOptions:) -> Bool {
 *       LexiconNetworkMonitor_shared.startNetworkMonitor()
 *       ...
 *   }
 *
 * (The exact Kotlin-to-Swift bridge name depends on the framework export
 * configuration; the function is exposed as a top-level Kotlin function
 * `startNetworkMonitor()` from the Lexicon KMM framework.)
 *
 * Idempotent — safe to call multiple times across app launches.
 */
fun startNetworkMonitor() {
    LexiconNetworkMonitor.start()
}

// ---------------------------------------------------------------------------
// actual fun isOnline(): Boolean
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — returns the most-recent cached `NWPath.status` verdict.
 *
 * Before [startNetworkMonitor] has been called (or before the first path
 * update arrives, ~100–300ms after start), returns `false`. The caller
 * (DeltaSyncClient) treats this as "definitely offline" — same default
 * as the Android actual's "no Context registered" case.
 */
actual fun isOnline(): Boolean {
    return LexiconNetworkMonitor.isOnline()
}

// ---------------------------------------------------------------------------
// actual fun getNetworkType(): String
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — returns the most-recent cached transport classification.
 *
 * Returns `"none"` until the first path update arrives, matching the
 * expect contract's "unknown → none" guarantee.
 */
actual fun getNetworkType(): String {
    return LexiconNetworkMonitor.networkType()
}
