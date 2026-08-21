// test_performance.cpp — benchmark tests for the Lexora performance scaffolding.
//
// Exercises the four instruments (ScopedTimer, LookupBenchmark, MemoryTracker,
// FrameTimer) and uses them to measure the spec's hard-gate scenarios:
//
//   1. 1M hash table lookups (measure p50/p95/p99).
//        Spec gate: "1M lookups in <50ms on Moto G".
//   2. 100K Levenshtein searches (measure throughput).
//        Spec gate: "100K Levenshtein searches" — drives the fuzzy path.
//   3. 10K DAWG prefix searches.
//        Spec gate: "10K DAWG prefix searches".
//   4. Cold start simulation (load + first lookup).
//        Spec gate: "<500ms cold start on iPhone SE".
//   5. Memory usage after 1M entries loaded.
//        Spec gate: implicit (must fit in device RAM alongside the OS).
//
// Plus a few correctness tests for the instruments themselves:
//   - compute_latency_stats_us percentiles.
//   - FrameTimer drop counting.
//   - ScopedTimer emit/no-op-when-off.
//   - PerfConfig env-var parsing.
//
// All benchmarks are self-contained: they build their own in-memory fixtures
// (no dependency on a pre-built .ht / .dawg / .sqlite database).
//
// The JSON perf log is emitted to LEXORA_PERF_LOG (default /tmp/lexora_perf.log)
// when LEXORA_PERF_LEVEL >= 1. Tests run cleanly at level 0 (no logging).
#include "lexora/performance.hpp"
#include "lexora/hash_table.hpp"
#include "lexora/levenshtein.hpp"
#include "lexora/dawg.hpp"
#include "lexora/engine.hpp"

#include <gtest/gtest.h>

#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <random>
#include <string>
#include <thread>
#include <vector>

using namespace lexora;

namespace {
// ---------------------------------------------------------------------------
//  make_word — deterministic synthetic word of length 4-12 (a-z).
//  Deterministic so benchmark fixtures are reproducible across runs.
// ---------------------------------------------------------------------------
std::string make_word(uint64_t i, uint64_t salt = 0) {
    // Simple LCG to derive characters — avoids std::mt19937 cost in hot loops.
    uint64_t state = i * 0x9E3779B97F4A7C15ull + salt;
    std::string out;
    int len = 4 + static_cast<int>((state >> 56) & 0x7);   // 4..11 chars
    out.reserve(len);
    for (int j = 0; j < len; ++j) {
        state = state * 6364136223846793005ull + 1442695040888963407ull;
        char c = static_cast<char>('a' + (state >> 33) % 26);
        out.push_back(c);
    }
    return out;
}
} // anonymous namespace

// =============================================================================
//  Instrument correctness tests
// =============================================================================

TEST(PerfConfigTest, DefaultsToOff) {
    // Without env vars set (or with LEXORA_PERF_LEVEL=0), level should be Off
    // until initialize_from_env() is explicitly called with an env var present.
    // This test only checks the off-state behaviour — we don't manipulate the
    // global env to avoid interfering with other tests.
    auto& cfg = PerfConfig::instance();
    // initialize_from_env is idempotent — first call wins. If a prior test
    // already ran it with level=0, this is a no-op. Either way, level()
    // should be a valid enum value (0/1/2).
    PerfLevel lvl = cfg.level();
    EXPECT_TRUE(lvl == PerfLevel::Off ||
                lvl == PerfLevel::Basic ||
                lvl == PerfLevel::Verbose)
        << "Unexpected PerfLevel: " << static_cast<int>(lvl);
}

TEST(LatencyStatsTest, PercentilesAreCorrect) {
    // 100 samples 0..99 → p50=49, p95=94, p99=98 (nearest-rank).
    std::vector<double> samples;
    samples.reserve(100);
    for (int i = 0; i < 100; ++i) {
        samples.push_back(static_cast<double>(i));
    }
    LatencyStats s = compute_latency_stats_us(samples);
    EXPECT_EQ(s.count, 100u);
    EXPECT_EQ(s.min_us, 0.0);
    EXPECT_EQ(s.max_us, 99.0);
    // Nearest-rank: rank = ceil(p/100 * n) - 1, clamped.
    // p50: ceil(50) - 1 = 49 → samples[49] = 49.0
    EXPECT_EQ(s.p50_us, 49.0);
    // p95: ceil(95) - 1 = 94 → samples[94] = 94.0
    EXPECT_EQ(s.p95_us, 94.0);
    // p99: ceil(99) - 1 = 98 → samples[98] = 98.0
    EXPECT_EQ(s.p99_us, 98.0);
    // Mean = sum(0..99)/100 = 4950/100 = 49.5
    EXPECT_TRUE(std::abs(s.mean_us - 49.5) <= 0.001);
}

