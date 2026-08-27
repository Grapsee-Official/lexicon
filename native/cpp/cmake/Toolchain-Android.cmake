# =============================================================================
#  cmake/Toolchain-Android.cmake — CMake toolchain for Android NDK.
#
#  This is a thin wrapper around the NDK's own android.toolchain.cmake that
#  injects project defaults (android-21, c++_shared, supported ABIs).
#
#  Usage:
#    cmake -B build/android-arm64 \
#          -DCMAKE_TOOLCHAIN_FILE=<repo>/native/cpp/cmake/Toolchain-Android.cmake \
#          -DANDROID_ABI=arm64-v8a \
#          -DTARGET_ANDROID=ON
#
#  To build all three ABIs, invoke the above three times with
#  -DANDROID_ABI=arm64-v8a / armeabi-v7a / x86_64, then collect the .so from
#  each build/lib/ directory.
# =============================================================================
cmake_minimum_required(VERSION 3.22)

# -----------------------------------------------------------------------------
# 1. Locate the NDK
# -----------------------------------------------------------------------------
if(NOT ANDROID_NDK)
    if(DEFINED ENV{ANDROID_NDK_HOME})
        set(ANDROID_NDK $ENV{ANDROID_NDK_HOME})
    elseif(DEFINED ENV{ANDROID_NDK_ROOT})
        set(ANDROID_NDK $ENV{ANDROID_NDK_ROOT})
    elseif(DEFINED ENV{ANDROID_HOME})
        file(GLOB _NDK_DIRS LIST_DIRECTORIES true "$ENV{ANDROID_HOME}/ndk/*")
        if(_NDK_DIRS)
            list(SORT _NDK_DIRS ORDER DESCENDING)
            list(GET _NDK_DIRS 0 ANDROID_NDK)
        endif()
    endif()
endif()

if(NOT ANDROID_NDK OR NOT EXISTS "${ANDROID_NDK}/build/cmake/android.toolchain.cmake")
    message(FATAL_ERROR
        "Android NDK not found.\n"
        "Set one of ANDROID_NDK / ANDROID_NDK_HOME / ANDROID_NDK_ROOT "
        "to the NDK root (r25+).\n"
        "  Example: export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/25.2.9519653")
endif()

set(ANDROID_NDK ${ANDROID_NDK} CACHE PATH "Android NDK root" FORCE)
message(STATUS "Toolchain-Android: using NDK at ${ANDROID_NDK}")

# -----------------------------------------------------------------------------
# 2. Project defaults (overridable on the cmake command line)
# -----------------------------------------------------------------------------
if(NOT ANDROID_ABI)
    set(ANDROID_ABI "arm64-v8a" CACHE STRING "Android ABI" FORCE)
endif()

# Validate ABI against the project whitelist before forwarding to the NDK
# toolchain (the NDK would also catch this, but with a less informative error).
set(_LEXICON_ANDROID_ABIS "arm64-v8a;armeabi-v7a;x86_64")
if(NOT ANDROID_ABI IN_LIST _LEXICON_ANDROID_ABIS)
    message(FATAL_ERROR
        "Unsupported ANDROID_ABI='${ANDROID_ABI}'.\n"
        "Must be one of: ${_LEXICON_ANDROID_ABIS}")
endif()

if(NOT ANDROID_PLATFORM)
    set(ANDROID_PLATFORM "android-21" CACHE STRING "Android platform level" FORCE)
endif()
if(NOT ANDROID_STL)
    set(ANDROID_STL "c++_shared" CACHE STRING "Android STL" FORCE)
endif()

# -----------------------------------------------------------------------------
# 3. Forward to the official NDK toolchain
# -----------------------------------------------------------------------------
# CMake toolchain chaining is fully supported. The NDK's android.toolchain.cmake
# is designed to be re-entrant (it sets CMAKE_SYSTEM_NAME=Android, locates the
# Clang/llvm prebuilt, sets ANDROID_NDK_MAJOR / ANDROID_LLVM_TRIPLE, etc.).
set(_NDK_TOOLCHAIN "${ANDROID_NDK}/build/cmake/android.toolchain.cmake")
include(${_NDK_TOOLCHAIN})

message(STATUS "Toolchain-Android: ABI=${ANDROID_ABI} PLATFORM=${ANDROID_PLATFORM} STL=${ANDROID_STL}")
