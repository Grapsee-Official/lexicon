// test_engine.cpp — Google Test for LexoraEngine (end-to-end)
#include "lexora/engine.hpp"
#include <gtest/gtest.h>
#include <filesystem>
#include <fstream>

using namespace lexora;

// Note: These tests require a pre-built database.
// In CI, the ETL pipeline would run first to generate the .ht + .dawg + .sqlite files.
// For unit testing without a real database, we test the engine's behavior when
// no database is loaded.

class EngineTest : public ::testing::Test {
protected:
    LexoraEngine engine;
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
    engine.enableDawg(false);
    EXPECT_FALSE(engine.isDawgEnabled());
    engine.enableDawg(true);
    // Should not crash even without database
}

TEST_F(EngineTest, MemoryUsageReportsZeroWithoutDatabase) {
    auto info = engine.getMemoryUsage();
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
