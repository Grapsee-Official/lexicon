#!/usr/bin/env python3
"""Catch stale/duplicate simulator frames even when every PNG is nonblank.

Word of the Day and Word Detail intentionally use the owner's
canonical reading-page composition. Expanded Exploration is a distinct
dictionary destination and MUST NOT be treated as an identical alias.
This detects wrong-route captures, not reference-image design parity.
"""
from pathlib import Path
from PIL import Image, ImageChops, ImageStat
import argparse
import itertools
import sys

# These routes are intentional aliases of the canonical reference entry.
# Do not exempt unrelated screens: a stale screenshot must still fail.
WORD_ENTRY_ALIASES = ("word_detail", "word_of_the_day")
ALLOWED_IDENTICAL = {
    frozenset((a, b))
    for a, b in itertools.combinations(WORD_ENTRY_ALIASES, 2)
}


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
        if len(paths) != 15:
            errors.append(f"{mode}: expected 15 named screens, got {len(paths)}")
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
