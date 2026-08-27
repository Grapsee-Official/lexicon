// FeatureFlags.android.kt — Android `actual` implementations for the three
// `expect fun`s declared in FeatureFlags.kt (commonMain).
//
// Per task P4-10:
//   - getDeviceModel()    → android.os.Build.MODEL
//   - isLowRamDevice()    → ActivityManager.isLowRamDevice
//   - getTotalRamBytes()  → ActivityManager.MemoryInfo.totalMem
//
// Why these APIs:
//   - `Build.MODEL` is a static final String populated from the
//     `ro.product.model` system property at process start. Zero allocation,
//     zero permissions, zero IPC — safe to call from any thread, including
//     from the uncaught-exception handler.
//   - `ActivityManager.isLowRamDevice` is the *system's* verdict — Android
//     Go / <2GB devices have `ro.config.low_ram=true` set by the OEM, and
//     `isLowRamDevice()` returns true iff that flag is set OR the device
//     meets the Go hardware profile. This is the same gate Android's own
//     system UI uses (e.g. to disable translucent system bars, animations).
//   - `ActivityManager.MemoryInfo.totalMem` is the sum of all RAM the
//     kernel exposes to user-space (`/proc/meminfo` `MemTotal`). Available
//     since API 16, so well within our `minSdk = 21` floor.
//
// Context plumbing:
//   `ActivityManager` is fetched from the registered `Context` (singleton
//   in OfflineModeManager.android.kt — same `LexiconAppContextHolder`).
//   `getDeviceModel()` doesn't need a Context (it reads a static), so it
//   works even before `initLexiconAppContext()` is called.
package com.lexicon.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build

// ---------------------------------------------------------------------------
// actual fun getDeviceModel(): String
// ---------------------------------------------------------------------------

/**
 * Android `actual` — returns `Build.MODEL`, the marketing device model
 * string (e.g. "Pixel 8 Pro", "SM-S918B").
 *
 * This is a `static final String` on `android.os.Build`, populated once
 * at process start from the `ro.product.model` system property. No
 * allocation, no IPC, no permission — safe to call from any thread,
 * including finalizers and uncaught-exception handlers.
 *
 * On misconfigured / engineering builds, `Build.MODEL` may be `"unknown"`
 * — the caller should treat that as a debug-build signal, not a crash.
 */
actual fun getDeviceModel(): String {
    return Build.MODEL ?: "unknown"
}

// ---------------------------------------------------------------------------
// actual fun isLowRamDevice(): Boolean
// ---------------------------------------------------------------------------

/**
 * Android `actual` — delegates to `ActivityManager.isLowRamDevice`.
 *
 * `isLowRamDevice()` was added in API 19; it reads `ro.config.low_ram`
 * (set by the OEM for Android Go targets and any device with <2GB RAM).
 * On our `minSdk = 21` floor, the call always succeeds — no
 * `Build.VERSION.SDK_INT` guard needed.
 *
 * The `getSystemService` cast returns `null` only in two cases:
 *   1. The host `Context` hasn't been registered (the app called neither
 *      `initLexiconAppContext()` nor `initLexiconDb()`). We return `false`
 *      — treat as "default RAM" so the DAWG preloads and the user gets
 *      the full experience. This is the conservative default for the
 *      feature-gating contract.
 *   2. The `Context` is from a stripped / headless Android (very unusual).
 *      Same conservative `false` default.
 */
actual fun isLowRamDevice(): Boolean {
    val context = LexiconAppContextHolder.context ?: return false
    val am = context.getSystemService(Context.ACTIVITY_SERVICE)
        as? ActivityManager ?: return false
    return am.isLowRamDevice
}

// ---------------------------------------------------------------------------
// actual fun getTotalRamBytes(): Long
// ---------------------------------------------------------------------------

/**
 * Android `actual` — returns `ActivityManager.MemoryInfo.totalMem`.
 *
 * `MemoryInfo.totalMem` is the total accessible RAM in bytes (matches
 * `/proc/meminfo` `MemTotal` × 1024). It does NOT include RAM reserved by
 * the kernel / hardware (modem, GPU, secure-enclave, …), so it'll be
 * 5–10% below the device's advertised RAM size — that's expected and is
 * what every other app sees too.
 *
 * Returns `0L` on any failure (no Context, AM unavailable, sysfs read
 * error). The caller should treat 0 as "unknown" and fall back to the
 * default cache sizes.
 *
 * Why allocate a fresh `MemoryInfo` per call (not cache it):
 *   RAM doesn't change at runtime, but caching the result would mean the
 *   first caller (typically a startup background thread) allocates it
 *   and all later callers pay a volatile read — minor, but the simpler
 *   contract is "no caching, every call is a fresh snapshot". The
 *   `getMemoryInfo` IPC is fast (~1µs in practice) and is called rarely
 *   (only at feature-flag decision points).
 */
actual fun getTotalRamBytes(): Long {
    val context = LexiconAppContextHolder.context ?: return 0L
    val am = context.getSystemService(Context.ACTIVITY_SERVICE)
        as? ActivityManager ?: return 0L
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    return info.totalMem
}
