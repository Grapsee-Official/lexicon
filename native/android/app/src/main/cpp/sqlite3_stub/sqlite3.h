/*
 * sqlite3.h — Minimal SQLite3 stub header for Android NDK builds that don't
 * have a system SQLite3 available.
 *
 * This stub is NOT a real SQLite implementation. It declares just enough of
 * the SQLite C API to let engine.cpp compile and link. At runtime every
 * function returns failure so the Lexora engine falls back to its
 * hash-table + DAWG path (engine.cpp already treats SQLite as optional).
 *
 * When a real SQLite3 is found via find_package(SQLite3) in CMakeLists.txt,
 * this header is NOT used — the real <sqlite3.h> from the system package
 * takes precedence because find_package adds its include path with higher
 * priority than the stub directory.
 */
#ifndef LEXORA_SQLITE3_STUB_H
#define LEXORA_SQLITE3_STUB_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Opaque handles — never actually populated by the stub. */
typedef struct sqlite3 sqlite3;
typedef struct sqlite3_stmt sqlite3_stmt;

/* Result codes — only the ones engine.cpp actually checks. */
#define SQLITE_OK         0
#define SQLITE_ERROR      1
#define SQLITE_CANTOPEN   14
#define SQLITE_ROW        100
#define SQLITE_DONE       101

/* Destructor policy — engine.cpp passes SQLITE_TRANSIENT to sqlite3_bind_text. */
#define SQLITE_STATIC     ((void(*)(void*))0)
#define SQLITE_TRANSIENT  ((void(*)(void*))-1)

/* Lifecycle. */
int   sqlite3_open(const char* filename, sqlite3** ppDb);
int   sqlite3_close(sqlite3* db);
int   sqlite3_exec(
        sqlite3* db, const char* sql,
        void* callback, void* arg,
        char** errmsg);

/* Prepared statements. */
int   sqlite3_prepare_v2(
        sqlite3* db, const char* zSql, int nByte,
        sqlite3_stmt** ppStmt, const char** pzTail);
int   sqlite3_step(sqlite3_stmt* stmt);
int   sqlite3_finalize(sqlite3_stmt* stmt);

/* Binding + column access (only the text variants engine.cpp uses). */
int   sqlite3_bind_text(
        sqlite3_stmt* stmt, int idx,
        const char* text, int n,
        void(*dtor)(void*));
const unsigned char* sqlite3_column_text(sqlite3_stmt* stmt, int col);

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* LEXORA_SQLITE3_STUB_H */
