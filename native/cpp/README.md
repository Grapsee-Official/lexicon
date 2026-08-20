# Lexora C++ Engine — Build System

C++17 cross-platform build for the **lexora-engine** library, producing:

| Target              | Output                              | Toolchain                            |
| ------------------- | ----------------------------------- | ------------------------------------ |
| Android (NDK r25+)  | `liblexora-engine.so`               | `cmake/Toolchain-Android.cmake`      |
| iOS (16.0+)         | `LexoraEngine.framework`            | `cmake/Toolchain-iOS.cmake`          |
| Linux / macOS host  | `liblexora-engine.{so,dylib}`       | system compiler                      |
| Tests               | `lexora-engine-tests`               | Google Test (vendored / system)      |

Requires **CMake ≥ 3.22** (first-class iOS framework + `gtest_discover_tests` PRE_TEST mode).

---

## 1. Repository layout

```
native/cpp/
├── CMakeLists.txt                 # Root build — project, options, library target
├── README.md                      # ← you are here
├── cmake/
│   ├── Platform.cmake             # Platform detection + flag helper
│   ├── GoogleTest.cmake           # Offline-safe gtest acquisition
│   ├── AndroidNDK.cmake           # NDK path / ABI whitelist / libc++_shared.so copy
│   ├── iOS.cmake                  # iOS framework + bitcode marker
│   ├── Toolchain-Android.cmake    # NDK toolchain wrapper
│   ├── Toolchain-iOS.cmake        # iOS cross toolchain
│   ├── HashTable.cmake            # Per-module fragment (built by parallel agents)
│   ├── Levenshtein.cmake
│   ├── Dawg.cmake
│   ├── Unicode.cmake
│   └── Engine.cmake
├── include/lexora/                # Public headers (engine.hpp, c_api.h, …)
├── src/                           # Implementation (.cpp)
└── tests/
    ├── CMakeLists.txt             # Test binary + gtest_discover_tests
    ├── test_*.cpp                 # Google Test cases
    └── googletest/                # (Optional) vendored GoogleTest source tree
```

### Module fragment contract

Each `cmake/<Module>.cmake` file is included with `OPTIONAL` from the root and MAY:

1. Append source files to the `LEXORA_SOURCES` list variable, **and/or**
2. Append public headers to the `LEXORA_HEADERS` list variable, **and/or**
3. Declare an OBJECT library named `lexora_<module>_obj` for isolated compilation.

If none of the fragments populate `LEXORA_SOURCES`, the root falls back to globbing
`src/*.cpp` so the build is functional even before all parallel-agent fragments land.

---

## 2. Build options

| Option                          | Default   | Description                                                   |
| ------------------------------- | --------- | ------------------------------------------------------------- |
| `BUILD_SHARED_LIBS`             | platform  | ON → `.so`/`.dylib`, OFF → `.a`/static framework              |
| `BUILD_TESTS`                   | ON        | Build Google Test binaries (host builds only)                 |
| `TARGET_ANDROID`                | OFF       | Mark as Android cross-compile (loads AndroidNDK.cmake)        |
| `TARGET_IOS`                    | OFF       | Mark as iOS cross-compile (loads iOS.cmake, sets framework)   |
| `LEXORA_BUILD_FRAMEWORK`        | OFF       | Produce Apple `.framework` bundle                             |
| `LEXORA_PER_FILE_TESTS`         | OFF       | Build one test binary per `test_*.cpp` (in addition to aggregate) |
| `LEXORA_ALLOW_GTEST_DOWNLOAD`   | OFF       | Allow `FetchContent` to download GoogleTest if missing        |

Default `BUILD_SHARED_LIBS` is **ON for Android/Linux, OFF for iOS** (static framework).

---

## 3. Build for Android (NDK)

### Prerequisites

- Android NDK **r25+** (r25.2.9519653 or newer)
- Set `ANDROID_NDK_HOME` (or `ANDROID_NDK_ROOT`) to the NDK root:

  ```bash
  export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/25.2.9519653
  ```

