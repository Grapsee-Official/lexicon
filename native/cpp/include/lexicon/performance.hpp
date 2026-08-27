// performance.hpp — Lexicon performance profiling scaffolding.
//
// Provides four primitive instruments, all thread-safe and tuned for the
// spec's hard gates:
//
//   * ScopedTimer       — RAII timer that logs each scope's elapsed time
//                         to /tmp/lexicon_perf.log (or app cache dir) as
//                         JSON. Overhead ≈ 2× steady_clock read (~50 ns) +
//                         one mutex-guarded file append. Use for the
//                         cold-start pipeline (engine.load, dawg.enable,
//                         etc.) where you measure a few hundred scopes/sec.
//
//   * LookupBenchmark   — Runs N lookups via a caller-supplied callable,
//                         computes p50/p95/p99 latency. Spec gate:
//                         "1M lookups in <50ms on Moto G" → drives this
//                         benchmark with iterations=1_000_000.
//
//   * MemoryTracker     — Tracks peak RSS + heap usage. Spec gate: keep
//                         the engine's resident footprint low enough to
//                         coexist with the system; this instrument reports
//                         the actual numbers so we can chase regressions.
//
//   * FrameTimer        — Measures per-frame render time, reports drops
//                         (frames >16 ms are "jank"). Spec gate:
//                         "95th percentile frame render time <16ms during
//                         list scrolling" + "Zero frame drops scrolling
//                         1000 items on Moto G". Both Android's
//                         Choreographer.FrameCallback and iOS's
//                         CADisplayLink feed into this instrument via
//                         record_frame(ms).
//
// Verbosity is controlled by the LEXICON_PERF_LEVEL env var (0=off,
// 1=basic, 2=verbose). Off → all instruments compile to no-ops at
// runtime (a single relaxed atomic load per measurement site). Basic →
// ScopedTimer + LookupBenchmark + MemoryTracker emit JSON events, but
// FrameTimer suppresses per-jank logs (only summary on report()).
// Verbose → FrameTimer also emits a per-jank event for every dropped
// frame (use only in CI / debugging — high log volume).
//
// The JSON log format is one event per line (JSON Lines / NDJSON) for
// easy streaming parse by jq, Python's json.loads-per-line, or the
// ETL perf-collector. Each event:
//
//   {"ts": <epoch_ms>, "type": "<event_type>", "data": { ... }}
//
// All instruments are non-copyable where they own state. All are safe
// to use from multiple threads concurrently. All use std::chrono::
// steady_clock (monotonic, not subject to NTP adjustments) for timing.
//
// C++17, no external deps. Header-only inline hot paths where it
// matters (the per-lookup steady_clock read in LookupBenchmark is
// in a static method, not a virtual, so the compiler inlines it).
#pragma once

#include <atomic>
#include <chrono>
#include <cstdint>
#include <functional>
#include <mutex>
#include <string>
#include <string_view>
#include <vector>

