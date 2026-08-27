# =============================================================================
#  cmake/Engine.cmake — Lexicon C++ engine facade (P3-05-CPP-ENGINE-API).
#
#  Per the root CMakeLists.txt contract, each module fragment MAY:
#    (a) append source files to the LEXICON_SOURCES list variable,
#    (b) append headers to the LEXICON_HEADERS,
#    (c) declare an OBJECT library named lexicon_<module>_obj.
#
#  This fragment uses (a) + (b): it appends src/engine.cpp + src/c_api.cpp
#  and include/lexicon/{engine.hpp,c_api.h} to the aggregate lists consumed
#  by the lexicon-engine library target.
#
#  In addition, this fragment acts as the BUILD INTEGRATOR for the engine
#  subsystem:
#    * Ensures src/hash_table.cpp is compiled with -DLEXICON_UNICODE_EXTERNAL
#      so its passthrough normalize_nfc is suppressed — the real symbol
#      comes from src/unicode.cpp (delivered by P3-04-CPP-UNICODE).
#    * Conditionally appends src/levenshtein.cpp if it has landed but the
#      sibling Levenshtein.cmake fragment has not; otherwise appends the
#      stopgap tests/stubs/levenshtein_fallback.cpp so the engine links.
#    * Conditionally appends src/unicode.cpp, src/dawg.cpp, src/hash_table.cpp
#      when their sibling .cmake fragments are absent (stopgap until the
#      parallel agents deliver Levenshtein.cmake / Unicode.cmake / etc).
#
#  Standalone mode (no LEXICON_SOURCES defined) creates a self-contained
#  `lexicon_engine` STATIC library for isolated development / testing.
# =============================================================================
include_guard(GLOBAL)

cmake_minimum_required(VERSION 3.16)

# Resolve the module root: this file lives at <root>/cmake/Engine.cmake.
get_filename_component(_LEXICON_ENGINE_DIR
    "${CMAKE_CURRENT_LIST_FILE}" DIRECTORY)
get_filename_component(_LEXICON_ENGINE_DIR
    "${_LEXICON_ENGINE_DIR}/.." ABSOLUTE)
set(LEXICON_ENGINE_ROOT "${_LEXICON_ENGINE_DIR}"
    CACHE PATH "Root of the Lexicon engine C++ module")

# ---------------------------------------------------------------------------
# Source / header manifest for THIS module (engine facade + C ABI).
# ---------------------------------------------------------------------------
set(LEXICON_ENGINE_SOURCES
    "${LEXICON_ENGINE_ROOT}/src/engine.cpp"
    "${LEXICON_ENGINE_ROOT}/src/c_api.cpp"
)
set(LEXICON_ENGINE_HEADERS
    "${LEXICON_ENGINE_ROOT}/include/lexicon/engine.hpp"
    "${LEXICON_ENGINE_ROOT}/include/lexicon/c_api.h"
)

# ---------------------------------------------------------------------------
# STOPGAP: ensure sibling-module .cpp files are in LEXICON_SOURCES when
# their own .cmake fragments haven't landed yet.
#
# Each check uses list(FIND ...) to avoid duplicate appends — once the
# sibling .cmake fragment is delivered, it appends first (it is included
# BEFORE Engine.cmake by the root CMakeLists.txt) and our check skips.
# ---------------------------------------------------------------------------
function(_lexicon_engine_append_if _src)
    if(NOT EXISTS "${_src}")
        return()
    endif()
    list(FIND LEXICON_SOURCES "${_src}" _idx)
    if(_idx EQUAL -1)
        list(APPEND LEXICON_SOURCES "${_src}")
        set(LEXICON_SOURCES ${LEXICON_SOURCES} PARENT_SCOPE)
    endif()
endfunction()

# hash_table.cpp, unicode.cpp, dawg.cpp — provided by sibling agents.
_lexicon_engine_append_if("${LEXICON_ENGINE_ROOT}/src/hash_table.cpp")
_lexicon_engine_append_if("${LEXICON_ENGINE_ROOT}/src/unicode.cpp")
_lexicon_engine_append_if("${LEXICON_ENGINE_ROOT}/src/dawg.cpp")

# levenshtein.cpp — if the real implementation hasn't landed, fall back to
# the stopgap Wagner-Fischer implementation in tests/stubs/.
if(EXISTS "${LEXICON_ENGINE_ROOT}/src/levenshtein.cpp")
    _lexicon_engine_append_if("${LEXICON_ENGINE_ROOT}/src/levenshtein.cpp")
