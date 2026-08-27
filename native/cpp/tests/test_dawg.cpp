// test_dawg.cpp — Google Test for DAWG
#include "lexicon/dawg.hpp"
#include <gtest/gtest.h>
#include <cstdio>

using namespace lexicon;

class DawgTest : public ::testing::Test {
protected:
    const char* test_file = "/tmp/lexicon_test.dawg";
    std::vector<std::string> words = {
        "apple", "application", "apply", "banana", "band", "bandana",
        "cat", "car", "card", "care", "careful"
    };

    void SetUp() override {
        Dawg dawg;
        dawg.build(words);
        dawg.save_to_file(test_file);
    }

    void TearDown() override {
        std::remove(test_file);
    }
};

TEST_F(DawgTest, ContainsExistingWords) {
    Dawg dawg;
    ASSERT_TRUE(dawg.load_from_file(test_file));

    for (const auto& word : words) {
        EXPECT_TRUE(dawg.contains(word)) << "Expected to find: " << word;
    }
}

TEST_F(DawgTest, DoesNotContainMissingWords) {
    Dawg dawg;
    ASSERT_TRUE(dawg.load_from_file(test_file));

    EXPECT_FALSE(dawg.contains("xyz"));
    EXPECT_FALSE(dawg.contains("banana_split"));
    EXPECT_FALSE(dawg.contains(""));
    EXPECT_FALSE(dawg.contains("APPLE")); // case-sensitive
}

TEST_F(DawgTest, PrefixSearch) {
    Dawg dawg;
    ASSERT_TRUE(dawg.load_from_file(test_file));

    auto results = dawg.prefix_search("ca", 10);
    EXPECT_GE(results.size(), 4u); // cat, car, card, care, careful

    // All results should start with "ca"
    for (const auto& word : results) {
        EXPECT_EQ(word.substr(0, 2), "ca");
    }
}

TEST_F(DawgTest, PrefixSearchWithLimit) {
    Dawg dawg;
    ASSERT_TRUE(dawg.load_from_file(test_file));

    auto results = dawg.prefix_search("app", 2);
    EXPECT_LE(results.size(), 2u);
}

TEST_F(DawgTest, PrefixSearchNoResults) {
    Dawg dawg;
    ASSERT_TRUE(dawg.load_from_file(test_file));

    auto results = dawg.prefix_search("xyz", 10);
    EXPECT_TRUE(results.empty());
}

TEST_F(DawgTest, UnloadReleasesMemory) {
    Dawg dawg;
    dawg.load_from_file(test_file);
    size_t before = dawg.memory_usage_bytes();
    EXPECT_GT(before, 0u);

    dawg.unload();
    EXPECT_FALSE(dawg.is_loaded());
    EXPECT_FALSE(dawg.contains("apple"));
}

TEST_F(DawgTest, ReloadAfterUnload) {
    Dawg dawg;
    dawg.load_from_file(test_file);
    EXPECT_TRUE(dawg.contains("apple"));

    dawg.unload();
    EXPECT_FALSE(dawg.contains("apple"));

    dawg.load_from_file(test_file);
    EXPECT_TRUE(dawg.contains("apple"));
}

TEST_F(DawgTest, MemoryUsageIsReported) {
    Dawg dawg;
    dawg.load_from_file(test_file);
    size_t usage = dawg.memory_usage_bytes();
    EXPECT_GT(usage, 0u);
}
