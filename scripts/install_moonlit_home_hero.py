#!/usr/bin/env python3
"""Install and verify the original repository artwork in native resources."""
from pathlib import Path
import argparse
import hashlib
import struct
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "All real art/Moonlit Cottage Storybook Vignette.png"
TARGETS = (
    ROOT / "native/android/app/src/main/res/drawable-nodpi/hero_dark.png",
    ROOT / "native/ios/Lexicon/Assets.xcassets/hero-dark.imageset/hero-dark.png",
    ROOT / "native/ios/Lexicon/Resources/hero-dark.png",
)


def dimensions(data: bytes) -> tuple[int, int]:
    if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise SystemExit("Expected a valid PNG header.")
    width, height, depth, color = struct.unpack(">IIBB", data[16:26])
    if width <= height or height < 512:
        raise SystemExit("Expected a full-resolution landscape hero.")
    if color not in (4, 6) and b"tRNS" not in data:
        raise SystemExit("Expected transparent artwork.")
    return width, height


def verify(data: bytes, original: bytes, label: str) -> None:
    dimensions(data)
    if data != original:
        raise SystemExit(f"{label}: does not match the approved repository PNG.")
    print(f"PASS {label}: SHA-256 {hashlib.sha256(data).hexdigest()}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artwork", type=Path, nargs="?", default=SOURCE)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--apk", type=Path, help="Check packaged hero resource")
    args = parser.parse_args()
    original = SOURCE.read_bytes()
    expected_size = dimensions(original)
    verify(args.artwork.read_bytes(), original, "Source artwork")
    for target in TARGETS:
        if not args.check:
            target.write_bytes(original)
        verify(target.read_bytes(), original, str(target.relative_to(ROOT)))
    if args.apk:
        with zipfile.ZipFile(args.apk) as apk:
            matches = [n for n in apk.namelist() if n.startswith("res/") and n.endswith("/hero_dark.png")]
            if len(matches) != 1:
                raise SystemExit(f"Expected exactly one dark hero PNG in APK, found {matches}")
            packaged = apk.read(matches[0])
            if dimensions(packaged) != expected_size:
                raise SystemExit("APK hero dimensions do not match the source.")
            print(f"PASS packaged hero: {matches[0]}, {expected_size[0]} x {expected_size[1]}")


if __name__ == "__main__":
    main()

# Test APK rebuild requested 2026-11-28; no runtime changes.
