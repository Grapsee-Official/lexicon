# SPDX-License-Identifier: MIT
# cmake/Dawg.cmake
#
# CMake fragment for the Lexicon C++ DAWG module (P3-03-CPP-DAWG).
#
# ----------------------------------------------------------------------------
# TWO MODES
# ----------------------------------------------------------------------------
#
# 1. FRAGMENT MODE (default, used by the parent liblexicon-engine build)
#
#    When the parent root CMakeLists.txt includes this file, it expects the
#    fragment to:
#      * Append its source files to the LEXICON_SOURCES list variable.
#      * Append its public headers to the LEXICON_HEADERS list variable.
#      * Declare an OBJECT library named lexicon_dawg_obj for isolated
#        compilation (consumed by the root's foreach over module objects).
#
#    The parent then assembles liblexicon-engine from LEXICON_SOURCES +
#    $<TARGET_OBJECTS:lexicon_*_obj>. This fragment does NOT create its own
#    STATIC/SHARED library in fragment mode — doing so would duplicate
#    symbols and conflict with the aggregate target.
#
# 2. STANDALONE MODE (set -DLEXICON_DAWG_STANDALONE=ON)
#
#    Creates a self-contained `lexicon::dawg` STATIC library target plus an
#    optional test executable. Use this to build and test JUST the DAWG
#    module in isolation, e.g.:
#
#      cmake -S native/cpp -B build/dawg -DLEXICON_DAWG_STANDALONE=ON \
#            -DLEXICON_DAWG_BUILD_TESTS=ON
#      cmake --build build/dawg -j
#      ctest --test-dir build/dawg --output-on-failure
#
# ----------------------------------------------------------------------------
# OPTIONS
# ----------------------------------------------------------------------------
#
# LEXICON_DAWG_STANDALONE       (BOOL, default OFF)
#     Force standalone mode (create lexicon::dawg STATIC lib + tests).
#
# LEXICON_DAWG_BUILD_TESTS      (BOOL, default OFF)
#     Build the GoogleTest suite (tests/test_dawg.cpp). Implies standalone
#     mode (tests need their own executable target). When OFF in fragment
#     mode, the parent build's aggregate test target picks up
#     tests/test_dawg.cpp via tests/CMakeLists.txt glob.
#
# LEXICON_DAWG_FETCH_GTEST      (BOOL, default ON when tests ON)
#     Fetch GoogleTest via FetchContent. Set OFF to use find_package(GTest)
#     or the bundled tests/gtest_lite.hpp shim.

cmake_minimum_required(VERSION 3.16)

# Resolve the module root: this file lives at <root>/cmake/Dawg.cmake.
get_filename_component(_lexicon_dawg_dir
    "${CMAKE_CURRENT_LIST_FILE}" DIRECTORY)
get_filename_component(_lexicon_dawg_dir
    "${_lexicon_dawg_dir}/.." ABSOLUTE)
set(LEXICON_DAWG_ROOT "${_lexicon_dawg_dir}"
    CACHE PATH "Root of the Lexicon DAWG C++ module")

# Source / header / test manifest.
set(LEXICON_DAWG_SOURCES
    "${LEXICON_DAWG_ROOT}/src/dawg.cpp"
)
set(LEXICON_DAWG_HEADERS
    "${LEXICON_DAWG_ROOT}/include/lexicon/dawg.hpp"
)
set(LEXICON_DAWG_TESTS
    "${LEXICON_DAWG_ROOT}/tests/test_dawg.cpp"
)

# ---------------------------------------------------------------------------
# Decide mode.
# ---------------------------------------------------------------------------
option(LEXICON_DAWG_BUILD_TESTS
       "Build the Lexicon DAWG GoogleTest suite" OFF)
option(LEXICON_DAWG_STANDALONE
       "Create a standalone lexicon::dawg library (not a fragment)" OFF)

if(LEXICON_DAWG_BUILD_TESTS AND NOT LEXICON_DAWG_STANDALONE)
    message(STATUS "Dawg: LEXICON_DAWG_BUILD_TESTS=ON implies "
                   "standalone mode (creating dedicated test executable).")
    set(LEXICON_DAWG_STANDALONE ON)
endif()

# ---------------------------------------------------------------------------
# FRAGMENT MODE: hand our sources/headers to the parent build.
# ---------------------------------------------------------------------------
# Always populate LEXICON_SOURCES / LEXICON_HEADERS (even in standalone mode)
# so a parent build that includes this file opportunistically still sees our
# files.
if(DEFINED LEXICON_SOURCES OR DEFINED LEXICON_HEADERS)
    list(APPEND LEXICON_SOURCES  ${LEXICON_DAWG_SOURCES})
    list(APPEND LEXICON_HEADERS ${LEXICON_DAWG_HEADERS})
endif()

