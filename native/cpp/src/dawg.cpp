// dawg.cpp — DAWG implementation
#include "lexicon/dawg.hpp"

#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <cstring>
#include <algorithm>
#include <functional>
#include <memory>
#include <unordered_map>
#include <vector>

namespace lexicon {

Dawg::Dawg()
    : fd_(-1), mapped_data_(nullptr), mapped_size_(0),
      header_(nullptr), nodes_(nullptr), loaded_(false),
      mem_root_offset_(0) {}

Dawg::~Dawg() {
    unload();
}

bool Dawg::load_from_file(const char* path) {
    unload();

    fd_ = open(path, O_RDONLY);
    if (fd_ < 0) return false;

    struct stat st;
    if (fstat(fd_, &st) != 0) {
        close(fd_);
        fd_ = -1;
        return false;
    }
    mapped_size_ = st.st_size;

    mapped_data_ = mmap(nullptr, mapped_size_, PROT_READ, MAP_SHARED, fd_, 0);
    if (mapped_data_ == MAP_FAILED) {
        close(fd_);
        fd_ = -1;
        mapped_data_ = nullptr;
        return false;
    }

    header_ = static_cast<const DawgHeader*>(mapped_data_);
    if (std::memcmp(header_->magic, "LEXICONDG", 8) != 0) {
        unload();
        return false;
    }

    nodes_ = reinterpret_cast<const DawgNode*>(
        static_cast<const char*>(mapped_data_) + sizeof(DawgHeader));
    loaded_ = true;

    madvise(const_cast<void*>(mapped_data_), mapped_size_, MADV_RANDOM);
    return true;
}

void Dawg::unload() {
    if (mapped_data_) {
        munmap(const_cast<void*>(mapped_data_), mapped_size_);
        mapped_data_ = nullptr;
    }
    if (fd_ >= 0) {
        close(fd_);
        fd_ = -1;
    }
    header_ = nullptr;
    nodes_ = nullptr;
    mapped_size_ = 0;
    loaded_ = false;

    mem_nodes_.clear();
    mem_nodes_.shrink_to_fit(); // ensure memory is freed (Valgrind-clean)
    mem_root_offset_ = 0;
}

bool Dawg::traverse(std::string_view word, uint32_t* out_node) const {
    if (!loaded_) return false;
    const DawgNode* arr = mem_nodes_.empty() ? nodes_ : mem_nodes_.data();
    uint32_t root = mem_nodes_.empty() ? header_->root_offset : mem_root_offset_;

    // `current` always points at the start of the sibling chain we are
    // currently scanning. We walk siblings until we find a slot whose
    // letter matches the next character of `word`.
    uint32_t current = root;
    for (size_t i = 0; i < word.size(); ++i) {
        char c = word[i];
        bool found = false;
        uint32_t matched = current;
        while (true) {
            DawgNode node = arr[current];
            if (get_letter(node) == c) {
                matched = current;
                found = true;
                break;
            }
            if (is_last_child(node)) break;
            ++current;
        }
        if (!found) return false;

        if (i + 1 == word.size()) {
            // Last character of the word: the word is present iff the
            // *matched* node carries the end-of-word flag. (The previous
            // implementation incorrectly descended into the child and
            // tested is_eow on the child, which returned false for every
            // word ending in a leaf or in a node that had children.)
            if (out_node) *out_node = matched;
            return is_eow(arr[matched]);
        }

        // Descend into the matched node's children for the next char.
        current = get_child(arr[matched]);
        if (current == 0) {
            // No children but more characters remain -> word not present.
            return false;
        }
    }
    // Empty word: the root is not marked end-of-word, so it is absent.
    return false;
}

bool Dawg::contains(std::string_view word) const {
    return traverse(word, nullptr);
}

void Dawg::collect_words(uint32_t node, std::string& current, std::vector<std::string>& results, size_t limit) const {
    if (results.size() >= limit) return;
    const DawgNode* arr = mem_nodes_.empty() ? nodes_ : mem_nodes_.data();

    while (true) {
        DawgNode n = arr[node];
        current.push_back(get_letter(n));

        if (is_eow(n)) {
            results.push_back(current);
            if (results.size() >= limit) {
                current.pop_back();
                return;
            }
        }

        // Recurse into children
        if (get_child(n) != 0) {
            collect_words(get_child(n), current, results, limit);
        }

        // A recursive call may have filled `results` up to `limit`; stop
        // walking further siblings so we never exceed the cap.
        if (results.size() >= limit) {
            current.pop_back();
            return;
        }

        current.pop_back();

        if (is_last_child(n)) break;
        ++node;
    }
}

std::vector<std::string> Dawg::prefix_search(std::string_view prefix, size_t limit) const {
    std::vector<std::string> results;
    if (!loaded_) return results;
    const DawgNode* arr = mem_nodes_.empty() ? nodes_ : mem_nodes_.data();
    uint32_t root = mem_nodes_.empty() ? header_->root_offset : mem_root_offset_;

    // Traverse to the prefix node
    uint32_t current = root;
    for (char c : prefix) {
        bool found = false;
        uint32_t matched = current;
        while (true) {
            DawgNode node = arr[current];
            if (get_letter(node) == c) {
                matched = current;
                found = true;
                break;
            }
            if (is_last_child(node)) break;
            ++current;
        }
        if (!found) return results; // prefix not found
        current = get_child(arr[matched]);
        if (current == 0) return results; // prefix has no continuations
    }

    // Collect all words from this node
    std::string prefix_str(prefix);
    collect_words(current, prefix_str, results, limit);
    return results;
}

size_t Dawg::memory_usage_bytes() const {
    if (!mem_nodes_.empty()) {
        return mem_nodes_.size() * sizeof(DawgNode);
    }
    return mapped_size_;
}

// ---------------------------------------------------------------------------
//  build() — DAWG construction with subtree merging.
//
//  Pipeline:
//    1. Build a plain trie on std::vector<BuildNode> (children kept sorted
//       by letter via std::lower_bound, which fixes the broken sibling-chain
//       bookkeeping of the previous implementation).
//    2. Post-order traversal that merges identical subtrees. A subtree's
//       signature is (letter, eow, vector<canonical child id>). The hash map
//       maps a signature to its canonical representative; equivalent nodes
//       collapse onto a single canonical node.
//    3. Linearise the resulting DAG into the contiguous-sibling array
//       format. Because siblings occupy consecutive slots (terminated by
//       the is_last_child flag), the unit of memory sharing is an entire
//       children chain, not an individual node. We therefore memoise
//       chains by their sequence of canonical child ids: two parents with
//       identical children sequences share one contiguous block of slots.
//       This is the DAWG optimisation — it folds shared suffix subtrees
//       (e.g. the "...ands" tail of "bands" and "hands") into a single
//       block of memory instead of duplicating them per parent.
// ---------------------------------------------------------------------------
void Dawg::build(const std::vector<std::string>& sorted_words) {
    mem_nodes_.clear();
    loaded_ = true;
    mem_root_offset_ = 0;

    // === Step 1: Build a trie ============================================
    struct BuildNode {
        char letter = 0;
        bool eow = false;
        std::vector<BuildNode*> children; // kept sorted by letter
        uint32_t canonical_id = 0;
    };

    // Owning arena — every BuildNode lives here and is freed when build()
    // returns, so dedup below never has to worry about ownership.
    std::vector<std::unique_ptr<BuildNode>> arena;
    auto new_node = [&](char letter) -> BuildNode* {
        auto n = std::make_unique<BuildNode>();
        n->letter = letter;
        BuildNode* raw = n.get();
        arena.push_back(std::move(n));
        return raw;
    };

    BuildNode* root = new_node('\0');

    for (const auto& word : sorted_words) {
        if (word.empty()) continue; // empty strings are not representable
        BuildNode* current = root;
        for (size_t i = 0; i < word.size(); ++i) {
            unsigned char c = static_cast<unsigned char>(word[i]);
            // Binary-search the sorted children vector for letter c.
            auto it = std::lower_bound(
                current->children.begin(), current->children.end(), c,
                [](BuildNode* n, unsigned char ch) {
                    return static_cast<unsigned char>(n->letter) < ch;
                });
            if (it != current->children.end() &&
                static_cast<unsigned char>((*it)->letter) == c) {
                current = *it;
            } else {
                BuildNode* child = new_node(static_cast<char>(c));
                current->children.insert(it, child);
                current = child;
            }
            if (i + 1 == word.size()) {
                current->eow = true;
            }
        }
    }

    // === Step 2: Post-order DAWG deduplication ==========================
    // Signature: (letter, eow, canonical-id sequence of children).
    // Two nodes with identical signatures describe identical subtrees and
    // collapse to a single canonical node.
    struct NodeSig {
        char letter;
        bool eow;
        std::vector<uint32_t> child_ids;
        bool operator==(const NodeSig& o) const {
            return letter == o.letter && eow == o.eow &&
                   child_ids == o.child_ids;
        }
    };
    struct NodeSigHash {
        size_t operator()(const NodeSig& s) const {
            size_t h = std::hash<char>()(s.letter);
            h ^= std::hash<bool>()(s.eow) + 0x9e3779b9u + (h << 6) + (h >> 2);
            for (uint32_t id : s.child_ids) {
                h ^= id + 0x9e3779b9u + (h << 6) + (h >> 2);
            }
            return h;
        }
    };
    std::unordered_map<NodeSig, BuildNode*, NodeSigHash> canonical_map;
    uint32_t next_canonical_id = 0;

    std::function<BuildNode*(BuildNode*)> dedup =
        [&](BuildNode* node) -> BuildNode* {
        // Post-order: canonicalise children first so this node's signature
        // is expressed in terms of canonical child ids.
        for (auto& child : node->children) {
            child = dedup(child);
        }
        NodeSig sig;
        sig.letter = node->letter;
        sig.eow = node->eow;
        sig.child_ids.reserve(node->children.size());
        for (auto* child : node->children) {
            sig.child_ids.push_back(child->canonical_id);
        }
        auto it = canonical_map.find(sig);
        if (it != canonical_map.end()) {
            // Equivalent subtree already exists — discard this node and
            // redirect the caller's pointer at the canonical one.
            return it->second;
        }
        node->canonical_id = next_canonical_id++;
        canonical_map[sig] = node;
        return node;
    };

    root = dedup(root);

    // === Step 3: Linearise into the contiguous-sibling array ============
    // Slot 0 is a dummy root: its letter is 0 (never matches a real byte),
    // its is_last_child flag is set only when the trie is empty, and the
    // slots starting at offset 1 are root's children chain. traverse() and
    // prefix_search() walk siblings beginning at mem_root_offset_ = 0, so
    // they transparently skip the dummy (letter 0 cannot match) and land
    // on the real first-letter chain at offset 1.
    mem_nodes_.push_back({0}); // dummy root at offset 0

    // A chain is uniquely identified by the canonical-id sequence of the
    // children it contains. Two parents whose children sequences are the
    // same share one contiguous run of slots — this is what delivers the
    // memory saving (shared suffix subtrees live once, not once per parent).
    struct ChainKey {
        std::vector<uint32_t> child_ids;
        bool operator==(const ChainKey& o) const {
            return child_ids == o.child_ids;
        }
    };
    struct ChainHash {
        size_t operator()(const ChainKey& k) const {
            size_t h = 0;
            for (uint32_t id : k.child_ids) {
                h ^= id + 0x9e3779b9u + (h << 6) + (h >> 2);
            }
            return h;
        }
    };
    std::unordered_map<ChainKey, uint32_t, ChainHash> chain_offset;

    // Lay out a single children chain and return the offset of its first
    // slot. Recursively lays out the children's own chains so that every
    // reachable chain is emitted exactly once. The DAG is acyclic, so the
    // recursion always terminates; chain_offset memoises so each unique
    // chain is allocated once.
    std::function<uint32_t(const std::vector<BuildNode*>&)> lay_out_chain =
        [&](const std::vector<BuildNode*>& children) -> uint32_t {
        if (children.empty()) {
            // No children -> child pointer 0 (the format reserves 0 as the
            // "null child" sentinel; the dummy root occupies offset 0, so
            // no real children chain ever lives at offset 0).
            return 0;
        }
        ChainKey key;
        key.child_ids.reserve(children.size());
        for (auto* child : children) {
            key.child_ids.push_back(child->canonical_id);
        }
        auto it = chain_offset.find(key);
        if (it != chain_offset.end()) {
            return it->second; // already laid out — share it
        }

        uint32_t offset = static_cast<uint32_t>(mem_nodes_.size());
        chain_offset[key] = offset;
        // Reserve the slots for this chain up-front so the recursive
        // calls below (which append their own chains) leave our reserved
        // range untouched. We fill the slots in afterwards.
        mem_nodes_.resize(mem_nodes_.size() + children.size());

        for (size_t i = 0; i < children.size(); ++i) {
            BuildNode* child = children[i];
            uint32_t child_chain_offset = lay_out_chain(child->children);
            DawgNode slot;
            slot.data = static_cast<uint8_t>(child->letter);
            if (child->eow) slot.data |= (1u << 8);
            if (i + 1 == children.size()) slot.data |= (1u << 9); // last child
            slot.data |= (child_chain_offset << 10);
            mem_nodes_[offset + i] = slot;
        }
        return offset;
    };

    uint32_t root_children_offset = lay_out_chain(root->children);

    // Backfill the dummy root: child pointer = first child's chain, and
    // is_last_child set iff the trie is empty (so traverse() on an empty
    // DAWG terminates immediately instead of running off the array).
    DawgNode root_slot;
    root_slot.data = 0;
    if (root->children.empty()) {
        root_slot.data |= (1u << 9); // is_last_child -> traverse() stops here
    }
    root_slot.data |= (root_children_offset << 10);
    mem_nodes_[0] = root_slot;
}

bool Dawg::save_to_file(const char* path) const {
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return false;

    DawgHeader hdr;
    std::memcpy(hdr.magic, "LEXICONDG", 8);
    hdr.version = 1;
    hdr.node_count = static_cast<uint32_t>(mem_nodes_.size());
    hdr.root_offset = mem_root_offset_;
    hdr.string_pool_offset = sizeof(DawgHeader) + mem_nodes_.size() * sizeof(DawgNode);
    hdr.string_pool_size = 0;

    write(fd, &hdr, sizeof(hdr));
    write(fd, mem_nodes_.data(), mem_nodes_.size() * sizeof(DawgNode));
    close(fd);
    return true;
}

} // namespace lexicon
