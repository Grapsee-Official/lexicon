#!/usr/bin/env python3
"""Repair localized holes in Lexora's ORIGINAL watercolor PNGs.

Never replace entire scenes. Missing pixels are filled with nearby original
watercolor strokes; no claim of exact lossless reconstruction is made.
This one-shot tool prepares candidate images by default. --apply synchronizes
approved repairs to matching Android and iOS asset catalogs.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import sys

import cv2
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/drawable-nodpi"
IOS = ROOT / "native/ios/Lexicon/Assets.xcassets"
CARDS = ("ill_col_favorites", "ill_col_bookmarks", "ill_col_notes", "ill_col_studylist")
HEROES = ("ill_hero_cottage", "ill_hero_bridge")
EXTRA = ("ill_note_flowers", "ill_banner_bridge")


def borrow_strokes(image: Image.Image, donor_image: Image.Image, center: tuple[int, int],
                   donor_center: tuple[int, int], radii: tuple[float, float],
                   feather: float = 0.22) -> Image.Image:
    original = np.asarray(image.convert("RGBA"), dtype=np.float32)
    donor = np.asarray(donor_image.convert("RGBA"), dtype=np.float32)
    h, w = original.shape[:2]
    sh, sw = donor.shape[:2]
    y, x = np.indices((h, w))
    distance = np.sqrt(((x - center[0]) / radii[0]) ** 2 +
                       ((y - center[1]) / radii[1]) ** 2)
    weight = np.clip((1.06 - distance) / feather, 0, 1)[:, :, None]
    dy = np.clip(y + donor_center[1] - center[1], 0, sh - 1)
    dx = np.clip(x + donor_center[0] - center[0], 0, sw - 1)
    strokes = donor[dy, dx]
    a0, a1 = original[:, :, 3:4] / 255.0, strokes[:, :, 3:4] / 255.0
    alpha = a0 * (1 - weight) + a1 * weight
    premul = original[:, :, :3] * a0 * (1 - weight) + strokes[:, :, :3] * a1 * weight
    rgb = np.where(alpha > 0, premul / np.maximum(alpha, 1e-5), 0)
    result = np.uint8(np.clip(np.concatenate((rgb, alpha * 255), axis=2), 0, 255))
    untouched = weight[:, :, 0] == 0
    result[untouched] = np.asarray(image.convert("RGBA"))[untouched]
    return Image.fromarray(result, "RGBA")


def remove_faint_text(image: Image.Image, rect: tuple[int, int, int, int],
                      dark: bool) -> Image.Image:
    """Inpaint only a small orphaned screenshot-label fragment."""
    rgba = np.asarray(image.convert("RGBA"), dtype=np.uint8).copy()
    mask = np.zeros(rgba.shape[:2], dtype=np.uint8)
    cv2.rectangle(mask, (rect[0], rect[1]), (rect[2], rect[3]), 255, -1)
    ground = np.array([19, 29, 48] if dark else [245, 237, 221], dtype=np.float32)
    alpha = rgba[:, :, 3:4].astype(np.float32) / 255
    composited = np.uint8(np.clip(rgba[:, :, :3] * alpha + ground * (1 - alpha), 0, 255))
    rgb = cv2.inpaint(composited, mask, 6, cv2.INPAINT_TELEA)
    restored_alpha = cv2.inpaint(rgba[:, :, 3], mask, 6, cv2.INPAINT_TELEA)
    changed = mask > 0
    rgba[changed, :3] = rgb[changed]
    rgba[changed, 3] = restored_alpha[changed]
    return Image.fromarray(rgba, "RGBA")


def restore(name: str, image: Image.Image, source: Path) -> Image.Image:
    dark = name.endswith("_dark")
    base = name[:-5] if dark else name
    if base in CARDS:
        if image.size != (280, 220):
            raise ValueError(f"{name}: source size changed: {image.size}")
        brush = image
        if base == "ill_col_studylist":
            brush = Image.open(source / ("ill_col_favorites_dark.png" if dark else
                                         "ill_col_favorites.png")).convert("RGBA")
        output = borrow_strokes(image, brush, (233, 180), (163, 180), (38, 37))
        if base == "ill_col_bookmarks":
            output = remove_faint_text(output, (0, 127, 35, 160), dark)
        return output
    if base == "ill_hero_cottage":
        if image.size != (508, 345): raise ValueError(name)
        return borrow_strokes(image, image, (442, 97), (90, 75), (53, 51), 0.24)
    if base == "ill_hero_bridge":
        if image.size != (418, 670): raise ValueError(name)
        clouds = Image.open(source / ("ill_hero_cottage_dark.png" if dark else
                                      "ill_hero_cottage.png")).convert("RGBA")
        return borrow_strokes(image, clouds, (371, 106), (90, 65), (67, 67), 0.27)
    if base == "ill_note_flowers":
        if image.size != (164, 248): raise ValueError(name)
        output = borrow_strokes(image, image, (123, 220), (78, 178), (36, 37), 0.24)
        output = borrow_strokes(output, image, (120, 5), (88, 42), (32, 32), 0.25)
        return remove_faint_text(output, (0, 109, 47, 144), dark)
    if base == "ill_banner_bridge":
        if image.size != (420, 206): raise ValueError(name)
        # The last 24px are unpainted source-canvas padding. Never crop pigment.
        if np.any(np.asarray(image.convert("RGBA"))[182:, :, 3] >= 32):
            raise ValueError(f"{name}: trailing band contains painted pixels")
        return image.crop((0, 0, 420, 182))
    raise ValueError(f"Unlisted source asset: {name}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-dir", type=Path, default=ANDROID)
    parser.add_argument("--output-dir", type=Path,
                        default=ROOT / "recovery-preview/restored")
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    if args.apply:
        # One-shot operation: do not paste a second floral patch onto a
        # previously repaired image.
        check = Image.open(ANDROID / "ill_col_favorites.png").convert("RGBA")
        if check.getpixel((233, 180))[3] > 160:
            raise SystemExit("Watercolor source appears already repaired; refusing to reapply")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    for base in (*CARDS, *HEROES, *EXTRA):
        for suffix in ("", "_dark"):
            name = f"{base}{suffix}"
            input_file = args.source_dir / f"{name}.png"
            original = Image.open(input_file).convert("RGBA")
            output = restore(name, original, args.source_dir)
            if output.size == original.size and np.array_equal(
                    np.asarray(original), np.asarray(output)):
                raise AssertionError(f"{name}: no repair applied")
            dest = args.output_dir / f"{name}.png"
            output.save(dest, "PNG", optimize=True)
            if args.apply:
                android = ANDROID / f"{name}.png"
                ios_name = name.replace("_", "-")
                ios = IOS / f"{ios_name}.imageset" / f"{ios_name}.png"
                if not ios.is_file():
                    raise FileNotFoundError(f"Matching original iOS source missing: {ios}")
                android.write_bytes(dest.read_bytes())
                ios.write_bytes(dest.read_bytes())
                assert android.read_bytes() == ios.read_bytes()
            print(f"RESTORED {name}: {original.size} -> {output.size}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
