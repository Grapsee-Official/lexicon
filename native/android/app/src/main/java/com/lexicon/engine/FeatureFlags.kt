package com.lexicon.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

enum class BetaChannel {
    STABLE,
    BETA,
    ALPHA,
}

object BetaConfig {
    const val BUILD_NUMBER: Int = 1
    const val VERSION_NAME: String = "1.0.0"
    const val FEATURE_FLAG_VERSION: Int = 1
    const val REMOTE_CONFIG_URL: String = "https://config.lexicon.app/api/v1/feature-flags"

    @Volatile
    private var activeChannel: BetaChannel = BetaChannel.BETA

    fun activeChannel(): BetaChannel = activeChannel

    fun setActiveChannel(channel: BetaChannel) {
        activeChannel = channel
        FeatureFlags.setChannel(channel)
    }

    fun isDebugMenuVisible(isDebugBuild: Boolean): Boolean = when (activeChannel) {
        BetaChannel.STABLE -> false
        BetaChannel.BETA, BetaChannel.ALPHA -> isDebugBuild
    }

    fun isVerboseLogging(): Boolean = activeChannel == BetaChannel.ALPHA

    fun isCrashReportingAutoEnabled(): Boolean = true

    fun defaultFlagsFor(channel: BetaChannel): Map<String, Boolean> {
        val base = FeatureFlag.values().associate { it.key to it.default }
        return when (channel) {
            BetaChannel.STABLE -> base
            BetaChannel.BETA -> base + mapOf(
                FeatureFlag.ANALYTICS.key to true,
                FeatureFlag.CLOUD_SYNC.key to true,
            )
            BetaChannel.ALPHA -> base + mapOf(
                FeatureFlag.FUZZY_SEARCH_V2.key to true,
                FeatureFlag.CLOUD_SYNC.key to true,
                FeatureFlag.ANALYTICS.key to true,
            )
        }
    }
}

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
        description = "Levenshtein automaton with early-exit pruning.",
    ),
    CLOUD_SYNC(
        key = "CLOUD_SYNC",
        default = false,
        title = "Cloud sync",
        description = "Enable delta sync with the Lexicon cloud server.",
    ),
    CRASH_REPORTING(
        key = "CRASH_REPORTING",
        default = true,
        title = "Crash reporting",
        description = "Upload crash stack traces to the Lexicon crash endpoint.",
    ),
    ANALYTICS(
        key = "ANALYTICS",
        default = false,
        title = "Anonymous analytics",
        description = "Aggregate, non-PII usage events.",
    ),
    DAWG_ENABLED_BY_DEFAULT(
        key = "DAWG_ENABLED_BY_DEFAULT",
        default = true,
        title = "DAWG on by default",
        description = "Load the Directed Acyclic Word Graph at engine init.",
    ),
    WATERCOLOR_ILLUSTRATIONS(
        key = "WATERCOLOR_ILLUSTRATIONS",
        default = true,
        title = "Watercolour illustrations",
        description = "Render hand-painted artwork.",
    ),
    DARK_MODE(
        key = "DARK_MODE",
        default = true,
        title = "Dark mode",
        description = "Expose the in-app Nocturnal Academia dark theme.",
    );

    companion object {
        fun fromKey(key: String): FeatureFlag? = values().firstOrNull { it.key == key }
    }
}

object FeatureFlags {
    private val lock = Any()

    private data class Snapshot(
        val channelDefaults: Map<String, Boolean>,
        val remoteOverrides: Map<String, Boolean>,
        val debugOverrides: Map<String, Boolean>,
        val remoteConfigVersion: Int,
        val remoteConfigEtag: String?,
    ) {
        fun resolved(): Map<String, Boolean> {
            val out = LinkedHashMap<String, Boolean>()
            out.putAll(channelDefaults)
            out.putAll(remoteOverrides)
            out.putAll(debugOverrides)
            for (f in FeatureFlag.values()) out.putIfAbsent(f.key, f.default)
            return out
        }
    }

    @Volatile
    private var snapshot = Snapshot(
        channelDefaults = BetaConfig.defaultFlagsFor(BetaChannel.BETA),
        remoteOverrides = emptyMap(),
        debugOverrides = emptyMap(),
        remoteConfigVersion = 0,
        remoteConfigEtag = null,
    )

    @Volatile
    private var resolvedCache: Map<String, Boolean> = snapshot.resolved()

    private val _state = MutableStateFlow(resolvedCache)
    val state: StateFlow<Map<String, Boolean>> = _state.asStateFlow()

    val remoteConfigVersion: Int get() = snapshot.remoteConfigVersion
    val remoteConfigEtag: String? get() = snapshot.remoteConfigEtag
    val debugOverrideCount: Int get() = snapshot.debugOverrides.size

    fun isEnabled(flag: FeatureFlag): Boolean = resolvedCache[flag.key] ?: flag.default

    fun setChannel(channel: BetaChannel) {
        mutate { it.copy(channelDefaults = BetaConfig.defaultFlagsFor(channel)) }
    }

    fun applyRemoteConfig(body: String, etag: String? = null): Int {
        val parsed = JSONObject(body)
        val version = parsed.optInt("version", 0)
        val flagsObj = parsed.optJSONObject("flags")
        val overrides = LinkedHashMap<String, Boolean>()
        if (flagsObj != null) {
            val keys = flagsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                if (flagsObj.has(k)) overrides[k] = flagsObj.optBoolean(k)
            }
        }
        mutate {
            it.copy(
                remoteOverrides = overrides,
                remoteConfigVersion = version,
                remoteConfigEtag = etag,
            )
        }
        return version
    }

    fun setDebugOverride(flag: FeatureFlag, value: Boolean?) {
        mutate { s ->
            val next = LinkedHashMap(s.debugOverrides)
            if (value == null) next.remove(flag.key) else next[flag.key] = value
            s.copy(debugOverrides = next)
        }
    }

    fun clearDebugOverrides() {
        mutate { it.copy(debugOverrides = emptyMap()) }
    }

    private inline fun mutate(transform: (Snapshot) -> Snapshot) {
        val newResolved: Map<String, Boolean>
        synchronized(lock) {
            val next = transform(snapshot)
            snapshot = next
            newResolved = next.resolved()
            resolvedCache = newResolved
        }
        _state.value = newResolved
    }
}

fun getDeviceModel(): String = Build.MODEL ?: "unknown"

fun isLowRamDevice(context: Context): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
    return am.isLowRamDevice
}
