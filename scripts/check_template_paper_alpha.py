#!/usr/bin/env python3
"""Reject recurring transparent cutouts in the original Quick Templates watercolor.

The star control is rendered by native UI. The adjacent paper area must be
fully painted; a transparent hole becomes a white circle in light mode and a
black circle in dark mode. Do not mask it with an opaque Compose/SwiftUI badge.
"""
from __future__ import annotations

from pathlib import Path
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/drawable-nodpi"
IOS = ROOT / "native/ios/Lexicon/Assets.xcassets"
NAMES = ("ill_tpl_daily", "ill_tpl_exam", "ill_tpl_write", "ill_tpl_travel")
Y, X = 31, 338

def main() -> None:
    yy, xx = np.ogrid[:152, :370]
    inside = (xx - X)**2 + (yy - Y)**2 <= 33**2
    for base in NAMES:
        for suffix in ("", "_dark"):
            key = base + suffix
            android = ANDROID / (key + ".png")
            ios_key = key.replace("_", "-")
            ios = IOS / f"{ios_key}.imageset" / f"{ios_key}.png"
            if android.read_bytes() != ios.read_bytes():
                raise AssertionError(f"{key}: light/dark native catalog mismatch")
            image = Image.open(android).convert("RGBA")
            if image.size != (370, 152):
                raise AssertionError(f"{key}: changed painting dimensions {image.size}")
            pixels = np.asarray(image)
            low_alpha = int(pixels[:,:,3][inside].min())
            if low_alpha < 250:
                raise AssertionError(f"{key}: leftover circular transparent star hole alpha={low_alpha}")
            print(f"PASS {key}: continuous watercolor paper; native assets identical")
    print("Eight repaired original template images verified. No star cutouts.")

if __name__ == "__main__":
    main()
