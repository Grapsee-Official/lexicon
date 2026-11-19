#!/usr/bin/env python3
"""Regression tests: native QA must reject Android ANR and blank iOS frames."""
from __future__ import annotations

from pathlib import Path
from tempfile import TemporaryDirectory
from PIL import Image, ImageDraw

from check_capture_quality import inspect


def fixture(dark: bool = False) -> Image.Image:
    paper = "#07121C" if dark else "#FBF5EB"
    image = Image.new("RGB", (1080, 2400), paper)
    paint = ImageDraw.Draw(image)
    paint.rounded_rectangle((40, 955, 438, 1250), radius=25, fill="#657D5A")
    paint.rectangle((470, 955, 1010, 1250), fill="#7C9297")
    return image


def main() -> None:
    with TemporaryDirectory() as directory:
        folder = Path(directory)

        for dark in (False, True):
            image = fixture(dark)
            path = folder / f"{'dark' if dark else 'light'}-valid.png"
            image.save(path)
            good, details = inspect(path)
            assert good, (path.name, details)

            dimmed = Image.blend(image, Image.new("RGB", image.size, "#08090C"), .58)
            draw = ImageDraw.Draw(dimmed)
            draw.rounded_rectangle((76, 935, 1004, 1390), radius=50,
                                   fill=(241, 241, 245))
            overlay = folder / f"{'dark' if dark else 'light'}-system-modal.png"
            dimmed.save(overlay)
            good, details = inspect(overlay)
            assert not good and "OS modal" in details, (overlay.name, details)
            print(f"PASS {overlay.name}: modal screenshot correctly rejected")

        # Regression for a successful simctl launch that screenshots SpringBoard.
        # This synthetic fixture contains the saturated iOS blue wallpaper;
        # actual app watercolor skies are muted rather than system-blue.
        launcher = folder / "ios-launcher.png"
        Image.new("RGB", (1080, 2400), "#228DE0").save(launcher)
        good, details = inspect(launcher)
        assert not good and "launcher" in details, (launcher.name, details)
        print("PASS iOS launcher wallpaper correctly rejected")

        violet_launcher = folder / "ios-violet-launcher.png"
        Image.new("RGB", (1080, 2400), "#8057B5").save(violet_launcher)
        good, details = inspect(violet_launcher)
        assert not good and "launcher" in details, (violet_launcher.name, details)
        print("PASS iOS violet launcher wallpaper correctly rejected")

        # Regression: the Nov 19 iOS light/dictionary_home CI artifact was
        # a pink SpringBoard home screen, yet passed the prior quality gate.
        pink_launcher = folder / "ios-pink-launcher.png"
        Image.new("RGB", (1080, 2400), "#D99CB8").save(pink_launcher)
        good, details = inspect(pink_launcher)
        assert not good and "launcher" in details, (pink_launcher.name, details)
        print("PASS iOS pink launcher wallpaper correctly rejected")

        blank = folder / "blank-white.png"
        Image.new("RGB", (1080, 2400), "#FFFFFF").save(blank)
        good, details = inspect(blank)
        assert not good, details
        print("PASS blank iOS backing canvas correctly rejected")

    print("All native screenshot-quality regression fixtures passed")


if __name__ == "__main__":
    main()
