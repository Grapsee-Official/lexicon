#include <thread>
#include <atomic>
// test_engine.cpp — Google Test for LexiconEngine (end-to-end)
#include "lexicon/engine.hpp"
#include <gtest/gtest.h>
#include <filesystem>
#include <fstream>

using namespace lexicon;

// Note: These tests require a pre-built database.
// In CI, the ETL pipeline would run first to generate the .ht + .dawg + .sqlite files.
// For unit testing without a real database, we test the engine's behavior when
// no database is loaded.

class EngineTest : public ::testing::Test {
protected:
    LexiconEngine engine;
};

TEST_F(EngineTest, LookupWithoutDatabaseReturnsNullopt) {
    auto result = engine.lookup("test");
    EXPECT_FALSE(result.has_value());
}

TEST_F(EngineTest, FuzzySearchWithoutDatabaseReturnsEmpty) {
    auto results = engine.fuzzy_search("test", 2, 10);
    EXPECT_TRUE(results.empty());
}

TEST_F(EngineTest, PrefixSearchWithoutDatabaseReturnsEmpty) {
    auto results = engine.prefix_search("te", 10);
    EXPECT_TRUE(results.empty());
}

TEST_F(EngineTest, DawgToggleDoesNotCrashWithoutDatabase) {
    engine.enable_dawg(false);
    EXPECT_FALSE(engine.is_dawg_enabled());
    engine.enable_dawg(true);
    // Should not crash even without database
}

TEST_F(EngineTest, MemoryUsageReportsZeroWithoutDatabase) {
    auto info = engine.get_memory_usage();
    EXPECT_EQ(info.total_bytes, 0u);
}

TEST_F(EngineTest, CloseIsIdempotent) {
    engine.close();
    engine.close(); // should not crash
}

TEST_F(EngineTest, ThreadSafeConcurrentLookups) {
    // Even without a database, concurrent calls should not crash
    std::vector<std::thread> threads;
    std::atomic<int> success_count{0};

    for (int i = 0; i < 10; ++i) {
        threads.emplace_back([&, i]() {
            auto result = engine.lookup("word_" + std::to_string(i));
            if (!result) {
                success_count++; // expecting no result (no DB loaded)
            }
        });
    }

    for (auto& t : threads) t.join();
    EXPECT_EQ(success_count.load(), 10);
}

TEST_F(EngineTest, EmbeddedStarterLexiconLookupFuzzyAndPrefix) {
    ASSERT_TRUE(engine.load_database(":embedded:"));

    auto exact = engine.lookup("Wanderlust");
    ASSERT_TRUE(exact.has_value());
    EXPECT_EQ(exact->word, "Wanderlust");
    EXPECT_EQ(exact->part_of_speech, "noun");
    ASSERT_FALSE(exact->definitions.empty());

    // Fuzzy typo search ("wandrlust" -> distance 1 to "wanderlust") without deadlock!
    auto fuzzy = engine.fuzzy_search("wandrlust", 2, 5);
    ASSERT_FALSE(fuzzy.empty());
    EXPECT_EQ(fuzzy[0].entry.word, "Wanderlust");
    EXPECT_EQ(fuzzy[0].edit_distance, 1);

    // Prefix search via DAWG
    auto pref = engine.prefix_search("seren", 5);
    ASSERT_FALSE(pref.empty());
    EXPECT_EQ(pref[0], "serendipity");

    // DAWG unload & reload cycle preserves functionality
    engine.enable_dawg(false);
    EXPECT_FALSE(engine.is_dawg_enabled());
    engine.enable_dawg(true);
    EXPECT_TRUE(engine.is_dawg_enabled());
    EXPECT_FALSE(engine.prefix_search("ephem", 5).empty());
}