else()
    list(FIND LEXICON_SOURCES
        "${LEXICON_ENGINE_ROOT}/tests/stubs/levenshtein_fallback.cpp" _idx)
    if(_idx EQUAL -1)
        list(APPEND LEXICON_SOURCES
            "${LEXICON_ENGINE_ROOT}/tests/stubs/levenshtein_fallback.cpp")
        message(STATUS "Engine: src/levenshtein.cpp not found — using "
                       "tests/stubs/levenshtein_fallback.cpp (stopgap).")
    endif()
endif()

# ---------------------------------------------------------------------------
# Suppress hash_table.cpp's passthrough normalize_nfc — the real symbol
# comes from src/unicode.cpp. Apply to both the source-file properties
# (covers direct compilation via LEXICON_SOURCES) and the OBJECT library
# target (covers compilation via lexicon_hash_table_obj).
# ---------------------------------------------------------------------------
set_source_files_properties(
    "${LEXICON_ENGINE_ROOT}/src/hash_table.cpp"
    PROPERTIES COMPILE_DEFINITIONS LEXICON_UNICODE_EXTERNAL)
if(TARGET lexicon_hash_table_obj)
    target_compile_definitions(lexicon_hash_table_obj PRIVATE
        LEXICON_UNICODE_EXTERNAL)
endif()

# ---------------------------------------------------------------------------
# FRAGMENT MODE: hand our sources/headers to the parent build.
# ---------------------------------------------------------------------------
if(DEFINED LEXICON_SOURCES)
    list(APPEND LEXICON_SOURCES  ${LEXICON_ENGINE_SOURCES})
    list(APPEND LEXICON_HEADERS ${LEXICON_ENGINE_HEADERS})
    set(LEXICON_SOURCES  ${LEXICON_SOURCES}  PARENT_SCOPE)
    set(LEXICON_HEADERS ${LEXICON_HEADERS} PARENT_SCOPE)
endif()

# ---------------------------------------------------------------------------
# Per-module OBJECT library (the root CMakeLists.txt's
# `lexicon_<module>_obj` convention). Cheap to declare; opt-in to use.
# ---------------------------------------------------------------------------
if(NOT TARGET lexicon_engine_obj)
    add_library(lexicon_engine_obj OBJECT ${LEXICON_ENGINE_SOURCES})
    target_include_directories(lexicon_engine_obj PUBLIC
        ${LEXICON_ENGINE_ROOT}/include)
    target_compile_features(lexicon_engine_obj PUBLIC cxx_std_17)
    find_package(Threads QUIET)
    if(Threads_FOUND)
        target_link_libraries(lexicon_engine_obj PUBLIC Threads::Threads)
    endif()
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(lexicon_engine_obj PRIVATE
            -Wall -Wextra -Wpedantic -Wno-unused-parameter)
    elseif(MSVC)
        target_compile_options(lexicon_engine_obj PRIVATE
            /W4 /permissive- /wd4127)
    endif()
endif()

# ---------------------------------------------------------------------------
# SQLite3 — required by engine.cpp (load_database opens a SQLite file).
# Use the system install; mobile NDKs bundle it as a static lib.
# ---------------------------------------------------------------------------
if(NOT TARGET lexicon_engine_obj)
    return()
endif()

find_package(SQLite3 QUIET)
if(SQLite3_FOUND)
    target_link_libraries(lexicon_engine_obj PUBLIC SQLite::SQLite3)
    message(STATUS "Engine: using system SQLite3 (${SQLite3_VERSION})")
else()
    # Fallback: assume the platform libc / libsqlite3 is available.
    # Linux: -lsqlite3. Android: libsqlite.so is part of the platform.
    # iOS: SQLite is in the SDK (libsqlite3.tbd).
    if(LEXICON_PLATFORM STREQUAL "android")
        # SQLite is a system shared library on Android (no link flag needed;
        # it's in the default libc.so symbol set since API 21+ via android.database.sqlite).
        # For NDK builds we link against the static libsqlite3.a from the NDK sysroot
        # when available; otherwise no link flag (the .so loads at runtime).
        target_link_libraries(lexicon_engine_obj PUBLIC log)
    elseif(LEXICON_PLATFORM STREQUAL "ios" OR LEXICON_PLATFORM STREQUAL "macos")
        find_library(SQLITE3_LIB sqlite3 REQUIRED)
        target_link_libraries(lexicon_engine_obj PUBLIC ${SQLITE3_LIB})
    else()
        # Linux / other: link the system libsqlite3.
        target_link_libraries(lexicon_engine_obj PUBLIC sqlite3)
        message(STATUS "Engine: linking system libsqlite3 (no find_package)")
    endif()
endif()

message(STATUS "Engine fragment:")
message(STATUS "  root            = ${LEXICON_ENGINE_ROOT}")
message(STATUS "  sources         = ${LEXICON_ENGINE_SOURCES}")
message(STATUS "  unicode external= ON (suppresses hash_table.cpp passthrough)")
