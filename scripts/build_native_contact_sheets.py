#!/usr/bin/env python3
"""Build contact sheets from actual native simulator screenshots.

Review aid only: a contact sheet does not assert reference-image parity.
"""
from pathlib import Path
import argparse
import math
from PIL import Image, ImageDraw, ImageFont


def build(source: Path, destination: Path, theme: str, platform: str) -> None:
    screenshots = sorted(source.glob(f"{theme}-*.png"))
    if not screenshots:
        raise ValueError(f"No {theme} screenshots in {source}")
    columns = 5
    card_width, image_height = 250, 520
    gap, margin, heading = 18, 24, 66
    rows = math.ceil(len(screenshots) / columns)
    total_w = margin * 2 + columns * card_width + (columns - 1) * gap
    total_h = heading + rows * (image_height + 58) + margin
    background = "#EDE7D9" if theme == "light" else "#18202C"
    ink = "#31462F" if theme == "light" else "#F0D89A"
    contact = Image.new("RGB", (total_w, total_h), background)
    draw = ImageDraw.Draw(contact)
    font = ImageFont.load_default()
    draw.text((margin, 20), f"LEXORA | {platform.upper()} | {theme.upper()} | {len(screenshots)} real simulator captures", fill=ink, font=font)
    for index, path in enumerate(screenshots):
        x = margin + (index % columns) * (card_width + gap)
        y = heading + (index // columns) * (image_height + 58)
        with Image.open(path) as image:
            thumb = image.convert("RGB")
            thumb.thumbnail((card_width, image_height), Image.Resampling.LANCZOS)
            contact.paste(thumb, (x + (card_width - thumb.width) // 2, y))
        draw.text((x, y + image_height + 7), path.stem.removeprefix(theme + "-").replace("_", " "), fill=ink, font=font)
    destination.parent.mkdir(parents=True, exist_ok=True)
    contact.save(destination, quality=85, optimize=True)
    print(f"Created {destination} ({len(screenshots)} {theme} screens)")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("screenshots", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--platform", required=True, choices=("ios", "android"))
    args = parser.parse_args()
    for mode in ("light", "dark"):
        build(args.screenshots, args.output / f"{mode}-contact.jpg", mode, args.platform)


if __name__ == "__main__":
    main()
