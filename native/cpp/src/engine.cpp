// engine.cpp — Lexicon Engine implementation (facade)
#include "lexicon/engine.hpp"
#include "lexicon/hash_table.hpp"
#include "lexicon/levenshtein.hpp"
#include "lexicon/dawg.hpp"
#include "lexicon/unicode.hpp"

#include "lexicon/sqlite3_shim.h"

#include <algorithm>
#include <cstring>
#include <fstream>
#include <mutex>
#include <sstream>
#include <unordered_map>

namespace lexicon {

struct LexiconEngine::Impl {
    MemoryMappedHashTable hash_table;
    Dawg dawg;
    LevenshteinAutomaton levenshtein;
#ifdef LEXICON_USE_SQLITE
    sqlite3* db = nullptr;
#endif
    std::string dawg_path;
    std::vector<std::string> indexed_words;
    std::unordered_map<std::string, std::string> id_to_word;
    bool dawg_enabled = false;
    bool loaded = false;
    mutable std::mutex mutex;

    ~Impl() {
#ifdef LEXICON_USE_SQLITE
        if (db) sqlite3_close(db);
#endif
        hash_table.unload();
        dawg.unload();
    }

    static void append_u16_str(std::vector<char>& buf, const std::string& s) {
        uint16_t len = static_cast<uint16_t>(std::min<size_t>(s.size(), 65535));
        const char* p = reinterpret_cast<const char*>(&len);
        buf.insert(buf.end(), p, p + 2);
        buf.insert(buf.end(), s.data(), s.data() + len);
    }

    static void append_str_vec(std::vector<char>& buf, const std::vector<std::string>& vec) {
        uint16_t count = static_cast<uint16_t>(std::min<size_t>(vec.size(), 65535));
        const char* p = reinterpret_cast<const char*>(&count);
        buf.insert(buf.end(), p, p + 2);
        for (uint16_t i = 0; i < count; ++i) {
            append_u16_str(buf, vec[i]);
        }
    }

    static std::string serialize(const WordEntry& entry) {
        std::vector<char> buf;
        buf.reserve(512);
        append_u16_str(buf, entry.id);
        append_u16_str(buf, entry.word);
        append_u16_str(buf, entry.phonetic);
        append_u16_str(buf, entry.part_of_speech);
        append_str_vec(buf, entry.definitions);
        append_u16_str(buf, entry.etymology);
        append_str_vec(buf, entry.synonyms);
        append_str_vec(buf, entry.antonyms);
        append_str_vec(buf, entry.examples);
        append_u16_str(buf, entry.language);
        return std::string(buf.data(), buf.size());
    }

