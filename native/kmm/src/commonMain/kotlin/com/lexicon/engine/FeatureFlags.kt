// FeatureFlags.kt — central gradual-rollout flag system for Lexicon.
//
// Per spec (P4-09, Week 23-24):
//   * Feature flag system for gradual rollout.
//   * Flags loaded from remote config (cloud) with local fallback.
//   * Can be overridden via debug menu for testing.
//   * Thread-safe read access.
//
// Resolution order (highest priority first):
//   1. Debug override   (set from the Debug Menu — session-only, never persisted)
//   2. Remote override  (fetched from the cloud remote-config endpoint defined
//                        in [BetaConfig.REMOTE_CONFIG_URL] and applied via
//                        [FeatureFlags.applyRemoteConfig])
//   3. Channel default  (derived from the active [BetaChannel] — see
//                        [BetaConfig.defaultFlagsFor])
//   4. Hard-coded base  ([FeatureFlag.default])
//
// Thread-safety contract
// -----------------------
// Reads are lock-free: a single `@Volatile` reference to an immutable
// [Snapshot] is swapped atomically on every write. Multiple threads can
// read concurrently without contention. Writes are guarded by an internal
// monitor so concurrent mutations serialise cleanly.
//
// Reads are suitable for hot paths (every `lookup` / `fuzzySearch` checks
// a flag) — the cost is one volatile read + one map lookup, ~5 ns.
//
// Remote config schema
// --------------------
// Expected JSON shape (server response body):
// ```json
// {
//   "version": 17,
//   "flags": {
//     "FUZZY_SEARCH_V2": true,
//     "CLOUD_SYNC": false,
//     "CRASH_REPORTING": true
//   },
//   "etag": "v3-2026-09-01"
// }
// ```
// Unknown keys are ignored (forward-compat with future flags). Missing
// keys fall back to the channel default (not the previous remote value)
// so a flag removed from the server response reverts to local policy,
// not stale state.

package com.lexicon.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Singleton façade over the feature-flag system. Every read goes through
 * a lock-free volatile field; every write goes through an internal monitor.
 *
 * Use [isEnabled] from hot paths. Use [state] from Compose / SwiftUI to
 * observe flag changes reactively.
 */
