// unicode.hpp — Unicode NFC normalization for Lexora
// C++17, no external deps (no ICU). Embedded Unicode 15.1 data.
#pragma once

#include <string>
#include <string_view>

namespace lexora::unicode {

// Normalize to NFC (Canonical Composition)
// Input: UTF-8 string. Output: NFC-normalized UTF-8 string.
std::string normalize_nfc(std::string_view input);

// Normalize to NFD (Canonical Decomposition)
std::string normalize_nfd(std::string_view input);

// Check if string is already in NFC form
bool is_nfc(std::string_view input);

// Casefold (for case-insensitive comparison)
std::string casefold(std::string_view input);

// Strip diacritics (for diacritic-insensitive search fallback)
// e.g., "tiếng" → "tieng", "über" → "uber"
std::string strip_diacritics(std::string_view input);

// Get the base character (without diacritic)
// e.g., 'é' → 'e', 'ü' → 'u', 'ọ' → 'o'
char32_t base_char(char32_t cp);

// Check if a code point is a combining mark
bool is_combining_mark(char32_t cp);

// UTF-8 helpers
std::u32string utf8_to_utf32(std::string_view s);
std::string utf32_to_utf8(const std::u32string& s);

} // namespace lexora::unicode
