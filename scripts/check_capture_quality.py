#!/usr/bin/env python3
"""Reject blank native screenshots before declaring visual QA successful.

This is a rendering-presence gate, NOT a pixel-level visual-fidelity score.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import sys

try:
    from PIL import Image, UnidentifiedImageError
except ImportError as error:
    raise SystemExit("Pillow is required: python -m pip install Pillow") from error


def inspect(path: Path) -> tuple[bool, str]:
    try:
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            image = image.convert("RGB")
            width, height = image.size
            if width < 300 or height < 500:
                return False, f"invalid dimensions {width}x{height}"
            # Ignore OS status/navigation bars; they can make an empty app look valid.
            core = image.crop((int(width * .08), int(height * .13),
                               int(width * .92), int(height * .83)))
            pixels = list(core.resize((96, 160), Image.Resampling.BILINEAR).getdata())
    except (OSError, UnidentifiedImageError) as error:
        return False, f"unreadable PNG: {error}"

    n = len(pixels)
    white = sum(min(rgb) >= 247 for rgb in pixels) / n
    colored = sum(max(rgb) - min(rgb) > 6 for rgb in pixels) / n
    # Every Lexora screen must show tinted parchment/night paper and illustration.
    if white > .92:
        return False, f"blank white app canvas (white={white:.1%}, color={colored:.1%})"
    if colored < .04:
        return False, f"missing themed app content (color={colored:.1%})"
    return True, f"rendered (white={white:.1%}, color={colored:.1%})"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", type=Path,
                        help="Screenshot PNG files or folders")
    parser.add_argument("--expected-count", type=int, default=None)
    args = parser.parse_args()
    paths = sorted({image for candidate in args.paths
                    for image in (candidate.glob("*.png") if candidate.is_dir() else [candidate])})
    if args.expected_count is not None and len(paths) != args.expected_count:
        print(f"FAIL: expected {args.expected_count} screenshots, got {len(paths)}", file=sys.stderr)
        return 1
    if not paths:
        print("FAIL: no screenshots found", file=sys.stderr)
        return 1
    failures = []
    for path in paths:
        good, reason = inspect(path)
        print(f"{'PASS' if good else 'FAIL'} {path}: {reason}")
        if not good:
            failures.append(path)
    print(f"Capture quality: {len(paths) - len(failures)}/{len(paths)} valid")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
