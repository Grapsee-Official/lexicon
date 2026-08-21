# SPDX-License-Identifier: MIT
# cmake/HashTable.cmake
#
# CMake fragment for the Lexora C++ Hash Table module (P3-01-CPP-HASHTABLE).
#
# ----------------------------------------------------------------------------
# TWO MODES
# ----------------------------------------------------------------------------
#
# This file supports two inclusion modes so it works both as a fragment of
# the parent Lexora engine build AND as a standalone module for isolated
# development / testing.
#
# 1. FRAGMENT MODE (default, used by the parent liblexora-engine build)
#
#    When the parent root CMakeLists.txt includes this file, it expects the
#    fragment to:
#      * Append its source files to the LEXORA_SOURCES list variable.
#      * Append its public headers to the LEXORA_HEADERS list variable.
#      * Optionally declare an OBJECT library named lexora_<module>_obj for
#        isolated compilation (used by LEXORA_PER_FILE_TESTS and for finer
#        dependency tracking).
#
#    The parent then assembles liblexora-engine from LEXORA_SOURCES. This
#    fragment does NOT create its own STATIC/SHARED library in this mode --
#    doing so would duplicate symbols and conflict with the aggregate target.
#
# 2. STANDALONE MODE (set -DLEXORA_HASH_TABLE_STANDALONE=ON)
#
#    Creates a self-contained `lexora::hash_table` STATIC library target
#    plus an optional test executable. Use this to build and test JUST the
#    hash table module in isolation, e.g.:
#
#      cmake -S native/cpp -B build/ht -DLEXORA_HASH_TABLE_STANDALONE=ON \
#            -DLEXORA_HASH_TABLE_BUILD_TESTS=ON
#      cmake --build build/ht -j
#      ctest --test-dir build/ht --output-on-failure
#
# ----------------------------------------------------------------------------
# OPTIONS
# ----------------------------------------------------------------------------
#
# LEXORA_HASH_TABLE_STANDALONE       (BOOL, default OFF)
#     Force standalone mode (create lexora::hash_table STATIC lib + tests).
#     When OFF, the fragment mode is used (append to LEXORA_SOURCES etc.).
#
# LEXORA_HASH_TABLE_BUILD_TESTS      (BOOL, default OFF)
#     Build the GoogleTest suite (tests/test_hash_table.cpp). Implies
#     standalone mode (tests need their own executable target). When OFF in
#     fragment mode, the parent build's aggregate test target is expected to
#     pick up tests/test_hash_table.cpp via tests/CMakeLists.txt.
#
# LEXORA_HASH_TABLE_FETCH_GTEST      (BOOL, default ON when tests ON)
#     Fetch GoogleTest via FetchContent. Set OFF to use find_package(GTest).
#
# LEXORA_GTEST_GIT_URL               (STRING, default official GoogleTest URL)
#     Override the gtest fetch URL (for offline mirrors).

cmake_minimum_required(VERSION 3.16)

# Resolve the module root: this file lives at <root>/cmake/HashTable.cmake.
get_filename_component(_lexora_ht_dir
    "${CMAKE_CURRENT_LIST_FILE}" DIRECTORY)
get_filename_component(_lexora_ht_dir
    "${_lexora_ht_dir}/.." ABSOLUTE)
set(LEXORA_HASH_TABLE_ROOT "${_lexora_ht_dir}"
    CACHE PATH "Root of the Lexora hash table C++ module")

# Source / header manifest for this module. Defined unconditionally so both
# modes (and the parent build) can reference them.
set(LEXORA_HASH_TABLE_SOURCES
    "${LEXORA_HASH_TABLE_ROOT}/src/hash_table.cpp"
)
set(LEXORA_HASH_TABLE_HEADERS
    "${LEXORA_HASH_TABLE_ROOT}/include/lexora/hash_table.hpp"
    "${LEXORA_HASH_TABLE_ROOT}/include/lexora/hash_table.inl"
)
set(LEXORA_HASH_TABLE_TESTS
    "${LEXORA_HASH_TABLE_ROOT}/tests/test_hash_table.cpp"
)

# ---------------------------------------------------------------------------
# Decide mode.
# ---------------------------------------------------------------------------
# Tests require their own executable target, which only makes sense in
# standalone mode. If the caller asked for tests but not standalone, force
# standalone on with a status message.
option(LEXORA_HASH_TABLE_BUILD_TESTS
       "Build the Lexora hash table GoogleTest suite" OFF)
option(LEXORA_HASH_TABLE_STANDALONE
       "Create a standalone lexora::hash_table library (not a fragment)" OFF)

# The hash table has a hard dependency on the Lexora Unicode module
# (src/unicode.cpp, header include/lexora/unicode.hpp) for NFC normalization
# of keys. In fragment mode the parent build globs src/*.cpp so unicode.cpp
# is always picked up. In standalone mode we explicitly add it below.

if(LEXORA_HASH_TABLE_BUILD_TESTS AND NOT LEXORA_HASH_TABLE_STANDALONE)
    message(STATUS "HashTable: LEXORA_HASH_TABLE_BUILD_TESTS=ON implies "
                   "standalone mode (creating dedicated test executable).")
    set(LEXORA_HASH_TABLE_STANDALONE ON)
endif()

# ---------------------------------------------------------------------------
# FRAGMENT MODE: hand our sources/headers to the parent build.
# ---------------------------------------------------------------------------
# Always populate LEXORA_SOURCES / LEXORA_HEADERS (even in standalone mode)
# so a parent build that includes this file opportunistically still sees our
# files. The standalone-mode target is created in ADDITION, guarded so it
# doesn't conflict.
if(DEFINED LEXORA_SOURCES OR DEFINED LEXORA_HEADERS)
    list(APPEND LEXORA_SOURCES  ${LEXORA_HASH_TABLE_SOURCES})
    list(APPEND LEXORA_HEADERS ${LEXORA_HASH_TABLE_HEADERS})