TEST(LatencyStatsTest, EmptySamplesReturnZero) {
    std::vector<double> samples;
    LatencyStats s = compute_latency_stats_us(samples);
    EXPECT_EQ(s.count, 0u);
    EXPECT_EQ(s.p50_us, 0.0);
    EXPECT_EQ(s.p95_us, 0.0);
    EXPECT_EQ(s.p99_us, 0.0);
}

TEST(ScopedTimerTest, ElapsedUsIncreasesWithSleep) {
    ScopedTimer t("test.sleep");
    double us1 = t.elapsed_us();
    std::this_thread::sleep_for(std::chrono::microseconds(500));
    double us2 = t.elapsed_us();
    EXPECT_GE(us2, us1);
    EXPECT_GT(us2 - us1, 400.0) << "Sleep 500us should produce >=400us delta";
    t.emit();   // disarm destructor
}

TEST(FrameTimerTest, CountsDroppedFramesAboveBudget) {
    FrameTimer ft("test_screen", /*frame_budget_ms=*/16.0);
    ft.record_frame(10.0);   // ok
    ft.record_frame(20.0);   // jank
    ft.record_frame(16.5);   // jank (just over)
    ft.record_frame(8.0);    // ok
    EXPECT_EQ(ft.total_frames(), 3u + 1u);   // 4
    EXPECT_EQ(ft.dropped_frames(), 2u);
    FrameStats st = ft.snapshot();
    EXPECT_EQ(st.total_frames, 4u);
    EXPECT_EQ(st.dropped_frames, 2u);
    EXPECT_TRUE(std::abs(st.jank_percent - 50.0) <= 0.01);
    EXPECT_EQ(st.min_ms, 8.0);
    EXPECT_EQ(st.max_ms, 20.0);
    double expected_mean = (10.0 + 20.0 + 16.5 + 8.0) / 4.0;
    EXPECT_TRUE(std::abs(st.mean_ms - expected_mean) <= 0.001);
    // p50 (nearest-rank with n=4): ceil(50/100 * 4) - 1 = ceil(2) - 1 = 1
    // → samples[1] = 10.0 (sorted: 8, 10, 16.5, 20)
    EXPECT_EQ(st.p50_ms, 10.0);
    ft.reset();
    EXPECT_EQ(ft.total_frames(), 0u);
}

TEST(MemoryTrackerTest, ProcessMemoryReturnsNonZeroRss) {
    MemorySnapshot snap = MemoryTracker::process_memory();
    // On any reasonable platform, RSS should be > 0 for a running process.
    // (The test process has loaded gtest + the lexora lib — at least a few MB.)
    EXPECT_GT(snap.rss_bytes, 0u)
        << "RSS should be non-zero for a running process";
}

// =============================================================================
//  Benchmark fixture — shared synthetic word list + populated hash table.
// =============================================================================
class PerformanceBenchmarkFixture : public ::testing::Test {
protected:
    static constexpr uint64_t HT_ENTRIES = 100000;   // 100K entries
    static constexpr uint64_t LOOKUP_ITERATIONS = 1000000;  // 1M lookups

    // A list of "known" words that we inserted into the hash table — used
    // to drive 1M lookups with a mix of hits and misses.
    std::vector<std::string> known_words;
    MemoryMappedHashTable   ht;
    MemoryTracker           mem_tracker;

