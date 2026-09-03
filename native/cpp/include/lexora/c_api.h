// c_api.h — C ABI for JNI (Android) and Swift C-interop (iOS)
// Pure C interface. All strings are UTF-8 const char*.
#pragma once

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Opaque handle
typedef struct LexoraEngineHandle LexoraEngineHandle;

// C struct for word entry (mirrors C++ WordEntry)
typedef struct {
    const char* id;
    const char* word;
    const char* phonetic;
    const char* part_of_speech;
    const char* etymology;
    const char* language;
    const char** definitions;
    uint32_t definition_count;
    const char** synonyms;
    uint32_t synonym_count;
    const char** antonyms;
    uint32_t antonym_count;
    const char** examples;
    uint32_t example_count;
} LexoraWordEntry;

typedef struct {
    LexoraWordEntry entry;
    int edit_distance;
} LexoraSearchResult;

typedef struct {
    LexoraSearchResult* results;
    uint32_t count;
} LexoraSearchResults;

// Lifecycle
LexoraEngineHandle* lexora_engine_create(void);
int lexora_engine_load(LexoraEngineHandle* handle, const char* db_dir);
void lexora_engine_close(LexoraEngineHandle* handle);

// Lookup
LexoraWordEntry* lexora_engine_lookup(LexoraEngineHandle* handle, const char* word);
LexoraSearchResults* lexora_engine_fuzzy_search(LexoraEngineHandle* handle, const char* query, int max_distance, uint32_t limit);

// Memory management
void lexora_engine_free_entry(LexoraWordEntry* entry);
void lexora_engine_free_results(LexoraSearchResults* results);

// DAWG control (for RAM pressure)
void lexora_engine_enable_dawg(LexoraEngineHandle* handle, int enabled);
int lexora_engine_is_dawg_enabled(LexoraEngineHandle* handle);

// Memory info
typedef struct {
    uint64_t hash_table_bytes;
    uint64_t dawg_bytes;
    uint64_t total_bytes;
} LexoraMemoryInfo;
LexoraMemoryInfo lexora_engine_get_memory_usage(LexoraEngineHandle* handle);

#ifdef __cplusplus
}
#endif