    WordEntry deserialize(const char* data, uint32_t len) const {
        WordEntry entry;
        const char* end = data + len;
        const char* p = data;

        auto read_string = [&](std::string& out) {
            if (p + 2 > end) return;
            uint16_t slen;
            std::memcpy(&slen, p, 2);
            p += 2;
            if (p + slen > end) return;
            out.assign(p, slen);
            p += slen;
        };

        auto read_string_vec = [&](std::vector<std::string>& out) {
            if (p + 2 > end) return;
            uint16_t count;
            std::memcpy(&count, p, 2);
            p += 2;
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

    // Unlocked helper — caller MUST already hold `mutex`!
    std::optional<WordEntry> lookup_unlocked(std::string_view word) const {
        if (!loaded) return std::nullopt;

        std::string normalized = unicode::normalize_nfc(word);
        std::string folded = unicode::casefold(normalized);

        uint32_t value_len = 0;
        const char* value = hash_table.lookup(folded, &value_len);
        if (!value) {
            value = hash_table.lookup(normalized, &value_len);
        }
        if (!value) {
            std::string stripped = unicode::strip_diacritics(folded);
            value = hash_table.lookup(stripped, &value_len);
        }
        if (!value) return std::nullopt;

        return deserialize(value, value_len);
    }

    std::optional<WordEntry> get_by_id_unlocked(const std::string& id) const {
        auto it = id_to_word.find(id);
        if (it != id_to_word.end()) {
            return lookup_unlocked(it->second);
        }
        if (auto direct = lookup_unlocked(id)) {
            return direct;
        }
#ifdef LEXICON_USE_SQLITE
        if (!db) return std::nullopt;
        sqlite3_stmt* stmt = nullptr;
        if (sqlite3_prepare_v2(
                db,
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
        if (auto* c = sqlite3_column_text(stmt, 0)) entry.word = reinterpret_cast<const char*>(c);
        if (auto* c = sqlite3_column_text(stmt, 1)) entry.phonetic = reinterpret_cast<const char*>(c);
        if (auto* c = sqlite3_column_text(stmt, 2)) entry.part_of_speech = reinterpret_cast<const char*>(c);
        sqlite3_finalize(stmt);
        return entry;
#else
        return std::nullopt;
#endif
    }

    void seed_starter_lexicon_unlocked() {
        const std::vector<WordEntry> starter = {
            {"wanderlust", "Wanderlust", "/ˈwɒnd.ər.lʌst/", "noun",
             {"A strong desire or urge to wander or travel and explore the world.",
              "An irresistible impulse to travel and explore, often without a fixed destination."},
             "Borrowed from German Wanderlust ('hiking desire'), from wandern ('to hike, roam') + Lust ('desire').",
             {"itchy feet", "restlessness", "travel bug", "nomadism"},
             {"homebody", "stay-at-home"},
             {"Her wanderlust could no longer be ignored, and she sailed across the Mediterranean."},
             "English (borrowed from German)"},
            {"ephemeral", "Ephemeral", "/ɪˈfɛm(ə)rəl/", "adjective",
             {"Lasting for a very short time; transitory."},
             "From Greek ephēmeros ('lasting only a day').",
             {"transient", "fleeting", "momentary", "evanescent"},
             {"permanent", "enduring", "perpetual"},
             {"The ephemeral beauty of cherry blossoms is what makes them precious."},
             "English (from Greek)"},
            {"serendipity", "Serendipity", "/ˌsɛr(ə)nˈdɪpɪti/", "noun",
             {"The occurrence and development of events by chance in a happy or beneficial way."},
             "Coined in 1754 by Horace Walpole after 'The Three Princes of Serendip'.",
             {"chance", "fortune", "kismet", "providence"},
             {"misfortune", "design"},
             {"Their meeting in a Cotswolds bookshop was pure serendipity."},
             "English (coined)"},
            {"limerence", "Limerence", "/ˈlɪmərəns/", "noun",
             {"The state of being involuntarity infatuated with another person, characterised by a strong desire for reciprocation."},
             "Coined in 1977 by psychologist Dorothy Tennov.",
             {"infatuation", "obsession", "crush"},
             {"indifference", "detachment"},
             {"Limerence hinges on uncertainty about the other person's feelings."},
             "English (coined)"},
            {"ethereal", "Ethereal", "/ɪˈθɪərɪəl/", "adjective",
             {"Extremely delicate and light in a way that seems almost too perfect for this world."},
             "From Latin aetherius ('of the upper pure air').",
             {"delicate", "airy", "celestial", "otherworldly"},
             {"heavy", "earthly"},
             {"Morning mist gave the valley an ethereal, dreamlike quality."},
             "English (from Latin/Greek)"},
            {"petrichor", "Petrichor", "/ˈpɛtrɪkɔː/", "noun",
             {"The pleasant, earthy smell produced when rain falls on dry soil."},
             "Coined in 1964 from Greek petros ('stone') + ichor.",
             {"rain-smell", "earth-scent"},
             {},
             {"The first summer rain released a wave of petrichor across the lane."},
             "English (scientific coinage)"},
            {"saudade", "Saudade", "/sɔːˈdɑːdə/", "noun",
             {"A deep emotional state of nostalgic longing for something or someone absent."},
             "Portuguese, central to fado music and Lusophone poetry.",
             {"longing", "nostalgia", "yearning"},
             {"contentment", "fulfilment"},
             {"Fado singers give voice to saudade."},
             "Portuguese"},
            {"hygge", "Hygge", "/ˈh(j)uːɡə/", "noun",
             {"A quality of cosiness and comfortable conviviality that engenders contentment."},
             "Danish, from Old Norse hygga ('to comfort').",
             {"cosiness", "comfort", "warmth"},
             {},
             {"Candlelight, woollen blankets, and good books — the essence of hygge."},
             "Danish"},
            {"ineffable", "Ineffable", "/ɪnˈɛfəb(ə)l/", "adjective",
             {"Too great or extreme to be expressed or described in words."},
             "From Latin ineffabilis, from in- ('not') + effari ('to speak out').",
             {"indescribable", "inexpressible", "beyond words"},
             {"expressible", "describable"},
             {"An ineffable sense of peace settled over the hills at dusk."},
             "English (from Latin)"},
            {"sonder", "Sonder", "/ˈsɒndə/", "noun",
             {"The realisation that each passerby is living a life as vivid and complex as your own."},
             "Coined by John Koenig in 'The Dictionary of Obscure Sorrows'.",
             {"empathic realisation"},
             {},
             {"Watching the train windows glow at twilight brought a quiet wave of sonder."},
             "English (neologism)"},
            {"querencia", "Querencia", "/kɛˈrɛnsɪə/", "noun",
             {"A place where one feels safe; a haven, refuge, or home where one draws strength."},
             "Spanish, from querer ('to love, desire').",
             {"haven", "sanctuary", "refuge"},
             {"danger zone"},
             {"The honey-stone cottage became her querencia."},
             "Spanish"},
            {"komorebi", "Komorebi", "/kɔmɔˈɹɛbi/", "noun",
             {"Sunlight filtering through the leaves of trees."},
             "Japanese 木漏れ日 (tree + leaking + sun).",
             {"dappled light", "sunlight through leaves"},
             {},
             {"The ancient oak lane was bathed in shifting komorebi."},
             "Japanese"},
            {"mellifluous", "Mellifluous", "/mɪˈlɪflʊəs/", "adjective",
             {"Pleasingly smooth and musical to hear; sweet-sounding."},
             "From Late Latin mellifluus ('flowing with honey').",
             {"sweet-sounding", "honeyed", "dulcet"},
             {"harsh", "grating"},
             {"Her mellifluous voice made every verse linger."},
             "English (from Latin)"},
            {"halcyon", "Halcyon", "/ˈhalsɪən/", "adjective",
             {"Denoting a period of time in the past that was idyllically happy and peaceful."},
             "From Greek halkyōn ('kingfisher').",
             {"golden", "idyllic", "peaceful", "serene"},
             {"turbulent", "stormy"},
             {"She remembered the halcyon summers spent reading in the meadow."},
             "English (from Greek)"},
            {"redolent", "Redolent", "/ˈrɛdələnt/", "adjective",
             {"Strongly reminiscent or suggestive of something; fragrant."},
             "From Latin redolere ('to emit a scent').",
             {"fragrant", "aromatic", "evocative"},
             {"odourless"},
             {"The study was redolent of aged paper and cedarwood."},
             "English (from Latin)"},
            {"susurrus", "Susurrus", "/sʊˈsʌrəs/", "noun",
             {"Whispering, murmuring, or a low rustling sound of leaves or wind."},
             "From Latin susurrus ('a whisper').",
             {"whisper", "murmur", "rustle"},
             {},
             {"Only the susurrus of evening wind in the elms broke the silence."},
             "English (from Latin)"},
            {"numinous", "Numinous", "/ˈnjuːmɪnəs/", "adjective",
             {"Awe-inspiring, mysterious, and arousing elevated spiritual feeling."},
             "Coined from Latin numen ('divine presence').",
             {"sacred", "transcendent", "mystical"},
             {"mundane", "secular"},
             {"Moonlight over the Cotswold hills lent the night a numinous hush."},
             "English"},
            {"lissom", "Lissom", "/ˈlɪs(ə)m/", "adjective",
             {"Supple, agile, and graceful in movement."},
             "From Middle English lithesome, from lithe ('supple') + -some.",
             {"supple", "limber", "lithe", "agile", "graceful"},
             {"stiff", "rigid", "clumsy"},
             {"The dancer's lissom frame moved gracefully across the stage."},
             "English"},
            {"solitude", "Solitude", "/ˈsɒlɪtjuːd/", "noun",
             {"The state of being alone or isolated.",
              "A lonely or uninhabited place; peaceful seclusion."},
             "From Old French solitude, from Latin solitudo ('loneliness'), from solus ('alone').",
             {"seclusion", "quietude", "isolation", "peace"},
             {"company", "companionship", "crowd"},
             {"She found quiet creative renewal in the solitude of the countryside cottage."},
             "English (from Latin)"},
            {"eloquence", "Eloquence", "/ˈɛləkwəns/", "noun",
             {"Fluent or persuasive speaking or writing.",
              "The art of using language with grace, clarity, and emotional power."},
             "From Old French eloquence, from Latin eloquentia, from eloqui ('to speak out').",
             {"fluency", "expressiveness", "oratory", "poise"},
             {"inarticulateness", "hesitation"},
             {"His quiet eloquence moved every listener in the parish hall."},
             "English (from Latin)"},
            {"nostalgia", "Nostalgia", "/nɒˈstældʒə/", "noun",
             {"A sentimental longing or wistful affection for the past.",
              "Bittersweet yearning for a fondly remembered place or time."},
             "Coined in 1688 as Modern Latin from Greek nostos ('return home') + algos ('pain, longing').",
             {"wistfulness", "longing", "reminiscence", "yearning"},
             {"indifference", "foresight"},
             {"Autumn mist over the stone bridge stirred a gentle nostalgia."},
             "English (from Greek)"},
            {"tranquility", "Tranquility", "/træŋˈkwɪlɪti/", "noun",
             {"The quality or state of being tranquil; calm, serene composure.",
              "Quiet freedom from disturbance or agitation."},
             "From Old French tranquillite, from Latin tranquillitas ('quietness, stillness').",
             {"serenity", "peacefulness", "stillness", "calm"},
             {"turmoil", "agitation", "chaos"},
             {"Evening settled over the river meadow in unbroken tranquility."},
             "English (from Latin)"},
            {"quixotic", "Quixotic", "/kwɪkˈsɒtɪk/", "adjective",
             {"Extremely idealistic, romantic, and impractical.",
              "Driven by chivalrous or lofty vision without regard to practicality."},
             "After Don Quixote, hero of Cervantes' 1605 novel, famed for noble yet impossible quests.",
             {"idealistic", "visionary", "romantic", "utopian"},
             {"pragmatic", "realistic", "cynical"},
             {"Restoring the forgotten village library seemed quixotic until the whole valley joined in."},
             "English (literary eponym)"},
            {"vellichor", "Vellichor", "/ˈvɛlɪkɔː/", "noun",
             {"The strange wistfulness of used bookstores, infused with the passage of time."},
             "From vellum ('parchment') + ichor.",
             {"bookstore nostalgia"},
             {},
             {"He wandered the quiet shelves, surrendering to their vellichor."},
             "English (neologism)"},
        };

        hash_table.unload();
        dawg.unload();
        indexed_words.clear();
        id_to_word.clear();

        std::vector<std::string> sorted_keys;
        sorted_keys.reserve(starter.size());

        for (const auto& e : starter) {
            std::string key = unicode::casefold(unicode::normalize_nfc(e.word));
            std::string blob = serialize(e);
            hash_table.insert(key, blob);
            sorted_keys.push_back(key);
            indexed_words.push_back(key);
            id_to_word[e.id] = key;
        }

        std::sort(sorted_keys.begin(), sorted_keys.end());
        sorted_keys.erase(std::unique(sorted_keys.begin(), sorted_keys.end()), sorted_keys.end());
        dawg.build(sorted_keys);
        dawg_enabled = true;
        loaded = true;
    }
};

LexiconEngine::LexiconEngine() : impl_(std::make_unique<Impl>()) {}
LexiconEngine::~LexiconEngine() = default;

bool LexiconEngine::load_database(const std::string& db_dir) {
    std::lock_guard<std::mutex> lock(impl_->mutex);

    if (db_dir == ":embedded:" || db_dir == "embedded") {
        impl_->seed_starter_lexicon_unlocked();
        return true;
    }

    std::string ht_path = db_dir + "/lexicon.ht";
    if (!impl_->hash_table.load_from_file(ht_path.c_str())) {
        // If no external .ht pack exists on disk yet, fall back to ":embedded:" only when explicitly requested
        return false;
    }

    impl_->dawg_path = db_dir + "/lexicon.dawg";
    if (impl_->dawg.load_from_file(impl_->dawg_path.c_str())) {
        impl_->dawg_enabled = true;
        impl_->indexed_words = impl_->dawg.prefix_search("", 5000);
        impl_->id_to_word.clear();
        for (const auto& w : impl_->indexed_words) {
            uint32_t vlen = 0;
            const char* val = impl_->hash_table.lookup(w, &vlen);
            if (val && vlen >= 2) {
                uint16_t id_len = 0;
                std::memcpy(&id_len, val, 2);
                if (2u + id_len <= vlen) {
                    impl_->id_to_word.emplace(std::string(val + 2, id_len), w);
                }
            }
        }
    }

#ifdef LEXICON_USE_SQLITE
    if (impl_->db) {
        sqlite3_close(impl_->db);
        impl_->db = nullptr;
    }
    std::string sqlite_path = db_dir + "/lexicon.sqlite";
    std::ifstream sq_check(sqlite_path);
    if (sq_check.good()) {
        if (sqlite3_open(sqlite_path.c_str(), &impl_->db) != SQLITE_OK) {
            impl_->db = nullptr;
        } else {
            sqlite3_exec(impl_->db, "PRAGMA journal_mode=WAL;", nullptr, nullptr, nullptr);
        }
    }
#endif

    impl_->loaded = true;
    return true;
}

std::optional<WordEntry> LexiconEngine::lookup(std::string_view word) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    return impl_->lookup_unlocked(word);
}

std::vector<LexiconSearchResult> LexiconEngine::fuzzy_search(
    std::string_view query,
    int max_distance,
    size_t limit
) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    std::vector<LexiconSearchResult> results;
    if (!impl_->loaded || query.empty()) return results;

    std::string normalized = unicode::casefold(unicode::normalize_nfc(query));
    std::vector<std::pair<std::string, std::string>> candidates;

    if (impl_->dawg_enabled && impl_->dawg.is_loaded()) {
        auto prefixes = impl_->dawg.prefix_search(normalized, std::max<size_t>(limit * 4, 64));
        for (const auto& word : prefixes) {
            uint32_t vlen = 0;
            const char* val = impl_->hash_table.lookup(word, &vlen);
            if (val && vlen >= 2) {
                uint16_t id_len = 0;
                std::memcpy(&id_len, val, 2);
                if (2u + id_len <= vlen) {
                    candidates.emplace_back(word, std::string(val + 2, id_len));
                }
            }
        }
    }

    // Also include indexed words within length tolerance so true typo fuzzy search (e.g. "wandrlust") works even when first char differs!
    if (max_distance > 0 && !impl_->indexed_words.empty()) {
        for (const auto& w : impl_->indexed_words) {
            int len_diff = static_cast<int>(w.size()) - static_cast<int>(normalized.size());
            if (len_diff < 0) len_diff = -len_diff;
            if (len_diff <= max_distance) {
                uint32_t vlen = 0;
                const char* val = impl_->hash_table.lookup(w, &vlen);
                if (val && vlen >= 2) {
                    uint16_t id_len = 0;
                    std::memcpy(&id_len, val, 2);
                    if (2u + id_len <= vlen) {
                        candidates.emplace_back(w, std::string(val + 2, id_len));
                    }
                }
            }
        }
    }

#ifdef LEXICON_USE_SQLITE
    if (impl_->db) {
        sqlite3_stmt* stmt = nullptr;
        if (sqlite3_prepare_v2(
                impl_->db,
                "SELECT word, id FROM words WHERE word MATCH ? LIMIT 100",
                -1, &stmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_text(stmt, 1, normalized.c_str(), -1, SQLITE_TRANSIENT);
            while (sqlite3_step(stmt) == SQLITE_ROW) {
                const char* word = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0));
                const char* id = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
                if (word && id) candidates.emplace_back(word, id);
            }
            sqlite3_finalize(stmt);
        }
    }
#endif

    // Deduplicate candidate pairs before Levenshtein evaluation
    std::sort(candidates.begin(), candidates.end());
    candidates.erase(std::unique(candidates.begin(), candidates.end()), candidates.end());

    if (max_distance == 0) {
        // Prefix-only mode (used by prefixSearch(maxDistance=0))
        for (const auto& cand : candidates) {
            if (cand.first.rfind(normalized, 0) == 0) {
                if (auto entry = impl_->lookup_unlocked(cand.first)) {
                    results.push_back({std::move(*entry), 0});
                    if (results.size() >= limit) break;
                }
            }
        }
        return results;
    }

    auto matches = impl_->levenshtein.search(normalized, candidates, max_distance, limit);

    for (const auto& m : matches) {
        auto entry = impl_->get_by_id_unlocked(m.word_id);
        if (!entry) {
            entry = impl_->lookup_unlocked(m.word);
        }
        if (entry) {
            results.push_back({std::move(*entry), m.edit_distance});
        }
    }

    return results;
}

std::vector<std::string> LexiconEngine::prefix_search(std::string_view prefix, size_t limit) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    if (!impl_->loaded || !impl_->dawg_enabled || !impl_->dawg.is_loaded()) return {};

