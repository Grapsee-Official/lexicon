/*
 * sqlite3_stub.c — No-op SQLite3 implementation for Android NDK builds
 * without a system SQLite3.
 *
 * Every function returns failure (SQLITE_ERROR) or nullptr so that
 * engine.cpp's load_database() treats SQLite as unavailable:
 *
 *     if (sqlite3_open(sqlite_path.c_str(), &impl_->db) != SQLITE_OK) {
 *         // Non-fatal — hash table + DAWG still work
 *         impl_->db = nullptr;
 *     }
 *
 * This keeps the .so linkable on a stock NDK install. To enable real FTS5
 * search, supply a real sqlite3.c amalgamation (or install SQLite3 where
 * CMake's find_package can see it) — CMakeLists.txt will pick that up
 * automatically and skip this stub.
 */
#include "sqlite3.h"

struct sqlite3     { int unused; };
struct sqlite3_stmt { int unused; };

int sqlite3_open(const char* filename, sqlite3** ppDb) {
    (void)filename;
    if (ppDb) { *ppDb = (sqlite3*)0; }
    return SQLITE_ERROR;
}

int sqlite3_close(sqlite3* db) {
    (void)db;
    return SQLITE_OK;
}

int sqlite3_exec(sqlite3* db, const char* sql,
                 void* callback, void* arg, char** errmsg) {
    (void)db; (void)sql; (void)callback; (void)arg;
    if (errmsg) { *errmsg = (char*)0; }
    return SQLITE_ERROR;
}

int sqlite3_prepare_v2(sqlite3* db, const char* zSql, int nByte,
                       sqlite3_stmt** ppStmt, const char** pzTail) {
    (void)db; (void)zSql; (void)nByte;
    if (ppStmt) { *ppStmt = (sqlite3_stmt*)0; }
    if (pzTail) { *pzTail = zSql; }
    return SQLITE_ERROR;
}

int sqlite3_step(sqlite3_stmt* stmt) {
    (void)stmt;
    return SQLITE_ERROR;
}

int sqlite3_finalize(sqlite3_stmt* stmt) {
    (void)stmt;
    return SQLITE_OK;
}

int sqlite3_bind_text(sqlite3_stmt* stmt, int idx,
                      const char* text, int n,
                      void(*dtor)(void*)) {
    (void)stmt; (void)idx; (void)text; (void)n; (void)dtor;
    return SQLITE_ERROR;
}

const unsigned char* sqlite3_column_text(sqlite3_stmt* stmt, int col) {
    (void)stmt; (void)col;
    return (const unsigned char*)0;
}