# Per-module OBJECT library (the root's "lexicon_<module>_obj" convention).
# The root's foreach(_mod hash_table levenshtein dawg unicode engine) loop
# collects $<TARGET_OBJECTS:lexicon_dawg_obj> into liblexicon-engine.
if(NOT TARGET lexicon_dawg_obj)
    add_library(lexicon_dawg_obj OBJECT
        ${LEXICON_DAWG_SOURCES}
    )
    target_include_directories(lexicon_dawg_obj PUBLIC
        ${LEXICON_DAWG_ROOT}/include
    )
    target_compile_features(lexicon_dawg_obj PUBLIC cxx_std_17)
    find_package(Threads REQUIRED)
    target_link_libraries(lexicon_dawg_obj PUBLIC Threads::Threads)
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(lexicon_dawg_obj PRIVATE
            -Wall -Wextra -Wpedantic -Wno-unused-parameter)
    elseif(MSVC)
        target_compile_options(lexicon_dawg_obj PRIVATE
            /W4 /permissive- /wd4127)
    endif()
endif()

# ---------------------------------------------------------------------------
# STANDALONE MODE: create the aggregate library + optional tests.
# ---------------------------------------------------------------------------
if(LEXICON_DAWG_STANDALONE)

    if(NOT TARGET lexicon_dawg)
        add_library(lexicon_dawg STATIC
            ${LEXICON_DAWG_SOURCES}
        )
        add_library(lexicon::dawg ALIAS lexicon_dawg)

        target_sources(lexicon_dawg
            PUBLIC
                FILE_SET HEADERS
                BASE_DIRS "${LEXICON_DAWG_ROOT}/include"
                FILES ${LEXICON_DAWG_HEADERS}
        )

        target_include_directories(lexicon_dawg
            PUBLIC
                $<BUILD_INTERFACE:${LEXICON_DAWG_ROOT}/include>
                $<INSTALL_INTERFACE:include>
        )

        target_compile_features(lexicon_dawg PUBLIC cxx_std_17)

        find_package(Threads REQUIRED)
        target_link_libraries(lexicon_dawg PUBLIC Threads::Threads)

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexicon_dawg PRIVATE
                -Wall -Wextra -Wpedantic -Wno-unused-parameter)
        elseif(MSVC)
            target_compile_options(lexicon_dawg PRIVATE
                /W4 /permissive- /wd4127)
        endif()
    endif()

    # ----- Tests ---------------------------------------------------------
    if(LEXICON_DAWG_BUILD_TESTS)
        option(LEXICON_DAWG_FETCH_GTEST
               "Fetch GoogleTest via FetchContent" ON)

        enable_testing()

        if(LEXICON_DAWG_FETCH_GTEST)
            include(FetchContent)
            FetchContent_Declare(
                googletest
                GIT_REPOSITORY "https://github.com/google/googletest.git"
                GIT_TAG        v1.14.0
            )
            set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
            FetchContent_MakeAvailable(googletest)
            set(_lexicon_dawg_gtest_libs gtest gtest_main)
            set(_lexicon_dawg_gtest_inc "")
        else()
            find_package(GTest QUIET)
            if(GTest_FOUND)
                set(_lexicon_dawg_gtest_libs GTest::gtest GTest::gtest_main)
                set(_lexicon_dawg_gtest_inc "")
            else()
                # Fall back to the bundled gtest_lite shim.
                set(_lexicon_dawg_gtest_libs "")
                set(_lexicon_dawg_gtest_inc ${LEXICON_DAWG_ROOT}/tests)
            endif()
        endif()

        add_executable(lexicon_dawg_test ${LEXICON_DAWG_TESTS})
        target_link_libraries(lexicon_dawg_test
            PRIVATE lexicon::dawg ${_lexicon_dawg_gtest_libs})
        target_include_directories(lexicon_dawg_test
            PRIVATE ${_lexicon_dawg_gtest_inc})
        target_compile_features(lexicon_dawg_test PRIVATE cxx_std_17)

        # When using the gtest_lite shim (no real GoogleTest), the test
        # binary needs a main() — provided by LEXICON_USE_GTEST_LITE_MAIN.
        if(NOT LEXICON_DAWG_FETCH_GTEST AND NOT GTest_FOUND)
            target_compile_definitions(lexicon_dawg_test PRIVATE
                LEXICON_USE_GTEST_LITE_MAIN)
        endif()

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexicon_dawg_test PRIVATE
                -Wall -Wextra -Wpedantic)
        endif()

        include(CTest)
        add_test(NAME lexicon_dawg_test
                 COMMAND lexicon_dawg_test)

        add_custom_target(lexicon_dawg_all
            DEPENDS lexicon_dawg lexicon_dawg_test
            COMMENT "Building Lexicon DAWG (lib + tests)"
        )
    else()
        add_custom_target(lexicon_dawg_all
            DEPENDS lexicon_dawg
            COMMENT "Building Lexicon DAWG (lib)"
        )
    endif()
endif()

# ---------------------------------------------------------------------------
# Configuration summary.
# ---------------------------------------------------------------------------
message(STATUS "Dawg fragment:")
message(STATUS "  root            = ${LEXICON_DAWG_ROOT}")
message(STATUS "  standalone      = ${LEXICON_DAWG_STANDALONE}")
message(STATUS "  build tests     = ${LEXICON_DAWG_BUILD_TESTS}")
