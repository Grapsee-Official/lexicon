// sanity_check.cpp — comprehensive coverage matching tests/test_unicode.cpp
#include "lexora/unicode.hpp"
#include <cstdio>
#include <string>

using namespace lexora::unicode;

static int failures = 0;
static int passes = 0;

#define CHECK_EQ(actual, expected, msg)                                        \
    do {                                                                       \
        std::string a = (actual);                                              \
        std::string e = (expected);                                            \
        if (a != e) {                                                          \
            std::fprintf(stderr, "FAIL: %s\n  actual:   ", (msg));             \
            for (unsigned char c : a) std::fprintf(stderr, "%02X ", c);        \
            std::fprintf(stderr, "\n  expected: ");                            \
            for (unsigned char c : e) std::fprintf(stderr, "%02X ", c);        \
            std::fprintf(stderr, "\n");                                        \
            ++failures;                                                        \
        } else { ++passes; }                                                   \
    } while (0)

#define CHECK_TRUE(cond, msg)                                                  \
    do {                                                                       \
        if (!(cond)) {                                                         \
            std::fprintf(stderr, "FAIL: %s (expected true)\n", (msg));         \
            ++failures;                                                        \
        } else { ++passes; }                                                   \
    } while (0)

#define CHECK_FALSE(cond, msg)                                                 \
    do {                                                                       \
        if ((cond)) {                                                          \
            std::fprintf(stderr, "FAIL: %s (expected false)\n", (msg));        \
            ++failures;                                                        \
        } else { ++passes; }                                                   \
    } while (0)

