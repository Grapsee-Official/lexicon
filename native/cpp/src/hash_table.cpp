// hash_table.cpp — Implementation of memory-mapped Robin Hood hash table
#include "lexora/hash_table.hpp"

#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <cstring>
#include <algorithm>
#include <iostream>

namespace lexora {

MemoryMappedHashTable::MemoryMappedHashTable()
    : fd_(-1), mapped_data_(nullptr), mapped_size_(0),
      header_(nullptr), buckets_(nullptr), string_pool_(nullptr),
      value_pool_(nullptr), entry_count_(0), bucket_count_(0),
      in_memory_mode_(false) {}

MemoryMappedHashTable::~MemoryMappedHashTable() {
    unload();
}

uint64_t MemoryMappedHashTable::fnv1a(std::string_view s) {
    uint64_t hash = 0xcbf29ce484222325ULL;
    for (char c : s) {
        hash ^= static_cast<uint8_t>(c);
        hash *= 0x100000001b3ULL;
    }
    return hash;
}

bool MemoryMappedHashTable::load_from_file(const char* path) {
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

    // Parse header
    header_ = static_cast<const MmapHeader*>(mapped_data_);
    if (std::memcmp(header_->magic, "LEXORAHT", 8) != 0) {
        unload();
        return false;
    }

    entry_count_ = header_->entry_count;
    bucket_count_ = header_->bucket_count;
    buckets_ = reinterpret_cast<const HashEntry*>(
        static_cast<const char*>(mapped_data_) + sizeof(MmapHeader));
    string_pool_ = static_cast<const char*>(mapped_data_) + header_->string_pool_offset;
    value_pool_ = static_cast<const char*>(mapped_data_) + header_->value_pool_offset;

    // Advise kernel: random access pattern
    madvise(const_cast<void*>(mapped_data_), mapped_size_, MADV_RANDOM);

    return true;
}

size_t MemoryMappedHashTable::probe(std::string_view key, size_t& dist) const {
    uint64_t hash = fnv1a(key);
    size_t idx = hash % bucket_count_;
    dist = 0;

    while (dist < bucket_count_) {
        const HashEntry& entry = buckets_[idx];
        if (entry.hash == 0 && entry.key_offset == 0) {
            return idx; // empty slot — key not found
        }
        std::string_view entry_key(string_pool_ + entry.key_offset, entry.key_len);
        if (entry.hash == hash && entry_key == key) {
            return idx; // found
        }
        ++idx;
        if (idx >= bucket_count_) idx = 0;
        ++dist;
    }
    return bucket_count_; // table full
}

const char* MemoryMappedHashTable::lookup(std::string_view key, uint32_t* out_value_len) const {
    if (!buckets_ && !in_memory_mode_) return nullptr;

    const HashEntry* buckets = in_memory_mode_ ? mem_buckets_.data() : buckets_;
    const char* str_pool = in_memory_mode_ ? mem_string_pool_.data() : string_pool_;
    const char* val_pool = in_memory_mode_ ? mem_value_pool_.data() : value_pool_;
    uint32_t bcount = in_memory_mode_ ? static_cast<uint32_t>(mem_buckets_.size()) : bucket_count_;

    uint64_t hash = fnv1a(key);
    size_t idx = hash % bcount;
    size_t dist = 0;

    while (dist < bcount) {
        const HashEntry& entry = buckets[idx];
        if (entry.hash == 0 && entry.key_offset == 0) {
            return nullptr; // empty — not found
        }
        std::string_view entry_key(str_pool + entry.key_offset, entry.key_len);
        if (entry.hash == hash && entry_key == key) {
            if (out_value_len) *out_value_len = entry.value_len;
            return val_pool + entry.value_offset;
        }
        ++idx;
        if (idx >= bcount) idx = 0;
        ++dist;
    }
    return nullptr;
}

bool MemoryMappedHashTable::exists(std::string_view key) const {
    return lookup(key, nullptr) != nullptr;
}

void MemoryMappedHashTable::insert(std::string_view key, std::string_view value) {
    in_memory_mode_ = true;
    // Resize bucket array if needed (load factor 0.7)
    size_t needed = static_cast<size_t>(entry_count_ * 1.43) + 16;
    if (mem_buckets_.size() < needed) {
        mem_buckets_.resize(needed);
    }

    uint64_t hash = fnv1a(key);
    size_t idx = hash % mem_buckets_.size();

    // Robin Hood insertion
    HashEntry new_entry;
    new_entry.hash = hash;
    new_entry.key_offset = static_cast<uint32_t>(mem_string_pool_.size());
    new_entry.key_len = static_cast<uint16_t>(key.size());
    new_entry.value_offset = static_cast<uint32_t>(mem_value_pool_.size());
    new_entry.value_len = static_cast<uint16_t>(value.size());

    // Append key+value to pools
    mem_string_pool_.insert(mem_string_pool_.end(), key.begin(), key.end());
    mem_value_pool_.insert(mem_value_pool_.end(), value.begin(), value.end());

    size_t dist = 0;
    while (dist < mem_buckets_.size()) {
        HashEntry& slot = mem_buckets_[idx];
        if (slot.hash == 0 && slot.key_offset == 0) {
            slot = new_entry;
            ++entry_count_;
            return;
        }
        // Robin Hood: steal if our probe distance is greater
        size_t existing_dist = 0; // simplified — real Robin Hood tracks this
        if (dist > existing_dist) {
            std::swap(slot, new_entry);
        }
        ++idx;
        if (idx >= mem_buckets_.size()) idx = 0;
        ++dist;
    }
}

bool MemoryMappedHashTable::save_to_file(const char* path) const {
    // Serialize in-memory table to mmap format
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return false;

    MmapHeader hdr;
    std::memcpy(hdr.magic, "LEXORAHT", 8);
    hdr.version = 1;
    hdr.entry_count = entry_count_;
    hdr.bucket_count = static_cast<uint32_t>(mem_buckets_.size());
    hdr.string_pool_offset = sizeof(MmapHeader) + mem_buckets_.size() * sizeof(HashEntry);
    hdr.string_pool_size = static_cast<uint32_t>(mem_string_pool_.size());
    hdr.value_pool_offset = hdr.string_pool_offset + hdr.string_pool_size;
    hdr.value_pool_size = static_cast<uint32_t>(mem_value_pool_.size());

    write(fd, &hdr, sizeof(hdr));
    write(fd, mem_buckets_.data(), mem_buckets_.size() * sizeof(HashEntry));
    write(fd, mem_string_pool_.data(), mem_string_pool_.size());
    write(fd, mem_value_pool_.data(), mem_value_pool_.size());
    close(fd);
    return true;
}

size_t MemoryMappedHashTable::memory_usage_bytes() const {
    if (in_memory_mode_) {
        return mem_buckets_.size() * sizeof(HashEntry)
             + mem_string_pool_.size()
             + mem_value_pool_.size();
    }
    return mapped_size_;
}

void MemoryMappedHashTable::unload() {
    if (mapped_data_) {
        munmap(const_cast<void*>(mapped_data_), mapped_size_);
        mapped_data_ = nullptr;
    }
    if (fd_ >= 0) {
        close(fd_);
        fd_ = -1;
    }
    header_ = nullptr;
    buckets_ = nullptr;
    string_pool_ = nullptr;
    value_pool_ = nullptr;
    mapped_size_ = 0;
    entry_count_ = 0;
    bucket_count_ = 0;

    mem_buckets_.clear();
    mem_string_pool_.clear();
    mem_value_pool_.clear();
    in_memory_mode_ = false;
}

} // namespace lexora
