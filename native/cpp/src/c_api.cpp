// c_api.cpp — C ABI implementation (wraps C++ LexoraEngine)
#include "lexora/c_api.h"
#include "lexora/engine.hpp"
#include <cstring>
#include <cstdlib>
#include <new>

struct LexoraEngineHandle {
    lexora::LexoraEngine engine;
};

extern "C" {

LexoraEngineHandle* lexora_engine_create(void) {
    auto* h = new (std::nothrow) LexoraEngineHandle();
    return h;
}

int lexora_engine_load(LexoraEngineHandle* handle, const char* db_dir) {
    if (!handle || !db_dir) return -1;
    return handle->engine.load_database(db_dir) ? 0 : -1;
}

void lexora_engine_close(LexoraEngineHandle* handle) {
    if (handle) {
        handle->engine.close();
        delete handle;
    }
}

// Helper: convert C++ string to malloc'd C string
static char* dup_string(const std::string& s) {
    char* p = static_cast<char*>(std::malloc(s.size() + 1));
    if (p) {
        std::memcpy(p, s.c_str(), s.size() + 1);
    }
    return p;
}

// Helper: convert vector<string> to C array
static void dup_string_vec(const std::vector<std::string>& vec, const char*** out_arr, uint32_t* out_count) {
    *out_count = static_cast<uint32_t>(vec.size());
    if (vec.empty()) {
        *out_arr = nullptr;
        return;
    }
    *out_arr = static_cast<const char**>(std::malloc(vec.size() * sizeof(char*)));
    if (!*out_arr) { *out_count = 0; return; }
    for (size_t i = 0; i < vec.size(); ++i) {
        (*out_arr)[i] = dup_string(vec[i]);
    }
}

// Helper: convert C++ WordEntry to C LexoraWordEntry (heap-allocated)
static LexoraWordEntry* convert_entry(const lexora::WordEntry& cpp_entry) {
    LexoraWordEntry* entry = static_cast<LexoraWordEntry*>(std::malloc(sizeof(LexoraWordEntry)));
    if (!entry) return nullptr;

    entry->id = dup_string(cpp_entry.id);
    entry->word = dup_string(cpp_entry.word);
    entry->phonetic = dup_string(cpp_entry.phonetic);
    entry->part_of_speech = dup_string(cpp_entry.part_of_speech);
    entry->etymology = dup_string(cpp_entry.etymology);
    entry->language = dup_string(cpp_entry.language);

    dup_string_vec(cpp_entry.definitions, &entry->definitions, &entry->definition_count);
    dup_string_vec(cpp_entry.synonyms, &entry->synonyms, &entry->synonym_count);
    dup_string_vec(cpp_entry.antonyms, &entry->antonyms, &entry->antonym_count);
    dup_string_vec(cpp_entry.examples, &entry->examples, &entry->example_count);

    return entry;
}

LexoraWordEntry* lexora_engine_lookup(LexoraEngineHandle* handle, const char* word) {
    if (!handle || !word) return nullptr;
    auto result = handle->engine.lookup(word);
    if (!result) return nullptr;
    return convert_entry(*result);
}

LexoraLexoraSearchResults* lexora_engine_fuzzy_search(LexoraEngineHandle* handle, const char* query, int max_distance, uint32_t limit) {
    if (!handle || !query) return nullptr;
    auto results = handle->engine.fuzzy_search(query, max_distance, limit ? limit : 20);
    if (results.empty()) return nullptr;

    LexoraLexoraSearchResults* out = static_cast<LexoraLexoraSearchResults*>(std::malloc(sizeof(LexoraLexoraSearchResults)));
    if (!out) return nullptr;

    out->count = static_cast<uint32_t>(results.size());
    out->results = static_cast<LexoraLexoraSearchResult*>(std::malloc(results.size() * sizeof(LexoraLexoraSearchResult)));
    if (!out->results) { std::free(out); return nullptr; }

    for (size_t i = 0; i < results.size(); ++i) {
        out->results[i].entry = *convert_entry(results[i].entry);
        out->results[i].edit_distance = results[i].edit_distance;
    }

    return out;
}

static void free_string_vec(const char** arr, uint32_t count) {
    if (!arr) return;
    for (uint32_t i = 0; i < count; ++i) {
        std::free(const_cast<char*>(arr[i]));
    }
    std::free(arr);
}

void lexora_engine_free_entry(LexoraWordEntry* entry) {
    if (!entry) return;
    std::free(const_cast<char*>(entry->id));
    std::free(const_cast<char*>(entry->word));
    std::free(const_cast<char*>(entry->phonetic));
    std::free(const_cast<char*>(entry->part_of_speech));
    std::free(const_cast<char*>(entry->etymology));
    std::free(const_cast<char*>(entry->language));
    free_string_vec(entry->definitions, entry->definition_count);
    free_string_vec(entry->synonyms, entry->synonym_count);
    free_string_vec(entry->antonyms, entry->antonym_count);
    free_string_vec(entry->examples, entry->example_count);
    std::free(entry);
}

void lexora_engine_free_results(LexoraLexoraSearchResults* results) {
    if (!results) return;
    for (uint32_t i = 0; i < results->count; ++i) {
        lexora_engine_free_entry(&results->results[i].entry);
    }
    std::free(results->results);
    std::free(results);
}

void lexora_engine_enable_dawg(LexoraEngineHandle* handle, int enabled) {
    if (handle) handle->engine.enable_dawg(enabled != 0);
}

int lexora_engine_is_dawg_enabled(LexoraEngineHandle* handle) {
    return handle && handle->engine.is_dawg_enabled() ? 1 : 0;
}

LexoraMemoryInfo lexora_engine_get_memory_usage(LexoraEngineHandle* handle) {
    LexoraMemoryInfo info = {0, 0, 0};
    if (handle) {
        auto m = handle->engine.get_memory_usage();
        info.hash_table_bytes = m.hash_table_bytes;
        info.dawg_bytes = m.dawg_bytes;
        info.total_bytes = m.total_bytes;
    }
    return info;
}

} // extern "C"