int main() {
    // ---- mirror tests/test_unicode.cpp exactly ----

    // AsciiPassthrough
    CHECK_EQ(normalize_nfc("hello"), "hello", "AsciiPassthrough hello");
    CHECK_EQ(normalize_nfc(""), "", "AsciiPassthrough empty");
    CHECK_EQ(normalize_nfc("12345"), "12345", "AsciiPassthrough digits");

    // AlreadyNFC
    {
        std::string s = "café";
        CHECK_EQ(normalize_nfc(s), s, "AlreadyNFC normalize");
        CHECK_TRUE(is_nfc(s), "AlreadyNFC is_nfc");
    }

    // NFDToNFC
    {
        std::string nfd = "cafe\xCC\x81";
        std::string nfc = normalize_nfc(nfd);
        CHECK_EQ(nfc, "café", "NFDToNFC");
        CHECK_TRUE(is_nfc(nfc), "NFDToNFC is_nfc");
    }

    // VietnameseNFC
    {
        std::string precomposed = "ti\xE1\xBA\xBFng";
        CHECK_TRUE(is_nfc(precomposed), "VietnameseNFC is_nfc");
        CHECK_EQ(normalize_nfc(precomposed), precomposed, "VietnameseNFC normalize");
    }

    // VietnameseDecomposedToNFC
    {
        std::string decomposed = "tie\xCC\x82\xCC\x81ng";
        std::string composed = normalize_nfc(decomposed);
        CHECK_EQ(composed, "ti\xE1\xBA\xBFng", "VietnameseDecomposedToNFC");
    }

    // GermanUmlaut
    {
        std::string nfd = "u\xCC\x88";
        std::string nfc = normalize_nfc(nfd);
        CHECK_EQ(nfc, "ü", "GermanUmlaut");
    }

    // StripDiacritics
    CHECK_EQ(strip_diacritics("café"), "cafe", "StripDiacritics cafe");
    CHECK_EQ(strip_diacritics("über"), "uber", "StripDiacritics uber");
    CHECK_EQ(strip_diacritics("naïve"), "naive", "StripDiacritics naive");
    CHECK_EQ(strip_diacritics("hello"), "hello", "StripDiacritics hello");

    // StripVietnameseDiacritics
    CHECK_EQ(strip_diacritics("tiếng"), "tieng", "StripVietnameseDiacritics");

    // Casefold
    CHECK_EQ(casefold("HELLO"), "hello", "Casefold HELLO");
    CHECK_EQ(casefold("Hello"), "hello", "Casefold Hello");
    CHECK_EQ(casefold("hello"), "hello", "Casefold hello");

    // CasefoldTurkishI
    CHECK_EQ(casefold("İstanbul"), "istanbul", "CasefoldTurkishI");

    // IsCombiningMark
    CHECK_TRUE(is_combining_mark(0x0300), "IsCombiningMark 0x0300");
    CHECK_TRUE(is_combining_mark(0x0301), "IsCombiningMark 0x0301");
    CHECK_TRUE(is_combining_mark(0x0302), "IsCombiningMark 0x0302");
    CHECK_FALSE(is_combining_mark(0x0041), "IsCombiningMark A");
    CHECK_FALSE(is_combining_mark(0x0061), "IsCombiningMark a");

    // BaseChar
    {
        EXPECT_BASE_EQ:
        // é → e, ü → u, A → A
        char32_t r1 = base_char(0x00E9); CHECK_TRUE(r1 == 0x0065, "BaseChar e-acute -> e");
        char32_t r2 = base_char(0x00FC); CHECK_TRUE(r2 == 0x0075, "BaseChar u-diaeresis -> u");
        char32_t r3 = base_char(0x0041); CHECK_TRUE(r3 == 0x0041, "BaseChar A -> A");
        // New: ế → e (recursive)
        char32_t r4 = base_char(0x1EBF); CHECK_TRUE(r4 == 0x0065, "BaseChar e-circumflex-acute -> e (recursive)");
        // New: ǜ → ü → u (recursive: ǜ = ü + grave, ü = u + diaeresis)
        char32_t r5 = base_char(0x01DC); CHECK_TRUE(r5 == 0x0075, "BaseChar u-diaeresis-grave -> u (recursive)");
    }

    // NFCIdempotent
    {
        std::string s = "tiếng Việt Nam";
        std::string nfc1 = normalize_nfc(s);
        std::string nfc2 = normalize_nfc(nfc1);
        CHECK_EQ(nfc1, nfc2, "NFCIdempotent");
        CHECK_TRUE(is_nfc(nfc1), "NFCIdempotent is_nfc");
    }

    // UTF8RoundTrip
    {
        std::string s = "héllo wörld tiếng";
        auto u32 = utf8_to_utf32(s);
        std::string back = utf32_to_utf8(u32);
        CHECK_EQ(back, s, "UTF8RoundTrip");
    }

    // ---- new ranges ----

    // Latin Extended-A: Ā roundtrip
    {
        std::string nfc = "\xC4\x80";
        std::string nfd = normalize_nfd(nfc);
        CHECK_EQ(nfd, "A\xCC\x84", "Latin Ext-A: A-macron NFD");
        CHECK_EQ(normalize_nfc(nfd), nfc, "Latin Ext-A: A-macron roundtrip");
    }

    // Latin Extended-B recursive: Ǭ
    {
        std::string deep_nfd = "O\xCC\xA8\xCC\x84";
        CHECK_EQ(normalize_nfc(deep_nfd), "\xC7\xAC",
                 "Latin Ext-B: O-ogonek-macron deep NFC");
    }

    // Greek recursive: ΐ
    {
        std::string deep = "\xCE\xB9\xCC\x88\xCC\x81";
        CHECK_EQ(normalize_nfc(deep), "\xCE\x90",
                 "Greek: iota-diaeresis-tonos deep NFC");
    }

    // Cyrillic recursive: Ӹ
    {
        std::string deep = "\xD0\xAB\xCC\x88";
        CHECK_EQ(normalize_nfc(deep), "\xD3\xB8",
                 "Cyrillic: Yeru-diaeresis deep NFC");
    }

    std::printf("\n%d passed, %d failed\n", passes, failures);
    return failures == 0 ? 0 : 1;
}
