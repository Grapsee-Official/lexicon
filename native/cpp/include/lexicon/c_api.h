// c_api.h — C ABI for JNI (Android) and Swift C-interop (iOS)
// Pure C interface. All strings are UTF-8 const char*.
#pragma once

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Opaque handle
typedef struct LexiconEngineHandle LexiconEngineHandle;

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
} LexiconWordEntry;

typedef struct {
    LexiconWordEntry entry;
    int edit_distance;
} LexiconSearchResult;

typedef struct {
    LexiconSearchResult* results;
    uint32_t count;
} LexiconSearchResults;

// Lifecycle
LexiconEngineHandle* lexicon_engine_create(void);
int lexicon_engine_load(LexiconEngineHandle* handle, const char* db_dir);
void lexicon_engine_close(LexiconEngineHandle* handle);

// Lookup
LexiconWordEntry* lexicon_engine_lookup(LexiconEngineHandle* handle, const char* word);
LexiconSearchResults* lexicon_engine_fuzzy_search(LexiconEngineHandle* handle, const char* query, int max_distance, uint32_t limit);

// Memory management
void lexicon_engine_free_entry(LexiconWordEntry* entry);
void lexicon_engine_free_results(LexiconSearchResults* results);

// DAWG control (for RAM pressure)
void lexicon_engine_enable_dawg(LexiconEngineHandle* handle, int enabled);
int lexicon_engine_is_dawg_enabled(LexiconEngineHandle* handle);

// Memory info
typedef struct {
    uint64_t hash_table_bytes;
    uint64_t dawg_bytes;
    uint64_t total_bytes;
} LexiconMemoryInfo;
LexiconMemoryInfo lexicon_engine_get_memory_usage(LexiconEngineHandle* handle);

#ifdef __cplusplus
}
#endif
