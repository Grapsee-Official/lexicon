// SPDX-License-Identifier: MIT
// lexora/hash_table.inl -- template implementation for MemoryMappedHashTable.
//
// Included at the bottom of hash_table.hpp. Guarded against double-inclusion
// by the parent header's include guard.

#include <algorithm>
#include <cstring>
#include <fstream>
#include <new>
#include <stdexcept>
#include <utility>

namespace lexora {

// ----- static thread_local value slot (one per (V, T) pair) --------------
template <typename V, typename T>
thread_local V MemoryMappedHashTable<V, T>::value_slot_{};

// ----- static helpers ------------------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE std::uint64_t
MemoryMappedHashTable<V, T>::next_pow2(std::uint64_t x) noexcept {
    if (x <= 1) return 1;
    --x;
    x |= x >> 1;
    x |= x >> 2;
    x |= x >> 4;
    x |= x >> 8;
    x |= x >> 16;
    x |= x >> 32;
    return x + 1;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE std::uint64_t
MemoryMappedHashTable<V, T>::normalize_and_hash(std::string_view key) {
    // NFC-normalize first. The unicode module returns a std::string; for the
    // common case where the input is already normalized, this is a single
    // allocation. (Future optimization: have normalize_nfc return a
    // string_view when the input is already in NFC.)
    std::string normalized = lexora::unicode::normalize_nfc(key);
    std::uint64_t h = lexora::fnv1a_unicode_hash(
        normalized.data(), normalized.size());
    return h;  // fnv1a_unicode_hash guarantees non-zero
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE bool
MemoryMappedHashTable<V, T>::bucket_matches(const Bucket& b,
                                            std::uint64_t key_hash,
                                            std::string_view key_buf,
                                            const char* pool_base) const noexcept {
    if (b.hash != key_hash) return false;
    if (b.key_len != key_buf.size()) return false;
    // Hash + length match: still must do the byte compare to rule out a
    // collision (two different keys with the same hash). The hash check
    // above means this memcmp almost never fires on the cold path.
    return std::memcmp(pool_base + b.key_offset, key_buf.data(),
                       key_buf.size()) == 0;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE std::uint64_t
MemoryMappedHashTable<V, T>::append_to_pool(std::vector<char>& pool,
                                            std::string_view bytes) {
    std::uint64_t offset = static_cast<std::uint64_t>(pool.size());
    // Bucket offsets are uint32_t (max 4 GB string pool). This is enforced
    // at save() too; the assert here catches runaway growth during build.
    if (offset > std::uint64_t{0xFFFFFFFFu}) {
        throw std::runtime_error(
            "lexora::MemoryMappedHashTable: string pool exceeds 4 GB "
            "(uint32_t offset limit)");
    }
    pool.insert(pool.end(), bytes.data(), bytes.data() + bytes.size());
    return offset;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE typename MemoryMappedHashTable<V, T>::Bucket*
MemoryMappedHashTable<V, T>::buckets_ptr() noexcept {
    if (mmap_mode_) {
        // buckets_offset is fixed at 64 (sizeof(Header)) -- we read it from
        // the header at load time but for an immutable layout we know it.
        return reinterpret_cast<Bucket*>(
            static_cast<char*>(mmap_.data()) + buckets_offset_cached_);
    }
    return buckets_.empty() ? nullptr : buckets_.data();
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE const typename MemoryMappedHashTable<V, T>::Bucket*
MemoryMappedHashTable<V, T>::buckets_ptr() const noexcept {
    if (mmap_mode_) {
        return reinterpret_cast<const Bucket*>(
            static_cast<const char*>(mmap_.data()) + buckets_offset_cached_);
    }
    return buckets_.empty() ? nullptr : buckets_.data();
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE const char*
MemoryMappedHashTable<V, T>::pool_base() const noexcept {
    if (mmap_mode_) {
        return static_cast<const char*>(mmap_.data()) + pool_offset_cached_;
    }
    return pool_.empty() ? nullptr : pool_.data();
}

// ----- BUILD MODE ----------------------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE void
MemoryMappedHashTable<V, T>::reserve(std::size_t n) {
    std::unique_lock<std::shared_mutex> lock(rwlock_);
    if (mmap_mode_) return;
    std::uint64_t needed = next_pow2(
        static_cast<std::uint64_t>(
            static_cast<double>(n) / kLoadFactorThreshold) + 1);
    if (needed > buckets_.size()) {
        rehash(needed);
    }
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE bool
MemoryMappedHashTable<V, T>::insert(std::string_view key, const V& value) {
    std::unique_lock<std::shared_mutex> lock(rwlock_);
    if (mmap_mode_) return false;  // read-only
    if (key.empty()) return false;

    // Grow if needed.
    if (buckets_.empty()) {
        rehash(16);
    } else {
        double lf = static_cast<double>(entry_count_) /
                    static_cast<double>(buckets_.size());
        if (lf >= kLoadFactorThreshold) {
            rehash(buckets_.size() * 2);
        }
    }

    std::string normalized = lexora::unicode::normalize_nfc(key);
    std::uint64_t h = lexora::fnv1a_unicode_hash(normalized.data(),
                                                 normalized.size());
    std::string value_blob = T::serialize(value);
    insert_internal(h, normalized, value_blob);
    return true;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE void
MemoryMappedHashTable<V, T>::insert_internal(std::uint64_t hash,
                                             std::string_view key,
                                             std::string_view value_blob) {
    const std::uint64_t mask = mask_for(buckets_.size());
    std::uint64_t idx = hash & mask;
    std::uint64_t dist = 0;  // probe distance from ideal slot

    // Build the "traveling" bucket that we will insert (possibly displacing
    // richer entries Robin Hood style).
    Bucket traveling;
    traveling.hash = hash;
    // Offsets are uint32_t in the on-disk Bucket; append_to_pool checks the
    // 4 GB ceiling and throws on overflow.
    traveling.key_offset =
        static_cast<std::uint32_t>(append_to_pool(pool_, key));
    traveling.key_len = static_cast<std::uint32_t>(key.size());
    traveling.value_offset =
        static_cast<std::uint32_t>(append_to_pool(pool_, value_blob));
    traveling.value_len = static_cast<std::uint32_t>(value_blob.size());

    const std::size_t cap = buckets_.size();
    for (std::size_t probes = 0; probes < cap; ++probes) {
        Bucket& cur = buckets_[idx];
        if (cur.empty()) {
            cur = traveling;
            ++entry_count_;
            return;
        }
        // Dedup: if the existing entry's key matches ours, overwrite the
        // value in place. (Robin Hood places all keys that share an ideal
        // slot in a contiguous run, so the matching key -- if it exists --
        // is guaranteed to be encountered before the first empty slot or a
        // slot whose probe-distance check below fires.)
        if (cur.hash == hash &&
            cur.key_len == traveling.key_len &&
            std::memcmp(pool_.data() + cur.key_offset,
                        key.data(), key.size()) == 0) {
            cur.value_offset = traveling.value_offset;
            cur.value_len = traveling.value_len;
            // Note: the previously-staged key bytes appended above become
            // dead pool space. Acceptable for a build-mode table that gets
            // serialized once; a compaction pass could reclaim the space.
            return;
        }
        // Existing entry's probe distance from its ideal slot:
        std::uint64_t cur_dist = (idx - (cur.hash & mask)) & mask;
        if (cur_dist < dist) {
            // Robin Hood swap: the existing entry is "richer" (closer to its
            // ideal slot) than our traveling entry. Displace it and continue
            // inserting it further down the probe sequence.
            Bucket tmp = cur;
            cur = traveling;
            traveling = tmp;
            dist = cur_dist;  // we now carry the displaced entry's distance
        }
        idx = (idx + 1) & mask;
        ++dist;
    }
    // Should be unreachable: the load-factor threshold guarantees a free slot
    // before we probe every bucket.
    throw std::runtime_error("lexora::MemoryMappedHashTable: table full");
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE void
MemoryMappedHashTable<V, T>::rehash(std::uint64_t new_count) {
    // new_count must be a power of 2.
    std::vector<Bucket> old_buckets;
    old_buckets.swap(buckets_);
    buckets_.assign(static_cast<std::size_t>(new_count), Bucket{0, 0, 0, 0, 0});

    // We keep the string pool as-is: existing offsets remain valid. New
    // inserts will append. (A production build could compact the pool here,
    // but for dictionary-build workloads the pool only grows.)

    // Re-insert every live bucket. The pool offsets don't change, only the
    // bucket positions do.
    for (const Bucket& b : old_buckets) {
        if (b.empty()) continue;
        // Re-derive the key bytes to compute probe distance during insertion.
        // We can't reuse insert_internal because that appends to the pool
        // again. Instead, do a Robin Hood insert that references the existing
        // pool offsets.
        const std::uint64_t mask = mask_for(buckets_.size());
        std::uint64_t idx = b.hash & mask;
        std::uint64_t dist = 0;
        Bucket traveling = b;
        const std::size_t cap = buckets_.size();
        for (std::size_t probes = 0; probes < cap; ++probes) {
            Bucket& cur = buckets_[idx];
            if (cur.empty()) {
                cur = traveling;
                break;
            }
            std::uint64_t cur_dist = (idx - (cur.hash & mask)) & mask;
            if (cur_dist < dist) {
                Bucket tmp = cur;
                cur = traveling;
                traveling = tmp;
                dist = cur_dist;
            }
            idx = (idx + 1) & mask;
            ++dist;
        }
    }
}

// ----- LOOKUP --------------------------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE const V*
MemoryMappedHashTable<V, T>::lookup(std::string_view key) {
    std::shared_lock<std::shared_mutex> lock(rwlock_);

    if (buckets_.empty() && !mmap_mode_) return nullptr;

    std::string normalized = lexora::unicode::normalize_nfc(key);
    std::uint64_t h = lexora::fnv1a_unicode_hash(normalized.data(),
                                                 normalized.size());
    const Bucket* bs = buckets_ptr();
    const char* pool = pool_base();
    if (bs == nullptr || pool == nullptr) return nullptr;

    const std::uint64_t bucket_count = mmap_mode_
        ? bucket_count_cached_
        : static_cast<std::uint64_t>(buckets_.size());
    if (bucket_count == 0) return nullptr;
    const std::uint64_t mask = bucket_count - 1;

    std::uint64_t idx = h & mask;
    std::uint64_t dist = 0;
    for (std::uint64_t probes = 0; probes < bucket_count; ++probes) {
        const Bucket& cur = bs[idx];
        if (cur.empty()) return nullptr;
        // Robin Hood early-out: if our probe distance exceeds the stored
        // entry's probe distance, the key cannot be present (Robin Hood
        // guarantees entries are ordered by distance from ideal slot).
        std::uint64_t cur_dist = (idx - (cur.hash & mask)) & mask;
        if (dist > cur_dist) return nullptr;

        if (bucket_matches(cur, h, normalized, pool)) {
            const char* vdata = pool + cur.value_offset;
            std::size_t vlen = cur.value_len;
            value_slot_ = T::deserialize(vdata, vlen);
            return &value_slot_;
        }
        idx = (idx + 1) & mask;
        ++dist;
    }
    return nullptr;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE bool
MemoryMappedHashTable<V, T>::exists(std::string_view key) {
    std::shared_lock<std::shared_mutex> lock(rwlock_);
    if (buckets_.empty() && !mmap_mode_) return false;

    std::string normalized = lexora::unicode::normalize_nfc(key);
    std::uint64_t h = lexora::fnv1a_unicode_hash(normalized.data(),
                                                 normalized.size());
    const Bucket* bs = buckets_ptr();
    const char* pool = pool_base();
    if (bs == nullptr || pool == nullptr) return false;

    const std::uint64_t bucket_count = mmap_mode_
        ? bucket_count_cached_
        : static_cast<std::uint64_t>(buckets_.size());
    if (bucket_count == 0) return false;
    const std::uint64_t mask = bucket_count - 1;

    std::uint64_t idx = h & mask;
    std::uint64_t dist = 0;
    for (std::uint64_t probes = 0; probes < bucket_count; ++probes) {
        const Bucket& cur = bs[idx];
        if (cur.empty()) return false;
        std::uint64_t cur_dist = (idx - (cur.hash & mask)) & mask;
        if (dist > cur_dist) return false;
        if (bucket_matches(cur, h, normalized, pool)) return true;
        idx = (idx + 1) & mask;
        ++dist;
    }
    return false;
}

// ----- MMAP MODE -----------------------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE bool
MemoryMappedHashTable<V, T>::load_from_mmap(const char* path) {
    std::unique_lock<std::shared_mutex> lock(rwlock_);
    if (!buckets_.empty() || mmap_mode_) {
        return false;  // already populated
    }

    if (!mmap_.open(path, MmapRegion::Mode::ReadOnly)) {
        return false;
    }
    if (mmap_.size() < sizeof(Header)) {
        mmap_.close();
        return false;
    }

    const Header* hdr = static_cast<const Header*>(mmap_.data());
    if (std::memcmp(hdr->magic, "LEXOHT01", 8) != 0) {
        mmap_.close();
        return false;
    }
    if (hdr->version != kVersion) {
        mmap_.close();
        return false;
    }
    if (!(hdr->flags & 0x1)) {
        // Big-endian file on a little-endian machine (or vice versa) -- not
        // supported in this revision. Reject.
        mmap_.close();
        return false;
    }

    // Sanity-check offsets against the file size.
    const std::uint64_t file_size = mmap_.size();
    if (hdr->buckets_offset > file_size ||
        hdr->string_pool_offset > file_size) {
        mmap_.close();
        return false;
    }
    if (hdr->bucket_count > (file_size - hdr->buckets_offset) / sizeof(Bucket)) {
        mmap_.close();
        return false;
    }

    entry_count_ = static_cast<std::size_t>(hdr->entry_count);
    bucket_count_cached_ = hdr->bucket_count;
    buckets_offset_cached_ = hdr->buckets_offset;
    pool_offset_cached_ = hdr->string_pool_offset;
    pool_size_cached_ = hdr->string_pool_size;
    mmap_mode_ = true;
    return true;
}

// ----- SAVE ----------------------------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE bool
MemoryMappedHashTable<V, T>::save(const char* path) const {
    std::shared_lock<std::shared_mutex> lock(rwlock_);
    if (mmap_mode_) return false;

    std::ofstream out(path, std::ios::binary | std::ios::trunc);
    if (!out) return false;

    Header hdr{};
    std::memcpy(hdr.magic, "LEXOHT01", 8);
    hdr.version = kVersion;
    hdr.flags = 0x1;  // little-endian
    hdr.entry_count = entry_count_;
    hdr.bucket_count = buckets_.size();
    hdr.buckets_offset = sizeof(Header);
    hdr.string_pool_offset = sizeof(Header) + buckets_.size() * sizeof(Bucket);
    hdr.string_pool_size = pool_.size();
    hdr.reserved = 0;
    out.write(reinterpret_cast<const char*>(&hdr), sizeof(hdr));
    if (!out) return false;

    if (!buckets_.empty()) {
        out.write(reinterpret_cast<const char*>(buckets_.data()),
                  static_cast<std::streamsize>(buckets_.size() * sizeof(Bucket)));
        if (!out) return false;
    }
    if (!pool_.empty()) {
        out.write(pool_.data(),
                  static_cast<std::streamsize>(pool_.size()));
        if (!out) return false;
    }
    out.flush();
    return static_cast<bool>(out);
}

// ----- INTROSPECTION / CLEAR ----------------------------------------------

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE std::size_t
MemoryMappedHashTable<V, T>::size() const noexcept {
    return entry_count_;
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE std::size_t
MemoryMappedHashTable<V, T>::bucket_count() const noexcept {
    if (mmap_mode_) return static_cast<std::size_t>(bucket_count_cached_);
    return buckets_.size();
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE double
MemoryMappedHashTable<V, T>::load_factor() const noexcept {
    std::size_t bc = bucket_count();
    if (bc == 0) return 0.0;
    return static_cast<double>(entry_count_) / static_cast<double>(bc);
}

template <typename V, typename T>
LEXORA_HASH_TABLE_INLINE void
MemoryMappedHashTable<V, T>::clear() noexcept {
    std::unique_lock<std::shared_mutex> lock(rwlock_);
    buckets_.clear();
    buckets_.shrink_to_fit();
    pool_.clear();
    pool_.shrink_to_fit();
    entry_count_ = 0;
    mmap_mode_ = false;
    mmap_.close();
    bucket_count_cached_ = 0;
    buckets_offset_cached_ = 0;
    pool_offset_cached_ = 0;
    pool_size_cached_ = 0;
}

}  // namespace lexora
