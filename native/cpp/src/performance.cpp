// performance.cpp — Lexora performance profiling scaffolding implementation.
//
// See include/lexora/performance.hpp for the design contract.
//
// JSON output format (one event per line, NDJSON / JSON Lines):
//
//   {"ts": <epoch_ms>, "type": "<event_type>", "data": { ... }}
//
// Where `ts` is Unix epoch milliseconds (UTC) — captured via
// std::chrono::system_clock at event-emit time, NOT at measurement time.
// For high-precision latency use the per-event "elapsed_us" / "elapsed_ms"
// fields which are measured via std::chrono::steady_clock (monotonic).
//
// Platform guards:
//   * Linux / Android: /proc/self/status + getrusage(RUSAGE_SELF) +
//     mallinfo2 (glibc ≥ 2.33) or mallinfo (older).
//   * Apple (macOS / iOS): mach_task_basic_info + malloc_zone_statistics.
//   * Other: memory functions return zeros (no crash, no data).
//
// C++17, no external deps. The file handle is opened lazily on first write
// and held open for the process lifetime (one ofstream, mutex-guarded).
#include "lexora/performance.hpp"

#include <algorithm>
#include <cctype>
#include <cerrno>
#include <chrono>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <ctime>
#include <fstream>
#include <sstream>
#include <utility>

// -----------------------------------------------------------------------------
//  Platform headers (guarded so the file still compiles on unknown platforms).
// -----------------------------------------------------------------------------
#if defined(__linux__) || defined(__ANDROID__)
    #include <sys/resource.h>   // getrusage, RUSAGE_SELF
    #include <sys/time.h>       // struct timeval
    #include <malloc.h>         // mallinfo / mallinfo2
    #include <unistd.h>
#elif defined(__APPLE__)
    #include <mach/mach.h>
    #include <mach/mach_init.h>
    #include <mach/task_info.h>
    #include <malloc/malloc.h>
#endif

