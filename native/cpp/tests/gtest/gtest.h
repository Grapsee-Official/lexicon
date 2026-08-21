// SPDX-License-Identifier: MIT
// tests/gtest/gtest.h
//
// Minimal compile-only stub of the Google Test (gtest) public API.
//
// Lets the test sources under tests/ (which `#include <gtest/gtest.h>`)
// compile in environments where the real GoogleTest distribution is not
// available — e.g. CI sandboxes without network access for FetchContent,
// or quick local syntax-check builds of the engine sources.
//
// This is NOT a real test framework. The EXPECT_*/ASSERT_* macros evaluate
// their condition expression(s) exactly once (so the compiler still
// type-checks them, and any side effects in the condition compile cleanly)
// but otherwise discard the result. There is no test registry, no runner,
// and no main() is provided by this header — linking a test binary that
// pulls only this stub will fail with "undefined reference to main" by
// design. The header exists for compile-time validation only.
//
// Production build (cmake/GoogleTest.cmake with LEXORA_GTEST_AVAILABLE=ON)
// adds the real gtest include path BEFORE this directory, so an
// `#include <gtest/gtest.h>` resolves to the real GoogleTest header and
// this stub is never seen.
//
// The macros and types below have the SAME names as real GoogleTest so
// the same test sources compile unchanged against either implementation.
#ifndef LEXORA_TESTS_GTEST_STUB_H
#define LEXORA_TESTS_GTEST_STUB_H

// ============================================================================
// ::testing::Test — fixture base class.
//
// Real gtest's ::testing::Test exposes many more members (TestInfo, Run(),
// HasFailure(), Setup() vs SetUp(), etc.). We surface only the public API
// the test sources under tests/ actually use: the virtual SetUp() and
// TearDown() hooks that fixture subclasses override, and a virtual
// destructor so fixtures with members of non-trivial types (LexoraEngine,
// std::string, ...) destroy cleanly.
// ============================================================================
namespace testing {
class Test {
   public:
    virtual ~Test() = default;
    virtual void SetUp() {}
    virtual void TearDown() {}
};
}  // namespace testing

// ============================================================================
// StreamingSink — absorbs the trailing `<< "context: " << x` message chain
// that real gtest supports on every EXPECT_*/ASSERT_* assertion. Each
// macro below evaluates its condition(s) once via static_cast<void>(...)
// and then yields a StreamingSink temporary; operator<< is a no-op that
// returns *this so chains of arbitrary length compile without effect.
// ============================================================================
namespace lexora_gtest_stub {
class StreamingSink {
   public:
    template <typename T>
    StreamingSink& operator<<(const T&) noexcept { return *this; }
};
}  // namespace lexora_gtest_stub

// ============================================================================
// TEST(suite, name) — free-standing test.
//
// Expands to an internal-linkage function definition. The body is parsed
// (so every EXPECT_*/ASSERT_* inside is type-checked) but the function is
// never invoked by any runner. [[maybe_unused]] silences -Wunused-function
// under the engine's -Wall -Wextra warning level.
// ============================================================================
#define TEST(suite, name)                                                    \
    [[maybe_unused]] static void suite##_##name##_impl()

// ============================================================================
// TEST_F(suite, name) — fixture-based test.
//
// `suite` is a user class derived publicly from ::testing::Test. The macro
// synthesises a uniquely-named subclass and emits the body as its
// TestBody() member, so the body sees the fixture's protected members via
// inheritance — exactly as real gtest does. The member is declared and
// defined (so the body compiles) but never invoked.
//
// We intentionally do NOT instantiate the synthesised subclass: doing so
// would run SetUp()/TearDown() and the body itself, which is undesirable
// for a pure compile-time check (and the body may assert preconditions
// that only hold at runtime with a real database loaded).
// ============================================================================
#define TEST_F(suite, name)                                                  \
    class suite##_##name##_Test : public suite {                             \
       public:                                                               \
        void TestBody();                                                     \
    };                                                                       \
    void suite##_##name##_Test::TestBody()

// ============================================================================
// EXPECT_* / ASSERT_* — no-op assertions.
//
// Each macro evaluates its argument(s) exactly once via static_cast<void>
// (so side-effecting conditions like `ht.lookup(...)` still compile-check
// and the operator is well-formed for lvalues, rvalues, void-returning
// calls, etc.) and then yields a StreamingSink temporary that swallows any
// trailing `<< msg` stream. There is no pass/fail recording and no
// "abort the test" behavior on ASSERT_* — there is no test runner to
// abort. The distinction between EXPECT_* and ASSERT_* is preserved only
// at the source level so test files compile unchanged against real gtest.
// ============================================================================
#define EXPECT_TRUE(cond)                                                    \
    (static_cast<void>(cond), ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_FALSE(cond)                                                   \
    (static_cast<void>(cond), ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_EQ(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_NE(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_LT(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_LE(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_GT(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define EXPECT_GE(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})

#define ASSERT_TRUE(cond)                                                    \
    (static_cast<void>(cond), ::lexora_gtest_stub::StreamingSink{})
#define ASSERT_FALSE(cond)                                                   \
    (static_cast<void>(cond), ::lexora_gtest_stub::StreamingSink{})
#define ASSERT_EQ(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})
#define ASSERT_NE(a, b)                                                      \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})

// EXPECT_STREQ — included for parity with the real gtest public API; none
// of the bundled test_*.cpp files currently use it, but it is harmless.
#define EXPECT_STREQ(a, b)                                                   \
    (static_cast<void>(a), static_cast<void>(b),                             \
     ::lexora_gtest_stub::StreamingSink{})

// ============================================================================
// ::testing::InitGoogleTest / RUN_ALL_TESTS — no-ops for parity.
//
// Real gtest's gtest_main provides int main() that calls these. The test
// sources under tests/ don't define main() themselves, so a binary built
// against only this stub will not link (no main). These declarations are
// here only so that any user-supplied main() that calls them compiles
// unchanged against either this stub or real gtest.
// ============================================================================
namespace testing {
inline void InitGoogleTest(int*, char**) {}
inline int RUN_ALL_TESTS() { return 0; }
}  // namespace testing
#define RUN_ALL_TESTS() ::testing::RUN_ALL_TESTS()

#endif  // LEXORA_TESTS_GTEST_STUB_H