namespace lexicon {

// -----------------------------------------------------------------------------
//  PerfLevel — verbosity controlled by LEXICON_PERF_LEVEL env var.
// -----------------------------------------------------------------------------
enum class PerfLevel : int {
    Off     = 0,  // No measurements recorded. Instruments are no-ops.
    Basic   = 1,  // Timers + benchmarks + memory + frame summary.
    Verbose = 2,  // Everything above + per-jank frame events.
};

// -----------------------------------------------------------------------------
//  LatencyStats — summary of a sample of latency measurements (microseconds).
//  Computed by compute_latency_stats_us() from a sortable sample vector.
// -----------------------------------------------------------------------------
struct LatencyStats {
    size_t  count      = 0;     // Number of samples (n)
    double  min_us     = 0.0;   // Minimum sample
    double  max_us     = 0.0;   // Maximum sample
    double  mean_us    = 0.0;   // Arithmetic mean
    double  stddev_us  = 0.0;   // Population standard deviation
    double  p50_us     = 0.0;   // 50th percentile (median)
    double  p95_us     = 0.0;   // 95th percentile
    double  p99_us     = 0.0;   // 99th percentile
};

// -----------------------------------------------------------------------------
//  MemorySnapshot — process memory at a point in time (bytes).
// -----------------------------------------------------------------------------
struct MemorySnapshot {
    uint64_t rss_bytes       = 0;  // Resident set size (current)
    uint64_t heap_bytes      = 0;  // Active heap allocation (mallinfo)
    uint64_t virtual_bytes   = 0;  // Virtual memory size
    uint64_t peak_rss_bytes  = 0;  // Peak RSS observed by tracker (0 if n/a)
};

// -----------------------------------------------------------------------------
//  FrameStats — accumulated frame statistics for one FrameTimer window.
// -----------------------------------------------------------------------------
struct FrameStats {
    std::string screen_name;          // Logical screen label
    uint64_t    total_frames    = 0;  // Frames observed in this window
    uint64_t    dropped_frames  = 0;  // Frames whose render time exceeded budget
    double      frame_budget_ms = 16.67;  // Per-frame budget (60 fps default)
    double      min_ms          = 0.0;
    double      max_ms          = 0.0;
    double      mean_ms         = 0.0;
    double      p50_ms          = 0.0;
    double      p95_ms          = 0.0;
    double      p99_ms          = 0.0;
    double      jank_percent    = 0.0;  // 100 * dropped_frames / total_frames
};

// -----------------------------------------------------------------------------
//  PerfConfig — global config singleton (thread-safe, lock-free reads).
//
//  Initialized once via initialize_from_env() (typically called from
//  LexiconColdStartOptimizer / LexiconApplication.onCreate). All instruments
//  read level() via a single relaxed atomic load — sub-nanosecond, suitable
//  for per-frame hot paths.
// -----------------------------------------------------------------------------
class PerfConfig {
public:
    // Singleton accessor.
    static PerfConfig& instance();

    // Read LEXICON_PERF_LEVEL and LEXICON_PERF_LOG env vars. Idempotent and
    // thread-safe. Returns the resolved level. Called automatically by the
    // first instrument that needs it, but callers should invoke explicitly
    // at startup so the level is settled before any measurement.
    PerfLevel initialize_from_env();

    // Current level. Off until initialize_from_env() runs (which the first
    // instrument will do lazily if needed).
    PerfLevel level() const noexcept {
        return static_cast<PerfLevel>(level_.load(std::memory_order_relaxed));
    }

    // Log file path. Defaults to "/tmp/lexicon_perf.log"; can be overridden
    // by LEXICON_PERF_LOG env var (e.g. "/data/data/com.lexicon/cache/lexicon_perf.log"
    // on Android, NSCachesDirectory on iOS).
    std::string log_path() const;

    // Override the log path explicitly (e.g. Android cacheDir, iOS
    // NSCachesDirectory). Thread-safe. Closes any currently-open file handle.
    void set_log_path(std::string path);

    // Write one line to the perf log. Thread-safe. No-op if level()==Off.
    // The line should already be a complete JSON object (no trailing newline
    // — this function adds it).
    void write_line(std::string_view json_line) const;

    // Convenience: write a structured JSON event of the form
    //   {"ts": <epoch_ms>, "type": "<type>", "data": {<json_body>}}
    // No-op if level()==Off. The body must already be valid JSON (an
    // object literal). This is the canonical entry point used by all
    // instruments.
    void write_event(std::string_view type, std::string_view json_body) const;

    // Whether per-jank events should be emitted (level==Verbose). FrameTimer
    // consults this to decide whether to log each dropped frame individually.
    bool should_log_jank() const noexcept {
        return level_.load(std::memory_order_relaxed) >=
               static_cast<int>(PerfLevel::Verbose);
    }