endif()

# Per-module OBJECT library (the README's "lexora_<module>_obj" convention).
# Useful for: per-file tests, dependency isolation, and assembling custom
# subsets of the engine. Always declared -- it's cheap and opt-in to use.
if(NOT TARGET lexora_hash_table_obj)
    add_library(lexora_hash_table_obj OBJECT
        ${LEXORA_HASH_TABLE_SOURCES}
    )
    target_include_directories(lexora_hash_table_obj PUBLIC
        ${LEXORA_HASH_TABLE_ROOT}/include
    )
    target_compile_features(lexora_hash_table_obj PUBLIC cxx_std_17)
    find_package(Threads REQUIRED)
    target_link_libraries(lexora_hash_table_obj PUBLIC Threads::Threads)
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(lexora_hash_table_obj PRIVATE
            -Wall -Wextra -Wpedantic -Wno-unused-parameter)
    elseif(MSVC)
        target_compile_options(lexora_hash_table_obj PRIVATE
            /W4 /permissive- /wd4127)
    endif()
endif()

# ---------------------------------------------------------------------------
# STANDALONE MODE: create the aggregate library + optional tests.
# ---------------------------------------------------------------------------
if(LEXORA_HASH_TABLE_STANDALONE)

    if(NOT TARGET lexora_hash_table)
        add_library(lexora_hash_table STATIC
            ${LEXORA_HASH_TABLE_SOURCES}
        )
        add_library(lexora::hash_table ALIAS lexora_hash_table)

        target_sources(lexora_hash_table
            PUBLIC
                FILE_SET HEADERS
                BASE_DIRS "${LEXORA_HASH_TABLE_ROOT}/include"
                FILES ${LEXORA_HASH_TABLE_HEADERS}
        )

        target_include_directories(lexora_hash_table
            PUBLIC
                $<BUILD_INTERFACE:${LEXORA_HASH_TABLE_ROOT}/include>
                $<INSTALL_INTERFACE:include>
        )

        target_compile_features(lexora_hash_table PUBLIC cxx_std_17)

        find_package(Threads REQUIRED)
        target_link_libraries(lexora_hash_table PUBLIC Threads::Threads)

        # Hard dependency on the Lexora Unicode module (NFC normalization).
        # Compile unicode.cpp into the standalone library so the symbol is
        # always available without requiring the caller to know about it.
        target_sources(lexora_hash_table PRIVATE
            "${LEXORA_HASH_TABLE_ROOT}/src/unicode.cpp"
        )

        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexora_hash_table PRIVATE
                -Wall -Wextra -Wpedantic -Wno-unused-parameter)
        elseif(MSVC)
            target_compile_options(lexora_hash_table PRIVATE
                /W4 /permissive- /wd4127)
        endif()
    endif()

    # ----- Tests ---------------------------------------------------------
    if(LEXORA_HASH_TABLE_BUILD_TESTS)
        option(LEXORA_HASH_TABLE_FETCH_GTEST
               "Fetch GoogleTest via FetchContent" ON)
        set(LEXORA_GTEST_GIT_URL
            "https://github.com/google/googletest.git"
            CACHE STRING "GoogleTest fetch URL (override for offline mirrors)")

        enable_testing()

        if(LEXORA_HASH_TABLE_FETCH_GTEST)
            include(FetchContent)
            FetchContent_Declare(
                googletest
                GIT_REPOSITORY "${LEXORA_GTEST_GIT_URL}"
                GIT_TAG        v1.14.0
            )
            set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
            FetchContent_MakeAvailable(googletest)
            set(_lexora_gtest_libs gtest gtest_main)
            set(_lexora_gtest_inc "")
        else()
            find_package(GTest REQUIRED)
            set(_lexora_gtest_libs GTest::GTest GTest::Main)
            set(_lexora_gtest_inc ${GTEST_INCLUDE_DIRS})
        endif()

        add_executable(lexora_hash_table_tests ${LEXORA_HASH_TABLE_TESTS})
        target_link_libraries(lexora_hash_table_tests
            PRIVATE lexora::hash_table ${_lexora_gtest_libs})
        target_include_directories(lexora_hash_table_tests
            PRIVATE ${_lexora_gtest_inc})
        target_compile_features(lexora_hash_table_tests PRIVATE cxx_std_17)
        if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
            target_compile_options(lexora_hash_table_tests PRIVATE
                -Wall -Wextra -Wpedantic)
        endif()

        include(CTest)
        add_test(NAME lexora_hash_table_tests
                 COMMAND lexora_hash_table_tests)

        add_custom_target(lexora_hash_table_all
            DEPENDS lexora_hash_table lexora_hash_table_tests
            COMMENT "Building Lexora hash table (lib + tests)"
        )
    else()
        add_custom_target(lexora_hash_table_all
            DEPENDS lexora_hash_table
            COMMENT "Building Lexora hash table (lib)"
        )
    endif()
endif()

# ---------------------------------------------------------------------------
# Configuration summary.
# ---------------------------------------------------------------------------
message(STATUS "HashTable fragment:")
message(STATUS "  root            = ${LEXORA_HASH_TABLE_ROOT}")
message(STATUS "  standalone      = ${LEXORA_HASH_TABLE_STANDALONE}")
message(STATUS "  build tests     = ${LEXORA_HASH_TABLE_BUILD_TESTS}")
message(STATUS "  depends on      = lexora::unicode (src/unicode.cpp)")
