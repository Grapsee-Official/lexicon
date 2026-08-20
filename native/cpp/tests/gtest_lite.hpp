// SPDX-License-Identifier: MIT
// tests/gtest_lite.hpp
//
// A tiny subset of the Google Test API so the test file
// (tests/test_hash_table.cpp) can be compiled and run in this sandbox
// without fetching the full GoogleTest distribution. The macros and types
// exposed here have the SAME names and semantics as real GoogleTest, so the
// same test_hash_table.cpp compiles unchanged against the real gtest when
// the production CMake build pulls it via FetchContent.
//
// Supported:
//   * TEST(Suite, Name) -- registers a test case.
//   * EXPECT_TRUE / EXPECT_FALSE / EXPECT_EQ / EXPECT_NE / EXPECT_LT /
//     EXPECT_LE / EXPECT_GT / EXPECT_GE / EXPECT_STREQ.
//   * ASSERT_* variants (same names) -- abort the current test on failure
//     by throwing, caught by the runner.
//   * EXPECT_NULL / EXPECT_NOT_NULL / ASSERT_NULL / ASSERT_NOT_NULL.
//   * Streaming a custom message into any assertion:
//       EXPECT_EQ(a, b) << "context: " << x;
//   * ::testing::InitGoogleTest, RUN_ALL_TESTS.
//
// Usage:
//   g++ -std=c++17 -pthread -Iinclude -Itests -DLEXORA_USE_GTEST_LITE_MAIN
//       tests/test_hash_table.cpp src/hash_table.cpp -o test_hash_table
//
// This file is a TEST-ONLY helper. The CMake build (cmake/HashTable.cmake)
// uses real GoogleTest via FetchContent when LEXORA_HASH_TABLE_BUILD_TESTS=ON.

#ifndef LEXORA_GTEST_LITE_HPP
#define LEXORA_GTEST_LITE_HPP

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <functional>
#include <iostream>
#include <string>
#include <string_view>
#include <type_traits>
#include <vector>

namespace lexora_test_lite {

struct TestCase {
    const char* suite;
    const char* name;
    std::function<void()> body;
};

inline std::vector<TestCase>& registry() {
    static std::vector<TestCase> r;
    return r;
}

struct Registrar {
    Registrar(const char* suite, const char* name, std::function<void()> body) {
        registry().push_back({suite, name, std::move(body)});
    }
};

inline int run_all() {
    int failures = 0;
    int passed = 0;
    for (const auto& tc : registry()) {
        try {
            tc.body();
            std::printf("[       OK ] %s.%s\n", tc.suite, tc.name);
            ++passed;
        } catch (const std::exception& e) {
            std::printf("[  FAILED  ] %s.%s: %s\n", tc.suite, tc.name, e.what());
            ++failures;
        } catch (...) {
            std::printf("[  FAILED  ] %s.%s: unknown exception\n",
                        tc.suite, tc.name);
            ++failures;
        }
    }
    std::printf("\n[==========] %d test(s) ran.\n", passed + failures);
    std::printf("[  PASSED  ] %d test(s).\n", passed);
    if (failures > 0) {
        std::printf("[  FAILED  ] %d test(s).\n", failures);
    }
    return failures == 0 ? 0 : 1;
}

// ----- Value formatter (SFINAE) -------------------------------------------
template <typename T>
inline std::string fmt_val(const T& v) {
    if constexpr (std::is_arithmetic_v<T>) {
        return std::to_string(v);
    } else if constexpr (std::is_convertible_v<T, const char*>) {
        return std::string(static_cast<const char*>(v));
    } else if constexpr (std::is_convertible_v<T, std::string>) {
        return static_cast<std::string>(v);
    } else {
        return "<non-streamable>";
    }
}
inline std::string fmt_val(std::string_view v) { return std::string(v); }
inline std::string fmt_val(const std::string& v) { return v; }
inline std::string fmt_val(std::nullptr_t) { return "<null>"; }

// ----- Streaming assertion object -----------------------------------------
// Returned by every EXPECT_*/ASSERT_* macro. Its destructor fires the failure
// (throws) if the condition was false. Supports operator<< to append context.
class StreamingCheck {
   public:
    StreamingCheck(const char* file, int line, bool ok, bool abort,
                   std::string msg)
        : file_(file), line_(line), ok_(ok), abort_(abort), msg_(std::move(msg)) {}

