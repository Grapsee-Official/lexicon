// test_hash_table.cpp — Google Test for MemoryMappedHashTable
#include "lexicon/hash_table.hpp"
#include <gtest/gtest.h>
#include <cstdio>
#include <fstream>

using namespace lexicon;

class HashTableTest : public ::testing::Test {
protected:
    const char* test_file = "/tmp/lexicon_test.ht";

    void SetUp() override {
        // Create a test hash table file
        MemoryMappedHashTable ht;
        ht.insert("hello", "value_hello");
        ht.insert("world", "value_world");
        ht.insert("test", "value_test");
        ht.save_to_file(test_file);
    }

    void TearDown() override {
        std::remove(test_file);
    }
};

TEST_F(HashTableTest, InsertAndLookup) {
    MemoryMappedHashTable ht;
    ht.insert("apple", "fruit");
    ht.insert("banana", "fruit");

    uint32_t len;
    const char* val = ht.lookup("apple", &len);
    ASSERT_NE(val, nullptr);
    EXPECT_EQ(std::string(val, len), "fruit");
}

TEST_F(HashTableTest, LookupMissingKey) {
    MemoryMappedHashTable ht;
    ht.insert("exists", "yes");

    EXPECT_EQ(ht.lookup("missing", nullptr), nullptr);
    EXPECT_FALSE(ht.exists("missing"));
}

TEST_F(HashTableTest, LoadFromMmap) {
    MemoryMappedHashTable ht;
    ASSERT_TRUE(ht.load_from_file(test_file));

    uint32_t len;
    const char* val = ht.lookup("hello", &len);
    ASSERT_NE(val, nullptr);
    EXPECT_EQ(std::string(val, len), "value_hello");
}

TEST_F(HashTableTest, CollisionHandling) {
    // Insert keys that might hash to the same bucket
    MemoryMappedHashTable ht;
    ht.insert("abc", "1");
    ht.insert("bca", "2");
    ht.insert("cab", "3");

    EXPECT_TRUE(ht.exists("abc"));
    EXPECT_TRUE(ht.exists("bca"));
    EXPECT_TRUE(ht.exists("cab"));
}

TEST_F(HashTableTest, LargeDataset) {
    MemoryMappedHashTable ht;
    const int N = 10000;

    for (int i = 0; i < N; ++i) {
        ht.insert("word_" + std::to_string(i), "def_" + std::to_string(i));
    }

    // Verify all lookups
    for (int i = 0; i < N; ++i) {
        uint32_t len;
        const char* val = ht.lookup("word_" + std::to_string(i), &len);
        ASSERT_NE(val, nullptr) << "Missing word_" << i;
    }

    EXPECT_FALSE(ht.exists("word_nonexistent"));
}

TEST_F(HashTableTest, UnloadReleasesMemory) {
    MemoryMappedHashTable ht;
    ht.load_from_file(test_file);
    size_t before = ht.memory_usage_bytes();
    EXPECT_GT(before, 0u);

    ht.unload();
    EXPECT_EQ(ht.memory_usage_bytes(), 0u);
    EXPECT_FALSE(ht.exists("hello"));
}
