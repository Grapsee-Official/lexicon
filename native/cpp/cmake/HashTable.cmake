# SPDX-License-Identifier: MIT
# cmake/HashTable.cmake
#
# CMake fragment for the Lexicon C++ Hash Table module (P3-01-CPP-HASHTABLE).
#
# ----------------------------------------------------------------------------
# TWO MODES
# ----------------------------------------------------------------------------
#
# This file supports two inclusion modes so it works both as a fragment of
# the parent Lexicon engine build AND as a standalone module for isolated
# development / testing.
#
# 1. FRAGMENT MODE (default, used by the parent liblexicon-engine build)
#
#    When the parent root CMakeLists.txt includes this file, it expects the
#    fragment to:
#      * Append its source files to the LEXICON_SOURCES list variable.
#      * Append its public headers to the LEXICON_HEADERS list variable.
#      * Optionally declare an OBJECT library named lexicon_<module>_obj for
#        isolated compilation (used by LEXICON_PER_FILE_TESTS and for finer
#        dependency tracking).
#
#    The parent then assembles liblexicon-engine from LEXICON_SOURCES. This
#    fragment does NOT create its own STATIC/SHARED library in this mode --
#    doing so would duplicate symbols and conflict with the aggregate target.
#
# 2. STANDALONE MODE (set -DLEXICON_HASH_TABLE_STANDALONE=ON)
#
#    Creates a self-contained `lexicon::hash_table` STATIC library target
#    plus an optional test executable. Use this to build and test JUST the
#    hash table module in isolation, e.g.:
#
#      cmake -S native/cpp -B build/ht -DLEXICON_HASH_TABLE_STANDALONE=ON \
#            -DLEXICON_HASH_TABLE_BUILD_TESTS=ON
#      cmake --build build/ht -j
#      ctest --test-dir build/ht --output-on-failure
#
# ----------------------------------------------------------------------------
# OPTIONS
# ----------------------------------------------------------------------------
#
# LEXICON_HASH_TABLE_STANDALONE       (BOOL, default OFF)
#     Force standalone mode (create lexicon::hash_table STATIC lib + tests).
#     When OFF, the fragment mode is used (append to LEXICON_SOURCES etc.).
#
# LEXICON_HASH_TABLE_BUILD_TESTS      (BOOL, default OFF)
#     Build the GoogleTest suite (tests/test_hash_table.cpp). Implies
#     standalone mode (tests need their own executable target). When OFF in
#     fragment mode, the parent build's aggregate test target is expected to
#     pick up tests/test_hash_table.cpp via tests/CMakeLists.txt.
#
# LEXICON_HASH_TABLE_FETCH_GTEST      (BOOL, default ON when tests ON)
#     Fetch GoogleTest via FetchContent. Set OFF to use find_package(GTest).
#
# LEXICON_GTEST_GIT_URL               (STRING, default official GoogleTest URL)
#     Override the gtest fetch URL (for offline mirrors).

cmake_minimum_required(VERSION 3.16)

# Resolve the module root: this file lives at <root>/cmake/HashTable.cmake.
get_filename_component(_lexicon_ht_dir
    "${CMAKE_CURRENT_LIST_FILE}" DIRECTORY)
get_filename_component(_lexicon_ht_dir
    "${_lexicon_ht_dir}/.." ABSOLUTE)
set(LEXICON_HASH_TABLE_ROOT "${_lexicon_ht_dir}"
    CACHE PATH "Root of the Lexicon hash table C++ module")

# Source / header manifest for this module. Defined unconditionally so both
# modes (and the parent build) can reference them.
set(LEXICON_HASH_TABLE_SOURCES
    "${LEXICON_HASH_TABLE_ROOT}/src/hash_table.cpp"
)
set(LEXICON_HASH_TABLE_HEADERS
    "${LEXICON_HASH_TABLE_ROOT}/include/lexicon/hash_table.hpp"
    "${LEXICON_HASH_TABLE_ROOT}/include/lexicon/hash_table.inl"
)
set(LEXICON_HASH_TABLE_TESTS
    "${LEXICON_HASH_TABLE_ROOT}/tests/test_hash_table.cpp"
)

# ---------------------------------------------------------------------------
# Decide mode.
# ---------------------------------------------------------------------------
# Tests require their own executable target, which only makes sense in
# standalone mode. If the caller asked for tests but not standalone, force
# standalone on with a status message.
option(LEXICON_HASH_TABLE_BUILD_TESTS
       "Build the Lexicon hash table GoogleTest suite" OFF)
option(LEXICON_HASH_TABLE_STANDALONE
       "Create a standalone lexicon::hash_table library (not a fragment)" OFF)

# The hash table has a hard dependency on the Lexicon Unicode module
# (src/unicode.cpp, header include/lexicon/unicode.hpp) for NFC normalization
# of keys. In fragment mode the parent build globs src/*.cpp so unicode.cpp
# is always picked up. In standalone mode we explicitly add it below.

if(LEXICON_HASH_TABLE_BUILD_TESTS AND NOT LEXICON_HASH_TABLE_STANDALONE)
    message(STATUS "HashTable: LEXICON_HASH_TABLE_BUILD_TESTS=ON implies "
                   "standalone mode (creating dedicated test executable).")
    set(LEXICON_HASH_TABLE_STANDALONE ON)
endif()

# ---------------------------------------------------------------------------
# FRAGMENT MODE: hand our sources/headers to the parent build.
# ---------------------------------------------------------------------------
# Always populate LEXICON_SOURCES / LEXICON_HEADERS (even in standalone mode)
# so a parent build that includes this file opportunistically still sees our
# files. The standalone-mode target is created in ADDITION, guarded so it
# doesn't conflict.
if(DEFINED LEXICON_SOURCES OR DEFINED LEXICON_HEADERS)
    list(APPEND LEXICON_SOURCES  ${LEXICON_HASH_TABLE_SOURCES})
    list(APPEND LEXICON_HEADERS ${LEXICON_HASH_TABLE_HEADERS})