    void SetUp() override {
        // Force perf logging on for the benchmark tests (level=2 → verbose,
        // so frame_jank events also fire — useful for the FrameTimer test
        // below). This is process-global, so it affects later tests too —
        // acceptable for a benchmark file.
        // We only set the env var if it isn't already set, to honor an
        // explicit CI override.
        if (std::getenv("LEXORA_PERF_LEVEL") == nullptr) {
            // Cannot use setenv safely in a portable test (some platforms
            // don't propagate). Instead, just call initialize_from_env and
            // rely on the default Off → no logs. The benchmarks still
            // compute + return LatencyStats; the EXPECT_LT checks below
            // exercise the spec gates regardless of logging.
        }
        PerfConfig::instance().initialize_from_env();

        known_words.reserve(HT_ENTRIES);
        for (uint64_t i = 0; i < HT_ENTRIES; ++i) {
            known_words.push_back(make_word(i));
        }
        // Insert into the hash table (in-memory mode — no mmap file).
        for (uint64_t i = 0; i < HT_ENTRIES; ++i) {
            ht.insert(known_words[i], "val_" + std::to_string(i));
        }
    }
};

// =============================================================================
//  Benchmark 1: 1M hash table lookups (spec gate: <50ms on Moto G)
// =============================================================================
TEST_F(PerformanceBenchmarkFixture, Benchmark_1M_HashTableLookups) {
    // Build a lookup pattern: cycle through known words, with every 5th
    // lookup being a deliberate miss (a word we didn't insert). This
    // exercises both hit and miss paths through Robin Hood probing.
    auto lookup_fn = [this](uint64_t i) -> bool {
        uint64_t idx = i % HT_ENTRIES;
        const std::string& key = (i % 5 == 0)
            ? std::string{"MISS_"} + known_words[idx]
            : known_words[idx];
        uint32_t len = 0;
        const char* v = ht.lookup(key, &len);
        return (v != nullptr) == (i % 5 != 0);  // hits return non-null
    };

    LatencyStats stats = LookupBenchmark::run_and_log(
        lookup_fn, LOOKUP_ITERATIONS, "1m_hash_table_lookups");

    EXPECT_EQ(stats.count, LOOKUP_ITERATIONS);
    EXPECT_GT(stats.mean_us, 0.0);

    // Compute total elapsed time = mean_us * iterations, in milliseconds.
    double total_ms = (stats.mean_us * static_cast<double>(LOOKUP_ITERATIONS)) / 1000.0;
    std::printf("[BENCH] 1M hash table lookups: total=%.3f ms, "
                "p50=%.3f us, p95=%.3f us, p99=%.3f us\n",
                total_ms, stats.p50_us, stats.p95_us, stats.p99_us);

    // Soft assertion (record-and-report, not fail-the-build): the spec's
    // hard gate is <50ms on Moto G. We're running on a (presumably faster)
    // dev machine, so we use a 5× headroom factor — if it takes >250ms here,
    // something is wrong on a workstation-class CPU.
    // The actual Moto G gate is enforced by the on-device benchmark runner.
    EXPECT_TRUE(total_ms <= 250.0)
        << "1M lookups took " << total_ms << "ms on a workstation "
        << "— Moto G gate (<50ms) is at severe risk. Investigate "
        << "hash function, probe sequence, or cache behavior.";
}

// =============================================================================
//  Benchmark 2: 100K Levenshtein searches (throughput)
// =============================================================================
TEST_F(PerformanceBenchmarkFixture, Benchmark_100K_LevenshteinSearches) {
    LevenshteinAutomaton lev;

    // Build a candidate list: 5000 (word, id) pairs sampled from known_words.
    std::vector<std::pair<std::string, std::string>> candidates;
    candidates.reserve(5000);
    for (uint64_t i = 0; i < 5000 && i < HT_ENTRIES; ++i) {
        candidates.emplace_back(known_words[i], "id_" + std::to_string(i));
    }

    auto search_fn = [&lev, &candidates](uint64_t i) -> bool {
        // Query with a slightly perturbed word (transposition → distance ≤ 1).
        std::string q = candidates[i % candidates.size()].first;
        if (q.size() >= 2) std::swap(q[0], q[1]);
        auto results = lev.search(q, candidates, /*max_distance=*/1, /*limit=*/20);
        return !results.empty();
    };

    constexpr uint64_t ITERS = 100000;
    LatencyStats stats = LookupBenchmark::run_and_log(
        search_fn, ITERS, "100k_levenshtein_searches");

    EXPECT_EQ(stats.count, ITERS);
    double total_ms = (stats.mean_us * static_cast<double>(ITERS)) / 1000.0;
    double throughput_per_sec = (total_ms > 0.0)
        ? static_cast<double>(ITERS) / (total_ms / 1000.0)
        : 0.0;
    std::printf("[BENCH] 100K Levenshtein searches: total=%.3f ms, "
                "throughput=%.0f searches/sec, p95=%.3f us\n",
                total_ms, throughput_per_sec, stats.p95_us);

    EXPECT_GT(throughput_per_sec, 0.0);
}

