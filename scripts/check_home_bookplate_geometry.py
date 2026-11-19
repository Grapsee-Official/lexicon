#!/usr/bin/env python3
"""Check a source-grounded Home anchor against the approved Lexora light reference.

Uses the solid sage Word-of-the-Day *bookplate*, not OCR. This is a focused
layout regression check, not a pixel-level or artistic-fidelity certification.
Original reference (864x1821): sage panel rows 44.3%-58.2% of full frame.
Different simulator status bars are tolerated by a 3.5% vertical margin.
"""
from __future__ import annotations

import argparse
from pathlib import Path
from PIL import Image

REFERENCE_TOP = 0.443
TOP_TOLERANCE = 0.035


def inspect(path: Path) -> tuple[bool, str]:
    with Image.open(path) as image:
        im = image.convert("RGB")
        w, h = im.size
        sample = im.resize((240, 520), Image.Resampling.BILINEAR)
        pixels = sample.load()
        row_counts = []
        for y in range(520):
            if not (0.28 <= y / 520 <= 0.75):
                row_counts.append(0)
                continue
            count = 0
            for x in range(12, 125):
                r, g, b = pixels[x, y]
                # The muted olive pigment of the canonical light bookplate.
                if 55 <= r <= 135 and 73 <= g <= 153 and g > 1.02 * r and g > 1.05 * b:
                    count += 1
            row_counts.append(count)
        hits = [y for y, n in enumerate(row_counts) if n >= 24]
        if not hits:
            return False, "missing sage Word-of-the-Day bookplate"
        groups: list[list[int]] = []
        for y in hits:
            if groups and y == groups[-1][-1] + 1:
                groups[-1].append(y)
            else:
                groups.append([y])
        band = max(groups, key=len)
        top = band[0] / 520
        bottom = band[-1] / 520
        good = (
            abs(top - REFERENCE_TOP) <= TOP_TOLERANCE
            and 0.10 <= bottom - top <= 0.19
        )
        return good, (
            f"sage bookplate y={top:.3f}..{bottom:.3f}, "
            f"target top={REFERENCE_TOP:.3f}±{TOP_TOLERANCE:.3f}"
        )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("image", type=Path, help="Actual native light-home PNG")
    args = parser.parse_args()
    valid, details = inspect(args.image)
    print(f"{'PASS' if valid else 'FAIL'} {args.image}: {details}")
    return 0 if valid else 1


if __name__ == "__main__":
    raise SystemExit(main())
