#!/usr/bin/env python3
"""Produce real path-only SVG proofs from Lexora's original artwork.

Install: python -m pip install pillow numpy opencv-python scikit-image
Run from repository root: python scripts/trace_home_artwork.py
No runtime assets are overwritten. Review each SVG before native export.
"""
import argparse
from pathlib import Path
import re
import numpy as np
from PIL import Image
import cv2
from skimage.measure import find_contours, approximate_polygon

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "All real art"
ARTWORK = (
    "Moonlit Cottage Storybook Vignette",
    "Word-Of-the-Day-Artwork",
    "Vocabulary", "Geography", "Literature", "Science", "History",
    "Mythology", "Philosophy", "Art & Architecture", "Nature",
    "Culture & Traditions",
)

def trace(source, output, width=540, colors=64):
    image = Image.open(source).convert("RGBA")
    image.thumbnail((width, width), Image.Resampling.LANCZOS)
    p = np.asarray(image).astype(np.float32) / 255
    h, w = p.shape[:2]
    alpha = p[:, :, 3]
    visible = alpha > .025
    # Cluster premultiplied RGB and coverage, not unassociated hidden pixels.
    features = np.concatenate(
        [p[:, :, :3] * alpha[:, :, None], alpha[:, :, None] * 1.35], axis=2
    )
    data = features[visible].astype(np.float32)
    if not len(data):
        raise ValueError(f"No visible pixels in {source}")
    colors = min(colors, len(data))
    cv2.setRNGSeed(17)
    _, labels, centers = cv2.kmeans(
        data, colors, None,
        (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_MAX_ITER, 35, .0005),
        1, cv2.KMEANS_PP_CENTERS,
    )
    classes = np.full((h, w), -1, np.int32)
    classes[visible] = labels[:, 0]
    paths = []
    for index in sorted(range(colors), key=lambda i: centers[i, 3]):
        a = min(1, float(centers[index, 3]) / 1.35)
        if a < .02:
            continue
        rgb = np.clip(np.rint(centers[index, :3] / a * 255), 0, 255).astype(int)
        mask = np.pad(classes == index, 1)
        segments = []
        for poly in find_contours(mask.astype(np.uint8), .5, fully_connected="low"):
            poly = approximate_polygon(poly, tolerance=.22)
            if len(poly) < 4:
                continue
            xy = poly[:, ::-1] - .5
            segments.append("M" + "L".join(f"{x:.1f},{y:.1f}" for x,y in xy[:-1]) + "Z")
        if segments:
            color = "#" + "".join(f"{x:02x}" for x in rgb)
            d = "".join(segments)
            paths.append(f'<path fill="{color}" fill-opacity="{a:.3f}" fill-rule="evenodd" d="{d}"/>')
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">\n'
        '<title>Lexora original artwork vector proof</title>\n'
        + "\n".join(paths) + "\n</svg>\n", encoding="utf-8"
    )
    print(f"{output}: {len(paths)} color layers, {output.stat().st_size:,} bytes")

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=SOURCE)
    parser.add_argument("--output", type=Path, default=ROOT / "artwork" / "vector-proofs")
    parser.add_argument("--width", type=int, default=540)
    parser.add_argument("--colors", type=int, default=64)
    parser.add_argument("--only", choices=ARTWORK, action="append")
    args = parser.parse_args()
    if args.width < 32 or not 2 <= args.colors <= 256:
        parser.error("width must be >= 32 and colors must be 2–256")
    for name in args.only or ARTWORK:
        slug = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")
        trace(args.source / (name + ".png"), args.output / (slug + ".svg"), args.width, args.colors)

if __name__ == "__main__":
    main()