### Option A — CMake + NDK (recommended)

Build all three ABIs (one CMake invocation per ABI — the NDK toolchain is single-ABI):

```bash
cd native/cpp

for ABI in arm64-v8a armeabi-v7a x86_64; do
  cmake -S . -B build/android-$ABI \
    -DCMAKE_TOOLCHAIN_FILE=cmake/Toolchain-Android.cmake \
    -DANDROID_ABI=$ABI \
    -DANDROID_PLATFORM=android-21 \
    -DTARGET_ANDROID=ON \
    -DBUILD_TESTS=OFF \
    -DCMAKE_BUILD_TYPE=Release
  cmake --build build/android-$ABI -j
done
```

Output (per ABI):

```
build/android-$ABI/lib/liblexora-engine.so
build/android-$ABI/lib/libc++_shared.so   # copied next to the engine .so
```

Wire these into the consuming Android app under `app/src/main/jniLibs/<ABI>/`
(or pass them through Gradle's `externalNativeBuild` CMake integration — see the
`native/android/` project for the Gradle side).

### Option B — ndk-build (legacy)

If your existing build pipeline uses `Android.mk` files instead of CMake, you can
hand-write an `Android.mk` that compiles `src/*.cpp` and produces `liblexora-engine.so`.
This task does **not** generate `Android.mk` files; the recommended path is Option A.

### JNI side

The Java/Kotlin side loads the library by its unversioned name:

```kotlin
companion object { init { System.loadLibrary("lexora-engine") } }
```

Native functions are declared in `include/lexora/c_api.h`.

---

## 4. Build for iOS

### Prerequisites

- macOS host with **Xcode 14+**
- iOS 16.0+ deployment target
- CMake ≥ 3.22

### Build a framework (arm64 device)

```bash
cd native/cpp
cmake -G Xcode -S . -B build/ios \
  -DCMAKE_TOOLCHAIN_FILE=cmake/Toolchain-iOS.cmake \
  -DTARGET_IOS=ON \
  -DLEXORA_BUILD_FRAMEWORK=ON \
  -DBUILD_SHARED_LIBS=OFF \
  -DBUILD_TESTS=OFF
cmake --build build/ios --config Release
```

Output:

```
build/ios/Release/LexoraEngine.framework
```

The framework contains:

- `LexoraEngine`       — static archive (arm64)
- `Headers/`           — public headers (`engine.hpp`, `c_api.h`, …)
- `Info.plist`         — auto-generated bundle metadata

### Embed in an Xcode app

