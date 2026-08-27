// levenshtein.hpp — Levenshtein automaton for fuzzy search (≤2 edits)
// C++17, no external deps. Bit-parallel Myers' algorithm for d=1.
#pragma once

#include <string>
#include <string_view>
#include <vector>
#include <cstdint>

namespace lexicon {

struct SearchResult {
    std::string word;
    int edit_distance;
    std::string word_id;
};

class LevenshteinAutomaton {
public:
    LevenshteinAutomaton();

    // Exact distance between two strings (classic DP, O(n*m))
    int distance(std::string_view a, std::string_view b) const;

    // Check if word is within max_distance edits of the query
    bool matches(std::string_view query, std::string_view word, int max_distance = 2) const;

    // Search a candidate list, return sorted results (by distance, then alpha)
    std::vector<SearchResult> search(
        std::string_view query,
        const std::vector<std::pair<std::string, std::string>>& candidates, // (word, id)
        int max_distance = 2,
        size_t limit = 20
    ) const;

    // Myers' bit-parallel algorithm for distance ≤ 1 (fast prefix check)
    bool within_one_edit(std::string_view query, std::string_view word) const;

    // Diacritic-insensitive comparison (0.5 cost for diacritic differences)
    int diacritic_aware_distance(std::string_view a, std::string_view b) const;

private:
    // Myers' bit-vector Peq (precomputed character positions)
    void build_peq(std::string_view pattern, uint64_t peq[256]) const;
};

} // namespace lexicon
