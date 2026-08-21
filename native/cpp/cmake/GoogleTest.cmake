# =============================================================================
#  cmake/GoogleTest.cmake — acquire Google Test WITHOUT network access.
#
#  Order of preference:
#    1. Vendored source tree at tests/googletest/ (FetchContent w/ SOURCE_DIR)
#    2. System-installed GoogleTest via find_package(GTest)
#    3. (Optional) Network fetch via FetchContent — only if
#       -DLEXORA_ALLOW_GTEST_DOWNLOAD=ON
#    4. Otherwise: skip tests with a warning (offline-friendly default).
#
#  Exposes:
#    LEXORA_GTEST_AVAILABLE  : TRUE / FALSE
#    LEXORA_GTEST_LIBS       : list of link targets (gtest gtest_main, or
#                              GTest::gtest GTest::gtest_main)
# =============================================================================
include_guard(GLOBAL)

# Default GoogleTest tag (used only if a network fetch is explicitly enabled).
set(_LEXORA_GTEST_TAG "release-1.14.0")

# (1) Vendored copy at tests/googletest/.
set(_LEXORA_GTEST_VENDORED_DIR ${CMAKE_SOURCE_DIR}/tests/googletest)

if(EXISTS "${_LEXORA_GTEST_VENDORED_DIR}/CMakeLists.txt")
    message(STATUS "GoogleTest: using vendored source at ${_LEXORA_GTEST_VENDORED_DIR}")
    include(FetchContent)
    FetchContent_Declare(
        googletest
        SOURCE_DIR ${_LEXORA_GTEST_VENDORED_DIR})
    # Source dir mode never triggers a download — uses the local tree as-is.
    FetchContent_MakeAvailable(googletest)
    # Hide gtest's own test/pthread defaults from the outer build.
    set(BUILD_GMOCK OFF CACHE BOOL "" FORCE)
    set(INSTALL_GTEST OFF CACHE BOOL "" FORCE)
    set(LEXORA_GTEST_AVAILABLE TRUE)
    set(LEXORA_GTEST_LIBS gtest gtest_main)

elseif(LEXORA_ALLOW_GTEST_DOWNLOAD)
    message(STATUS "GoogleTest: LEXORA_ALLOW_GTEST_DOWNLOAD=ON — fetching ${_LEXORA_GTEST_TAG} from GitHub.")
    include(FetchContent)
    FetchContent_Declare(
        googletest
        GIT_REPOSITORY https://github.com/google/googletest.git
        GIT_TAG        ${_LEXORA_GTEST_TAG})
    set(BUILD_GMOCK OFF CACHE BOOL "" FORCE)
    set(INSTALL_GTEST OFF CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(googletest)
    set(LEXORA_GTEST_AVAILABLE TRUE)
    set(LEXORA_GTEST_LIBS gtest gtest_main)

else()
    # (2) System-installed GoogleTest.
    find_package(GTest QUIET)
    if(GTest_FOUND)
        message(STATUS "GoogleTest: using system install (version ${GTest_VERSION})")
        set(LEXORA_GTEST_AVAILABLE TRUE)
        set(LEXORA_GTEST_LIBS GTest::gtest GTest::gtest_main)
    else()
        # (3) Nothing found — skip tests gracefully.
        message(WARNING
            "Google Test is NOT available:\n"
            "  - No vendored copy at tests/googletest/\n"
            "  - No system GTest findable via find_package(GTest)\n"
            "  - Network download is disabled by default (offline-friendly)\n"
            "Skipping test build. To enable, choose ONE of:\n"
            "  (a) git clone --depth 1 --branch release-1.14.0 \\\n"
            "        https://github.com/google/googletest.git tests/googletest\n"
            "  (b) install libgtest-dev (Debian/Ubuntu) or gtest-devel (Fedora), or\n"
            "  (c) reconfigure with -DLEXORA_ALLOW_GTEST_DOWNLOAD=ON to fetch over network.")
        set(LEXORA_GTEST_AVAILABLE FALSE)
        set(LEXORA_GTEST_LIBS "")
    endif()
endif()