// =============================================================================
//  Benchmark 3: 10K DAWG prefix searches
// =============================================================================
TEST_F(PerformanceBenchmarkFixture, Benchmark_10K_DawgPrefixSearches) {
    Dawg dawg;
    // Build a DAWG from a sorted subset of known_words.
    std::vector<std::string> sorted_words;
    sorted_words.reserve(HT_ENTRIES);
    for (uint64_t i = 0; i < HT_ENTRIES; ++i) {
        sorted_words.push_back(known_words[i]);
    }
    std::sort(sorted_words.begin(), sorted_words.end());
    dawg.build(sorted_words);
    ASSERT_TRUE(dawg.is_loaded());

    auto prefix_fn = [&dawg](uint64_t i) -> bool {
        // Use the first 2-3 chars of a known word as the prefix.
        std::string word = make_word(i, /*salt=*/7);
        std::string prefix = word.substr(0, std::min<size_t>(word.size(), 3));
        auto results = dawg.prefix_search(prefix, /*limit=*/50);
        return !results.empty();
    };

    constexpr uint64_t ITERS = 10000;
    LatencyStats stats = LookupBenchmark::run_and_log(
        prefix_fn, ITERS, "10k_dawg_prefix_searches");

    EXPECT_EQ(stats.count, ITERS);
    double total_ms = (stats.mean_us * static_cast<double>(ITERS)) / 1000.0;
    double throughput_per_sec = (total_ms > 0.0)
        ? static_cast<double>(ITERS) / (total_ms / 1000.0)
        : 0.0;
    std::printf("[BENCH] 10K DAWG prefix searches: total=%.3f ms, "
                "throughput=%.0f searches/sec, p95=%.3f us\n",
                total_ms, throughput_per_sec, stats.p95_us);

    EXPECT_GT(throughput_per_sec, 0.0);
}

// =============================================================================
//  Benchmark 4: Cold start simulation (load + first lookup)
// =============================================================================
TEST(PerformanceBenchmarkColdStart, SimulatedColdStartUnder500ms) {
    // Simulate the cold-start path: construct engine, "load" (we can't load
    // a real DB without fixtures, so we time the construction + a no-op
    // load + first lookup), and measure wall-clock.
    //
    // This is a *simulation* — the real on-device measurement is done by
    // LexoraColdStartOptimizer (Android/iOS). The simulation's job is to
    // verify the scaffolding's plumbing works end-to-end and to catch
    // regressions in engine construction cost.
    auto t0 = std::chrono::steady_clock::now();

    LexoraEngine engine;
    // loadDatabase is expected to return false (no DB) — that's fine for
    // the simulation. We're measuring the construction + call overhead.
    engine.load_database("/tmp/lexora_nonexistent_db");
    auto first_lookup = engine.lookup("hello");

    auto t1 = std::chrono::steady_clock::now();
    double ms = std::chrono::duration<double, std::milli>(t1 - t0).count();

    std::printf("[BENCH] Cold start simulation (construct + load + first lookup): "
                "%.3f ms\n", ms);

    // The simulation should complete in well under the 500ms gate, since
    // we're not actually loading a database. Use a generous 200ms ceiling
    // to catch any pathological regression in engine construction.
    EXPECT_LT(ms, 200.0)
        << "Cold-start simulation took " << ms << "ms — investigate engine "
        << "construction cost (the on-device 500ms gate is at risk).";
    EXPECT_FALSE(first_lookup.has_value())
        << "Lookup on empty DB should return nullopt";

    // Also exercise ScopedTimer on the same path to verify it doesn't
    // noticeably inflate the cold-start measurement.
    {
        ScopedTimer t("cold_start.scoped");
        LexoraEngine e2;
        e2.load_database("/tmp/lexora_nonexistent_db");
        e2.lookup("world");
    }
}

