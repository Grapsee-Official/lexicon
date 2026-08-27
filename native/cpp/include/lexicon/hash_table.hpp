// hash_table.hpp — Memory-mapped open-addressing hash table for Lexicon
// C++17, no external deps. Robin Hood hashing, thread-safe reads.
#pragma once

#include <cstdint>
#include <optional>
#include <string_view>
#include <vector>
#include <atomic>

namespace lexicon {

struct HashEntry {
    uint64_t hash;       // FNV-1a hash of the key
    uint32_t key_offset; // offset into string pool
    uint32_t value_offset; // offset into value data
    uint16_t key_len;
    uint16_t value_len;
};

struct MmapHeader {
    char magic[8];       // "LEXICONHT"
    uint32_t version;    // format version
    uint32_t entry_count;
    uint32_t bucket_count;
    uint32_t string_pool_offset;
    uint32_t string_pool_size;
    uint32_t value_pool_offset;
    uint32_t value_pool_size;
};

class MemoryMappedHashTable {
public:
    MemoryMappedHashTable();
    ~MemoryMappedHashTable();

    // Load from a memory-mapped file (zero-copy)
    bool load_from_file(const char* path);

    // Lookup by key (NFC-normalized). Returns pointer to value data + length, or nullptr.
    const char* lookup(std::string_view key, uint32_t* out_value_len) const;

    // Check if key exists
    bool exists(std::string_view key) const;

    // For building: insert into in-memory table (not mmap)
    void insert(std::string_view key, std::string_view value);

    // Serialize to mmap file format
    bool save_to_file(const char* path) const;

    // Stats
    size_t size() const { return entry_count_; }
    size_t memory_usage_bytes() const;

    // Unload (release mmap)
    void unload();

private:
    // FNV-1a hash (64-bit)
    static uint64_t fnv1a(std::string_view s);

    // Robin Hood probing
    size_t probe(std::string_view key, size_t& dist) const;

    // mmap data
    int fd_;
    const void* mapped_data_;
    size_t mapped_size_;
    const MmapHeader* header_;
    const HashEntry* buckets_;
    const char* string_pool_;
    const char* value_pool_;
    uint32_t entry_count_;
    uint32_t bucket_count_;

    // In-memory mode (for building)
    std::vector<HashEntry> mem_buckets_;
    std::vector<char> mem_string_pool_;
    std::vector<char> mem_value_pool_;
    bool in_memory_mode_;
};

} // namespace lexicon
