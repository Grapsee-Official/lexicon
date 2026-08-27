// sqlite3_shim.h — Conditional SQLite3 include
//
// Includes the real system <sqlite3.h> when the development headers are
// available, otherwise falls back to lexicon/sqlite3_stub.h so the engine
// compiles (and links, thanks to the stub's inline no-op definitions) on
// machines without libsqlite3-dev installed.
//
// Usage in engine.cpp:
//
//     #include "lexicon/sqlite3_shim.h"   // instead of <sqlite3.h>
//
// The shim is silent: it pulls in exactly one of the two headers and
// defines no symbols of its own.
#pragma once

#ifdef __has_include
  #if __has_include(<sqlite3.h>)
    #include <sqlite3.h>
  #else
    #include "lexicon/sqlite3_stub.h"
  #endif
#else
  #include "lexicon/sqlite3_stub.h"
#endif
