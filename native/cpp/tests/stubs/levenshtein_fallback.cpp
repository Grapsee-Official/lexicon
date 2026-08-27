// SPDX-License-Identifier: MIT
//
// tests/stubs/levenshtein_fallback.cpp
//
// STOPGAP implementation of lexicon::LevenshteinAutomaton.
//
// This file exists ONLY so the engine facade can link and run its tests
// before the parallel Levenshtein module (task P3-02-CPP-LEVENSHTEIN)
// delivers src/levenshtein.cpp + cmake/Levenshtein.cmake.
//
// The implementation here is the textbook Wagner-Fischer O(m*n) DP with
// early-exit when the row minimum exceeds max_distance. It is correct but
// slower than the bit-parallel / Ukkonen-cutoff algorithms the real module
// will ship with — fine for tests (dictionaries of <10k words), not for
// production.
//
// When the real levenshtein.cpp lands, this file should be deleted (or the
// CMake conditional in cmake/Engine.cmake that includes it will silently
// skip it — see the `if(NOT EXISTS .../src/levenshtein.cpp)` guard).
//
// API surface mirrors include/lexicon/levenshtein.hpp exactly.

#include "lexicon/levenshtein.hpp"

#include <algorithm>
#include <cstddef>
#include <cstring>
#include <utility>
#include <vector>

namespace lexicon {

// ---------------------------------------------------------------------------
// Internal types
// ---------------------------------------------------------------------------

struct LevenshteinAutomaton::PrecomputedQuery {
    std::string query_nfc;       // NFC-normalised bound query.
    int max_distance = 0;        // Bound max_distance (clamped to kMaxDistance).
};

struct LevenshteinAutomaton::Cluster {
    char32_t base = 0;
    std::vector<char32_t> marks;
};

// ---------------------------------------------------------------------------
// Constructors / destructor
// ---------------------------------------------------------------------------

LevenshteinAutomaton::LevenshteinAutomaton()
    : precomputed_(std::make_unique<PrecomputedQuery>()) {}

LevenshteinAutomaton::LevenshteinAutomaton(std::vector<std::string> lexicon)
    : lexicon_(std::move(lexicon)),
      precomputed_(std::make_unique<PrecomputedQuery>()) {}

LevenshteinAutomaton::LevenshteinAutomaton(const std::string& query,
                                           int max_distance)
    : precomputed_(std::make_unique<PrecomputedQuery>()) {
    set_query(query, max_distance);
}

LevenshteinAutomaton::~LevenshteinAutomaton() = default;

LevenshteinAutomaton::LevenshteinAutomaton(LevenshteinAutomaton&&) noexcept
    = default;
LevenshteinAutomaton& LevenshteinAutomaton::operator=(LevenshteinAutomaton&&)
    noexcept = default;

// ---------------------------------------------------------------------------
// Mutators
// ---------------------------------------------------------------------------

void LevenshteinAutomaton::set_lexicon(std::vector<std::string> lexicon) {
    lexicon_ = std::move(lexicon);
}

void LevenshteinAutomaton::set_query(const std::string& query, int max_distance) {
    precomputed_ = std::make_unique<PrecomputedQuery>();
    precomputed_->query_nfc = unicode::normalize_nfc(query);
    precomputed_->max_distance = std::min(max_distance, kMaxDistance);
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

namespace {

// Classic Wagner-Fischer DP with Ukkonen early-exit at max_distance.
// Returns the exact edit distance, or max_distance+1 if it exceeds the cap.
int wf_capped(const std::string& a, const std::string& b, int cap) {
    const int m = static_cast<int>(a.size());
    const int n = static_cast<int>(b.size());
    if (m == 0) return n;
    if (n == 0) return m;

    // Two rolling rows.
    std::vector<int> prev(n + 1), cur(n + 1);
    for (int j = 0; j <= n; ++j) prev[j] = j;

    for (int i = 1; i <= m; ++i) {
        cur[0] = i;
        int row_min = cur[0];
        for (int j = 1; j <= n; ++j) {
            int cost = (a[i - 1] == b[j - 1]) ? 0 : 1;
            int del = prev[j] + 1;
            int ins = cur[j - 1] + 1;
            int sub = prev[j - 1] + cost;
            int v = std::min({del, ins, sub});
            cur[j] = v;
            if (v < row_min) row_min = v;
        }
        if (row_min > cap) return cap + 1;  // early exit
        std::swap(prev, cur);
    }
    return prev[n];
}

} // namespace

// ---------------------------------------------------------------------------
// Instance API
// ---------------------------------------------------------------------------

bool LevenshteinAutomaton::matches(const std::string& candidate) const {
    if (!precomputed_ || precomputed_->query_nfc.empty()) return false;
    return matches(candidate, precomputed_->max_distance);
}

int LevenshteinAutomaton::distance(const std::string& candidate) const {
    if (!precomputed_) return -1;
    int cap = precomputed_->max_distance;
    if (cap < 0) return -1;
    std::string c = unicode::normalize_nfc(candidate);
    int d = wf_capped(precomputed_->query_nfc, c, cap);
    return d > cap ? -1 : d;
}

bool LevenshteinAutomaton::matches(const std::string& word,
                                   int max_distance) const {
    if (!precomputed_) return false;
    int cap = std::min(max_distance, kMaxDistance);
    std::string w = unicode::normalize_nfc(word);
    int d = wf_capped(precomputed_->query_nfc, w, cap);
    return d <= cap;
}

// ---------------------------------------------------------------------------
// Static utility
// ---------------------------------------------------------------------------

int LevenshteinAutomaton::distance(const std::string& word1,
                                   const std::string& word2) {
    std::string a = unicode::normalize_nfc(word1);
    std::string b = unicode::normalize_nfc(word2);
    // Use a large cap (= max length) so we always get the true distance.
    int cap = static_cast<int>(std::max(a.size(), b.size()));
    return wf_capped(a, b, cap);
}

// ---------------------------------------------------------------------------
// Lexicon search
// ---------------------------------------------------------------------------

std::vector<Match> LevenshteinAutomaton::search_with_edits(
    const std::string& query, int max_distance) const {
    std::vector<Match> out;
    int cap = std::min(max_distance, kMaxDistance);
    std::string q = unicode::normalize_nfc(query);
    for (const auto& word : lexicon_) {
        std::string w = unicode::normalize_nfc(word);
        int d = wf_capped(q, w, cap);
        if (d <= cap) {
            out.push_back(Match{word, d, static_cast<float>(d), false});
        }
    }
    std::sort(out.begin(), out.end(),
              [](const Match& a, const Match& b) {
                  if (a.edit_distance != b.edit_distance)
                      return a.edit_distance < b.edit_distance;
                  return a.word < b.word;
              });
    return out;
}

} // namespace lexicon