namespace lexora {

namespace {

// ---------------------------------------------------------------------------
//  now_epoch_ms — Unix epoch milliseconds (for the JSON "ts" field).
// ---------------------------------------------------------------------------
inline int64_t now_epoch_ms() noexcept {
    using namespace std::chrono;
    return duration_cast<milliseconds>(
        system_clock::now().time_since_epoch()).count();
}

// ---------------------------------------------------------------------------
//  now_steady — convenience wrapper for steady_clock::now().
// ---------------------------------------------------------------------------
inline std::chrono::steady_clock::time_point now_steady() noexcept {
    return std::chrono::steady_clock::now();
}

// ---------------------------------------------------------------------------
//  elapsed_us_between — microseconds between two steady_clock time_points.
// ---------------------------------------------------------------------------
inline double elapsed_us_between(std::chrono::steady_clock::time_point start,
                                 std::chrono::steady_clock::time_point end) noexcept {
    using namespace std::chrono;
    return static_cast<double>(
        duration_cast<nanoseconds>(end - start).count()) / 1000.0;
}

// ---------------------------------------------------------------------------
//  trim — trim leading/trailing whitespace from a std::string_view. Used
//  when parsing the LEXORA_PERF_LEVEL env var.
// ---------------------------------------------------------------------------
std::string_view trim(std::string_view s) noexcept {
    while (!s.empty() && std::isspace(static_cast<unsigned char>(s.front()))) {
        s.remove_prefix(1);
    }
    while (!s.empty() && std::isspace(static_cast<unsigned char>(s.back()))) {
        s.remove_suffix(1);
    }
    return s;
}

// ---------------------------------------------------------------------------
//  json_escape — escape a string for JSON output (RFC 8259 § 7).
//  Returns a std::string. Handles control chars, quotes, backslashes, and
//  UTF-8 pass-through (we don't escape non-ASCII — JSON allows raw UTF-8).
// ---------------------------------------------------------------------------
std::string json_escape(std::string_view s) {
    std::string out;
    out.reserve(s.size() + 8);
    for (char c : s) {
        switch (c) {
            case '"':  out += "\\\""; break;
            case '\\': out += "\\\\"; break;
            case '\b': out += "\\b";  break;
            case '\f': out += "\\f";  break;
            case '\n': out += "\\n";  break;
            case '\r': out += "\\r";  break;
            case '\t': out += "\\t";  break;
            default:
                if (static_cast<unsigned char>(c) < 0x20) {
                    char buf[8];
                    std::snprintf(buf, sizeof(buf), "\\u%04x",
                                  static_cast<unsigned char>(c));
                    out += buf;
                } else {
                    out += c;
                }
        }
    }
    return out;
}

// ---------------------------------------------------------------------------
//  Platform memory snapshot helpers.
// ---------------------------------------------------------------------------
#if defined(__linux__) || defined(__ANDROID__)

// Parse /proc/self/status for VmRSS, VmPeak, VmSize (in KB → bytes).
// Returns true if at least one field was found.
bool read_proc_status(MemorySnapshot& snap) noexcept {
    std::ifstream f("/proc/self/status");
    if (!f) return false;
    std::string line;
    bool found_any = false;
    while (std::getline(f, line)) {
        // Lines look like: "VmRSS:      12345 kB"
        auto colon = line.find(':');
        if (colon == std::string::npos) continue;
        std::string key = line.substr(0, colon);
        std::string val = line.substr(colon + 1);
        // Strip trailing " kB" / whitespace.
        // Find first digit, parse unsigned long.
        const char* p = val.c_str();
        while (*p && !std::isdigit(static_cast<unsigned char>(*p))) ++p;
        if (!*p) continue;
        char* endp = nullptr;
        errno = 0;
        unsigned long kb = std::strtoul(p, &endp, 10);
        if (errno != 0) continue;
        uint64_t bytes = static_cast<uint64_t>(kb) * 1024ull;
        if (key == "VmRSS")  { snap.rss_bytes     = bytes; found_any = true; }
        else if (key == "VmPeak") { /* peak is per-process, not tracker — keep current RSS as peak placeholder */ found_any = true; }
        else if (key == "VmSize") { snap.virtual_bytes = bytes; found_any = true; }
    }
    return found_any;
}

// getrusage(RUSAGE_SELF).ru_maxrss is in KB on Linux.
uint64_t get_rusage_peak_rss_kb() noexcept {
    struct rusage ru {};
    if (getrusage(RUSAGE_SELF, &ru) != 0) return 0;
    return static_cast<uint64_t>(ru.ru_maxrss);  // KB on Linux
}

uint64_t get_heap_bytes_linux() noexcept {
#if defined(__GLIBC__) && (__GLIBC__ > 2 || (__GLIBC__ == 2 && __GLIBC_MINOR__ >= 33))
    // mallinfo2 (glibc ≥ 2.33) — returns 64-bit fields.
    struct mallinfo2 mi = mallinfo2();
    return static_cast<uint64_t>(mi.uordblks) + static_cast<uint64_t>(mi.hblkhd);
#else
    // mallinfo (older) — 32-bit fields. Truncates above 4 GB.
    struct mallinfo mi = mallinfo();
    return static_cast<uint64_t>(static_cast<unsigned int>(mi.uordblks)) +
           static_cast<uint64_t>(static_cast<unsigned int>(mi.hblkhd));
#endif
}

MemorySnapshot read_process_memory() noexcept {
    MemorySnapshot snap;
    read_proc_status(snap);
    snap.heap_bytes = get_heap_bytes_linux();
    // ru_maxrss is the high-water mark since process start. Use it as the
    // peak if /proc didn't give us a current RSS reading.
    uint64_t ru_peak = get_rusage_peak_rss_kb() * 1024ull;
    if (snap.rss_bytes == 0) snap.rss_bytes = ru_peak;
    snap.peak_rss_bytes = ru_peak;
    return snap;
}

#elif defined(__APPLE__)

MemorySnapshot read_process_memory() noexcept {
    MemorySnapshot snap;
    // mach_task_basic_info gives resident_size + virtual_size + current RSS.
    task_t self_task = mach_task_self();
    mach_task_basic_info_data_t info;
    mach_msg_type_number_t count = MACH_TASK_BASIC_INFO_COUNT;
    kern_return_t kr = task_info(self_task, MACH_TASK_BASIC_INFO,
                                  reinterpret_cast<task_info_t>(&info), &count);
    if (kr == KERN_SUCCESS) {
        snap.rss_bytes     = static_cast<uint64_t>(info.resident_size);
        snap.virtual_bytes = static_cast<uint64_t>(info.virtual_size);
        // info.resident_size_peak is the high-water mark (macOS 10.9+).
        snap.peak_rss_bytes = static_cast<uint64_t>(info.resident_size);
    }
    // Heap via malloc_zone_statistics (default zone).
    malloc_zone_t* zone = malloc_default_zone();
    if (zone) {
        malloc_statistics_t stats;
        zone->introspect->statistics(zone, &stats);
        // stats.size_in_use is currently-allocated bytes; stats.max_size_in_use
        // is the high-water mark.
        snap.heap_bytes = static_cast<uint64_t>(stats.size_in_use);
        if (snap.peak_rss_bytes == 0) {
            snap.peak_rss_bytes = static_cast<uint64_t>(stats.max_size_in_use);
        }
    }
    return snap;
}

#else
// Unknown platform — return zeros (no crash).
MemorySnapshot read_process_memory() noexcept {
    return MemorySnapshot{};
}
#endif

} // anonymous namespace

// =============================================================================
//  PerfConfig
// =============================================================================
PerfConfig::PerfConfig() = default;

PerfConfig& PerfConfig::instance() {
    // Meyers singleton — thread-safe in C++11+ (§6.7 [stmt.dcl]/4).
    static PerfConfig inst;
    return inst;
}

PerfLevel PerfConfig::initialize_from_env() {
    bool already = env_initialized_.exchange(true, std::memory_order_acq_rel);
    if (already) {
        // Already initialized — return current level. The first call wins.
        return level();
    }

    // Read LEXORA_PERF_LEVEL.
    int parsed = 0;
    if (const char* env = std::getenv("LEXORA_PERF_LEVEL")) {
        std::string_view sv = trim(env);
        if (!sv.empty()) {
            // Accept "0" / "1" / "2" or case-insensitive "off"/"basic"/"verbose".
            if (sv.size() == 1 && sv[0] >= '0' && sv[0] <= '2') {
                parsed = sv[0] - '0';
            } else {
                std::string lower;
                lower.reserve(sv.size());
                for (char c : sv) lower.push_back(
                    static_cast<char>(std::tolower(static_cast<unsigned char>(c))));
                if (lower == "off")     parsed = 0;
                else if (lower == "basic")   parsed = 1;
                else if (lower == "verbose") parsed = 2;
            }
        }
    }
    level_.store(parsed, std::memory_order_relaxed);

    // Read LEXORA_PERF_LOG (optional).
    if (const char* env = std::getenv("LEXORA_PERF_LOG")) {
        std::string_view sv = trim(env);
        if (!sv.empty()) {
            // Lock-ordering contract: file_mu_ before path_mu_ when both
            // held (see write_line). Here we update the path under path_mu_
            // alone, then close the file under file_mu_ alone — never both.
            {
                std::lock_guard<std::mutex> lk(path_mu_);
                log_path_ = std::string(sv);
            }
            std::lock_guard<std::mutex> flk(file_mu_);
            if (file_handle_) {
                auto* ofs = static_cast<std::ofstream*>(file_handle_);
                ofs->close();
                delete ofs;
                file_handle_ = nullptr;
            }
            file_open_failed_.store(false, std::memory_order_relaxed);
        }
    }
    return level();
}

std::string PerfConfig::log_path() const {
    std::lock_guard<std::mutex> lk(path_mu_);
    return log_path_;
}

void PerfConfig::set_log_path(std::string path) {
    // Lock-ordering contract: file_mu_ is always acquired BEFORE path_mu_
    // when both are held (see write_line). Here we update the path under
    // path_mu_ alone, then close the file under file_mu_ alone — never
    // holding both simultaneously. This avoids the classic A-B / B-A
    // deadlock between set_log_path() and write_line().
    {
        std::lock_guard<std::mutex> lk(path_mu_);
        log_path_ = std::move(path);
    }
    {
        std::lock_guard<std::mutex> flk(file_mu_);
        if (file_handle_) {
            auto* ofs = static_cast<std::ofstream*>(file_handle_);
            ofs->close();
            delete ofs;
            file_handle_ = nullptr;
        }
        file_open_failed_.store(false, std::memory_order_relaxed);
    }
}

void PerfConfig::write_line(std::string_view json_line) const {
    if (!should_record()) return;
    if (json_line.empty()) return;

    // Lazy-open the file on first write (so a binary that never instruments
    // never touches the filesystem).
    std::lock_guard<std::mutex> lk(file_mu_);
    if (!file_handle_) {
        if (file_open_failed_.load(std::memory_order_relaxed)) return;
        std::string path;
        { // acquire path_mu_ only briefly to copy out the string.
            std::lock_guard<std::mutex> plk(path_mu_);
            path = log_path_;
        }
        auto* ofs = new std::ofstream();
        // Open in append mode so multiple processes (or restarts within the
        // same process) accumulate into one log. Use explicit binary + ate
        // to seek to end on open.
        ofs->open(path, std::ios::out | std::ios::app | std::ios::ate);
        if (!*ofs) {
            file_open_failed_.store(true, std::memory_order_relaxed);
            delete ofs;
            // Emit a one-shot stderr notice so the developer knows perf
            // logging is broken (subsequent writes are then silently no-op'd
            // to avoid log spam on every measurement).
            std::fprintf(stderr,
                "[lexora] PerfConfig: failed to open '%s' for writing — "
                "perf events will be dropped.\n", path.c_str());
            return;
        }
        file_handle_ = ofs;
        // Write a header marker so the log is self-describing.
        *ofs << "{\"ts\": " << now_epoch_ms()
             << ", \"type\": \"_perf_log_open\", \"data\": {\"path\": \""
             << json_escape(path) << "\", \"level\": "
             << level_.load(std::memory_order_relaxed) << "}}\n";
        ofs->flush();
    }
    auto* ofs = static_cast<std::ofstream*>(file_handle_);
    *ofs << json_line;
    if (json_line.back() != '\n') *ofs << '\n';
    ofs->flush();   // small files + frequent flush = OK for perf log volume.
}

void PerfConfig::write_event(std::string_view type,
                             std::string_view json_body) const {
    if (!should_record()) return;
    // Build: {"ts": <epoch_ms>, "type": "<type>", "data": <body>}
    std::string line;
    line.reserve(64 + type.size() + json_body.size());
    line += "{\"ts\": ";
    line += std::to_string(now_epoch_ms());
    line += ", \"type\": \"";
    line += json_escape(type);
    line += "\", \"data\": ";
    line += json_body;
    line += "}";
    write_line(line);
}

// =============================================================================
//  ScopedTimer
// =============================================================================
ScopedTimer::ScopedTimer(std::string name, std::string tags_json)
    : name_(std::move(name)),
      tags_(std::move(tags_json)),
      start_(now_steady()),
      armed_(true) {
    // Force env initialization on first ScopedTimer use so the user doesn't
    // have to call PerfConfig::initialize_from_env() explicitly. Cheap if
    // already initialized (one atomic exchange).
    PerfConfig::instance().initialize_from_env();
}

ScopedTimer::~ScopedTimer() {
    if (armed_) {
        emit();
    }
}

double ScopedTimer::elapsed_us() const noexcept {
    return elapsed_us_between(start_, now_steady());
}

void ScopedTimer::emit() noexcept {
    if (!armed_) return;
    armed_ = false;
    auto& cfg = PerfConfig::instance();
    if (!cfg.should_record()) return;
    double us = elapsed_us_between(start_, now_steady());
    // Build JSON data object:
    //   {"name": "<name>", "elapsed_us": <us><, "tags": <tags> if non-empty>}
    std::string body;
    body.reserve(64 + name_.size() + tags_.size());
    body += "{\"name\": \"";
    body += json_escape(name_);
    body += "\", \"elapsed_us\": ";
    // Format with 3 decimal places (microsecond precision).
    {
        char buf[32];
        std::snprintf(buf, sizeof(buf), "%.3f", us);
        body += buf;
    }
    if (!tags_.empty()) {
        body += ", \"tags\": ";
        // tags_ is already a JSON object fragment — but to be safe we wrap
        // it as a string if it doesn't start with '{'.
        if (!tags_.empty() && tags_.front() == '{') {
            body += tags_;
        } else {
            body += "\"";
            body += json_escape(tags_);
            body += "\"";
        }
    }
    body += "}";
    cfg.write_event("scoped_timer", body);
}

// =============================================================================
//  compute_latency_stats_us
// =============================================================================
LatencyStats compute_latency_stats_us(std::vector<double>& samples_us) {
    LatencyStats s;
    s.count = samples_us.size();
    if (samples_us.empty()) return s;
    std::sort(samples_us.begin(), samples_us.end());
    s.min_us = samples_us.front();
    s.max_us = samples_us.back();
    double sum = 0.0;
    for (double v : samples_us) sum += v;
    s.mean_us = sum / static_cast<double>(samples_us.size());
    // Population standard deviation.
    double sq_sum = 0.0;
    for (double v : samples_us) {
        double d = v - s.mean_us;
        sq_sum += d * d;
    }
    s.stddev_us = std::sqrt(sq_sum / static_cast<double>(samples_us.size()));
    // Percentiles via nearest-rank method (standard for latency reporting).
    auto pct = [&](double p) -> double {
        if (samples_us.empty()) return 0.0;
        // p in [0, 100]. Rank = ceil(p/100 * n) - 1, clamped to [0, n-1].
        size_t n = samples_us.size();
        size_t rank = static_cast<size_t>(std::ceil((p / 100.0) * static_cast<double>(n)));
        if (rank == 0) rank = 1;
        rank -= 1;
        if (rank >= n) rank = n - 1;
        return samples_us[rank];
    };
    s.p50_us = pct(50.0);
    s.p95_us = pct(95.0);
    s.p99_us = pct(99.0);
    return s;
}

// =============================================================================
//  LookupBenchmark
// =============================================================================
LatencyStats LookupBenchmark::run(LookupFn fn,
                                  uint64_t iterations,
                                  uint64_t warmup) {
    // Warmup phase — discard timings.
    for (uint64_t i = 0; i < warmup; ++i) {
        fn(i);
    }
    // Measurement phase.
    std::vector<double> samples;
    samples.reserve(static_cast<size_t>(iterations));
    for (uint64_t i = 0; i < iterations; ++i) {
        auto t0 = now_steady();
        fn(i);
        auto t1 = now_steady();
        samples.push_back(elapsed_us_between(t0, t1));
    }
    return compute_latency_stats_us(samples);
}

LatencyStats LookupBenchmark::run_and_log(LookupFn fn,
                                          uint64_t iterations,
                                          std::string_view benchmark_name,
                                          uint64_t warmup) {
    auto& cfg = PerfConfig::instance();
    cfg.initialize_from_env();

    LatencyStats stats = run(fn, iterations, warmup);

    if (cfg.should_record()) {
        std::string body;
        body.reserve(256 + benchmark_name.size());
        body += "{\"name\": \"";
        body += json_escape(benchmark_name);
        body += "\", \"iterations\": ";
        body += std::to_string(iterations);
        body += ", \"warmup\": ";
        body += std::to_string(warmup);
        char buf[256];
        std::snprintf(buf, sizeof(buf),
            ", \"count\": %zu, \"min_us\": %.3f, \"max_us\": %.3f, "
            "\"mean_us\": %.3f, \"stddev_us\": %.3f, "
            "\"p50_us\": %.3f, \"p95_us\": %.3f, \"p99_us\": %.3f",
            stats.count, stats.min_us, stats.max_us,
            stats.mean_us, stats.stddev_us,
            stats.p50_us, stats.p95_us, stats.p99_us);
        body += buf;
        body += "}";
        cfg.write_event("lookup_benchmark", body);
    }
    return stats;
}

// =============================================================================
//  MemoryTracker
// =============================================================================
MemoryTracker::MemoryTracker() = default;
MemoryTracker::~MemoryTracker() = default;

MemorySnapshot MemoryTracker::sample(std::string_view label) {
    MemorySnapshot snap = read_process_memory();
    // Update peak trackers (relaxed atomics — we don't need cross-field
    // consistency, just monotonic-ish growth).
    uint64_t cur_rss = peak_rss_.load(std::memory_order_relaxed);
    while (snap.rss_bytes > cur_rss &&
           !peak_rss_.compare_exchange_weak(cur_rss, snap.rss_bytes,
                                             std::memory_order_relaxed)) {
        // cur_rss was reloaded by CAS failure — loop.
    }
    uint64_t cur_heap = peak_heap_.load(std::memory_order_relaxed);
    while (snap.heap_bytes > cur_heap &&
           !peak_heap_.compare_exchange_weak(cur_heap, snap.heap_bytes,
                                              std::memory_order_relaxed)) {
        // cur_heap was reloaded — loop.
    }
    // Augment snapshot with the tracker's peak (caller may want it).
    snap.peak_rss_bytes = peak_rss_.load(std::memory_order_relaxed);
    log_snapshot(label, snap);
    return snap;
}

void MemoryTracker::reset_peak() noexcept {
    peak_rss_.store(0, std::memory_order_relaxed);
    peak_heap_.store(0, std::memory_order_relaxed);
}

MemorySnapshot MemoryTracker::process_memory() {
    return read_process_memory();
}

void MemoryTracker::log_snapshot(std::string_view label,
                                  const MemorySnapshot& snap) {
    auto& cfg = PerfConfig::instance();
    if (!cfg.should_record()) return;
    std::string body;
    body.reserve(128 + label.size());
    body += "{\"label\": \"";
    body += json_escape(label);
    body += "\", \"rss_bytes\": ";
    body += std::to_string(snap.rss_bytes);
    body += ", \"heap_bytes\": ";
    body += std::to_string(snap.heap_bytes);
    body += ", \"virtual_bytes\": ";
    body += std::to_string(snap.virtual_bytes);
    body += ", \"peak_rss_bytes\": ";
    body += std::to_string(snap.peak_rss_bytes);
    body += "}";
    cfg.write_event("memory", body);
}

// =============================================================================
//  FrameTimer
// =============================================================================
FrameTimer::FrameTimer(std::string screen_name, double frame_budget_ms)
    : screen_name_(std::move(screen_name)),
      frame_budget_ms_(frame_budget_ms) {
    buffer_.reserve(BUFFER_CAPACITY);
    // Force env initialization so the level is settled before any frame
    // is recorded.
    PerfConfig::instance().initialize_from_env();
}

FrameTimer::~FrameTimer() {
    // Emit a final report if there are unreported frames. This is best-effort
    // — if the destructor runs during process teardown, the file may already
    // be closed; write_event handles that gracefully (returns silently).
    if (total_frames_.load(std::memory_order_relaxed) > 0) {
        report();
    }
}

void FrameTimer::frame_begin() {
    frame_start_ = now_steady();
    frame_in_progress_ = true;
}

void FrameTimer::frame_end() {
    if (!frame_in_progress_) {
        // frame_end without frame_begin — treat as no-op (defensive).
        return;
    }
    frame_in_progress_ = false;
    double us = elapsed_us_between(frame_start_, now_steady());
    record_internal(us / 1000.0);  // convert to ms
}

void FrameTimer::record_frame(double render_ms) {
    record_internal(render_ms);
}

void FrameTimer::record_internal(double render_ms) {
    total_frames_.fetch_add(1, std::memory_order_relaxed);
    bool is_jank = render_ms > frame_budget_ms_;
    if (is_jank) {
        dropped_frames_.fetch_add(1, std::memory_order_relaxed);
    }
    // Push to ring buffer (under mutex — never contended at 60 fps).
    {
        std::lock_guard<std::mutex> lk(buffer_mu_);
        if (buffer_.size() < BUFFER_CAPACITY) {
            buffer_.push_back(render_ms);
        } else {
            buffer_[buffer_head_] = render_ms;
            buffer_head_ = (buffer_head_ + 1) & (BUFFER_CAPACITY - 1);
            buffer_full_ = true;
        }
    }
    // Per-jank verbose log.
    if (is_jank) {
        auto& cfg = PerfConfig::instance();
        if (cfg.should_log_jank()) {
            std::string body;
            body.reserve(128 + screen_name_.size());
            body += "{\"screen\": \"";
            body += json_escape(screen_name_);
            body += "\", \"render_ms\": ";
            char buf[32];
            std::snprintf(buf, sizeof(buf), "%.3f", render_ms);
            body += buf;
            body += ", \"budget_ms\": ";
            std::snprintf(buf, sizeof(buf), "%.3f", frame_budget_ms_);
            body += buf;
            body += "}";
            cfg.write_event("frame_jank", body);
        }
    }
}

FrameStats FrameTimer::snapshot() const {
    FrameStats st;
    st.screen_name      = screen_name_;
    st.frame_budget_ms  = frame_budget_ms_;
    st.total_frames     = total_frames_.load(std::memory_order_relaxed);
    st.dropped_frames   = dropped_frames_.load(std::memory_order_relaxed);
    if (st.total_frames == 0) return st;
    st.jank_percent     = 100.0 * static_cast<double>(st.dropped_frames) /
                          static_cast<double>(st.total_frames);
    // Copy + sort the ring buffer for percentile computation.
    std::vector<double> samples;
    {
        std::lock_guard<std::mutex> lk(buffer_mu_);
        if (buffer_full_) {
            // Buffer is wrapping — reconstruct in-order: start from head,
            // wrap around. The samples are not chronological after sort
            // anyway, so this is only relevant if we wanted a timeline.
            samples.reserve(BUFFER_CAPACITY);
            samples.insert(samples.end(),
                           buffer_.begin() + buffer_head_, buffer_.end());
            samples.insert(samples.end(),
                           buffer_.begin(), buffer_.begin() + buffer_head_);
        } else {
            samples = buffer_;
        }
    }
    if (samples.empty()) return st;
    std::sort(samples.begin(), samples.end());
    st.min_ms  = samples.front();
    st.max_ms  = samples.back();
    double sum = 0.0;
    for (double v : samples) sum += v;
    st.mean_ms = sum / static_cast<double>(samples.size());
    auto pct = [&](double p) -> double {
        size_t n = samples.size();
        size_t rank = static_cast<size_t>(
            std::ceil((p / 100.0) * static_cast<double>(n)));
        if (rank == 0) rank = 1;
        rank -= 1;
        if (rank >= n) rank = n - 1;
        return samples[rank];
    };
    st.p50_ms = pct(50.0);
    st.p95_ms = pct(95.0);
    st.p99_ms = pct(99.0);
    return st;
}

void FrameTimer::report() {
    auto& cfg = PerfConfig::instance();
    FrameStats st = snapshot();
    if (cfg.should_record() && st.total_frames > 0) {
        std::string body;
        body.reserve(256 + screen_name_.size());
        body += "{\"screen\": \"";
        body += json_escape(screen_name_);
        body += "\", \"total_frames\": ";
        body += std::to_string(st.total_frames);
        body += ", \"dropped_frames\": ";
        body += std::to_string(st.dropped_frames);
        body += ", \"jank_percent\": ";
        char buf[256];
        std::snprintf(buf, sizeof(buf), "%.4f", st.jank_percent);
        body += buf;
        std::snprintf(buf, sizeof(buf),
            ", \"budget_ms\": %.3f, \"min_ms\": %.3f, \"max_ms\": %.3f, "
            "\"mean_ms\": %.3f, \"p50_ms\": %.3f, \"p95_ms\": %.3f, \"p99_ms\": %.3f",
            st.frame_budget_ms, st.min_ms, st.max_ms, st.mean_ms,
            st.p50_ms, st.p95_ms, st.p99_ms);
        body += buf;
        body += "}";
        cfg.write_event("frame_stats", body);
    }
    reset();
}

void FrameTimer::reset() noexcept {
    total_frames_.store(0, std::memory_order_relaxed);
    dropped_frames_.store(0, std::memory_order_relaxed);
    std::lock_guard<std::mutex> lk(buffer_mu_);
    buffer_.clear();
    buffer_head_ = 0;
    buffer_full_ = false;
}

} // namespace lexora
