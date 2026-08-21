// BetaConfig.kt — release-channel configuration for Lexora.
//
// Per spec (P4-09, Week 23-24 — Beta testing, OOM monitoring, App Store /
// Play Store submission prep):
//
//   STABLE  — all features stable, no debug menu, conservative flag defaults.
//   BETA    — feature flags enabled for gradual rollout, debug menu visible.
//   ALPHA   — all features on, verbose logging, debug menu visible.
//
// The active channel is chosen at build time via a Gradle / Xcode build
// config field so the same source compiles for all three channels. The
// remote-config URL is the same across channels; the server selects the
// payload based on the `channel` query parameter.

package com.lexora.engine

/**
 * Singleton holding the build's release-channel identity and the policy
 * derived from it. The active channel drives:
 *   - which flag defaults [FeatureFlags] uses as its base layer,
 *   - whether the Debug Menu is visible at all,
 *   - whether verbose (engine + sync) logging is emitted.
 *
 * Version tracking:
 *   - [BUILD_NUMBER]: monotonically increasing integer across all channels
 *     and platforms. Used as the `build` query parameter on remote-config
 *     fetches so the server can serve build-specific payloads.
 *   - [VERSION_NAME]: human-readable SemVer, shown in the Debug Menu and
 *     the About row.
 *   - [FEATURE_FLAG_VERSION]: bumped whenever a flag is added, removed, or
 *     has its default changed. Sent as `ffv` so the server can serve a
 *     schema-appropriate payload.
 */
object BetaConfig {

    /**
     * Build number — monotonically increasing across all channels and
     * platforms. In production this is injected from CI (e.g. GitHub
     * Actions run number) via a Gradle `buildConfigField` and an Xcode
     * `INFOPLIST_KEY_CFBundleVersion` override.
     */
    const val BUILD_NUMBER: Int = 1

    /** Human-readable version name (SemVer). */
    const val VERSION_NAME: String = "1.0.0"

    /**
     * Version of the feature-flag schema. Bumped whenever a flag is added,
     * removed, or has its default changed. Sent with every remote-config
     * request so the server can serve a schema-appropriate payload.
     */
    const val FEATURE_FLAG_VERSION: Int = 1

    /**
     * Base URL of the Lexora remote-config endpoint. The full URL for a
     * given build is:
     * ```
     * {REMOTE_CONFIG_URL}?channel={channel}&ffv={ffv}&build={build}&platform={android|ios}
     * ```
     * The server returns the JSON shape documented in [FeatureFlags].
     */
    const val REMOTE_CONFIG_URL: String =
        "https://config.lexora.app/api/v1/feature-flags"

    // ── Channel state ───────────────────────────────────────────────────────

    /**
     * Active channel for this build. Defaults to STABLE; overwritten once
     * at app startup from the build-config-derived value via
     * [setActiveChannel].
     */
    @Volatile
    private var activeChannel: BetaChannel = BetaChannel.STABLE

    /** The active channel for this build. */
    fun activeChannel(): BetaChannel = activeChannel

    /**
     * Set the active channel. Called once at app startup from a
     * build-config-derived value. Propagates the channel-derived flag
     * defaults into [FeatureFlags].
     */
    fun setActiveChannel(channel: BetaChannel) {
        activeChannel = channel
        FeatureFlags.setChannel(channel)
    }

    // ── Channel policy ──────────────────────────────────────────────────────

    /**
     * True if the Debug Menu should be visible in this build.
     *
     * STABLE  → never.
     * BETA    → only when compiled as a debug build (TestFlight internal
     *           testers get debug builds; external testers don't).
     * ALPHA   → only when compiled as a debug build.
     *
     * The [isDebugBuild] parameter is `BuildConfig.DEBUG` on Android and
     * `#if DEBUG` on iOS (translated to a Boolean by the platform shim).
     */
    fun isDebugMenuVisible(isDebugBuild: Boolean): Boolean = when (activeChannel) {
        BetaChannel.STABLE -> false
        BetaChannel.BETA -> isDebugBuild
        BetaChannel.ALPHA -> isDebugBuild
    }

    /** True if verbose logging (engine internals, sync trace) should be emitted. */
    fun isVerboseLogging(): Boolean = when (activeChannel) {
        BetaChannel.STABLE -> false
        BetaChannel.BETA -> false
        BetaChannel.ALPHA -> true
    }

    /**
     * True if crash reporting should auto-enable without explicit opt-in.
     * Always true on every channel — crash reporting is the safety net
     * we never disable, even on STABLE.
     */
    fun isCrashReportingAutoEnabled(): Boolean = true

    /**
     * Channel-derived flag defaults. These take precedence over the
     * per-flag [FeatureFlag.default] when the channel is BETA or ALPHA,
     * but are themselves overridden by remote and debug overrides.
     *
     * STABLE: every flag uses its [FeatureFlag.default].
     * BETA:   ANALYTICS + CLOUD_SYNC turned on (gradual rollout of
     *         instrumentation + the new sync path).
     * ALPHA:  every "experimental" flag turned on (FUZZY_SEARCH_V2,
     *         CLOUD_SYNC, ANALYTICS). CRASH_REPORTING stays on.
     */
    fun defaultFlagsFor(channel: BetaChannel): Map<String, Boolean> {
        // Base layer = each flag's hard-coded default. Always present so the
        // resolved snapshot is total (every defined flag has a value).
        val base: Map<String, Boolean> =
            FeatureFlag.values().associate { it.key to it.default }
        return when (channel) {
            BetaChannel.STABLE ->
                base
            BetaChannel.BETA ->
                base + mapOf(
                    FeatureFlag.ANALYTICS.key to true,
                    FeatureFlag.CLOUD_SYNC.key to true,
                )
            BetaChannel.ALPHA ->
                base + mapOf(
                    FeatureFlag.FUZZY_SEARCH_V2.key to true,
                    FeatureFlag.CLOUD_SYNC.key to true,
                    FeatureFlag.ANALYTICS.key to true,
                )
        }
    }

    /**
     * Build the full remote-config fetch URL for this build. The platform
     * HTTP layer (DeltaSyncClient's HttpClient) appends an `If-None-Match`
     * header from [FeatureFlags.remoteConfigEtag] to enable 304 short-circuit.
     */
    fun remoteConfigFetchUrl(platform: String): String =
        "$REMOTE_CONFIG_URL" +
            "?channel=${activeChannel.name.lowercase()}" +
            "&ffv=$FEATURE_FLAG_VERSION" +
            "&build=$BUILD_NUMBER" +
            "&platform=$platform"
}

/**
 * The three release channels for Lexora.
 *
 * The numeric `ordinal` is stable across versions (do not reorder); it is
 * sent to the remote-config server as the `channel` query parameter and
 * used in analytics events.
 */
enum class BetaChannel {
    /** Production release — App Store / Play Store. Conservative defaults, no debug menu. */
    STABLE,

    /** TestFlight / Internal testing on Play Console. Flag rollout, debug menu visible (debug builds only). */
    BETA,

    /** Internal QA / dogfood. All experimental flags on, verbose logging, debug menu visible. */
    ALPHA,
}
