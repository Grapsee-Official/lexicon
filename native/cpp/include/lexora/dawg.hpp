// dawg.hpp — Directed Acyclic Word Graph (DAWG) for prefix search
// C++17, no external deps. Memory-mapped, toggleable for RAM pressure.
#pragma once

#include <cstdint>
#include <string>
#include <string_view>
#include <vector>

namespace lexora {

// Compact node: 32 bits
// Bits 0-7:   letter (ASCII/UTF-8 byte)
// Bit 8:      end-of-word flag
// Bit 9:      last-child flag (no more siblings)
// Bits 10-31: child pointer (offset into node array)
struct DawgNode {
    uint32_t data;
};

struct DawgHeader {
    char magic[8];        // "LEXORADG"
    uint32_t version;
    uint32_t node_count;
    uint32_t root_offset;
    uint32_t string_pool_offset;
    uint32_t string_pool_size;
};

class Dawg {
public:
    Dawg();
    ~Dawg();

    // Load from .dawg file (memory-mapped)
    bool load_from_file(const char* path);

    // Unload (release memory/mmap) — toggleable for RAM pressure
    void unload();

    // Check if word exists in the DAWG
    bool contains(std::string_view word) const;

    // Find all words with the given prefix
    std::vector<std::string> prefix_search(std::string_view prefix, size_t limit = 50) const;

    // Memory usage
    size_t memory_usage_bytes() const;

    // Is the DAWG currently loaded?
    bool is_loaded() const { return loaded_; }

    // Build from sorted word list (for ETL pipeline)
    void build(const std::vector<std::string>& sorted_words);

    // Save to .dawg file
    bool save_to_file(const char* path) const;

private:
    // Traverse from root following the word's characters
    bool traverse(std::string_view word, uint32_t* out_node) const;

    // Recursive prefix search helper
    void collect_words(uint32_t node, std::string& current, std::vector<std::string>& results, size_t limit) const;

    // Get the letter, eow, last-child, child pointer from a node
    static inline char get_letter(DawgNode n) { return static_cast<char>(n.data & 0xFF); }
    static inline bool is_eow(DawgNode n) { return (n.data >> 8) & 1; }
    static inline bool is_last_child(DawgNode n) { return (n.data >> 9) & 1; }
    static inline uint32_t get_child(DawgNode n) { return (n.data >> 10) & 0x3FFFFF; }

    // mmap data
    int fd_;
    const void* mapped_data_;
    size_t mapped_size_;
    const DawgHeader* header_;
    const DawgNode* nodes_;
    bool loaded_;

    // In-memory build mode
    std::vector<DawgNode> mem_nodes_;
    uint32_t mem_root_offset_;
};

} // namespace lexora
