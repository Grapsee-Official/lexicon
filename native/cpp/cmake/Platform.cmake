# =============================================================================
#  cmake/Platform.cmake — platform detection and compile-flag helper.
#
#  Sets:
#    LEXICON_PLATFORM  : "android" | "ios" | "macos" | "linux" | "unknown"
#    Platform-specific cache variables (ANDROID_PLATFORM, ANDROID_ABI,
#    CMAKE_OSX_DEPLOYMENT_TARGET, CMAKE_OSX_ARCHITECTURES, CMAKE_OSX_SYSROOT).
#
#  Defines:
#    lexicon_apply_platform_flags(<target>)
#        Applies -O2 -Wall -Wextra -std=c++17 plus platform extras
#        (visibility, -fPIC, -fembed-bitcode-marker, ...) as PRIVATE
#        compile options on the given target. Also sets OUTPUT_NAME on Android
#        so the .so lands as liblexicon-engine.so.
# =============================================================================
include_guard(GLOBAL)

# -----------------------------------------------------------------------------
# Platform detection
# -----------------------------------------------------------------------------
if(NOT DEFINED LEXICON_PLATFORM)
    if(ANDROID)
        set(LEXICON_PLATFORM "android")
    elseif(CMAKE_SYSTEM_NAME STREQUAL "iOS")
        set(LEXICON_PLATFORM "ios")
    elseif(APPLE)
        set(LEXICON_PLATFORM "macos")
    elseif(CMAKE_SYSTEM_NAME STREQUAL "Linux")
        set(LEXICON_PLATFORM "linux")
    else()
        set(LEXICON_PLATFORM "unknown")
    endif()
endif()

message(STATUS "Lexicon platform: ${LEXICON_PLATFORM} (CMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME})")

# -----------------------------------------------------------------------------
# Platform-specific cache defaults
# -----------------------------------------------------------------------------
if(LEXICON_PLATFORM STREQUAL "android")
    # ANDROID_ABI is normally set by the NDK toolchain; ensure a sane default.
    if(NOT ANDROID_ABI)
        set(ANDROID_ABI "arm64-v8a" CACHE STRING "Android ABI" FORCE)
    endif()
    if(NOT ANDROID_PLATFORM)
        set(ANDROID_PLATFORM "android-21" CACHE STRING "Android platform level" FORCE)
    endif()
    message(STATUS "  ANDROID_ABI      = ${ANDROID_ABI}")
    message(STATUS "  ANDROID_PLATFORM = ${ANDROID_PLATFORM}")
    if(ANDROID_NDK)
        message(STATUS "  ANDROID_NDK      = ${ANDROID_NDK}")
    endif()
endif()

if(LEXICON_PLATFORM STREQUAL "ios")
    # iOS 16+ deployment target.
    if(NOT CMAKE_OSX_DEPLOYMENT_TARGET)
        set(CMAKE_OSX_DEPLOYMENT_TARGET "16.0" CACHE STRING "iOS deployment target" FORCE)
    endif()
    # arm64 only — no x86_64 simulator slice here. (Use Xcode to build simulator slices.)
    if(NOT CMAKE_OSX_ARCHITECTURES)
        set(CMAKE_OSX_ARCHITECTURES "arm64" CACHE STRING "iOS architectures" FORCE)
    endif()
    # try_compile must not try to run iOS binaries on the host.
    set(CMAKE_TRY_COMPILE_TARGET_TYPE STATIC_LIBRARY)
    message(STATUS "  CMAKE_OSX_DEPLOYMENT_TARGET = ${CMAKE_OSX_DEPLOYMENT_TARGET}")
    message(STATUS "  CMAKE_OSX_ARCHITECTURES     = ${CMAKE_OSX_ARCHITECTURES}")
endif()

if(LEXICON_PLATFORM STREQUAL "macos")
    if(NOT CMAKE_OSX_DEPLOYMENT_TARGET)
        set(CMAKE_OSX_DEPLOYMENT_TARGET "13.0" CACHE STRING "macOS deployment target" FORCE)
    endif()
    message(STATUS "  CMAKE_OSX_DEPLOYMENT_TARGET = ${CMAKE_OSX_DEPLOYMENT_TARGET}")
endif()

# -----------------------------------------------------------------------------
# Base compiler flags
# -----------------------------------------------------------------------------
# Applied to all platforms unless overridden below.
set(LEXICON_BASE_CXX_FLAGS -O2 -Wall -Wextra)

# Note: -std=c++17 is set globally via CMAKE_CXX_STANDARD in the root CMakeLists,
# but we add it explicitly here too so it is visible in the compile command for
# IDEs and downstream consumers.
list(APPEND LEXICON_BASE_CXX_FLAGS $<$<STREQUAL:${CMAKE_CXX_STANDARD},17>:-std=c++17>)

if(CMAKE_CXX_COMPILER_ID MATCHES "Clang|AppleClang")
    set(LEXICON_COMPILER_IS_CLANG ON)
    list(APPEND LEXICON_BASE_CXX_FLAGS
        -Wno-unused-parameter         # template-heavy code triggers this a lot
        -Wno-deprecated-declarations) # engine uses some legacy POSIX bits on Android
elseif(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
    set(LEXICON_COMPILER_IS_GCC ON)
elseif(MSVC)
    message(WARNING "MSVC is not officially supported; engine uses POSIX/Clang idioms.")
    set(LEXICON_BASE_CXX_FLAGS /O2 /W4 /std:c++17 /EHsc /Zc:__cplusplus)
endif()

# -----------------------------------------------------------------------------
# lexicon_apply_platform_flags(<target>)
# -----------------------------------------------------------------------------
function(lexicon_apply_platform_flags _target)
    # Base flags.
    target_compile_options(${_target} PRIVATE ${LEXICON_BASE_CXX_FLAGS})

    if(LEXICON_PLATFORM STREQUAL "android")
        # PIE / PIC for shared library on Android.
        set_target_properties(${_target} PROPERTIES
            POSITION_INDEPENDENT_CODE ON
            OUTPUT_NAME "lexicon-engine")          # → liblexicon-engine.so
        target_compile_options(${_target} PRIVATE
            -fPIC
            -fvisibility=hidden
            -fvisibility-inlines-hidden
            -fexceptions)
        # NDK may ship with -ffunction-sections -fdata-sections for linker GC.
        target_compile_options(${_target} PRIVATE -ffunction-sections -fdata-sections)
        target_link_options(${_target} PRIVATE
            -Wl,--gc-sections
            -Wl,--exclude-libs,ALL
            -Wl,-z,defs -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now)
    endif()

    if(LEXICON_PLATFORM STREQUAL "ios")
        set_target_properties(${_target} PROPERTIES
            POSITION_INDEPENDENT_CODE ON
            OSX_ARCHITECTURES "${CMAKE_OSX_ARCHITECTURES}")
        target_compile_options(${_target} PRIVATE
            -fembed-bitcode-marker            # bitcode disabled (Xcode 14+); marker for thinning
            -fvisibility=hidden
            -fvisibility-inlines-hidden
            -fobjc-arc)
        # Apple linker flags for the framework binary.
        target_link_options(${_target} PRIVATE
            -Wl,-dead_strip
            -Wl,-export_dynamic)
    endif()

    if(LEXICON_PLATFORM STREQUAL "macos" OR LEXICON_PLATFORM STREQUAL "linux")
        target_compile_options(${_target} PRIVATE
            -fvisibility=hidden
            -fvisibility-inlines-hidden)
    endif()
endfunction()