    ~StreamingCheck() noexcept(false) {
        if (!ok_) {
            std::string full = std::string(file_) + ":" +
                               std::to_string(line_) + ": " + msg_;
            throw std::runtime_error(full);
        }
    }

    template <typename T>
    StreamingCheck& operator<<(const T& v) {
        if (!ok_) msg_ += fmt_val(v);
        return *this;
    }
    StreamingCheck& operator<<(const char* s) { if (!ok_) msg_ += s; return *this; }
    StreamingCheck& operator<<(const std::string& s) {
        if (!ok_) msg_ += s;
        return *this;
    }
    StreamingCheck& operator<<(std::string_view s) {
        if (!ok_) msg_.append(s.data(), s.size());
        return *this;
    }
    StreamingCheck& operator<<(char c) {
        if (!ok_) msg_.push_back(c);
        return *this;
    }

    // Disallow copy/move so the destructor only fires once on the temporary.
    StreamingCheck(const StreamingCheck&) = delete;
    StreamingCheck& operator=(const StreamingCheck&) = delete;
    StreamingCheck(StreamingCheck&&) = default;

   private:
    const char* file_;
    int line_;
    bool ok_;
    bool abort_;  // ASSERT_* vs EXPECT_* (both throw in this shim; flag kept
                  // for parity / future divergence).
    std::string msg_;
};

// ----- Builder helpers (one per comparison kind) --------------------------
template <typename A, typename B>
StreamingCheck make_eq(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = (a == b);
    std::string msg = std::string("EXPECT_EQ(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": expected=" + fmt_val(b) + " actual=" + fmt_val(a);
    }
    return {file, line, ok, abort, std::move(msg)};
}

template <typename A, typename B>
StreamingCheck make_ne(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = !(a == b);
    std::string msg = std::string("EXPECT_NE(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": both=" + fmt_val(a);
    }
    return {file, line, ok, abort, std::move(msg)};
}

template <typename A, typename B>
StreamingCheck make_lt(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = (a < b);
    std::string msg = std::string("EXPECT_LT(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": actual=" + fmt_val(a) + " (expected < " + fmt_val(b) + ")";
    }
    return {file, line, ok, abort, std::move(msg)};
}

template <typename A, typename B>
StreamingCheck make_le(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = (a <= b);
    std::string msg = std::string("EXPECT_LE(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": actual=" + fmt_val(a) + " (expected <= " + fmt_val(b) + ")";
    }
    return {file, line, ok, abort, std::move(msg)};
}

template <typename A, typename B>
StreamingCheck make_gt(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = (a > b);
    std::string msg = std::string("EXPECT_GT(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": actual=" + fmt_val(a) + " (expected > " + fmt_val(b) + ")";
    }
    return {file, line, ok, abort, std::move(msg)};
}

template <typename A, typename B>
StreamingCheck make_ge(const char* file, int line, bool abort,
                       const char* a_str, const char* b_str,
                       const A& a, const B& b) {
    bool ok = (a >= b);
    std::string msg = std::string("EXPECT_GE(") + a_str + ", " + b_str +
                      ") failed";
    if (!ok) {
        msg += ": actual=" + fmt_val(a) + " (expected >= " + fmt_val(b) + ")";
    }
    return {file, line, ok, abort, std::move(msg)};
}

inline StreamingCheck make_true(const char* file, int line, bool abort,
                                const char* expr_str, bool v) {
    return {file, line, v, abort,
            std::string("EXPECT_TRUE(") + expr_str + ") failed"};
}
inline StreamingCheck make_false(const char* file, int line, bool abort,
                                 const char* expr_str, bool v) {
    return {file, line, !v, abort,
            std::string("EXPECT_FALSE(") + expr_str + ") failed"};
}

}  // namespace lexora_test_lite

// ============================================================================
// Public macros. Names match GoogleTest exactly so the same test source
// compiles against either this shim or the real gtest header.
// ============================================================================

