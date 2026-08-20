# SPDX-License-Identifier: MIT
# cmake/Dawg.cmake
#
# CMake fragment for the Lexora C++ DAWG module (P3-03-CPP-DAWG).
#
# ----------------------------------------------------------------------------
# TWO MODES
# ----------------------------------------------------------------------------
#
# 1. FRAGMENT MODE (default, used by the parent liblexora-engine build)
#
#    When the parent root CMakeLists.txt includes this file, it expects the
#    fragment to:
#      * Append its source files to the LEXORA_SOURCES list variable.
#      * Append its public headers to the LEXORA_HEADERS list variable.
#      * Declare an OBJECT library named lexora_dawg_obj for isolated
#        compilation (consumed by the root's foreach over module objects).
#
#    The parent then assembles liblexora-engine from LEXORA_SOURCES +
#    $<TARGET_OBJECTS:lexora_*_obj>. This fragment does NOT create its own
#    STATIC/SHARED library in fragment mode — doing so would duplicate
#    symbols and conflict with the aggregate target.
#
# 2. STANDALONE MODE (set -DLEXORA_DAWG_STANDALONE=ON)
#
#    Creates a self-contained `lexora::dawg` STATIC library target plus an
#    optional test executable. Use this to build and test JUST the DAWG
#    module in isolation, e.g.:
#
#      cmake -S native/cpp -B build/dawg -DLEXORA_DAWG_STANDALONE=ON \
#            -DLEXORA_DAWG_BUILD_TESTS=ON
#      cmake --build build/dawg -j
#      ctest --test-dir build/dawg --output-on-failure
#
# ----------------------------------------------------------------------------
# OPTIONS
# ----------------------------------------------------------------------------
#
# LEXORA_DAWG_STANDALONE       (BOOL, default OFF)
#     Force standalone mode (create lexora::dawg STATIC lib + tests).
#
# LEXORA_DAWG_BUILD_TESTS      (BOOL, default OFF)
#     Build the GoogleTest suite (tests/test_dawg.cpp). Implies standalone
#     mode (tests need their own executable target). When OFF in fragment
#     mode, the parent build's aggregate test target picks up
#     tests/test_dawg.cpp via tests/CMakeLists.txt glob.
#
# LEXORA_DAWG_FETCH_GTEST      (BOOL, default ON when tests ON)
#     Fetch GoogleTest via FetchContent. Set OFF to use find_package(GTest)
#     or the bundled tests/gtest_lite.hpp shim.

cmake_minimum_required(VERSION 3.16)

# Resolve the module root: this file lives at <root>/cmake/Dawg.cmake.
get_filename_component(_lexora_dawg_dir
    "${CMAKE_CURRENT_LIST_FILE}" DIRECTORY)
get_filename_component(_lexora_dawg_dir
    "${_lexora_dawg_dir}/.." ABSOLUTE)
set(LEXORA_DAWG_ROOT "${_lexora_dawg_dir}"
    CACHE PATH "Root of the Lexora DAWG C++ module")

# Source / header / test manifest.
set(LEXORA_DAWG_SOURCES
    "${LEXORA_DAWG_ROOT}/src/dawg.cpp"
)
set(LEXORA_DAWG_HEADERS
    "${LEXORA_DAWG_ROOT}/include/lexora/dawg.hpp"
)
set(LEXORA_DAWG_TESTS
    "${LEXORA_DAWG_ROOT}/tests/test_dawg.cpp"
)

# ---------------------------------------------------------------------------
# Decide mode.
# ---------------------------------------------------------------------------
option(LEXORA_DAWG_BUILD_TESTS
       "Build the Lexora DAWG GoogleTest suite" OFF)
option(LEXORA_DAWG_STANDALONE
       "Create a standalone lexora::dawg library (not a fragment)" OFF)

if(LEXORA_DAWG_BUILD_TESTS AND NOT LEXORA_DAWG_STANDALONE)
    message(STATUS "Dawg: LEXORA_DAWG_BUILD_TESTS=ON implies "
                   "standalone mode (creating dedicated test executable).")
    set(LEXORA_DAWG_STANDALONE ON)
endif()

# ---------------------------------------------------------------------------
# FRAGMENT MODE: hand our sources/headers to the parent build.
# ---------------------------------------------------------------------------
# Always populate LEXORA_SOURCES / LEXORA_HEADERS (even in standalone mode)
# so a parent build that includes this file opportunistically still sees our
# files.
if(DEFINED LEXORA_SOURCES OR DEFINED LEXORA_HEADERS)
    list(APPEND LEXORA_SOURCES  ${LEXORA_DAWG_SOURCES})
    list(APPEND LEXORA_HEADERS ${LEXORA_DAWG_HEADERS})
endif()

