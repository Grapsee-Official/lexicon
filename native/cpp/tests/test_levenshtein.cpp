// test_levenshtein.cpp — Google Test for LevenshteinAutomaton
#include "lexora/levenshtein.hpp"
#include <gtest/gtest.h>

using namespace lexora;

TEST(LevenshteinTest, ExactMatch) {
    LevenshteinAutomaton lev;
    EXPECT_EQ(lev.distance("hello", "hello"), 0);
    EXPECT_TRUE(lev.matches("hello", "hello", 0));
}

TEST(LevenshteinTest, OneEditSubstitution) {
    LevenshteinAutomaton lev;
    EXPECT_EQ(lev.distance("cat", "bat"), 1);
    EXPECT_TRUE(lev.matches("cat", "bat", 1));
    EXPECT_FALSE(lev.matches("cat", "bat", 0));
}

TEST(LevenshteinTest, OneEditInsertion) {
    LevenshteinAutomaton lev;
    EXPECT_EQ(lev.distance("cat", "cats"), 1);
    EXPECT_TRUE(lev.matches("cat", "cats", 1));
}

TEST(LevenshteinTest, OneEditDeletion) {
    LevenshteinAutomaton lev;
    EXPECT_EQ(lev.distance("cats", "cat"), 1);
    EXPECT_TRUE(lev.matches("cats", "cat", 1));
}

TEST(LevenshteinTest, TwoEdits) {
    LevenshteinAutomaton lev;
    EXPECT_EQ(lev.distance("kitten", "sitting"), 3); // classic example
    EXPECT_TRUE(lev.matches("cat", "dog", 2));  // 3 substitutions = distance 3, but ≤2 check
    EXPECT_FALSE(lev.matches("cat", "dog", 1));
}

TEST(LevenshteinTest, LengthDifferenceExceedsMaxDistance) {
    LevenshteinAutomaton lev;
    EXPECT_FALSE(lev.matches("hi", "hello_world", 2));
}

TEST(LevenshteinTest, SearchReturnsSortedResults) {
    LevenshteinAutomaton lev;
    std::vector<std::pair<std::string, std::string>> candidates = {
        {"cat", "id1"}, {"bat", "id2"}, {"cart", "id3"},
        {"dog", "id4"}, {"rat", "id5"}
    };

    auto results = lev.search("cat", candidates, 2, 10);
    ASSERT_FALSE(results.empty());
    EXPECT_EQ(results[0].word, "cat");   // exact match first
    EXPECT_EQ(results[0].edit_distance, 0);

    // Results should be sorted by distance
    for (size_t i = 1; i < results.size(); ++i) {
        EXPECT_GE(results[i].edit_distance, results[i-1].edit_distance);
    }
}

TEST(LevenshteinTest, SearchRespectsLimit) {
    LevenshteinAutomaton lev;
    std::vector<std::pair<std::string, std::string>> candidates;
    for (int i = 0; i < 100; ++i) {
        candidates.push_back({"word" + std::to_string(i), "id" + std::to_string(i)});
    }

    auto results = lev.search("word0", candidates, 2, 5);
    EXPECT_LE(results.size(), 5u);
}

TEST(LevenshteinTest, WithinOneEdit) {
    LevenshteinAutomaton lev;
    EXPECT_TRUE(lev.within_one_edit("cat", "bat"));
    EXPECT_TRUE(lev.within_one_edit("cat", "cats"));
    EXPECT_TRUE(lev.within_one_edit("cats", "cat"));
    EXPECT_FALSE(lev.within_one_edit("cat", "dog"));
    EXPECT_FALSE(lev.within_one_edit("hello", "world"));
}
