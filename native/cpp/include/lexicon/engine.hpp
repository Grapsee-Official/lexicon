// engine.hpp — Lexicon Engine public C++ API
// Orchestrates Hash Table, Levenshtein, DAWG, Unicode modules.
#pragma once

#include <string>
#include <string_view>
#include <optional>
#include <vector>
#include <memory>

namespace lexicon {

struct WordEntry {
    std::string id;
    std::string word;
    std::string phonetic;
    std::string part_of_speech;
    std::vector<std::string> definitions;
    std::string etymology;
    std::vector<std::string> synonyms;
    std::vector<std::string> antonyms;
    std::vector<std::string> examples;
    std::string language;
};

struct LexiconSearchResult {
    WordEntry entry;
    int edit_distance;
};

struct MemoryInfo {
    size_t hash_table_bytes;
    size_t dawg_bytes;
    size_t total_bytes;
};

class LexiconEngine {
public:
    LexiconEngine();
    ~LexiconEngine();

    // Load database from directory (contains .ht, .dawg, .sqlite files)
    bool load_database(const std::string& db_dir);

    // Exact lookup (NFC-normalized internally)
    std::optional<WordEntry> lookup(std::string_view word) const;

    // Fuzzy search (Levenshtein ≤ max_distance)
    std::vector<LexiconSearchResult> fuzzy_search(std::string_view query, int max_distance = 2, size_t limit = 20) const;

    // Prefix search via DAWG
    std::vector<std::string> prefix_search(std::string_view prefix, size_t limit = 50) const;

    // Get word by ID
    std::optional<WordEntry> get_word_by_id(const std::string& id) const;

    // DAWG toggle (for RAM pressure management)
    void enable_dawg(bool enabled);
    bool is_dawg_enabled() const;

    // Memory usage
    MemoryInfo get_memory_usage() const;

    // Close and release all resources
    void close();

private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};

} // namespace lexicon