object FeatureFlags {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        // `coerceInputValues = true` would null-coerce bad types; we instead
        // silently drop them in the parser so a single bad flag value can't
        // blank the whole remote payload.
    }

    // ── Internal mutable state ──────────────────────────────────────────────

    private val lock = Any()

    /**
     * Immutable snapshot of every flag layer. Swapped wholesale on every
     * write so concurrent readers see a consistent picture (no torn read
     * between, say, the remoteOverrides and remoteConfigVersion fields).
     */
    private data class Snapshot(
        val channelDefaults: Map<String, Boolean>,
        val remoteOverrides: Map<String, Boolean>,
        val debugOverrides: Map<String, Boolean>,
        val remoteConfigVersion: Int,
        val remoteConfigEtag: String?,
    ) {
        /** Apply the resolution chain: debug > remote > channel > base-default. */
        fun resolved(): Map<String, Boolean> {
            // Start from channel defaults (already contains every defined flag
            // because BetaConfig.defaultFlagsFor iterates FeatureFlag.values()).
            val out: MutableMap<String, Boolean> =
                LinkedHashMap(channelDefaults.size + remoteOverrides.size + debugOverrides.size + 4)
            out.putAll(channelDefaults)
            for ((k, v) in remoteOverrides) out[k] = v
            for ((k, v) in debugOverrides) out[k] = v
            // Defensive: ensure every defined flag is present even if a future
            // caller constructs a Snapshot with an incomplete channelDefaults map.
            for (f in FeatureFlag.values()) out.putIfAbsent(f.key, f.default)
            return out
        }
    }

    @Volatile
    private var snapshot: Snapshot = Snapshot(
        channelDefaults = BetaConfig.defaultFlagsFor(BetaChannel.STABLE),
        remoteOverrides = emptyMap(),
        debugOverrides = emptyMap(),
        remoteConfigVersion = 0,
        remoteConfigEtag = null,
    )

    /**
     * Flattened, read-only view of the currently-resolved flags. Updated
     * in lock-step with [snapshot] on every write. Reads consult this field
     * directly (one volatile read) — never call [Snapshot.resolved] on the
     * hot path.
     */
    @Volatile
    private var resolvedCache: Map<String, Boolean> = snapshot.resolved()

    private val _state: MutableStateFlow<Map<String, Boolean>> =
        MutableStateFlow(resolvedCache)

    /**
     * Observable stream of resolved flag maps. Compose can `collectAsState()`
     * on this; iOS consumers can poll via a periodic refresh or wrap with
     * `ObservableObject` on the Swift side.
     */
    val state: StateFlow<Map<String, Boolean>> = _state.asStateFlow()

    // ── Public read API (lock-free) ─────────────────────────────────────────

    /** True iff [flag] is enabled after applying the full resolution chain. */
    fun isEnabled(flag: FeatureFlag): Boolean =
        resolvedCache[flag.key] ?: flag.default

    /** Same as [isEnabled] but takes a raw string key. Forward-compat with server-pushed unknown flags. */
    fun isEnabled(key: String): Boolean {
        val r = resolvedCache
        return r[key] ?: FeatureFlag.fromKey(key)?.default ?: false
    }

    /** Snapshot of all resolved flags. Safe to cache on the caller side. */
    fun snapshot(): Map<String, Boolean> = resolvedCache

    /** Version of the last successfully applied remote config (0 = none). */
    val remoteConfigVersion: Int get() = snapshot.remoteConfigVersion

    /** ETag of the last successfully applied remote config (null = none). */
    val remoteConfigEtag: String? get() = snapshot.remoteConfigEtag

    /** Number of debug overrides currently active (0 = running on server+channel policy). */
    val debugOverrideCount: Int get() = snapshot.debugOverrides.size

    // ── Public write API (serialized) ───────────────────────────────────────

    /** Switch the active [BetaChannel]. Recomputes channel defaults. */
    fun setChannel(channel: BetaChannel) {
        mutate { it.copy(channelDefaults = BetaConfig.defaultFlagsFor(channel)) }
    }

    /**
     * Apply a remote-config JSON document. Parses the body, replaces the
     * current remote overrides wholesale, bumps [remoteConfigVersion] and
     * records the etag. Returns the parsed `version` field (0 if absent).
     *
     * Throws [kotlinx.serialization.SerializationException] on malformed
     * JSON — the caller should catch and keep the previous overrides
     * (don't blank them on a parse failure).
     */
    fun applyRemoteConfig(body: String, etag: String? = null): Int {
        val parsed: JsonObject = json.parseToJsonElement(body).jsonObject
        val version: Int = parsed["version"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.toIntOrNull() ?: 0
        val flagsObj: JsonObject? = parsed["flags"]?.jsonObject
        val overrides: MutableMap<String, Boolean> = LinkedHashMap()
        if (flagsObj != null) {
            for ((key, value) in flagsObj) {
                val bv: Boolean? = (value as? JsonPrimitive)?.booleanOrNull
                if (bv != null) overrides[key] = bv
            }
        }
        applyRemoteOverrides(overrides, version, etag)
        return version
    }

    /**
     * Apply remote overrides directly (programmatic path — used when the
     * platform already decoded the JSON, e.g. via a typed kotlinx-serialization
     * wrapper, or in tests).
     */
    fun applyRemoteOverrides(
        overrides: Map<String, Boolean>,
        version: Int = 0,
        etag: String? = null,
    ) {
        mutate {
            it.copy(
                remoteOverrides = overrides.toMap(),
                remoteConfigVersion = version,
                remoteConfigEtag = etag,
            )
        }
    }

    /** Set a debug override (or clear it by passing null). Debug menu only. */
    fun setDebugOverride(flag: FeatureFlag, value: Boolean?) {
        mutate { s ->
            val next: MutableMap<String, Boolean> = LinkedHashMap(s.debugOverrides)
            if (value == null) next.remove(flag.key) else next[flag.key] = value
            s.copy(debugOverrides = next)
        }
    }

    /** Clear every debug override in one shot. */
    fun clearDebugOverrides() {
        mutate { s ->
            if (s.debugOverrides.isEmpty()) s
            else s.copy(debugOverrides = emptyMap())
        }
    }

    /** Clear remote overrides (e.g. on server 4xx). Keeps channel defaults. */
    fun clearRemoteOverrides() {
        mutate { s ->
            if (s.remoteOverrides.isEmpty() && s.remoteConfigVersion == 0) s
            else s.copy(
                remoteOverrides = emptyMap(),
                remoteConfigVersion = 0,
                remoteConfigEtag = null,
            )
        }
    }

    /** Reset everything to channel-only state. */
    fun reset() {
        mutate { s ->
            Snapshot(
                channelDefaults = s.channelDefaults,
                remoteOverrides = emptyMap(),
                debugOverrides = emptyMap(),
                remoteConfigVersion = 0,
                remoteConfigEtag = null,
            )
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    /**
     * Apply [transform] under the write lock, then publish the new snapshot
     * to both the volatile cache and the StateFlow. The StateFlow update is
     * done outside the lock so a synchronous collector that calls back into
     * [FeatureFlags] can't deadlock.
     */
    private inline fun mutate(transform: (Snapshot) -> Snapshot) {
        val newResolved: Map<String, Boolean>
        synchronized(lock) {
            val newSnapshot: Snapshot = transform(snapshot)
            snapshot = newSnapshot
            newResolved = newSnapshot.resolved()
            resolvedCache = newResolved
        }
        _state.value = newResolved
    }
}

/**
 * The catalogue of feature flags. Each entry carries:
 *  - [key]: stable string identifier used in remote-config JSON, debug-menu
 *    prefs, and analytics events.
 *  - [default]: the hard-coded base default (the value used when no override
 *    exists at any layer). Reflects "what we want for STABLE channel if the
 *    server is unreachable".
 *  - [title] / [description]: human-readable text shown in the Debug Menu.
 */
enum class FeatureFlag(
    val key: String,
    val default: Boolean,
    val title: String,
    val description: String,
) {
    FUZZY_SEARCH_V2(
        key = "FUZZY_SEARCH_V2",
        default = false,
        title = "Fuzzy search v2",
        description = "New Levenshtein algorithm with early-exit pruning and " +
            "prefix-bounded candidate generation. Off by default until parity " +
            "benchmarks pass on 1M lookups.",
    ),
    CLOUD_SYNC(
        key = "CLOUD_SYNC",
        default = false,
        title = "Cloud sync",
        description = "Enable delta sync with the Lexicon cloud server. " +
            "Requires a logged-in account; falls back to local-only if disabled.",
    ),
    CRASH_REPORTING(
        key = "CRASH_REPORTING",
        default = true,
        title = "Crash reporting",
        description = "Upload crash stack traces to the Lexicon crash endpoint. " +
            "PII is stripped before upload.",
    ),
    ANALYTICS(
        key = "ANALYTICS",
        default = false,
        title = "Anonymous analytics",
        description = "Aggregate, non-PII usage events (lookup count, session " +
            "length). Off by default; user must opt in from Settings.",
    ),
    DAWG_ENABLED_BY_DEFAULT(
        key = "DAWG_ENABLED_BY_DEFAULT",
        default = true,
        title = "DAWG on by default",
        description = "Load the Directed Acyclic Word Graph at engine init for " +
            "O(k) prefix search. Auto-disabled on devices below the RAM floor.",
    ),
    WATERCOLOR_ILLUSTRATIONS(
        key = "WATERCOLOR_ILLUSTRATIONS",
        default = true,
        title = "Watercolour illustrations",
        description = "Render the hand-painted hero / category / WotD artwork. " +
            "Auto-disabled on low-RAM devices (<3 GB) by the platform.",
    ),
    DARK_MODE(
        key = "DARK_MODE",
        default = true,
        title = "Dark mode",
        description = "Expose the in-app dark theme toggle. The toggle itself " +
            "is always present; this gates whether the dark palette is offered.",
    ),
    ;

    companion object {
        /** Look up a flag by its [key]. Null if unknown (forward-compat: server may push new keys). */
        fun fromKey(key: String): FeatureFlag? = values().firstOrNull { it.key == key }
    }
}

// ===========================================================================
// expect declarations — device-tier / capability probing
// ===========================================================================
//
// Phase 4 (P4-10): top-level `expect fun`s for device-tier gating. These
// are intentionally NOT members of the [FeatureFlags] object — they
// describe the *hardware*, not a user-tweakable flag, so they belong at
// the package level. The platform actuals live in:
//   androidMain/.../FeatureFlags.android.kt
//   iosMain/.../FeatureFlags.ios.kt
//
// The [FeatureFlag.DAWG_ENABLED_BY_DEFAULT] / [FeatureFlag.WATERCOLOR_ILLUSTRATIONS]
// flags both auto-disable on low-RAM devices; the host app consults
// [isLowRamDevice] (or [getTotalRamBytes] for finer-grained thresholds)
// at startup to apply those auto-disables.

/**
 * @return the device's marketing / hardware model string.
 *
 *   Android: `android.os.Build.MODEL` (e.g. "Pixel 8 Pro").
 *   iOS:     `UIDevice.currentDevice.model` (e.g. "iPhone").
 */
expect fun getDeviceModel(): String

/**
 * @return `true` if the device should be treated as "low-RAM" for the
 *         purpose of feature gating (DAWG preload disabled, smaller caches,
 *         fewer concurrent sync batches, …).
 *
 *   Android: `ActivityManager.isLowRamDevice` — the system's own gate
 *            (Android Go / <2GB devices, or `ro.config.low_ram=true`).
 *   iOS:     total physical RAM < 3 GiB (heuristic; there is no iOS
 *            equivalent of `isLowRamDevice`).
 */
expect fun isLowRamDevice(): Boolean

/**
 * @return the device's total physical RAM in bytes.
 *
 *   Android: `ActivityManager.MemoryInfo.totalMem` (matches `/proc/meminfo`
 *            `MemTotal`).
 *   iOS:     `sysctlbyname("hw.memsize")` (uint64_t).
 *
 * Returns `0L` if the value cannot be determined (no Context registered on
 * Android, or `sysctl` failure on iOS).
 */
expect fun getTotalRamBytes(): Long
