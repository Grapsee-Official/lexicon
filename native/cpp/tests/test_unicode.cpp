// test_unicode.cpp — Google Test for Unicode NFC normalization
#include "lexicon/unicode.hpp"
#include <gtest/gtest.h>

using namespace lexicon::unicode;

TEST(UnicodeTest, AsciiPassthrough) {
    EXPECT_EQ(normalize_nfc("hello"), "hello");
    EXPECT_EQ(normalize_nfc(""), "");
    EXPECT_EQ(normalize_nfc("12345"), "12345");
}

TEST(UnicodeTest, AlreadyNFC) {
    std::string s = "café";
    EXPECT_EQ(normalize_nfc(s), s);
    EXPECT_TRUE(is_nfc(s));
}

TEST(UnicodeTest, NFDToNFC) {
    // "café" decomposed: c-a-f-e-U+0301
    std::string nfd = "cafe\xCC\x81"; // e + combining acute
    std::string nfc = normalize_nfc(nfd);
    EXPECT_EQ(nfc, "café"); // should be precomposed é
    EXPECT_TRUE(is_nfc(nfc));
}

TEST(UnicodeTest, VietnameseNFC) {
    // Vietnamese "tiếng" — precomposed form
    std::string precomposed = "ti\xE1\xBA\xBFng"; // tiếng (U+1EBF)
    EXPECT_TRUE(is_nfc(precomposed));
    EXPECT_EQ(normalize_nfc(precomposed), precomposed);
}

TEST(UnicodeTest, VietnameseDecomposedToNFC) {
    // "tiếng" decomposed → should compose back
    std::string decomposed = "tie\xCC\x82\xCC\x81ng"; // e + combining circumflex + combining acute
    std::string composed = normalize_nfc(decomposed);
    // The composed form should have the precomposed ế (U+1EBF)
    EXPECT_EQ(composed, "ti\xE1\xBA\xBFng");
}

TEST(UnicodeTest, GermanUmlaut) {
    std::string nfd = "u\xCC\x88"; // u + combining diaeresis
    std::string nfc = normalize_nfc(nfd);
    EXPECT_EQ(nfc, "ü");
}

TEST(UnicodeTest, StripDiacritics) {
    EXPECT_EQ(strip_diacritics("café"), "cafe");
    EXPECT_EQ(strip_diacritics("über"), "uber");
    EXPECT_EQ(strip_diacritics("naïve"), "naive");
    EXPECT_EQ(strip_diacritics("hello"), "hello");
}

TEST(UnicodeTest, StripVietnameseDiacritics) {
    // "tiếng Việt" → "tieng Viet"
    std::string stripped = strip_diacritics("tiếng");
    EXPECT_EQ(stripped, "tieng");
}

TEST(UnicodeTest, Casefold) {
    EXPECT_EQ(casefold("HELLO"), "hello");
    EXPECT_EQ(casefold("Hello"), "hello");
    EXPECT_EQ(casefold("hello"), "hello");
}

TEST(UnicodeTest, CasefoldTurkishI) {
    // Turkish İ (U+0130) should casefold to i
    EXPECT_EQ(casefold("İstanbul"), "istanbul");
}

TEST(UnicodeTest, IsCombiningMark) {
    EXPECT_TRUE(is_combining_mark(0x0300));  // combining grave
    EXPECT_TRUE(is_combining_mark(0x0301));  // combining acute
    EXPECT_TRUE(is_combining_mark(0x0302));  // combining circumflex
    EXPECT_FALSE(is_combining_mark(0x0041)); // A
    EXPECT_FALSE(is_combining_mark(0x0061)); // a
}

TEST(UnicodeTest, BaseChar) {
    EXPECT_EQ(base_char(0x00E9), static_cast<char32_t>(0x0065)); // é → e
    EXPECT_EQ(base_char(0x00FC), static_cast<char32_t>(0x0075)); // ü → u
    EXPECT_EQ(base_char(0x0041), static_cast<char32_t>(0x0041)); // A → A (no change)
}

TEST(UnicodeTest, NFCIdempotent) {
    std::string s = "tiếng Việt Nam";
    std::string nfc1 = normalize_nfc(s);
    std::string nfc2 = normalize_nfc(nfc1);
    EXPECT_EQ(nfc1, nfc2);
}

TEST(UnicodeTest, UTF8RoundTrip) {
    std::string s = "héllo wörld tiếng";
    auto u32 = utf8_to_utf32(s);
    std::string back = utf32_to_utf8(u32);
    EXPECT_EQ(s, back);
}
