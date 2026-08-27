// OfflineModeManager.android.kt — Android `actual` implementations for the
// two `expect fun`s declared in OfflineModeManager.kt (commonMain).
//
// Per task P4-10:
//   - isOnline()        → ConnectivityManager.activeNetwork +
//                          NetworkCapabilities (NET_CAPABILITY_INTERNET +
//                          NET_CAPABILITY_VALIDATED).
//   - getNetworkType()  → transport classification:
//                          TRANSPORT_WIFI     → "wifi"
//                          TRANSPORT_CELLULAR → "cellular"
//                          anything else / unknown → "none"
//
// Why ConnectivityManager (not the deprecated NetworkInfo / "isConnected"):
//   - `getActiveNetworkInfo()` + `isConnectedOrConnecting()` were
//     deprecated in API 29. The recommended replacement is
//     `getNetworkCapabilities(activeNetwork)` + `hasCapability(...)`,
//     which (a) works on minSdk 21 (NetworkCapabilities was added in API 21)
//     and (b) gives us a *validated* internet path
//     (NET_CAPABILITY_VALIDATED) — the system actually probed it via a
//     captive-portal check. The old API reported "connected" even when the
//     device was behind a captive portal with no real internet.
//
// Context plumbing:
//   These are top-level `expect fun`s with no `Context` parameter, so the
//   Android app must register its `Application` Context once at startup via
//   `initLexiconAppContext(this)` (mirrors the `initLexiconDb(path)` pattern
//   in DeltaSyncClient.android.kt). A missing registration makes both
//   actuals return their "offline / none" defaults.
package com.lexicon.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// ---------------------------------------------------------------------------
// App-Context singleton — registered by the host Android app at startup.
// ---------------------------------------------------------------------------

/**
 * Internal holder for the application `Context`. The Android app must call
 * [initLexiconAppContext] once (from `Application.onCreate`) before any
 * network-state query, otherwise [isOnline] / [getNetworkType] /
 * [isLowRamDevice] / [getTotalRamBytes] all fall back to their "unknown /
 * offline / 0" defaults.
 *
 * Mirrors the `LexiconDbProvider` pattern in DeltaSyncClient.android.kt:
 * `@Volatile var` so reads from any thread see the latest value without
 * explicit synchronization, and a missing registration returns the safe
 * default instead of throwing `UninitializedPropertyAccessException`.
 */
internal object LexiconAppContextHolder {
    @Volatile
    var context: Context? = null
}

/**
 * Register the application `Context` used by the top-level platform actuals
 * (OfflineModeManager + FeatureFlags). Call from `Application.onCreate`:
 *
 *   initLexiconAppContext(this)
 *
 * Safe to call multiple times — the latest Context wins.
 */
fun initLexiconAppContext(context: Context) {
    LexiconAppContextHolder.context = context.applicationContext
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Looks up the [ConnectivityManager] system service and returns the
 * [NetworkCapabilities] for the current default network, or `null` if any
 * of the following are true:
 *   - No Context has been registered with [initLexiconAppContext].
 *   - The CONNECTIVITY_SERVICE system service is unavailable (very unusual;
 *     would indicate the app is running on a stripped / headless Android).
 *   - There is no active network (`activeNetwork == null`).
 *   - The active network has no capabilities (network went down between the
 *     `activeNetwork` read and the `getNetworkCapabilities` call — a TOCTOU
 *     race; we just treat it as "no network" and the caller retries next
 *     call).
 *
 * Centralised here so [isOnline] and [getNetworkType] share the exact same
 * lookup logic — they MUST agree on what "the current network" is.
 */
private fun currentNetworkCapabilities(): NetworkCapabilities? {
    val context = LexiconAppContextHolder.context ?: return null
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        as? ConnectivityManager ?: return null
    val network = cm.activeNetwork ?: return null
    return cm.getNetworkCapabilities(network)
}

// ---------------------------------------------------------------------------
// actual fun isOnline(): Boolean
// ---------------------------------------------------------------------------

/**
 * Android `actual` — a device is considered online iff the default network
 * has BOTH:
 *   - `NET_CAPABILITY_INTERNET`  — the system believes this network can
 *                                  route to the internet.
 *   - `NET_CAPABILITY_VALIDATED` — the system has actually confirmed
 *                                  reachability (captive-portal probe
 *                                  succeeded) on this network.
 *
 * `NET_CAPABILITY_VALIDATED` was added in API 23, but `hasCapability()`
 * itself gracefully returns `false` on API 21–22 networks (which never have
 * the validated flag set), so this code is safe under the project's
 * `minSdk = 21` floor without an explicit Build.VERSION check.
 *
 * If no Context has been registered, returns `false` — safer than crashing
 * a sync caller that runs before `Application.onCreate` finishes.
 */
actual fun isOnline(): Boolean {
    val caps = currentNetworkCapabilities() ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

// ---------------------------------------------------------------------------
// actual fun getNetworkType(): String
// ---------------------------------------------------------------------------

/**
 * Android `actual` — classifies the current default network's transport.
 *
 *   - `TRANSPORT_WIFI`      → "wifi"
 *   - `TRANSPORT_CELLULAR`  → "cellular"
 *   - anything else (Ethernet, Bluetooth, VPN-only, LoWPAN, …) → "none"
 *
 * The "none" bucket for non-WiFi/non-cellular is intentional: the sync
 * client only cares about the metered-vs-unmetered-vs-offline distinction,
 * and other transports are vanishingly rare on phones + don't map cleanly
 * onto the wifi/cellular backoff strategy. A future task could add
 * "ethernet" if Android-on-ChromeOS becomes a target.
 *
 * The `when` is ordered WiFi-first because on dual-stack devices (e.g. a
 * phone on WiFi calling via cellular backhaul) the OS reports WiFi as the
 * user-facing transport and that's the right answer for "should we sync
 * aggressively".
 */
actual fun getNetworkType(): String {
    val caps = currentNetworkCapabilities() ?: return "none"
    return when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
        else -> "none"
    }
}
