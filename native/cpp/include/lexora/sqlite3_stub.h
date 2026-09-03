// sqlite3_stub.h — Minimal SQLite3 stub for compiling the Lexora engine
// without libsqlite3 development headers installed.
//
// This header provides:
//   - The result-code macros used by engine.cpp (SQLITE_OK, SQLITE_ROW)
//   - The SQLITE_TRANSIENT destructor sentinel
//   - Opaque typedefs for sqlite3 / sqlite3_stmt
//   - No-op inline definitions for the subset of the SQLite C API used by
//     the engine (open, close, prepare, bind, step, column_text, finalize,
//     exec).
//
// Every stub succeeds silently: integer-returning functions return SQLITE_OK,
// pointer-returning functions return nullptr. The engine treats a missing
// database as non-fatal (hash table + DAWG keep working), so these no-ops
// preserve that graceful-degradation contract.
//
// For real database functionality, link against libsqlite3 and let
// sqlite3_shim.h pull in the real <sqlite3.h> instead of this stub.
#pragma once

#include <stddef.h> /* NULL / nullptr support in C */

#ifdef __cplusplus
extern "C" {
#endif

/* -------------------------------------------------------------------------
 * Result codes (subset)
 * ------------------------------------------------------------------------- */
#define SQLITE_OK   0     /* Successful result */
#define SQLITE_ROW  100   /* sqlite3_step() has another row ready */

/* -------------------------------------------------------------------------
 * Destructor policy for bound parameters
 *
 * The real sqlite3.h defines SQLITE_TRANSIENT as a magic function pointer
 * ((sqlite3_destructor_type)-1) that tells SQLite to make its own private
 * copy of the bound value. We replicate the exact bit pattern so that
 * callers passing SQLITE_TRANSIENT compile unchanged against the stub.
 * ------------------------------------------------------------------------- */
#define SQLITE_TRANSIENT ((void(*)(void*))-1)

/* -------------------------------------------------------------------------
 * Opaque handle typedefs
 *
 * Defined as incomplete structs so that `sqlite3*` and `sqlite3_stmt*` are
 * valid pointer types but cannot be dereferenced — matching the real API's
 * opacity. The stub never allocates instances of these; all stub functions
 * treat the pointers as opaque.
 * ------------------------------------------------------------------------- */
typedef struct sqlite3      sqlite3;
typedef struct sqlite3_stmt sqlite3_stmt;

/* -------------------------------------------------------------------------
 * Function stubs
 *
 * `static inline` gives the functions internal linkage, so:
 *   - In C: each translation unit gets its own copy (no ODR concerns).
 *   - In C++: the inline definition is emitted on demand, deduplicated.
 * This means the stubs satisfy the linker without requiring libsqlite3 to
 * be present at link time.
 * ------------------------------------------------------------------------- */

/* Opens a database file. Stub: reports success but hands back a null handle
 * so the engine's `if (impl_->db)` guards short-circuit to the no-DB path. */
static inline int sqlite3_open(const char* filename, sqlite3** ppDb) {
    (void)filename;
    if (ppDb) { *ppDb = (sqlite3*)0; }
    return SQLITE_OK;
}

/* Closes a database handle. Stub: no-op. */
static inline int sqlite3_close(sqlite3* db) {
    (void)db;
    return SQLITE_OK;
}

/* Compiles an SQL statement into a prepared-statement handle.
 * Stub: reports success but hands back a null stmt pointer, so the engine's
 * subsequent step/column calls become guarded no-ops. */
static inline int sqlite3_prepare_v2(sqlite3* db,
                                     const char* zSql,
                                     int nByte,
                                     sqlite3_stmt** ppStmt,
                                     const char** pzTail) {
    (void)db; (void)zSql; (void)nByte;
    if (pzTail)  { *pzTail  = (const char*)0; }
    if (ppStmt)  { *ppStmt  = (sqlite3_stmt*)0; }
    return SQLITE_OK;
}

/* Binds a UTF-8 text value to a prepared-statement parameter.
 * Stub: no-op, reports success. */
static inline int sqlite3_bind_text(sqlite3_stmt* pStmt,
                                    int i,
                                    const char* zData,
                                    int nData,
                                    void (*xDel)(void*)) {
    (void)pStmt; (void)i; (void)zData; (void)nData; (void)xDel;
    return SQLITE_OK;
}

/* Steps a prepared statement to the next result row.
 * Stub: returns SQLITE_OK (not SQLITE_ROW) so the engine's
 * `while (sqlite3_step(stmt) == SQLITE_ROW)` loops terminate immediately. */
static inline int sqlite3_step(sqlite3_stmt* pStmt) {
    (void)pStmt;
    return SQLITE_OK;
}

/* Retrieves the UTF-8 text of column iCol for the current row.
 * Stub: returns nullptr — engine code reinterpret_casts to const char* and
 * treats nullptr as an empty/missing value. */
static inline const unsigned char* sqlite3_column_text(sqlite3_stmt* pStmt,
                                                       int iCol) {
    (void)pStmt; (void)iCol;
    return (const unsigned char*)0;
}

/* Finalizes (destroys) a prepared statement. Stub: no-op. */
static inline int sqlite3_finalize(sqlite3_stmt* pStmt) {
    (void)pStmt;
    return SQLITE_OK;
}

/* Runs zero or more semicolon-terminated SQL statements with an optional
 * per-row callback. Stub: no-op, reports success, clears any error message
 * slot the caller passed in. */
static inline int sqlite3_exec(sqlite3* db,
                               const char* sql,
                               int (*callback)(void*, int, char**, char**),
                               void* arg,
                               char** errmsg) {
    (void)db; (void)sql; (void)callback; (void)arg;
    if (errmsg) { *errmsg = (char*)0; }
    return SQLITE_OK;
}

#ifdef __cplusplus
} /* extern "C" */
#endif