    // Whether basic instrumentation should fire (level >= Basic).
    bool should_record() const noexcept {
        return level_.load(std::memory_order_relaxed) >=
               static_cast<int>(PerfLevel::Basic);
    }

private:
    PerfConfig();
    std::atomic<int>  level_{static_cast<int>(PerfLevel::Off)};
    mutable std::mutex path_mu_;
    std::string        log_path_{"/tmp/lexicon_perf.log"};
    mutable std::mutex file_mu_;
    mutable void*      file_handle_{nullptr};  // std::ofstream* stored as void* to keep header lean
    mutable std::atomic<bool> file_open_failed_{false};
    std::atomic<bool>  env_initialized_{false};
};

// -----------------------------------------------------------------------------
//  ScopedTimer — RAII timer that logs to file on scope exit.
//
//  Overhead budget: <1 μs per measurement (steady_clock is ~20-50 ns on
//  Linux/macOS via vDSO / mach_absolute_time; the file append is a single
//  mutex + write, amortized over ~100 µs of buffered I/O).
//
//  Use for the cold-start pipeline (engine.load_database, dawg.enable,
//  first lookup, etc.) — anywhere you measure tens to thousands of scopes
//  per second. For per-frame timing use FrameTimer instead.
//
//  When level()==Off the constructor captures only a steady_clock time
//  point (~25 ns) and the destructor does nothing — the impact on a hot
//  loop is negligible. For ultra-hot paths, guard with `if (PerfConfig::
//  instance().should_record())` to skip even the clock read.
// -----------------------------------------------------------------------------
class ScopedTimer {
public:
    // name: dotted metric path, e.g. "engine.lookup", "dawg.prefix_search".
    // tags_json: optional inline JSON object fragment merged into "data"
    // (e.g. "\"word\":\"hello\",\"lang\":\"en\""). May be empty.
    ScopedTimer(std::string name, std::string tags_json = "");
    ~ScopedTimer();

    // Non-copyable, non-movable (holds state read by destructor).
    ScopedTimer(const ScopedTimer&) = delete;
    ScopedTimer& operator=(const ScopedTimer&) = delete;
    ScopedTimer(ScopedTimer&&) = delete;
    ScopedTimer& operator=(ScopedTimer&&) = delete;

    // Elapsed so far in microseconds (does not log). Cheap: one steady_clock
    // read + one subtract. Safe to call mid-scope.
    double elapsed_us() const noexcept;

    // Manually emit the log event now (and disarm the destructor log).
    // Useful when the scope exit is in an unusual place (early return
    // inside a loop, etc.). After emit(), the destructor becomes a no-op.
    void emit() noexcept;

private:
    std::string name_;
    std::string tags_;
    std::chrono::steady_clock::time_point start_;
    bool armed_;
};

// -----------------------------------------------------------------------------
//  LookupBenchmark — runs N lookups via a callable, computes p50/p95/p99.
//
//  Designed for the spec's "1M lookups in <50ms" hard gate. The callable
//  is invoked `iterations` times after `warmup` discarded iterations
//  (cache + branch predictor warmup). Per-iteration latency is captured
//  via steady_clock and stored in a pre-reserved vector for offline
//  percentile computation.
//
//  Storage cost: 8 bytes × iterations. For 1M iterations = 8 MB, which is
//  acceptable for a benchmark harness (not for a hot path). For 100K
//  iterations = 800 KB. If you need to bench >10M iterations, switch to
//  a streaming percentile sketch (e.g. t-digest) — out of scope here.
// -----------------------------------------------------------------------------
class LookupBenchmark {
public:
    using LookupFn = std::function<bool(uint64_t /*index*/)>;

    // Run `iterations` lookups. Returns LatencyStats in microseconds.
    // `warmup` iterations are run first (and discarded) to stabilize the
    // CPU cache + branch predictor before measurement starts.
    static LatencyStats run(LookupFn fn,
                            uint64_t iterations,
                            uint64_t warmup = 1000);

