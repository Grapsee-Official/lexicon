# =============================================================================
#  CMakeLists.txt — Android app module build for liblexora-engine.so
#
#  Compiles the shared C++ engine (from ../../../../../cpp/src) together with
#  the JNI bridge (lexora_jni.cpp in this directory) into a single shared
#  library that the Kotlin side loads via System.loadLibrary("lexora-engine").
#
#  Path note: this file lives at
#    <repo>/native/android/app/src/main/cpp/CMakeLists.txt
#  so going up 5 levels reaches <repo>/native/, which contains cpp/src and
#  cpp/include. (The task brief said 4 levels — that resolves to
#  native/android/cpp/, which does not exist; 5 is correct.)
# =============================================================================
cmake_minimum_required(VERSION 3.22.1)

project(lexora-engine LANGUAGES C CXX VERSION 1.0.0)

# -----------------------------------------------------------------------------
# 1. C++17
# -----------------------------------------------------------------------------
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# -----------------------------------------------------------------------------
# 2. Locate the shared engine sources.
#    5 levels up from this file → <repo>/native/
# -----------------------------------------------------------------------------
set(LEXORA_ENGINE_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../cpp")
set(LEXORA_SRC_DIR    "${LEXORA_ENGINE_ROOT}/src")
set(LEXORA_INCLUDE_DIR "${LEXORA_ENGINE_ROOT}/include")

if(NOT EXISTS "${LEXORA_SRC_DIR}/engine.cpp")
    message(FATAL_ERROR
        "Lexora engine sources not found at ${LEXORA_SRC_DIR}. "
        "Expected <repo>/native/cpp/src/engine.cpp. Check the relative path "
        "in this CMakeLists.txt.")
endif()

# Engine source manifest (must match the standalone native/cpp/CMakeLists.txt).
set(LEXORA_ENGINE_SOURCES
    "${LEXORA_SRC_DIR}/engine.cpp"
    "${LEXORA_SRC_DIR}/c_api.cpp"
    "${LEXORA_SRC_DIR}/hash_table.cpp"
    "${LEXORA_SRC_DIR}/unicode.cpp"
    "${LEXORA_SRC_DIR}/dawg.cpp"
    "${LEXORA_SRC_DIR}/levenshtein.cpp"
)

# -----------------------------------------------------------------------------
# 3. JNI bridge
# -----------------------------------------------------------------------------
set(LEXORA_JNI_SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/lexora_jni.cpp")

if(NOT EXISTS "${LEXORA_JNI_SOURCE}")
    message(FATAL_ERROR "JNI bridge not found: ${LEXORA_JNI_SOURCE}")
endif()

# -----------------------------------------------------------------------------
# 4. Shared library target — liblexora-engine.so
# -----------------------------------------------------------------------------
add_library(lexora-engine SHARED
    ${LEXORA_ENGINE_SOURCES}
    ${LEXORA_JNI_SOURCE}
)

target_include_directories(lexora-engine PUBLIC
    ${LEXORA_INCLUDE_DIR}
)

# hash_table.cpp ships a passthrough normalize_nfc that is shadowed by the
# real symbol in unicode.cpp. Suppress it the same way the standalone build
# does (see native/cpp/cmake/Engine.cmake).
set_source_files_properties(
    "${LEXORA_SRC_DIR}/hash_table.cpp"
    PROPERTIES COMPILE_DEFINITIONS LEXORA_UNICODE_EXTERNAL
)

# -----------------------------------------------------------------------------
# 5. SQLite3 — try the system package first, fall back to a built-in stub.
#
#    The stub (sqlite3_stub/sqlite3.h + sqlite3_stub.c) provides no-op
#    definitions of the symbols engine.cpp references. sqlite3_open() returns
#    SQLITE_ERROR so the engine treats SQLite as unavailable — engine.cpp
#    already handles that case gracefully (hash table + DAWG still work;
#    full-text search simply won't).
# -----------------------------------------------------------------------------
find_package(SQLite3 QUIET)
if(SQLite3_FOUND)
    target_link_libraries(lexora-engine PUBLIC SQLite::SQLite3)
    message(STATUS "Lexora: using system SQLite3 (${SQLite3_VERSION})")
else()
    message(STATUS
        "Lexora: SQLite3 not found via find_package — using built-in stub. "
        "Engine will run without FTS5 (hash table + DAWG remain functional).")
    target_include_directories(lexora-engine PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/sqlite3_stub")
    target_sources(lexora-engine PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/sqlite3_stub/sqlite3_stub.c")
endif()

# -----------------------------------------------------------------------------
# 6. Android runtime deps + hardening flags.
#    - log: __android_log_print used by NDK (and any logging we add later).
#    - visibility hidden: only JNI exports (Java_com_lexora_engine_*) are
#      left visible thanks to the JNI ABI's extern "C" + default visibility.
# -----------------------------------------------------------------------------
find_library(LOG_LIB log)
target_link_libraries(lexora-engine PUBLIC ${LOG_LIB})

target_compile_options(lexora-engine PRIVATE
    -O2
    -Wall -Wextra
    -Wno-unused-parameter
    -fPIC
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -fexceptions
    -ffunction-sections
    -fdata-sections
)

target_link_options(lexora-engine PRIVATE
    -Wl,--gc-sections
    -Wl,--exclude-libs,ALL
    -Wl,-z,defs
    -Wl,-z,noexecstack
    -Wl,-z,relro
    -Wl,-z,now
)

# Output name → lib<OUTPUT_NAME>.so → liblexora-engine.so
set_target_properties(lexora-engine PROPERTIES
    OUTPUT_NAME "lexora-engine"
    SUFFIX ".so"
)