endif()

# Per-module OBJECT library (the README's "lexicon_<module>_obj" convention).
# Useful for: per-file tests, dependency isolation, and assembling custom
# subsets of the engine. Always declared -- it's cheap and opt-in to use.
if(NOT TARGET lexicon_hash_table_obj)
    add_library(lexicon_hash_table_obj OBJECT
        ${LEXICON_HASH_TABLE_SOURCES}
    )
    target_include_directories(lexicon_hash_table_obj PUBLIC
        ${LEXICON_HASH_TABLE_ROOT}/include
    )
    target_compile_features(lexicon_hash_table_obj PUBLIC cxx_std_17)
    find_package(Threads REQUIRED)
    target_link_libraries(lexicon_hash_table_obj PUBLIC Threads::Threads)
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(lexicon_hash_table_obj PRIVATE
            -Wall -Wextra -Wpedantic -Wno-unused-parameter)
    elseif(MSVC)
        target_compile_options(lexicon_hash_table_obj PRIVATE
            /W4 /permissive- /wd4127)
    endif()
endif()

# ---------------------------------------------------------------------------
# STANDALONE MODE: create the aggregate library + optional tests.
# ---------------------------------------------------------------------------
if(LEXICON_HASH_TABLE_STANDALONE)

    if(NOT TARGET lexicon_hash_table)
        add_library(lexicon_hash_table STATIC
            ${LEXICON_HASH_TABLE_SOURCES}
        )
        add_library(lexicon::hash_table ALIAS lexicon_hash_table)

        target_sources(lexicon_hash_table
            PUBLIC
                FILE_SET HEADERS
                BASE_DIRS "${LEXICON_HASH_TABLE_ROOT}/include"
                FILES ${LEXICON_HASH_TABLE_HEADERS}
        )

        target_include_directories(lexicon_hash_table
            PUBLIC
                $<BUILD_INTERFACE:${LEXICON_HASH_TABLE_ROOT}/include>
                $<INSTALL_INTERFACE:include>
        )

        target_compile_features(lexicon_hash_table PUBLIC cxx_std_17)

        find_package(Threads REQUIRED)
        target_link_libraries(lexicon_hash_table PUBLIC Threads::Threads)

        # Hard dependency on the Lexicon Unicode module (NFC normalization).
        # Compile unicode.cpp into the standalone library so the symbol is
        # always available without requiring the caller to know about it.
        target_sources(lexicon_hash_table PRIVATE
            "${LEXICON_HASH_TABLE_ROOT}/src/unicode.cpp"
        )

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexicon_hash_table PRIVATE
                -Wall -Wextra -Wpedantic -Wno-unused-parameter)
        elseif(MSVC)
            target_compile_options(lexicon_hash_table PRIVATE
                /W4 /permissive- /wd4127)
        endif()
    endif()

    # ----- Tests ---------------------------------------------------------
    if(LEXICON_HASH_TABLE_BUILD_TESTS)
        option(LEXICON_HASH_TABLE_FETCH_GTEST
               "Fetch GoogleTest via FetchContent" ON)
        set(LEXICON_GTEST_GIT_URL
            "https://github.com/google/googletest.git"
            CACHE STRING "GoogleTest fetch URL (override for offline mirrors)")

        enable_testing()

        if(LEXICON_HASH_TABLE_FETCH_GTEST)
            include(FetchContent)
            FetchContent_Declare(
                googletest
                GIT_REPOSITORY "${LEXICON_GTEST_GIT_URL}"
                GIT_TAG        v1.14.0
            )
            set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
            FetchContent_MakeAvailable(googletest)
            set(_lexicon_gtest_libs gtest gtest_main)
            set(_lexicon_gtest_inc "")
        else()
            find_package(GTest REQUIRED)
            set(_lexicon_gtest_libs GTest::GTest GTest::Main)
            set(_lexicon_gtest_inc ${GTEST_INCLUDE_DIRS})
        endif()

        add_executable(lexicon_hash_table_tests ${LEXICON_HASH_TABLE_TESTS})
        target_link_libraries(lexicon_hash_table_tests
            PRIVATE lexicon::hash_table ${_lexicon_gtest_libs})
        target_include_directories(lexicon_hash_table_tests
            PRIVATE ${_lexicon_gtest_inc})
        target_compile_features(lexicon_hash_table_tests PRIVATE cxx_std_17)
        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexicon_hash_table_tests PRIVATE
                -Wall -Wextra -Wpedantic)
        endif()

        include(CTest)
        add_test(NAME lexicon_hash_table_tests
                 COMMAND lexicon_hash_table_tests)

        add_custom_target(lexicon_hash_table_all
            DEPENDS lexicon_hash_table lexicon_hash_table_tests
            COMMENT "Building Lexicon hash table (lib + tests)"
        )
    else()
        add_custom_target(lexicon_hash_table_all
            DEPENDS lexicon_hash_table
            COMMENT "Building Lexicon hash table (lib)"
        )
    endif()
endif()

# ---------------------------------------------------------------------------
# Configuration summary.
# ---------------------------------------------------------------------------
message(STATUS "HashTable fragment:")
message(STATUS "  root            = ${LEXICON_HASH_TABLE_ROOT}")
message(STATUS "  standalone      = ${LEXICON_HASH_TABLE_STANDALONE}")
message(STATUS "  build tests     = ${LEXICON_HASH_TABLE_BUILD_TESTS}")
message(STATUS "  depends on      = lexicon::unicode (src/unicode.cpp)")
