# =============================================================================
#  cmake/iOS.cmake — iOS build configuration for lexora-engine.
#
#  Loaded by the root CMakeLists when TARGET_IOS=ON (or when
#  cmake/Toolchain-iOS.cmake has already set CMAKE_SYSTEM_NAME=iOS).
#
#  Responsibilities:
#    * Force CMAKE_OSX_DEPLOYMENT_TARGET=16.0 (iOS 16+)
#    * Force CMAKE_OSX_ARCHITECTURES=arm64 (no x86_64 simulator slice here)
#    * Locate iphoneos SDK via xcrun
#    * Bitcode: disabled per Xcode 14+; use -fembed-bitcode-marker for thinning
#    * Expose lexora_make_ios_framework(<target>) which configures the target
#      as LexoraEngine.framework
#
#  Output: LexoraEngine.framework  (static binary inside, BUILD_SHARED_LIBS=OFF)
# =============================================================================
include_guard(GLOBAL)

# -----------------------------------------------------------------------------
# iOS deployment target + architecture
# -----------------------------------------------------------------------------
if(NOT CMAKE_OSX_DEPLOYMENT_TARGET)
    set(CMAKE_OSX_DEPLOYMENT_TARGET "16.0" CACHE STRING "iOS deployment target" FORCE)
endif()

# arm64 only — no x86_64 simulator slice. Build simulator binaries through
# Xcode directly (xcodebuild -sdk iphonesimulator) for the sim workflow.
if(NOT CMAKE_OSX_ARCHITECTURES)
    set(CMAKE_OSX_ARCHITECTURES "arm64" CACHE STRING "iOS architecture(s)" FORCE)
endif()

# try_compile must not try to run iOS binaries on the host.
set(CMAKE_TRY_COMPILE_TARGET_TYPE STATIC_LIBRARY)

# -----------------------------------------------------------------------------
# Locate the iphoneos SDK
# -----------------------------------------------------------------------------
if(NOT CMAKE_OSX_SYSROOT)
    execute_process(
        COMMAND xcrun --sdk iphoneos --show-sdk-path
        OUTPUT_VARIABLE _IOS_SDK_PATH
        OUTPUT_STRIP_TRAILING_WHITESPACE
        ERROR_VARIABLE _IOS_SDK_ERR
        RESULT_VARIABLE _IOS_SDK_RC)
    if(_IOS_SDK_RC EQUAL 0 AND _IOS_SDK_PATH)
        set(CMAKE_OSX_SYSROOT "${_IOS_SDK_PATH}" CACHE PATH "iOS SDK root" FORCE)
    else()
        message(WARNING
            "Could not locate iphoneos SDK via xcrun:\n${_IOS_SDK_ERR}\n"
            "Set CMAKE_OSX_SYSROOT manually or run from a shell with Xcode selected "
            "(xcode-select -p).")
    endif()
endif()

message(STATUS "iOS deployment target: ${CMAKE_OSX_DEPLOYMENT_TARGET}")
message(STATUS "iOS architecture:      ${CMAKE_OSX_ARCHITECTURES}")
message(STATUS "iOS sysroot:            ${CMAKE_OSX_SYSROOT}")

# -----------------------------------------------------------------------------
# Bitcode handling (Xcode 14+ drops full bitcode)
# -----------------------------------------------------------------------------
# Use the marker so App Store thinning still has sizing information, but do NOT
# emit full bitcode (ENABLE_BITCODE=NO at the Xcode level).
set(LEXORA_IOS_BITCODE_FLAGS "-fembed-bitcode-marker")

# Framework naming — must match the Java/JNI side (Android .so) and the Swift
# wrapper in the iOS app.
set(LEXORA_IOS_FRAMEWORK_NAME "LexoraEngine")

# -----------------------------------------------------------------------------
# lexora_make_ios_framework(<target>)
#
# Configures <target> as LexoraEngine.framework. The actual FRAMEWORK property
# is also set by the root CMakeLists (in the LEXORA_BUILD_FRAMEWORK branch);
# this function adds the iOS-specific Xcode attributes that cannot live in the
# root file (they only make sense when targeting iOS).
# -----------------------------------------------------------------------------
function(lexora_make_ios_framework _target)
    set_target_properties(${_target} PROPERTIES
        OUTPUT_NAME                     "${LEXORA_IOS_FRAMEWORK_NAME}"
        XCODE_ATTRIBUTE_ENABLE_BITCODE  "NO"
        XCODE_ATTRIBUTE_BITCODE_GENERATION_MODE "marker"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET "${CMAKE_OSX_DEPLOYMENT_TARGET}"
        XCODE_ATTRIBUTE_ARCHS           "${CMAKE_OSX_ARCHITECTURES}"
        XCODE_ATTRIBUTE_TARGETED_DEVICE_FAMILY "1,2"     # iPhone + iPad
        XCODE_ATTRIBUTE_CODE_SIGNING_REQUIRED "NO"
        XCODE_ATTRIBUTE_CODE_SIGN_IDENTITY  "")
    message(STATUS "Configured ${_target} as ${LEXORA_IOS_FRAMEWORK_NAME}.framework "
                   "(arm64, iOS ${CMAKE_OSX_DEPLOYMENT_TARGET}, bitcode=NO marker)")
endfunction()
