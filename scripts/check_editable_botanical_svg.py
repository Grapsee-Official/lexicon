#!/usr/bin/env python3
"""Validate canonical editable botanical artwork for native Lexora."""
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1] / "artwork" / "vector"
NAMESPACE = "{http://www.w3.org/2000/svg}"

FILES = {
    "sprig": ("lexora-sprig-parchment.svg", "lexora-sprig-midnight.svg"),
    "garden": ("lexora-english-garden-bookplate-parchment.svg",
               "lexora-english-garden-bookplate-midnight.svg"),
}


def validate(path: Path, kind: str) -> dict[str, int]:
    source = path.read_text(encoding="utf-8")
    blocked = ("<image", "<foreignObject", "data:image/", ";base64,", "<script")
    assert not any(token in source for token in blocked), (
        f"{path.name}: raster embedding or executable markup is forbidden"
    )
    root = ET.fromstring(source)
    assert root.tag == NAMESPACE + "svg", f"{path}: not SVG"
    assert root.get("viewBox"), f"{path}: missing editable viewBox"
    paths = root.findall(".//" + NAMESPACE + "path")
    ellipses = root.findall(".//" + NAMESPACE + "ellipse")
    circles = root.findall(".//" + NAMESPACE + "circle")
    groups = root.findall(".//" + NAMESPACE + "g")
    assert len(paths) >= (5 if kind == "sprig" else 52), (
        f"{path.name}: missing editable organic geometry"
    )
    if kind == "garden":
        assert len(ellipses) >= 48, f"{path.name}: painted petals are missing"
        assert len(circles) >= 190, f"{path.name}: independent pigment marks are missing"
    ids = [e.get("id") for e in root.iter() if e.get("id") is not None]
    assert len(ids) == len(set(ids)), f"{path.name}: repeated editable layer ID"
    result = dict(paths=len(paths), ellipses=len(ellipses),
                  circles=len(circles), groups=len(groups))
    print(f"{path.name}: valid editable SVG | {result}")
    return result


def main() -> None:
    for kind, (light, dark) in FILES.items():
        first = validate(ROOT / light, kind)
        second = validate(ROOT / dark, kind)
        assert first == second, f"{kind}: night/day layer structures differ"


if __name__ == "__main__":
    main()
