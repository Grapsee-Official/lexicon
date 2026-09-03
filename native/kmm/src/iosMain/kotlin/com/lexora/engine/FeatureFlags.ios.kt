// FeatureFlags.ios.kt — iOS `actual` implementations for the three
// `expect fun`s declared in FeatureFlags.kt (commonMain).
//
// Per task P4-10:
//   - getDeviceModel()    → UIDevice.currentDevice.model
//   - isLowRamDevice()    → heuristic: getTotalRamBytes() < 3 GiB
//   - getTotalRamBytes()  → sysctlbyname("hw.memsize")
//
// Why these APIs:
//   - `UIDevice.currentDevice.model` is the public, App-Store-safe way to
//     identify the device family ("iPhone", "iPad", "iPod touch"). Reading
//     the hardware identifier (e.g. "iPhone15,2") would require either
//     `sysctlbyname("hw.machine")` (still App-Store-safe but returns a
//     non-localised identifier) or the now-private `UIDevice.identifierForVendor`
//     (which doesn't return model info anyway). The expect-file contract
//     says "returns UIDevice.model", so we honour that exactly.
//   - `sysctlbyname("hw.memsize")` is the only App-Store-safe way to read
//     the total physical RAM on iOS. It returns a `uint64_t` (8 bytes)
//     populated by the kernel at boot. The `sysctl` syscall itself is
//     declared in `<sys/sysctl.h>` and exposed by the Kotlin/Native
//     darwin cinterop as a C function we can bind via @SymbolName.
//   - There is no iOS equivalent of Android's `ActivityManager.isLowRamDevice`
//     — Apple doesn't expose a "low-RAM device" flag. We approximate it
//     by thresholding `hw.memsize` at 3 GiB, which catches the only
//     currently-shipping low-RAM iOS devices (the iPhone SE 2nd/3rd gen
//     at 2–3 GB and the iPhone 8 at 2 GB). The 3 GiB threshold is a
//     deliberately conservative cut — devices with exactly 3 GB (iPhone X
//     / iPhone XR) are NOT flagged as low-RAM, matching the spec
//     "<3GB RAM" wording.
//
// Note on `sysctlbyname`:
//   The function is exported by libsystem_c.dylib (part of the iOS system
//   library `libSystem.B.dylib`, which Kotlin/Native links by default).
//   We bind it via `@SymbolName` rather than cinterop because (a) the
//   function is a plain C function (no struct types involved), and (b)
//   the existing DeltaSyncClient.ios.kt + LexoraEngine.ios.kt already use
//   the same @SymbolName pattern for `sqlite3_*` and `ZSTD_*` — consistency
//   wins.
package com.lexora.engine

import kotlinx.cinterop.*
import platform.UIKit.UIDevice

// ---------------------------------------------------------------------------
// sysctl(3) C API binding
// ---------------------------------------------------------------------------

/**
 * `int sysctlbyname(const char *name, void *oldp, size_t *oldlenp,
 *                   const void *newp, size_t newlen);`
 *
 * Look up a kernel sysctl variable by ASCII name. We bind it via
 * @SymbolName (resolves at link time via the default libSystem linkage —
 * no extra cinterop needed).
 *
 * Type mapping on 64-bit iOS (LP64):
 *   - `const char*`   → `CPointer<ByteVar>?`  (UTF-8 NUL-terminated)
 *   - `void*`         → `COpaquePointer?`     (typed pointers passed via reinterpret)
 *   - `size_t*`       → `CPointer<ULongVar>?` (size_t == unsigned long == 8 bytes)
 *   - `size_t`        → `ULong`               (8 bytes)
 *
 * Return: 0 on success, -1 on error (errno set; we don't inspect errno —
 * the only realistic failure is "name not found", which means this build
 * is running on a platform that doesn't expose `hw.memsize`, which is
 * never the case on shipping iOS).
 */
@SymbolName("sysctlbyname")
private external fun sysctlbyname(
    name: CPointer<ByteVar>?,
    oldp: COpaquePointer?,
    oldlenp: CPointer<ULongVar>?,
    newp: COpaquePointer?,
    newlen: ULong
): Int