    std::string normalized = unicode::casefold(unicode::normalize_nfc(prefix));
    return impl_->dawg.prefix_search(normalized, limit);
}

std::optional<WordEntry> LexiconEngine::get_word_by_id(const std::string& id) const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    return impl_->get_by_id_unlocked(id);
}

void LexiconEngine::enable_dawg(bool enabled) {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    if (enabled && !impl_->dawg.is_loaded()) {
        if (!impl_->dawg_path.empty()) {
            impl_->dawg.load_from_file(impl_->dawg_path.c_str());
        } else if (!impl_->indexed_words.empty()) {
            std::vector<std::string> sorted = impl_->indexed_words;
            std::sort(sorted.begin(), sorted.end());
            impl_->dawg.build(sorted);
        }
    } else if (!enabled && impl_->dawg.is_loaded()) {
        impl_->dawg.unload();
    }
    impl_->dawg_enabled = enabled;
}

bool LexiconEngine::is_dawg_enabled() const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    return impl_->dawg_enabled;
}

MemoryInfo LexiconEngine::get_memory_usage() const {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    size_t ht = impl_->hash_table.memory_usage_bytes();
    size_t dg = impl_->dawg.memory_usage_bytes();
    return {ht, dg, ht + dg};
}

void LexiconEngine::close() {
    std::lock_guard<std::mutex> lock(impl_->mutex);
    impl_->hash_table.unload();
    impl_->dawg.unload();
    impl_->indexed_words.clear();
    impl_->id_to_word.clear();
#ifdef LEXICON_USE_SQLITE
    if (impl_->db) {
        sqlite3_close(impl_->db);
        impl_->db = nullptr;
    }
#endif
    impl_->dawg_enabled = false;
    impl_->loaded = false;
}

} // namespace lexicon
