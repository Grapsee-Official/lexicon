#!/usr/bin/env python3
"""Catch stale/duplicate simulator frames even when every PNG is nonblank.

Expanded Exploration intentionally reuses the same word-detail presentation;
all other named screens must render distinguishable content. This detects
wrong-route captures, not reference-image design parity.
"""
from pathlib import Path
from PIL import Image, ImageChops, ImageStat
import argparse
import itertools
import sys

ALLOWED_IDENTICAL = {frozenset(("expanded_exploration", "word_detail"))}


def thumbnail(path: Path) -> Image.Image:
    with Image.open(path) as source:
        image = source.convert("RGB")
        w, h = image.size
        return image.crop((int(w * .08), int(h * .12),
                           int(w * .92), int(h * .88))).resize(
            (48, 96), Image.Resampling.BILINEAR
        )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    args = parser.parse_args()
    errors = []
    for mode in ("light", "dark"):
        paths = sorted(args.folder.glob(f"{mode}-*.png"))
        if len(paths) != 13:
            errors.append(f"{mode}: expected 13 named screens, got {len(paths)}")
            continue
        images = {p.stem.removeprefix(f"{mode}-"): thumbnail(p) for p in paths}
        for a, b in itertools.combinations(images, 2):
            if frozenset((a, b)) in ALLOWED_IDENTICAL:
                continue
            difference = ImageChops.difference(images[a], images[b])
            score = sum(ImageStat.Stat(difference).mean) / 3
            if score < 2.5:
                errors.append(f"{mode}: {a} and {b} appear identical (difference {score:.2f})")
        print(f"{mode}: checked {len(images)} screenshots for incorrect duplicate routes")
    for issue in errors:
        print("FAIL: " + issue, file=sys.stderr)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
