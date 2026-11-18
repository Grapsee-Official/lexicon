#!/usr/bin/env python3
"""Verify that Android and iOS bundle the *same* named book typography.

This prevents silent UIKit/Compose fallback caused by missing or mismatched TTFs.
It is a packaging check; final glyph-shape fidelity still needs screenshot review.
"""
from __future__ import annotations

import hashlib
import plistlib
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/font"
APPLE = ROOT / "native/ios/Lexicon/Resources"
PLIST = ROOT / "native/ios/Lexicon/Info.plist"

FONTS = {
    "PlayfairDisplay.ttf": ("playfair_display.ttf", "Playfair Display"),
    "PlayfairDisplay-Italic.ttf": ("playfair_display_italic.ttf", "Playfair Display"),
    "Inter.ttf": ("inter.ttf", "Inter"),
    "Lora.ttf": ("lora.ttf", "Lora"),
    "Lora-Italic.ttf": ("lora_italic.ttf", "Lora"),
}


def read_family_names(font: bytes) -> set[str]:
    assert len(font) > 1000, "Truncated font data"
    num_tables = struct.unpack_from(">H", font, 4)[0]
    name_table = None
    for index in range(num_tables):
        table_offset = 12 + 16 * index
        tag, _checksum, offset, length = struct.unpack_from(">4sIII", font, table_offset)
        if tag == b"name":
            name_table = font[offset:offset + length]
            break
    assert name_table is not None, "Font contains no SFNT name table"
    _fmt, count, string_offset = struct.unpack_from(">HHH", name_table, 0)
    names = set()
    for index in range(count):
        platform, _encoding, _language, name_id, length, offset = struct.unpack_from(
            ">HHHHHH", name_table, 6 + 12 * index
        )
        if name_id not in (1, 4, 6, 16):
            continue
        data = name_table[string_offset + offset:string_offset + offset + length]
        try:
            name = data.decode("utf-16-be" if platform == 3 else "latin-1").strip()
            if name:
                names.add(name)
        except UnicodeError:
            pass
    return names


def main() -> None:
    with PLIST.open("rb") as f:
        listed = plistlib.load(f).get("UIAppFonts", [])
    assert sorted(listed) == sorted(FONTS.keys()), (
        "iOS UIAppFonts list and installed font files disagree"
    )
    for apple_file, (android_file, family) in FONTS.items():
        apple = (APPLE / apple_file).read_bytes()
        android = (ANDROID / android_file).read_bytes()
        assert hashlib.sha256(apple).digest() == hashlib.sha256(android).digest(), (
            f"iOS and Android use different font binaries for {family}"
        )
        internal_names = read_family_names(apple)
        assert any(family.casefold() in item.casefold() for item in internal_names), (
            f"{apple_file} does not advertise the required family '{family}': {internal_names}"
        )
        print(f"PASS {apple_file} = {android_file}: {family}; SHA256 identical")
    print("Verified: five font files, shared cross-platform glyph sources, iOS registrations")


if __name__ == "__main__":
    main()
