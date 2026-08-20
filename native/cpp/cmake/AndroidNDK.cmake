# =============================================================================
#  cmake/AndroidNDK.cmake — Android NDK build configuration.
#
#  Loaded by the root CMakeLists when TARGET_ANDROID=ON or ANDROID=TRUE
#  (the latter is set by cmake/Toolchain-Android.cmake).
#
#  Responsibilities:
#    * Locate and validate the NDK (r25+)
#    * Define the ABI whitelist (arm64-v8a, armeabi-v7a, x86_64)
#    * Force ANDROID_PLATFORM=android-21 and ANDROID_STL=c++_shared
#    * Expose lexora_install_android_shared_libraries(<target> <dest>) which
#      copies libc++_shared.so next to the engine .so
# =============================================================================
include_guard(GLOBAL)

# -----------------------------------------------------------------------------
# Locate the NDK
# -----------------------------------------------------------------------------
if(NOT ANDROID_NDK)
    if(DEFINED ENV{ANDROID_NDK_HOME})
        set(ANDROID_NDK $ENV{ANDROID_NDK_HOME} CACHE PATH "Android NDK root")
    elseif(DEFINED ENV{ANDROID_NDK_ROOT})
        set(ANDROID_NDK $ENV{ANDROID_NDK_ROOT} CACHE PATH "Android NDK root")
    elseif(DEFINED ENV{ANDROID_HOME})
        # Try $ANDROID_HOME/ndk/<version>/ — pick the highest version dir.
        file(GLOB _NDK_CANDIDATES LIST_DIRECTORIES true "$ENV{ANDROID_HOME}/ndk/*")
        if(_NDK_CANDIDATES)
            list(SORT _NDK_CANDIDATES ORDER DESCENDING)
            list(GET _NDK_CANDIDATES 0 _NDK_GUESS)
            set(ANDROID_NDK "${_NDK_GUESS}" CACHE PATH "Android NDK root (auto-guess)")
        endif()
    endif()
endif()

if(NOT ANDROID_NDK OR NOT EXISTS "${ANDROID_NDK}/source.properties")
    message(FATAL_ERROR
        "Android NDK not found.\n"
        "Set ANDROID_NDK or ANDROID_NDK_HOME to the NDK root (r25+).\n"
        "  Example: export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/25.2.9519653\n"
        "Or pass the toolchain file directly:\n"
        "  cmake -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake ...")
endif()

message(STATUS "Android NDK: ${ANDROID_NDK}")

# Validate NDK major version (r25+).
if(EXISTS "${ANDROID_NDK}/source.properties")
    file(READ "${ANDROID_NDK}/source.properties" _NDK_PROPS)
    string(REGEX MATCH "Pkg\\.Revision = ([0-9]+)\\." _m "${_NDK_PROPS}")
    if(CMAKE_MATCH_1)
        set(_NDK_MAJOR ${CMAKE_MATCH_1})
        if(_NDK_MAJOR LESS 25)
            message(WARNING
                "Android NDK r${_NDK_MAJOR} detected; r25+ is recommended for C++17 "
                "and the libc++_shared.so layout used by this project.")
        else()
            message(STATUS "Android NDK version: r${_NDK_MAJOR}")
        endif()
    endif()
endif()

# -----------------------------------------------------------------------------
# ABI whitelist
# -----------------------------------------------------------------------------
# One CMake invocation builds ONE ABI (NDK toolchain is single-ABI). Build all
# three by invoking CMake three times — see README.md §Android for the helper
# command, or wrap with build_android.sh.
set(LEXORA_ANDROID_ABIS "arm64-v8a;armeabi-v7a;x86_64"
    CACHE STRING "Android ABIs to build (one CMake invocation per ABI)")

message(STATUS "Android ABIs (whitelist): ${LEXORA_ANDROID_ABIS}")

# Sanity: the active ABI must be in the whitelist.
if(ANDROID_ABI AND NOT ANDROID_ABI IN_LIST LEXORA_ANDROID_ABIS)
    message(FATAL_ERROR
        "Unsupported ANDROID_ABI='${ANDROID_ABI}'.\n"
        "Must be one of: ${LEXORA_ANDROID_ABIS}")
endif()

# -----------------------------------------------------------------------------
# Platform level + STL
# -----------------------------------------------------------------------------
if(NOT ANDROID_PLATFORM)
    set(ANDROID_PLATFORM "android-21" CACHE STRING "Android platform level" FORCE)
endif()
if(NOT ANDROID_STL)
    set(ANDROID_STL "c++_shared" CACHE STRING "Android STL" FORCE)
endif()

message(STATUS "Android platform: ${ANDROID_PLATFORM}")
message(STATUS "Android STL:      ${ANDROID_STL}")

# Output library name (matches the Android JNI convention
# System.loadLibrary("lexora-engine")).
set(LEXORA_ANDROID_OUTPUT_NAME "lexora-engine")

# -----------------------------------------------------------------------------
# lexora_install_android_shared_libraries(<target> <dest>)
#
# Copies libc++_shared.so for the active ABI into <dest>, next to the engine
# .so. Required because we link with ANDROID_STL=c++_shared — the app must ship
# libc++_shared.so alongside liblexora-engine.so (or use libc++_static.a).
# -----------------------------------------------------------------------------
function(lexora_install_android_shared_libraries _target _dest)
    if(NOT ANDROID)
        return()
    endif()

    # libc++_shared.so lives under:
    #   $NDK/toolchains/llvm/prebuilt/<host-tag>/sysroot/usr/lib/<llvm-triple>/libc++_shared.so
    # ANDROID_LLVM_TRIPLE is set by the NDK toolchain (e.g. aarch64-linux-android).
    file(GLOB _LIBCXX_SHARED
        "${ANDROID_NDK}/toolchains/llvm/prebuilt/*/sysroot/usr/lib/${ANDROID_LLVM_TRIPLE}/libc++_shared.so"
        "${ANDROID_NDK}/toolchains/llvm/prebuilt/*/sysroot/usr/lib/${ANDROID_LLVM_TRIPLE}/*/libc++_shared.so")

    if(_LIBCXX_SHARED)
        list(GET _LIBCXX_SHARED 0 _LIBCXX_FILE)
        add_custom_command(TARGET ${_target} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E make_directory  "${_dest}"
            COMMAND ${CMAKE_COMMAND} -E copy_if_different
                "${_LIBCXX_FILE}" "${_dest}/libc++_shared.so"
            COMMENT "Copying libc++_shared.so → ${_dest} (for ${ANDROID_ABI})")
        message(STATUS "libc++_shared.so for ${ANDROID_LLVM_TRIPLE}: ${_LIBCXX_FILE}")
    else()
        message(WARNING
            "Could not locate libc++_shared.so for ABI=${ANDROID_ABI} "
            "(triple=${ANDROID_LLVM_TRIPLE}). The consuming Android app must "
            "either ship its own libc++_shared.so or ANDROID_STL should be "
            "switched to c++_static.")
    endif()
endfunction()
