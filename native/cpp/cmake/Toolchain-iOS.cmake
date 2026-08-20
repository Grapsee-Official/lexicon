# =============================================================================
#  cmake/Toolchain-iOS.cmake — CMake toolchain for iOS cross-compilation.
#
#  CMake 3.14+ has first-class iOS support — setting CMAKE_SYSTEM_NAME=iOS
#  causes CMake to use the iphoneos SDK, set APPLE, and disable try_run.
#
#  Usage (Xcode generator recommended for framework output):
#    cmake -G Xcode -B build/ios \
#          -DCMAKE_TOOLCHAIN_FILE=<repo>/native/cpp/cmake/Toolchain-iOS.cmake \
#          -DTARGET_IOS=ON \
#          -DLEXORA_BUILD_FRAMEWORK=ON
#
#  To build:
#    cmake --build build/ios --config Release
#    # → build/ios/Release/LexoraEngine.framework
# =============================================================================
cmake_minimum_required(VERSION 3.22)

# -----------------------------------------------------------------------------
# 1. iOS as the target system
# -----------------------------------------------------------------------------
set(CMAKE_SYSTEM_NAME      iOS    CACHE STRING "CMake system name"  FORCE)
set(CMAKE_SYSTEM_VERSION   16.0   CACHE STRING "iOS version"        FORCE)

# Device only — arm64. (For the simulator, build separately with
# CMAKE_OSX_SYSROOT=iphonesimulator and CMAKE_OSX_ARCHITECTURES=x86_64;arm64
# via xcodebuild, NOT this toolchain.)
set(CMAKE_OSX_ARCHITECTURES      "arm64" CACHE STRING "iOS arch(s)" FORCE)
set(CMAKE_OSX_DEPLOYMENT_TARGET  "16.0"  CACHE STRING "iOS deploy target" FORCE)

# -----------------------------------------------------------------------------
# 2. Locate the iphoneos SDK
# -----------------------------------------------------------------------------
execute_process(
    COMMAND xcrun --sdk iphoneos --show-sdk-path
    OUTPUT_VARIABLE _LEXORA_IOS_SDK_PATH
    OUTPUT_STRIP_TRAILING_WHITESPACE
    RESULT_VARIABLE _LEXORA_IOS_SDK_RC
    ERROR_VARIABLE _LEXORA_IOS_SDK_ERR)

if(_LEXORA_IOS_SDK_RC EQUAL 0 AND _LEXORA_IOS_SDK_PATH)
    set(CMAKE_OSX_SYSROOT "${_LEXORA_IOS_SDK_PATH}" CACHE PATH "iOS SDK root" FORCE)
else()
    message(FATAL_ERROR
        "Could not locate iphoneos SDK via `xcrun --sdk iphoneos --show-sdk-path`.\n"
        "Error: ${_LEXORA_IOS_SDK_ERR}\n"
        "Run `xcode-select -p` and ensure Xcode is properly installed.")
endif()

# -----------------------------------------------------------------------------
# 3. try_compile must not try to run iOS binaries on the host
# -----------------------------------------------------------------------------
set(CMAKE_TRY_COMPILE_TARGET_TYPE STATIC_LIBRARY)

# -----------------------------------------------------------------------------
# 4. find_* should look only inside the iOS SDK, not the host
# -----------------------------------------------------------------------------
set(CMAKE_FIND_ROOT_PATH              ${CMAKE_OSX_SYSROOT})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

message(STATUS "Toolchain-iOS: arm64 / iOS ${CMAKE_OSX_DEPLOYMENT_TARGET} / SDK ${CMAKE_OSX_SYSROOT}")