    // Run + emit a JSON benchmark event to the perf log. The event's
    // "data" object contains: name, iterations, warmup, and all LatencyStats
    // fields (count/min/max/mean/stddev/p50/p95/p99).
    static LatencyStats run_and_log(LookupFn fn,
                                    uint64_t iterations,
                                    std::string_view benchmark_name,
                                    uint64_t warmup = 1000);
};

// -----------------------------------------------------------------------------
//  MemoryTracker — tracks peak RSS, heap usage over the tracker's lifetime.
//
//  Sampling is on-demand (no background thread). Call sample() at strategic
//  points — after database load, after DAWG build, after a search burst —
//  to record the current RSS/heap and update the peak trackers. Each
//  sample() also emits a JSON memory event to the perf log (if level >=
//  Basic) so the ETL perf-collector can build a timeline.
//
//  Platform coverage:
//    * Linux / Android: /proc/self/status (VmRSS, VmPeak, VmSize) +
//      mallinfo2() (glibc ≥ 2.33) or mallinfo() (older).
//    * macOS / iOS: mach_task_basic_info (resident_size, virtual_size) +
//      malloc_zone_statistics.
//    * Other: returns zeros (no crash — just no data).
// -----------------------------------------------------------------------------
class MemoryTracker {
public:
    MemoryTracker();
    ~MemoryTracker();

    // Non-copyable.
    MemoryTracker(const MemoryTracker&) = delete;
    MemoryTracker& operator=(const MemoryTracker&) = delete;

    // Take a snapshot now, update peak tracking, emit JSON event (if level
    // >= Basic). Returns current values. `label` is a free-form tag (e.g.
    // "after_db_load", "after_dawg_build") that goes into the JSON event.
    MemorySnapshot sample(std::string_view label = "");

    // Peak RSS observed since construction (or last reset_peak()).
    uint64_t peak_rss_bytes() const noexcept {
        return peak_rss_.load(std::memory_order_relaxed);
    }
    uint64_t peak_heap_bytes() const noexcept {
        return peak_heap_.load(std::memory_order_relaxed);
    }

    // Reset peak trackers to zero (does not affect current values).
    void reset_peak() noexcept;

    // One-shot snapshot of process memory (no tracker state updated).
    // Use this when you don't want to instantiate a tracker.
    static MemorySnapshot process_memory();

    // Emit a JSON memory event for a given snapshot. The "data" object
    // contains: label, rss_bytes, heap_bytes, virtual_bytes, peak_rss_bytes.
    static void log_snapshot(std::string_view label, const MemorySnapshot& snap);

private:
    std::atomic<uint64_t> peak_rss_{0};
    std::atomic<uint64_t> peak_heap_{0};
};

// -----------------------------------------------------------------------------
//  FrameTimer — measures frame render time, reports drops (jank).
//
//  Two usage modes:
//
//  (A) begin/end around your render block:
//    FrameTimer ft("home_screen");
//    // ... inside your render callback ...
//    ft.frame_begin();
//    render_my_screen();
//    ft.frame_end();   // auto-logs jank if render_ms > budget
//
//  (B) record a pre-measured frame duration:
//    ft.record_frame(16.3);  // ms — e.g. from Choreographer doFrame callback
//
//  Mode (B) is preferred on Android/iOS because the platform already
//  measures the frame interval more accurately than we can (Choreographer
//  gives the vsync timestamp; CADisplayLink gives the previous frame
//  timestamp). FrameTimer's job is to aggregate, percentile, and emit.
//
//  Overhead: per frame = 1 steady_clock read (mode A) + a handful of
//  atomic ops + a mutex-guarded ring-buffer push. At 60 fps that's
//  ~60 mutex locks/sec — negligible. Percentile computation is deferred
//  to report() (called every 1-2 sec on a background thread).
//
//  The ring buffer keeps the last 4096 frame durations (power-of-two for
//  cheap modulo). When report() is called, the buffer is copied out,
//  sorted, and percentiles computed. The buffer is then reset so the
//  next report() covers a fresh window.
//
//  Spec gate: "95th percentile frame render time <16ms during list
//  scrolling" + "Zero frame drops scrolling 1000 items on Moto G".
//  FrameTimer.surface_jank_summary() emits the p95 + jank_percent that
//  directly answer these gates.
// -----------------------------------------------------------------------------
class FrameTimer {
public:
    // frame_budget_ms: 16.67 for 60fps, 8.33 for 120fps. Frames exceeding
    // this are counted as drops.
    explicit FrameTimer(std::string screen_name,
                        double frame_budget_ms = 16.67);
    ~FrameTimer();

