#!/usr/bin/env python3
"""Repair the *transparent holes* left by prior template-star removal.

All eight original template watercolors have already had their flattened UI star
RGB painted out. However, that earlier operation accidentally left an alpha=0
circular hole. On actual screens, the hole becomes a bright or dark circle.

Fix only alpha in the empty badge/paper region. Never redraw a flower, book,
quill or travel object; original RGB bytes and alpha outside the small paper
region remain exact. Preview by default; --apply updates both native catalogs.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/drawable-nodpi"
IOS = ROOT / "native/ios/Lexicon/Assets.xcassets"
NAMES = ("ill_tpl_daily", "ill_tpl_exam", "ill_tpl_write", "ill_tpl_travel")
SOURCE_SIZE = (370, 152)
CENTER = (338, 31)
# The original badge's semitransparent fringe extends beyond its 27px core.
# This 39px region is blank surrounding paper, not painted subjects.
PAPER_RADIUS = 39


def repair(source: Image.Image) -> tuple[Image.Image, int]:
    if source.size != SOURCE_SIZE:
        raise ValueError(f"Unexpected original size: {source.size}")
    rgba = np.asarray(source.convert("RGBA"), dtype=np.uint8)
    if int(rgba[CENTER[1], CENTER[0], 3]) > 16:
        raise ValueError("Expected alpha hole is absent; refusing repeat edit")
    y, x = np.ogrid[:rgba.shape[0], :rgba.shape[1]]
    region = ((x - CENTER[0]) ** 2 + (y - CENTER[1]) ** 2) <= PAPER_RADIUS ** 2
    fix = region & (rgba[:, :, 3] < 255)
    if np.count_nonzero(fix) < 2700 or np.count_nonzero(fix) > 2900:
        raise AssertionError("Unexpected badge alpha-damage geometry")
    target = rgba.copy()
    # Existing RGB is source-paper color, reconstructed previously from
    # adjacent ORIGINAL paper. Restore full paper opacity only.
    target[:, :, 3][fix] = 255
    if not np.array_equal(target[:, :, :3], rgba[:, :, :3]):
        raise AssertionError("Color or watercolor subject was modified")
    if not np.array_equal(target[~region], rgba[~region]):
        raise AssertionError("Pixels outside localized paper region changed")
    if int(target[CENTER[1], CENTER[0], 3]) != 255:
        raise AssertionError("Transparent center remains")
    return Image.fromarray(target, "RGBA"), int(np.count_nonzero(fix))


def composite(image: Image.Image, dark: bool) -> Image.Image:
    paper = Image.new("RGBA", image.size,
                      "#0E131B" if dark else "#F3EBDD")
    return Image.alpha_composite(paper, image.convert("RGBA")).convert("RGB")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=ROOT / "qa-template-paper-repair")
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    (args.output / "before").mkdir(parents=True, exist_ok=True)
    (args.output / "after").mkdir(parents=True, exist_ok=True)
    comparison = Image.new("RGB", (740, 8 * 174), "#F3EBDD")
    draw = ImageDraw.Draw(comparison)
    for i, name in enumerate(NAMES):
        for k, suffix in enumerate(("", "_dark")):
            key = name + suffix
            android = ANDROID / f"{key}.png"
            ios_name = key.replace("_", "-")
            ios = IOS / f"{ios_name}.imageset" / f"{ios_name}.png"
            if android.read_bytes() != ios.read_bytes():
                raise RuntimeError(f"Native source pair does not match: {key}")
            original = Image.open(android).convert("RGBA")
            fixed, pixels = repair(original)
            original.save(args.output / "before" / f"{key}.png")
            target = args.output / "after" / f"{key}.png"
            fixed.save(target, optimize=True)
            y = (i * 2 + k) * 174
            draw.text((3, y + 2), key + " — alpha before / after", fill="#233F33")
            comparison.paste(composite(original, bool(suffix)), (0, y + 20))
            comparison.paste(composite(fixed, bool(suffix)), (370, y + 20))
            if args.apply:
                android.write_bytes(target.read_bytes())
                ios.write_bytes(target.read_bytes())
                assert android.read_bytes() == ios.read_bytes()
            print(f"{key}: repaired {pixels} paper-alpha pixels; RGB intact")
    comparison.save(args.output / "comparison.png")
    print("Eight watercolor paper regions restored. No subjects repainted.")


if __name__ == "__main__":
    main()
