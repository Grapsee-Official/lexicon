// hash_table.cpp — Implementation of memory-mapped Robin Hood hash table
#include "lexicon/hash_table.hpp"

#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <cstring>
#include <algorithm>
#include <iostream>

namespace lexicon {

namespace {

// Robin Hood insertion of `entry` into `buckets`.
//
// For each occupied slot we compute the occupant's probe distance from its
// stored hash (home = hash % bucket_count; dist = (idx - home) mod count).
// If the entry being carried has probed further than the occupant, the
// occupant is "richer" and we steal its slot, then continue probing with
// the displaced entry. This restores the Robin Hood invariant: within each
// probe run, probe distances are non-decreasing, so linear probing until
// the first empty slot always finds a key if it is present.
//
// Pre: `buckets` is zero-initialised so that (hash==0 && key_offset==0)
// unambiguously marks an empty slot.
// Returns true if placed, false if the table is full (no empty slot found).
bool robin_hood_insert(std::vector<HashEntry>& buckets, HashEntry entry) {
    const size_t bcount = buckets.size();
    if (bcount == 0) return false;

    size_t idx = static_cast<size_t>(entry.hash % bcount);
    size_t dist = 0;
    while (dist < bcount) {
        HashEntry& slot = buckets[idx];
        if (slot.hash == 0 && slot.key_offset == 0) {
            slot = entry;
            return true;
        }
        // Probe distance of the existing occupant, derived from its hash.
        const size_t home = static_cast<size_t>(slot.hash % bcount);
        const size_t existing_dist = (idx + bcount - home) % bcount;
        // Robin Hood stealing: take the slot from the richer occupant and
        // carry the displaced entry forward. The displaced entry had probed
        // `existing_dist` slots to get here, so it resumes one step further.
        if (dist > existing_dist) {
            std::swap(slot, entry);
            dist = existing_dist;
        }
        ++idx;
        if (idx >= bcount) idx = 0;
        ++dist;
    }
    return false; // table full
}

} // anonymous namespace

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
    if (std::memcmp(header_->magic, "LEXICONHT", 8) != 0) {
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

    // Target a ~0.7 load factor (1 / 1.43). When the bucket array must grow,
    // rehash every existing entry, because each entry's home bucket is
    // hash % bucket_count and that changes when bucket_count changes. Growing
    // without rehashing would leave previously-inserted entries unreachable
    // and would also invalidate the probe distances computed in
    // robin_hood_insert(). Grow geometrically (at least doubling) to keep
    // amortised insertion O(1).
    const size_t target_buckets =
        static_cast<size_t>((entry_count_ + 1) * 1.43) + 16;
    if (mem_buckets_.size() < target_buckets) {
        size_t new_size = mem_buckets_.empty() ? target_buckets
                                               : mem_buckets_.size();
        while (new_size < target_buckets) new_size *= 2;

        std::vector<HashEntry> old_buckets;
        old_buckets.swap(mem_buckets_);              // pull old entries out
        mem_buckets_.assign(new_size, HashEntry{});  // zeroed new array

        for (const HashEntry& e : old_buckets) {
            if (e.hash == 0 && e.key_offset == 0) continue; // skip empty
            robin_hood_insert(mem_buckets_, e);
        }
    }

    uint64_t hash = fnv1a(key);

    // Append key + value to the pools first so the new entry's offsets are
    // fixed before it is placed in a bucket.
    HashEntry new_entry{};
    new_entry.hash = hash;
    new_entry.key_offset = static_cast<uint32_t>(mem_string_pool_.size());
    new_entry.key_len = static_cast<uint16_t>(key.size());
    new_entry.value_offset = static_cast<uint32_t>(mem_value_pool_.size());
    new_entry.value_len = static_cast<uint16_t>(value.size());
    mem_string_pool_.insert(mem_string_pool_.end(), key.begin(), key.end());
    mem_value_pool_.insert(mem_value_pool_.end(), value.begin(), value.end());

    // Robin Hood insertion (steals from richer occupants).
    if (robin_hood_insert(mem_buckets_, new_entry)) {
        ++entry_count_;
    }
}

bool MemoryMappedHashTable::save_to_file(const char* path) const {
    // Serialize in-memory table to mmap format
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return false;

    MmapHeader hdr;
    std::memcpy(hdr.magic, "LEXICONHT", 8);
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

} // namespace lexicon
