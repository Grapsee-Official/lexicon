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
            # Android ANR/system permission sheets can leave the app visible
            # under a dim scrim. A nonblank screenshot is NOT a valid capture.
            center = image.crop((int(width * .15), int(height * .42),
                                 int(width * .85), int(height * .56)))
            center_pixels = list(center.resize((70, 40), Image.Resampling.BILINEAR).getdata())
            upper = image.crop((int(width * .05), int(height * .12),
                                int(width * .30), int(height * .28)))
            upper_pixels = list(upper.resize((30, 20), Image.Resampling.BILINEAR).getdata())
    except (OSError, UnidentifiedImageError) as error:
        return False, f"unreadable PNG: {error}"

    modal_fraction = sum(
        min(rgb) > 210 and max(rgb) - min(rgb) < 20 for rgb in center_pixels
    ) / len(center_pixels)
    upper_brightness = sum(sum(rgb) / 3 for rgb in upper_pixels) / len(upper_pixels)
    if modal_fraction > .79 and upper_brightness < 165:
        return False, (
            f"OS modal/ANR obscures app (neutral modal={modal_fraction:.1%}, "
            f"dimmed background={upper_brightness:.0f})"
        )

    # A simulator can return the iOS SpringBoard (blue launcher wallpaper)
    # after simctl launch reports success. Reject it here so capture retries
    # foreground the app instead of treating a launcher as valid artwork.
    # This targets saturated system-blue, not Lexora's muted watercolor sky.
    launcher_blue = sum(
        blue > 125 and blue > red * 1.25 and blue > green * 1.08
        for red, green, blue in pixels
    ) / len(pixels)
    if launcher_blue > .45:
        return False, f"iOS launcher/system-blue wallpaper visible ({launcher_blue:.1%})"

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
