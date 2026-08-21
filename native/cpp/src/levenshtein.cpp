// levenshtein.cpp — Levenshtein automaton implementation
#include "lexora/levenshtein.hpp"
#include <algorithm>
#include <cstring>

namespace lexora {

LevenshteinAutomaton::LevenshteinAutomaton() {}

int LevenshteinAutomaton::distance(std::string_view a, std::string_view b) const {
    int n = static_cast<int>(a.size());
    int m = static_cast<int>(b.size());
    if (n == 0) return m;
    if (m == 0) return n;

    // Use two rows for O(min(n,m)) space
    if (n < m) {
        std::swap(a, b);
        std::swap(n, m);
    }

    std::vector<int> prev(m + 1), curr(m + 1);
    for (int j = 0; j <= m; ++j) prev[j] = j;

    for (int i = 1; i <= n; ++i) {
        curr[0] = i;
        for (int j = 1; j <= m; ++j) {
            int cost = (a[i-1] == b[j-1]) ? 0 : 1;
            curr[j] = std::min({prev[j] + 1, curr[j-1] + 1, prev[j-1] + cost});
        }
        std::swap(prev, curr);
    }
    return prev[m];
}

bool LevenshteinAutomaton::matches(std::string_view query, std::string_view word, int max_distance) const {
    // Early termination: length difference > max_distance → can't match
    int len_diff = std::abs(static_cast<int>(query.size()) - static_cast<int>(word.size()));
    if (len_diff > max_distance) return false;

    // For distance 0, just compare
    if (max_distance == 0) return query == word;

    // For distance 1, use Myers' bit-parallel
    if (max_distance == 1) return within_one_edit(query, word);

    // For distance 2, use banded DP (only compute within 2*max_distance+1 diagonal)
    int n = static_cast<int>(query.size());
    int m = static_cast<int>(word.size());
    int bandwidth = max_distance;

    // Banded DP
    std::vector<int> prev(m + 1), curr(m + 1);
    for (int j = 0; j <= m; ++j) prev[j] = j;

    for (int i = 1; i <= n; ++i) {
        curr[0] = i;
        int j_start = std::max(1, i - bandwidth);
        int j_end = std::min(m, i + bandwidth);

        for (int j = j_start; j <= j_end; ++j) {
            int cost = (query[i-1] == word[j-1]) ? 0 : 1;
            curr[j] = std::min({prev[j] + 1, curr[j-1] + 1, prev[j-1] + cost});
        }
        std::swap(prev, curr);
    }

    return prev[m] <= max_distance;
}

std::vector<SearchResult> LevenshteinAutomaton::search(
    std::string_view query,
    const std::vector<std::pair<std::string, std::string>>& candidates,
    int max_distance,
    size_t limit
) const {
    std::vector<SearchResult> results;

    for (const auto& [word, id] : candidates) {
        if (matches(query, word, max_distance)) {
            int dist = distance(query, word);
            if (dist <= max_distance) {
                results.push_back({word, dist, id});
            }
        }
    }

    // Sort by distance, then alphabetically
    std::sort(results.begin(), results.end(), [](const SearchResult& a, const SearchResult& b) {
        if (a.edit_distance != b.edit_distance) return a.edit_distance < b.edit_distance;
        return a.word < b.word;
    });

    if (results.size() > limit) {
        results.resize(limit);
    }
    return results;
}

bool LevenshteinAutomaton::within_one_edit(std::string_view query, std::string_view word) const {
    int n = static_cast<int>(query.size());
    int m = static_cast<int>(word.size());
    int diff = std::abs(n - m);
    if (diff > 1) return false;

    // If same length: check for exactly 1 substitution
    if (n == m) {
        int mismatches = 0;
        for (int i = 0; i < n; ++i) {
            if (query[i] != word[i]) {
                if (++mismatches > 1) return false;
            }
        }
        return mismatches == 1 || mismatches == 0;
    }

    // Different lengths: check for 1 insertion/deletion
    std::string_view longer = (n > m) ? query : word;
    std::string_view shorter = (n > m) ? word : query;

    int i = 0, j = 0;
    bool found_diff = false;
    while (i < static_cast<int>(longer.size()) && j < static_cast<int>(shorter.size())) {
        if (longer[i] != shorter[j]) {
            if (found_diff) return false;
            found_diff = true;
            ++i; // skip one char in longer
        } else {
            ++i;
            ++j;
        }
    }
    return true;
}

int LevenshteinAutomaton::diacritic_aware_distance(std::string_view a, std::string_view b) const {
    // Strip diacritics from both, then compare with 0.5 cost for diacritic differences
    // This is a simplified version — full implementation would use unicode module
    // For now: compute standard distance, then check if remaining diff is only diacritics
    int base_dist = distance(a, b);
    if (base_dist == 0) return 0;

    // If base distance is 1 and the difference is a diacritic, return 0.5 (rounded to 1 for int)
    // Full implementation would compare base characters
    return base_dist;
}

void LevenshteinAutomaton::build_peq(std::string_view pattern, uint64_t peq[256]) const {
    std::memset(peq, 0, sizeof(uint64_t) * 256);
    for (size_t i = 0; i < pattern.size() && i < 64; ++i) {
        peq[static_cast<unsigned char>(pattern[i])] |= (1ULL << i);
    }
}

} // namespace lexora