// =============================================================================
//  Benchmark 5: Memory usage after 1M entries loaded
// =============================================================================
TEST(PerformanceBenchmarkMemory, MemoryAfter1MEntries) {
    MemoryTracker tracker;
    MemorySnapshot before = tracker.sample("before_1m_load");

    MemoryMappedHashTable ht;
    constexpr uint64_t N = 1000000;
    for (uint64_t i = 0; i < N; ++i) {
        ht.insert(make_word(i), "val_" + std::to_string(i));
    }

    MemorySnapshot after = tracker.sample("after_1m_load");
    uint64_t rss_delta = (after.rss_bytes > before.rss_bytes)
        ? (after.rss_bytes - before.rss_bytes)
        : 0;
    uint64_t heap_delta = (after.heap_bytes > before.heap_bytes)
        ? (after.heap_bytes - before.heap_bytes)
        : 0;

    std::printf("[BENCH] Memory after 1M entries loaded:\n");
    std::printf("  RSS before:   %.2f MB\n", before.rss_bytes / 1048576.0);
    std::printf("  RSS after:    %.2f MB\n", after.rss_bytes / 1048576.0);
    std::printf("  RSS delta:    %.2f MB\n", rss_delta / 1048576.0);
    std::printf("  Heap before:  %.2f MB\n", before.heap_bytes / 1048576.0);
    std::printf("  Heap after:   %.2f MB\n", after.heap_bytes / 1048576.0);
    std::printf("  Heap delta:   %.2f MB\n", heap_delta / 1048576.0);
    std::printf("  Peak RSS:     %.2f MB\n", tracker.peak_rss_bytes() / 1048576.0);

    // Sanity: loading 1M entries should increase memory by at least ~30 MB
    // (8 bytes per HashEntry + ~15 bytes per key + ~15 bytes per value +
    // hash table overhead). Use 10 MB as a conservative lower bound.
    EXPECT_GT(rss_delta, 10ull * 1024 * 1024)
        << "Expected RSS to grow by >10MB after 1M entries; got "
        << rss_delta << " bytes";
    // Sanity: peak should be >= current.
    EXPECT_GE(tracker.peak_rss_bytes(), after.rss_bytes);
}

// =============================================================================
//  Integration test: FrameTimer with simulated 60fps scroll for 1 sec.
// =============================================================================
TEST(FrameTimerScrollTest, SimulatedScrollNoJankUnder16ms) {
    // Simulate 60 frames of "smooth scroll" — each frame renders in 8ms
    // (well under the 16.67ms budget). Should produce zero jank.
    FrameTimer ft("dictionary_scroll");
    for (int i = 0; i < 60; ++i) {
        ft.record_frame(8.0);
    }
    FrameStats st = ft.snapshot();
    EXPECT_EQ(st.total_frames, 60u);
    EXPECT_EQ(st.dropped_frames, 0u);
    EXPECT_TRUE(std::abs(st.jank_percent - 0.0) <= 0.001);
    EXPECT_LT(st.p95_ms, 16.0)
        << "p95 frame render (" << st.p95_ms << "ms) should be <16ms";
    ft.report();
}

TEST(FrameTimerScrollTest, SimulatedScrollWithJank) {
    // Simulate 60 frames where 6 of them are janky (20ms each).
    FrameTimer ft("home_scroll");
    for (int i = 0; i < 60; ++i) {
        ft.record_frame((i % 10 == 0) ? 20.0 : 10.0);   // 6 jank frames
    }
    FrameStats st = ft.snapshot();
    EXPECT_EQ(st.total_frames, 60u);
    EXPECT_EQ(st.dropped_frames, 6u);
    EXPECT_TRUE(std::abs(st.jank_percent - 10.0) <= 0.01);
    ft.report();
}

// =============================================================================
//  main() — only defined when the gtest_lite shim is in use (real GoogleTest
//  provides its own main via gtest_main). When LEXORA_USE_GTEST_LITE_MAIN is
//  defined (per tests/CMakeLists.txt), this main() drives the shim's
//  registration-based test runner.
// =============================================================================
#ifdef LEXORA_USE_GTEST_LITE_MAIN
int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
#endif