// ---------------------------------------------------------------------------
// actual fun getDeviceModel(): String
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — returns `UIDevice.currentDevice.model`.
 *
 * The `currentDevice` singleton is lazily initialised on first access by
 * UIKit; `.model` returns an NSString* that Kotlin/Native bridges to
 * `String`. The value is one of `"iPhone"`, `"iPad"`, `"iPod touch"`,
 * `"Apple TV"`, or `"Apple Vision"` (depending on the running platform).
 *
 * No permission, no IPC, no allocation on the Kotlin side (the NSString
 * is bridge-converted to a Kotlin String on first read). Safe to call
 * from any thread — `UIDevice` is documented thread-safe for read-only
 * properties.
 */
actual fun getDeviceModel(): String {
    return UIDevice.currentDevice.model
}

// ---------------------------------------------------------------------------
// actual fun isLowRamDevice(): Boolean
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — heuristic: total RAM < 3 GiB.
 *
 * The threshold mirrors the spec wording ("<3GB RAM"). On currently-
 * shipping iOS devices this catches:
 *   - iPhone SE (2nd gen, 3 GB)         → NOT flagged (exactly 3 GB)
 *   - iPhone SE (3rd gen, 4 GB)         → NOT flagged
 *   - iPhone 8 / 8 Plus (2 / 3 GB)      → flagged / NOT flagged
 *   - iPhone X / XR (3 GB)              → NOT flagged
 *   - iPhone 11 / 12 / 13 / 14 (4 GB+)  → NOT flagged
 *   - All iPads since 2017 (3 GB+)      → NOT flagged
 *
 * The heuristic is conservative: it's better to NOT flag a borderline
 * device as low-RAM (and let the runtime adapt via MemoryPressureListener)
 * than to flag a 3 GB device as low-RAM and disable the DAWG preload
 * unnecessarily. This matches the Android actual's contract — Android's
 * `isLowRamDevice` only fires on Android Go devices (typically <2 GB),
 * which is roughly the same population.
 */
actual fun isLowRamDevice(): Boolean {
    val threeGib = 3L * 1024L * 1024L * 1024L  // 3,221,225,472 bytes
    return getTotalRamBytes() < threeGib
}

// ---------------------------------------------------------------------------
// actual fun getTotalRamBytes(): Long
// ---------------------------------------------------------------------------

/**
 * iOS `actual` — reads `hw.memsize` via `sysctlbyname`.
 *
 * Flow:
 *   1. Allocate a `ULongVar` for the value buffer (`oldp`) and a second
 *      `ULongVar` for the size buffer (`oldlenp`). Pre-set `oldlenp` to
 *      8 (the size of a `uint64_t`) so the kernel knows how much to write.
 *   2. Call `sysctlbyname("hw.memsize", &value, &size, NULL, 0)`.
 *   3. On success (rc == 0), return `value.toLong()`. On failure, return
 *      `0L` — the caller treats 0 as "unknown" and falls back to default
 *      cache sizes (same contract as the Android actual).
 *
 * `memScoped` owns the C allocations and frees them when the block exits;
 * the `sysctlbyname` call completes synchronously inside it, so the
 * pointers are valid for the call's duration.
 *
 * `valueVar.ptr.reinterpret()` widens the typed `CPointer<ULongVar>` to a
 * `COpaquePointer?` for the `void* oldp` parameter. This is type-safe —
 * the bytes at that address ARE a `uint64_t`, which is exactly what
 * `hw.memsize` writes.
 */
actual fun getTotalRamBytes(): Long {
    return memScoped {
        val nameC = "hw.memsize".cstr.getPointer(this)
        val sizeVar = alloc<ULongVar>()
        sizeVar.value = 8UL  // uint64_t is 8 bytes; the kernel won't write more.
        val valueVar = alloc<ULongVar>()
        val rc = sysctlbyname(
            name = nameC,
            oldp = valueVar.ptr.reinterpret(),
            oldlenp = sizeVar.ptr,
            newp = null,
            newlen = 0UL
        )
        if (rc != 0) {
            return@memScoped 0L
        }
        valueVar.value.toLong()
    }
}