    // Non-copyable.
    FrameTimer(const FrameTimer&) = delete;
    FrameTimer& operator=(const FrameTimer&) = delete;

    // Mark the start of a frame. Cheap (one steady_clock read). If
    // frame_begin() is called twice without an intervening frame_end(),
    // the second call resets the start (the first frame is implicitly
    // dropped — a sign of a misbehaving host).
    void frame_begin();

    // Mark the end of a frame. Computes elapsed since the matching
    // frame_begin(), records it. If elapsed > frame_budget_ms and level
    // >= Basic, increments the drop counter; if level == Verbose, also
    // emits a per-jank JSON event.
    void frame_end();

    // Convenience: record a pre-measured frame duration in milliseconds.
    // Same bookkeeping as frame_end() but the host did the timing.
    void record_frame(double render_ms);

    // Snapshot of accumulated stats. Resets nothing. Computes percentiles
    // from the current ring buffer (sorts a copy under mutex).
    FrameStats snapshot() const;

    // Emit a JSON frame stats event to the perf log AND reset internal
    // accumulators (so the next report() covers a fresh window).
    // Call from a background thread every 1-2 seconds, or on screen exit.
    void report();

    // Reset accumulators without logging. Safe to call from any thread.
    void reset() noexcept;

    // Accessor for the configured budget (for host-side sanity checks).
    double frame_budget_ms() const noexcept { return frame_budget_ms_; }

    // Accessors for the cheap atomic counters (no percentile computation).
    uint64_t total_frames() const noexcept {
        return total_frames_.load(std::memory_order_relaxed);
    }
    uint64_t dropped_frames() const noexcept {
        return dropped_frames_.load(std::memory_order_relaxed);
    }

private:
    std::string screen_name_;
    double      frame_budget_ms_;

    // Cheap atomic counters updated on every frame_end / record_frame.
    std::atomic<uint64_t> total_frames_{0};
    std::atomic<uint64_t> dropped_frames_{0};

    // Ring buffer for percentile computation. Power-of-two capacity for
    // cheap modulo. Mutex-guarded on write (one frame per ~16 ms — the
    // mutex is never contended in practice).
    static constexpr size_t BUFFER_CAPACITY = 4096;
    mutable std::mutex buffer_mu_;
    std::vector<double> buffer_;
    size_t   buffer_head_ = 0;
    bool     buffer_full_ = false;

    // For frame_begin/frame_end mode. Read+reset on frame_end.
    std::chrono::steady_clock::time_point frame_start_;
    bool frame_in_progress_ = false;

    // Internal: push a sample to the ring buffer + update atomic counters.
    void record_internal(double render_ms);
};

// -----------------------------------------------------------------------------
//  compute_latency_stats_us — compute p50/p95/p99 etc. from a sample vector.
//  Sorts `samples` in-place (O(n log n)). Returns a fully-populated
//  LatencyStats. Empty input → zero stats with count=0.
// -----------------------------------------------------------------------------
LatencyStats compute_latency_stats_us(std::vector<double>& samples_us);

} // namespace lexicon
