// engine.cpp — Lexora Engine implementation (facade)
#include "lexora/engine.hpp"
#include "lexora/hash_table.hpp"
#include "lexora/levenshtein.hpp"
#include "lexora/dawg.hpp"
#include "lexora/unicode.hpp"

#include "lexora/sqlite3_shim.h"

#include <fstream>
#include <sstream>
#include <cstring>
#include <mutex>

namespace lexora {

struct LexoraEngine::Impl {
    MemoryMappedHashTable hash_table;
    Dawg dawg;
    LevenshteinAutomaton levenshtein;
#ifdef LEXORA_USE_SQLITE
    sqlite3* db = nullptr;
#endif
    bool dawg_enabled = false;
    bool loaded = false;
    mutable std::mutex mutex;

    ~Impl() {
#ifdef LEXORA_USE_SQLITE
        if (db) sqlite3_close(db);
#endif
        hash_table.unload();
        dawg.unload();
    }

    // Deserialize a WordEntry from the hash table value blob
    // Format: [id_len:2][id][phon_len:2][phon][pos_len:2][pos]
    //         [def_count:2][def_len:2][def]...
    //         [etym_len:2][etym][syn_count:2][syn_len:2][syn]...
    //         [ant_count:2][ant_len:2][ant]...
    //         [ex_count:2][ex_len:2][ex]...
    //         [lang_len:2][lang]
    WordEntry deserialize(const char* data, uint32_t len) const {
        WordEntry entry;
        const char* end = data + len;
        const char* p = data;

        auto read_string = [&](std::string& out) {
            if (p + 2 > end) return;
            uint16_t slen;
            std::memcpy(&slen, p, 2); p += 2;
            if (p + slen > end) return;
            out.assign(p, slen); p += slen;
        };

        auto read_string_vec = [&](std::vector<std::string>& out) {
            if (p + 2 > end) return;
            uint16_t count;
            std::memcpy(&count, p, 2); p += 2;
            out.reserve(count);
            for (uint16_t i = 0; i < count; ++i) {
                std::string s;
                read_string(s);
                out.push_back(std::move(s));
            }
        };

        read_string(entry.id);
        read_string(entry.word);
        read_string(entry.phonetic);
        read_string(entry.part_of_speech);
        read_string_vec(entry.definitions);
        read_string(entry.etymology);
        read_string_vec(entry.synonyms);
        read_string_vec(entry.antonyms);
        read_string_vec(entry.examples);
        read_string(entry.language);

        return entry;
    }
};

LexoraEngine::LexoraEngine() : impl_(std::make_unique<Impl>()) {}
LexoraEngine::~LexoraEngine() = default;

bool LexoraEngine::load_database(const std::string& db_dir) {
    std::lock_guard<std::mutex> lock(impl_->mutex);

    // Load hash table
    std::string ht_path = db_dir + "/lexora.ht";
    if (!impl_->hash_table.load_from_file(ht_path.c_str())) {
        return false;
    }

    // Load DAWG (optional — can be toggled)
    std::string dawg_path = db_dir + "/lexora.dawg";
    if (impl_->dawg.load_from_file(dawg_path.c_str())) {
        impl_->dawg_enabled = true;
    }

    // Open SQLite for full-text search (optional — only when LEXORA_USE_SQLITE
    // is defined at compile time). Without it, hash table + DAWG still work.
#ifdef LEXORA_USE_SQLITE
    std::string sqlite_path = db_dir + "/lexora.sqlite";
    if (sqlite3_open(sqlite_path.c_str(), &impl_->db) != SQLITE_OK) {
        // Non-fatal — hash table + DAWG still work
        impl_->db = nullptr;
    } else {
        // Enable WAL mode for concurrent reads
        sqlite3_exec(impl_->db, "PRAGMA journal_mode=WAL;", nullptr, nullptr, nullptr);
    }
#endif

    impl_->loaded = true;
    return true;
}

std::optional<WordEntry> LexoraEngine::lookup(std::string_view word) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    if (!impl_->loaded) return std::nullopt;

    // NFC normalize the query
    std::string normalized = unicode::normalize_nfc(word);

    // Exact lookup in hash table
    uint32_t value_len = 0;
    const char* value = impl_->hash_table.lookup(normalized, &value_len);
    if (!value) {
        // Try diacritic-insensitive fallback
        std::string stripped = unicode::strip_diacritics(normalized);
        value = impl_->hash_table.lookup(stripped, &value_len);
        if (!value) return std::nullopt;
    }

    return impl_->deserialize(value, value_len);
}