# Per-module OBJECT library (the root's "lexora_<module>_obj" convention).
# The root's foreach(_mod hash_table levenshtein dawg unicode engine) loop
# collects $<TARGET_OBJECTS:lexora_dawg_obj> into liblexora-engine.
if(NOT TARGET lexora_dawg_obj)
    add_library(lexora_dawg_obj OBJECT
        ${LEXORA_DAWG_SOURCES}
    )
    target_include_directories(lexora_dawg_obj PUBLIC
        ${LEXORA_DAWG_ROOT}/include
    )
    target_compile_features(lexora_dawg_obj PUBLIC cxx_std_17)
    find_package(Threads REQUIRED)
    target_link_libraries(lexora_dawg_obj PUBLIC Threads::Threads)
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(lexora_dawg_obj PRIVATE
            -Wall -Wextra -Wpedantic -Wno-unused-parameter)
    elseif(MSVC)
        target_compile_options(lexora_dawg_obj PRIVATE
            /W4 /permissive- /wd4127)
    endif()
endif()

# ---------------------------------------------------------------------------
# STANDALONE MODE: create the aggregate library + optional tests.
# ---------------------------------------------------------------------------
if(LEXORA_DAWG_STANDALONE)

    if(NOT TARGET lexora_dawg)
        add_library(lexora_dawg STATIC
            ${LEXORA_DAWG_SOURCES}
        )
        add_library(lexora::dawg ALIAS lexora_dawg)

        target_sources(lexora_dawg
            PUBLIC
                FILE_SET HEADERS
                BASE_DIRS "${LEXORA_DAWG_ROOT}/include"
                FILES ${LEXORA_DAWG_HEADERS}
        )

        target_include_directories(lexora_dawg
            PUBLIC
                $<BUILD_INTERFACE:${LEXORA_DAWG_ROOT}/include>
                $<INSTALL_INTERFACE:include>
        )

        target_compile_features(lexora_dawg PUBLIC cxx_std_17)

        find_package(Threads REQUIRED)
        target_link_libraries(lexora_dawg PUBLIC Threads::Threads)

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexora_dawg PRIVATE
                -Wall -Wextra -Wpedantic -Wno-unused-parameter)
        elseif(MSVC)
            target_compile_options(lexora_dawg PRIVATE
                /W4 /permissive- /wd4127)
        endif()
    endif()

    # ----- Tests ---------------------------------------------------------
    if(LEXORA_DAWG_BUILD_TESTS)
        option(LEXORA_DAWG_FETCH_GTEST
               "Fetch GoogleTest via FetchContent" ON)

        enable_testing()

        if(LEXORA_DAWG_FETCH_GTEST)
            include(FetchContent)
            FetchContent_Declare(
                googletest
                GIT_REPOSITORY "https://github.com/google/googletest.git"
                GIT_TAG        v1.14.0
            )
            set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
            FetchContent_MakeAvailable(googletest)
            set(_lexora_dawg_gtest_libs gtest gtest_main)
            set(_lexora_dawg_gtest_inc "")
        else()
            find_package(GTest QUIET)
            if(GTest_FOUND)
                set(_lexora_dawg_gtest_libs GTest::gtest GTest::gtest_main)
                set(_lexora_dawg_gtest_inc "")
            else()
                # Fall back to the bundled gtest_lite shim.
                set(_lexora_dawg_gtest_libs "")
                set(_lexora_dawg_gtest_inc ${LEXORA_DAWG_ROOT}/tests)
            endif()
        endif()

        add_executable(lexora_dawg_test ${LEXORA_DAWG_TESTS})
        target_link_libraries(lexora_dawg_test
            PRIVATE lexora::dawg ${_lexora_dawg_gtest_libs})
        target_include_directories(lexora_dawg_test
            PRIVATE ${_lexora_dawg_gtest_inc})
        target_compile_features(lexora_dawg_test PRIVATE cxx_std_17)

        # When using the gtest_lite shim (no real GoogleTest), the test
        # binary needs a main() — provided by LEXORA_USE_GTEST_LITE_MAIN.
        if(NOT LEXORA_DAWG_FETCH_GTEST AND NOT GTest_FOUND)
            target_compile_definitions(lexora_dawg_test PRIVATE
                LEXORA_USE_GTEST_LITE_MAIN)
        endif()

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexora_dawg_test PRIVATE
                -Wall -Wextra -Wpedantic)
        endif()

        include(CTest)
        add_test(NAME lexora_dawg_test
                 COMMAND lexora_dawg_test)

        add_custom_target(lexora_dawg_all
            DEPENDS lexora_dawg lexora_dawg_test
            COMMENT "Building Lexora DAWG (lib + tests)"
        )
    else()
        add_custom_target(lexora_dawg_all
            DEPENDS lexora_dawg
            COMMENT "Building Lexora DAWG (lib)"
        )
    endif()
endif()

# ---------------------------------------------------------------------------
# Configuration summary.
# ---------------------------------------------------------------------------
message(STATUS "Dawg fragment:")
message(STATUS "  root            = ${LEXORA_DAWG_ROOT}")
message(STATUS "  standalone      = ${LEXORA_DAWG_STANDALONE}")
message(STATUS "  build tests     = ${LEXORA_DAWG_BUILD_TESTS}")