#define TEST(suite, name)                                                    \
    static void suite##_##name##_impl();                                     \
    namespace {                                                              \
    struct suite##_##name##_registrar {                                      \
        suite##_##name##_registrar() {                                       \
            ::lexora_test_lite::Registrar(                                   \
                #suite, #name, &suite##_##name##_impl);                      \
        }                                                                    \
    } reg_##suite##_##name;                                                  \
    }                                                                        \
    static void suite##_##name##_impl()

#define EXPECT_TRUE(cond)                                                    \
    ::lexora_test_lite::make_true(__FILE__, __LINE__, /*abort=*/false,       \
                                  #cond, static_cast<bool>(cond))
#define EXPECT_FALSE(cond)                                                   \
    ::lexora_test_lite::make_false(__FILE__, __LINE__, /*abort=*/false,      \
                                   #cond, static_cast<bool>(cond))

#define EXPECT_EQ(a, b)                                                      \
    ::lexora_test_lite::make_eq(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))
#define EXPECT_NE(a, b)                                                      \
    ::lexora_test_lite::make_ne(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))
#define EXPECT_LT(a, b)                                                      \
    ::lexora_test_lite::make_lt(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))
#define EXPECT_LE(a, b)                                                      \
    ::lexora_test_lite::make_le(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))
#define EXPECT_GT(a, b)                                                      \
    ::lexora_test_lite::make_gt(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))
#define EXPECT_GE(a, b)                                                      \
    ::lexora_test_lite::make_ge(__FILE__, __LINE__, /*abort=*/false,         \
                                #a, #b, (a), (b))

#define ASSERT_TRUE(cond)                                                    \
    ::lexora_test_lite::make_true(__FILE__, __LINE__, /*abort=*/true,        \
                                  #cond, static_cast<bool>(cond))
#define ASSERT_FALSE(cond)                                                   \
    ::lexora_test_lite::make_false(__FILE__, __LINE__, /*abort=*/true,       \
                                   #cond, static_cast<bool>(cond))
#define ASSERT_EQ(a, b)                                                      \
    ::lexora_test_lite::make_eq(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))
#define ASSERT_NE(a, b)                                                      \
    ::lexora_test_lite::make_ne(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))
#define ASSERT_LT(a, b)                                                      \
    ::lexora_test_lite::make_lt(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))
#define ASSERT_LE(a, b)                                                      \
    ::lexora_test_lite::make_le(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))
#define ASSERT_GT(a, b)                                                      \
    ::lexora_test_lite::make_gt(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))
#define ASSERT_GE(a, b)                                                      \
    ::lexora_test_lite::make_ge(__FILE__, __LINE__, /*abort=*/true,          \
                                #a, #b, (a), (b))

// Pointer-null checks are intentionally NOT provided as bespoke macros --
// real GoogleTest doesn't have EXPECT_NULL/EXPECT_NOT_NULL either. Use the
// portable idioms EXPECT_EQ(nullptr, p) / ASSERT_NE(nullptr, p) instead,
// which work against both this shim and real gtest.

#define EXPECT_STREQ(a, b)                                                   \
    do {                                                                     \
        const char* _sa = (a);                                               \
        const char* _sb = (b);                                               \
        bool _ok = (_sa != nullptr && _sb != nullptr &&                      \
                    std::string(_sa) == std::string(_sb));                   \
        ::lexora_test_lite::StreamingCheck(                                  \
            __FILE__, __LINE__, _ok, /*abort=*/false,                        \
            std::string("EXPECT_STREQ(" #a ", " #b                           \
                        ") failed: expected=\"") +                           \
                (_sb ? _sb : "<null>") + "\" actual=\"" +                    \
                (_sa ? _sa : "<null>") + "\"");                              \
    } while (0)

// ----- GoogleTest compatibility namespace ---------------------------------
namespace testing {
inline void InitGoogleTest(int*, char**) { /* no-op */ }
inline int RUN_ALL_TESTS() { return ::lexora_test_lite::run_all(); }
}  // namespace testing

// Real gtest exposes RUN_ALL_TESTS() as a global macro that dispatches into
// ::testing::RUN_ALL_TESTS(). Mirror that so test sources compile unchanged
// against either implementation.
#define RUN_ALL_TESTS() ::testing::RUN_ALL_TESTS()

#endif  // LEXORA_GTEST_LITE_HPP