1. Drag `LexoraEngine.framework` into the Xcode project navigator (under the
   Lexora target's "Frameworks, Libraries, and Embedded Content" section).
2. Set **Embed** = "Embed & Sign" (or "Do Not Embed" if you link statically and
   only need the headers).
3. Add `#import <LexoraEngine/c_api.h>` (or `#include <LexoraEngine/engine.hpp>`)
   where needed.

### iOS simulator

This toolchain builds the **device** slice (arm64, iphoneos SDK). For the
simulator, build a separate slice via `xcodebuild -sdk iphonesimulator` and
lipo the two into a fat framework (standard Xcframework workflow):

```bash
xcodebuild -create-xcframework \
  -framework build/ios/Release/LexoraEngine.framework \
  -framework build/ios-sim/Release/LexoraEngine.framework \
  -output build/LexoraEngine.xcframework
```

### Bitcode

Bitcode is **disabled** (`ENABLE_BITCODE=NO`) per Xcode 14+ requirements. The
`-fembed-bitcode-marker` flag is retained so App Store thinning still has sizing
information.

---

## 5. Build for Linux / macOS (host, for testing)

```bash
cd native/cpp
cmake -S . -B build/host -DCMAKE_BUILD_TYPE=Debug
cmake --build build/host -j
```

Outputs:

```
build/host/lib/liblexora-engine.so       # or .a if -DBUILD_SHARED_LIBS=OFF
build/host/bin/lexora-engine-tests       # test binary
```

### Run tests

```bash
cd build/host
ctest --output-on-failure
# or run the binary directly:
./bin/lexora-engine-tests --gtest_color=yes
```

---

## 6. Tests

Tests are built automatically when `BUILD_TESTS=ON` (default) and the host is
**not** a mobile target. They are skipped when cross-compiling for Android or iOS.

### Acquiring Google Test (offline-friendly)

`cmake/GoogleTest.cmake` does **not** download Google Test by default. It looks
for, in order:

1. **Vendored source tree** at `tests/googletest/` — clone with:
   ```bash
   cd native/cpp/tests
   git clone --depth 1 --branch release-1.14.0 \
     https://github.com/google/googletest.git
   ```
2. **System-installed GTest** via `find_package(GTest)` — install with:
   - Debian/Ubuntu: `sudo apt install libgtest-dev`
   - Fedora/RHEL:   `sudo dnf install gtest-devel`
   - macOS (brew):  `brew install googletest`
3. **(Opt-in) Network fetch** — pass `-DLEXORA_ALLOW_GTEST_DOWNLOAD=ON` to allow
   `FetchContent` to fetch `release-1.14.0` from GitHub.

If none of these succeed, the test build is **skipped with a warning** — the
library still builds normally. This is the offline-safe default.

### Test discovery

Tests are discovered via `gtest_discover_tests()` (the modern replacement for
`GTEST_ADD_TEST`) with `DISCOVERY_MODE PRE_TEST`, so newly added `TEST()` cases
appear in `ctest` without re-running CMake.

### Per-file test binaries

For isolating failures, pass `-DLEXORA_PER_FILE_TESTS=ON` to also build one
binary per `test_*.cpp` file (named after the file stem, e.g. `test_hash_table`).

---

## 7. Cross-compilation troubleshooting

| Symptom                                                  | Fix                                                                                         |
| -------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| `Android NDK not found`                                  | `export ANDROID_NDK_HOME=/path/to/ndk`                                                       |
| `Unsupported ANDROID_ABI='...'`                          | Must be one of `arm64-v8a`, `armeabi-v7a`, `x86_64`                                          |
| `Could not locate iphoneos SDK via xcrun`                | Run `xcode-select -p`; if missing, `sudo xcode-select --install`                             |
| `CMake Error: ... FRAMEWORK ...`                         | Use the Xcode generator (`-G Xcode`) for iOS framework builds                                |
| `Google Test is NOT available`                           | Clone `tests/googletest/` (see §6) or `-DLEXORA_ALLOW_GTEST_DOWNLOAD=ON`                     |
| `liblexora-engine.so` runs but `UnsatisfiedLinkError`    | Ensure `libc++_shared.so` is shipped alongside the engine .so in `jniLibs/<ABI>/`           |

---

## 8. Toolchain & version matrix

| Component           | Minimum   | Recommended                                  |
| ------------------- | --------- | -------------------------------------------- |
| CMake               | 3.22      | 3.27+ (best iOS framework support)           |
| Android NDK         | r25       | r25b or newer                                 |
| Xcode               | 14.0      | 15.x                                          |
| iOS deployment      | 16.0      | 16.0+                                         |
| C++ standard        | C++17     | C++17 (do not downgrade to C++14)             |
| Google Test         | 1.14.0    | 1.14.0                                        |
| Clang (host)        | 14        | 16+                                           |
| GCC (host)          | 11        | 13+                                           |

---

## 9. Artifacts summary

After a full build, the expected outputs are:

```
build/android-arm64-v8a/lib/liblexora-engine.so
build/android-arm64-v8a/lib/libc++_shared.so
build/android-armeabi-v7a/lib/liblexora-engine.so
build/android-armeabi-v7a/lib/libc++_shared.so
build/android-x86_64/lib/liblexora-engine.so
build/android-x86_64/lib/libc++_shared.so
build/ios/Release/LexoraEngine.framework/
build/host/lib/liblexora-engine.so
build/host/bin/lexora-engine-tests
```

Pass these into the Android Gradle build (under `jniLibs/`) and the Xcode
project (as an embedded framework) respectively.
