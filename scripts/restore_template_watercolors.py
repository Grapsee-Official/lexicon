#!/usr/bin/env python3
"""Remove baked-in star badges from Lexora's ORIGINAL template paintings.

Only the flat UI badge area is reconstructed using adjacent in-image paper
pixels. All painted books, flowers, quills, and travel objects retain their
original per-pixel RGBA values. Preview is the default; --apply requires the
matching original Android and iOS files to have identical bytes.
"""
from __future__ import annotations

import argparse
from hashlib import sha256
from pathlib import Path

import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/drawable-nodpi"
IOS = ROOT / "native/ios/Lexicon/Assets.xcassets"
NAMES = ("ill_tpl_daily", "ill_tpl_exam", "ill_tpl_write", "ill_tpl_travel")
SOURCE_SIZE = (370, 152)
# The round native-action badge was flattened into all four source PNGs.
# These are the badge coordinates, not the actual watercolor subject.
CENTER = (338, 31)
RADIUS = 27


def repair(original: Image.Image) -> tuple[Image.Image, int]:
    src = np.asarray(original.convert("RGBA"), dtype=np.uint8).copy()
    if original.size != SOURCE_SIZE:
        raise ValueError(f"Unexpected source dimensions: {original.size}")
    mask = np.zeros((src.shape[0], src.shape[1]), dtype=np.uint8)
    cv2.circle(mask, CENTER, RADIUS, 255, thickness=-1)
    # Restore only inside the tiny badge disk, borrowing the neighboring
    # original paper texture; no paint is erased or replaced elsewhere.
    restored = cv2.inpaint(src[:, :, :3], mask, 4, cv2.INPAINT_TELEA)
    out = src.copy()
    out[:, :, :3] = np.where(mask[:, :, None] > 0, restored, src[:, :, :3])
    if not np.array_equal(out[mask == 0], src[mask == 0]):
        raise AssertionError("Image changed outside the explicitly targeted badge")
    if not np.array_equal(out[:, :, 3], src[:, :, 3]):
        raise AssertionError("Restoration altered original alpha")
    count = np.count_nonzero(np.any(out[:, :, :3] != src[:, :, :3], axis=2))
    if count < 500 or count > 2600:
        raise AssertionError(f"Unexpected number of repaired pixels: {count}")
    return Image.fromarray(out, "RGBA"), int(count)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=ROOT / "qa-template-restoration")
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    preview_dir = args.output
    (preview_dir / "original").mkdir(parents=True, exist_ok=True)
    (preview_dir / "restored").mkdir(parents=True, exist_ok=True)
    contact = Image.new("RGB", (2 * SOURCE_SIZE[0], 8 * (SOURCE_SIZE[1] + 28)), "#F3EBDD")
    draw = ImageDraw.Draw(contact)

    for index, base in enumerate(NAMES):
        for suffix in ("", "_dark"):
            name = base + suffix
            android = ANDROID / f"{name}.png"
            ios_name = name.replace("_", "-")
            ios = IOS / f"{ios_name}.imageset" / f"{ios_name}.png"
            if not android.is_file() or not ios.is_file():
                raise FileNotFoundError(f"Native source pair missing for {name}")
            # Do not replace a distinct iOS painting with an Android image.
            if android.read_bytes() != ios.read_bytes():
                raise RuntimeError(f"Original Android/iOS paintings differ: {name}")
            original = Image.open(android).convert("RGBA")
            original.save(preview_dir / "original" / f"{name}.png")
            candidate, changed = repair(original)
            out_path = preview_dir / "restored" / f"{name}.png"
            candidate.save(out_path, optimize=True)

            y = (index * 2 + (1 if suffix else 0)) * (SOURCE_SIZE[1] + 28)
            draw.text((8, y + 7), f"{name} — ORIGINAL", fill="#31462F")
            draw.text((SOURCE_SIZE[0] + 8, y + 7), "CANDIDATE", fill="#31462F")
            contact.paste(original.convert("RGB"), (0, y + 28))
            contact.paste(candidate.convert("RGB"), (SOURCE_SIZE[0], y + 28))
            if args.apply:
                if sha256(android.read_bytes()).digest() == sha256(out_path.read_bytes()).digest():
                    raise RuntimeError(f"Source already appears restored: {name}")
                android.write_bytes(out_path.read_bytes())
                ios.write_bytes(out_path.read_bytes())
                assert android.read_bytes() == ios.read_bytes()
            print(f"{name}: restored {changed} original UI-badge pixels; scene preserved")
    contact.save(preview_dir / "comparison.png")
    print("Completed eight light/dark source candidates without redrawing watercolor.")


if __name__ == "__main__":
    main()