std::vector<LexoraSearchResult> LexoraEngine::fuzzy_search(std::string_view query, int max_distance, size_t limit) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    std::vector<LexoraSearchResult> results;

    if (!impl_->loaded) return results;

    std::string normalized = unicode::normalize_nfc(query);

    // If DAWG is loaded, use prefix search to get candidates
    std::vector<std::pair<std::string, std::string>> candidates;

    if (impl_->dawg_enabled) {
        // Get prefix matches as candidates
        auto prefixes = impl_->dawg.prefix_search(normalized, 100);
        for (const auto& word : prefixes) {
            // Look up the ID from the hash table
            uint32_t vlen;
            const char* val = impl_->hash_table.lookup(word, &vlen);
            if (val) {
                // Extract ID (first field in serialized data)
                uint16_t id_len;
                std::memcpy(&id_len, val, 2);
                candidates.emplace_back(word, std::string(val + 2, id_len));
            }
        }
    }

    // If we have SQLite, also get FTS5 candidates. Skipped entirely when
    // SQLite support is not compiled in (LEXORA_USE_SQLITE undefined).
#ifdef LEXORA_USE_SQLITE
    if (impl_->db) {
        sqlite3_stmt* stmt;
        if (sqlite3_prepare_v2(impl_->db,
            "SELECT word, id FROM words WHERE word MATCH ? LIMIT 100",
            -1, &stmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_text(stmt, 1, normalized.c_str(), -1, SQLITE_TRANSIENT);
            while (sqlite3_step(stmt) == SQLITE_ROW) {
                const char* word = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
                const char* id = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
                candidates.emplace_back(word, id);
            }
            sqlite3_finalize(stmt);
        }
    }
#endif

    // Run Levenshtein on candidates
    auto matches = impl_->levenshtein.search(normalized, candidates, max_distance, limit);

    // Convert to LexoraSearchResult with full entries
    for (const auto& m : matches) {
        auto entry = get_word_by_id(m.word_id);
        if (entry) {
            results.push_back({std::move(*entry), m.edit_distance});
        }
    }

    return results;
}

std::vector<std::string> LexoraEngine::prefix_search(std::string_view prefix, size_t limit) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    if (!impl_->loaded || !impl_->dawg_enabled) return {};

    std::string normalized = unicode::normalize_nfc(prefix);
    return impl_->dawg.prefix_search(normalized, limit);
}

std::optional<WordEntry> LexoraEngine::get_word_by_id(const std::string& id) const {
#ifdef LEXORA_USE_SQLITE
    if (!impl_->db) return std::nullopt;

    sqlite3_stmt* stmt;
    if (sqlite3_prepare_v2(impl_->db,
        "SELECT word, phonetic, part_of_speech, definitions, etymology, "
        "synonyms, antonyms, examples, language FROM words WHERE id = ?",
        -1, &stmt, nullptr) != SQLITE_OK) {
        return std::nullopt;
    }

    sqlite3_bind_text(stmt, 1, id.c_str(), -1, SQLITE_TRANSIENT);

    if (sqlite3_step(stmt) != SQLITE_ROW) {
        sqlite3_finalize(stmt);
        return std::nullopt;
    }

    WordEntry entry;
    entry.id = id;
    entry.word = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
    entry.phonetic = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
    entry.part_of_speech = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));
    // definitions, etymology, etc. would be JSON or delimited strings
    // For simplicity, treating them as JSON arrays — a full impl would parse
    sqlite3_finalize(stmt);
    return entry;
#else
    // SQLite fallback not compiled in — cannot resolve arbitrary IDs.
    // Callers (e.g. fuzzy_search) will simply drop these candidates.
    (void)id;
    return std::nullopt;
#endif
}

void LexoraEngine::enable_dawg(bool enabled) {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    if (enabled && !impl_->dawg.is_loaded()) {
        // Would need to reload from file — store the path
    } else if (!enabled && impl_->dawg.is_loaded()) {
        impl_->dawg.unload();
    }
    impl_->dawg_enabled = enabled;
}

bool LexoraEngine::is_dawg_enabled() const {
    return impl_->dawg_enabled;
}

MemoryInfo LexoraEngine::get_memory_usage() const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    return {
        impl_->hash_table.memory_usage_bytes(),
        impl_->dawg.memory_usage_bytes(),
        impl_->hash_table.memory_usage_bytes() + impl_->dawg.memory_usage_bytes()
    };
}

void LexoraEngine::close() {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    impl_->hash_table.unload();
    impl_->dawg.unload();
#ifdef LEXORA_USE_SQLITE
    if (impl_->db) {
        sqlite3_close(impl_->db);
        impl_->db = nullptr;
    }
#endif
    impl_->loaded = false;
}

} // namespace lexora
