#!/usr/bin/env python3
"""Assert editable parchment/night SVG geometry matches the native Canvas recipe."""
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCES = ROOT / "artwork/vector"
SVG = "{http://www.w3.org/2000/svg}"
WIDTH, HEIGHT, COUNT = 390, 844, 620


def validate(name: str, ink: str, opacity: str) -> None:
    path = SOURCES / name
    svg = ET.parse(path).getroot()
    grains = svg.findall(f".//{SVG}g[@id='paper-grain']/{SVG}circle")
    assert len(grains) == COUNT, f"{name}: wanted {COUNT} editable grains, found {len(grains)}"
    for i, mark in enumerate(grains):
        x = ((i * 73 + 19) % 997) / 997 * WIDTH
        y = ((i * 101 + 37) % 991) / 991 * HEIGHT
        r = 0.36 + i % 5 * 0.13
        assert mark.get("id") == f"grain-{i + 1}", f"{name}: misplaced editable ID"
        assert abs(float(mark.get("cx", "nan")) - x) <= .002
        assert abs(float(mark.get("cy", "nan")) - y) <= .002
        assert abs(float(mark.get("r", "nan")) - r) <= .002
        assert mark.get("fill") == ink and mark.get("fill-opacity") == opacity
    print(f"PASS {name}: {COUNT} matching editable marks")


if __name__ == "__main__":
    validate("countryside-parchment-material.svg", "#8A7152", ".062")
    validate("countryside-midnight-material.svg", "#F0D89A", ".045")
