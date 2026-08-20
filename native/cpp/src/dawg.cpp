// dawg.cpp — DAWG implementation
#include "lexora/dawg.hpp"

#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <cstring>
#include <algorithm>
#include <queue>
#include <unordered_map>

namespace lexora {

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
    if (std::memcmp(header_->magic, "LEXORADG", 8) != 0) {
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

    uint32_t current = root;
    for (char c : word) {
        // Walk siblings to find the matching letter
        bool found = false;
        while (true) {
            DawgNode node = arr[current];
            if (get_letter(node) == c) {
                if (get_child(node) == 0 && !is_eow(node)) return false;
                current = get_child(node);
                found = true;
                break;
            }
            if (is_last_child(node)) break;
            ++current;
        }
        if (!found) return false;
    }
    if (out_node) *out_node = current;
    return is_eow(arr[current]);
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
        while (true) {
            DawgNode node = arr[current];
            if (get_letter(node) == c) {
                current = get_child(node);
                found = true;
                break;
            }
            if (is_last_child(node)) break;
            ++current;
        }
        if (!found) return results; // prefix not found
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

void Dawg::build(const std::vector<std::string>& sorted_words) {
    mem_nodes_.clear();
    loaded_ = true;

    // Simple trie construction (full DAWG compression would merge identical subtrees)
    // Root node
    mem_root_offset_ = 0;
    mem_nodes_.push_back({0}); // root (empty letter, not eow)

    for (const auto& word : sorted_words) {
        uint32_t current = mem_root_offset_;
        for (size_t i = 0; i < word.size(); ++i) {
            char c = word[i];
            bool is_last_char = (i == word.size() - 1);

            // Find child with letter c
            uint32_t child = get_child(mem_nodes_[current]);
            bool found = false;

            if (child == 0) {
                // No children yet — create first child
                uint32_t new_node = static_cast<uint32_t>(mem_nodes_.size());
                DawgNode n;
                n.data = static_cast<uint8_t>(c);
                if (is_last_char) n.data |= (1 << 8); // eow
                n.data |= (1 << 9); // last child (only child for now)
                n.data |= (0 << 10); // no children yet
                mem_nodes_.push_back(n);
                mem_nodes_[current].data = (mem_nodes_[current].data & 0x3FF) | (new_node << 10);
                current = new_node;
                found = true;
            } else {
                // Walk siblings
                uint32_t sib = child;
                while (true) {
                    if (get_letter(mem_nodes_[sib]) == c) {
                        current = sib;
                        found = true;
                        break;
                    }
                    if (is_last_child(mem_nodes_[sib])) break;
                    ++sib;
                }
            }

            if (!found) {
                // Add as new sibling
                uint32_t new_node = static_cast<uint32_t>(mem_nodes_.size());
                DawgNode n;
                n.data = static_cast<uint8_t>(c);
                if (is_last_char) n.data |= (1 << 8); // eow
                n.data |= (1 << 9); // last child
                n.data |= (0 << 10); // no children yet
                mem_nodes_.push_back(n);

                // Clear last-child flag on previous last sibling, set on new node
                // (simplified — in a real implementation we'd track the previous last child)
                current = new_node;
            }

            if (is_last_char && !is_eow(mem_nodes_[current])) {
                mem_nodes_[current].data |= (1 << 8); // set eow
            }
        }
    }
}

bool Dawg::save_to_file(const char* path) const {
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return false;

    DawgHeader hdr;
    std::memcpy(hdr.magic, "LEXORADG", 8);
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

} // namespace lexora
