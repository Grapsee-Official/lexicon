#!/usr/bin/env python3
"""Install the approved moonlit Home painting verbatim on both native platforms."""
from pathlib import Path
import argparse
import hashlib
import struct

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = "550909662f7a229d90651560b7e4256ca57896f96995e1ea8b1a2a834de2ca8c"
TARGETS = (
    ROOT / "native/android/app/src/main/res/drawable-nodpi/hero_dark.png",
    ROOT / "native/ios/Lexicon/Assets.xcassets/hero-dark.imageset/hero-dark.png",
    ROOT / "native/ios/Lexicon/Resources/hero-dark.png",
)


def verify(data: bytes) -> None:
    if hashlib.sha256(data).hexdigest() != EXPECTED:
        raise SystemExit("Not the exact approved artwork (SHA-256 mismatch).")
    if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise SystemExit("Expected a PNG.")
    width, height, depth, color = struct.unpack(">IIBB", data[16:26])
    if (width, height, depth, color) != (1536, 1024, 8, 6):
        raise SystemExit("Expected 1536 x 1024 RGBA artwork.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artwork", type=Path, nargs="?", help="Original supplied PNG")
    parser.add_argument("--check", action="store_true", help="Verify installed resources")
    args = parser.parse_args()
    if args.check:
        for target in TARGETS:
            verify(target.read_bytes())
            print("PASS", target.relative_to(ROOT))
        return
    if args.artwork is None:
        parser.error("provide the approved PNG or --check")
    data = args.artwork.read_bytes()
    verify(data)
    if not all(target.is_file() for target in TARGETS):
        raise SystemExit("Existing native hero resources missing; run in the Lexicon repository.")
    for target in TARGETS:
        target.write_bytes(data)
        verify(target.read_bytes())
        print("Installed", target.relative_to(ROOT))


if __name__ == "__main__":
    main()
