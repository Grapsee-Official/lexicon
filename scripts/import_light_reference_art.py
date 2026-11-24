#!/usr/bin/env python3
"""Install reviewed SVG/VectorDrawable board crops without touching original art.

Usage: python3 scripts/import_light_reference_art.py /path/to/lexora-reference-vectors.zip
The zip contains a matching .svg and .xml for each light-* scene.
"""
import argparse
import json
import pathlib
import re
import xml.etree.ElementTree as ET
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
ANDROID = ROOT / "native/android/app/src/main/res/drawable"
IOS = ROOT / "native/ios/Lexicon/Assets.xcassets"
SOURCE = ROOT / "artwork/reference-svg-light"
SVG = "{http://www.w3.org/2000/svg}"
A = "{http://schemas.android.com/apk/res/android}"

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("pack", type=pathlib.Path)
    args = p.parse_args()
    pending = []
    with zipfile.ZipFile(args.pack) as z:
        names = set(z.namelist())
        for name in sorted(names):
            if not re.fullmatch(r"light-[a-z0-9-]+\.svg", name):
                continue
            stem = name[:-4]
            native = stem.replace("-", "_") + ".xml"
            svg = z.read(name)
            xml = z.read(native)
            if len(svg) > 8_000_000 or len(xml) > 8_000_000:
                raise ValueError(f"Excessive vector size: {stem}")
            s, a = ET.fromstring(svg), ET.fromstring(xml)
            if s.tag != SVG + "svg" or a.tag != "vector":
                raise ValueError(f"Invalid vector root: {stem}")
            if any(n.tag != SVG + "path" for n in s) or any(n.tag != "path" for n in a):
                raise ValueError(f"Non-path artwork: {stem}")
            if not len(s) or [n.attrib.get("d") for n in s] != [n.attrib.get(A + "pathData") for n in a]:
                raise ValueError(f"Geometry mismatch: {stem}")
            pending.append((stem, svg, xml))
    if not pending:
        raise ValueError("No light reference vectors in the pack")
    for stem, svg, xml in pending:
        SOURCE.mkdir(parents=True, exist_ok=True)
        (SOURCE / (stem + ".svg")).write_bytes(svg)
        (ANDROID / ("ref_" + stem.replace("-", "_") + ".xml")).write_bytes(xml)
        imageset = IOS / ("ref-" + stem + ".imageset")
        imageset.mkdir(parents=True, exist_ok=True)
        (imageset / "art.svg").write_bytes(svg)
        (imageset / "Contents.json").write_text(json.dumps({
            "images": [{"filename": "art.svg", "idiom": "universal"}],
            "info": {"author": "xcode", "version": 1},
            "properties": {"preserves-vector-representation": True}
        }, indent=2) + "\n")
        print(f"Installed {stem}")
    print(f"Installed {len(pending)} independent board illustrations on both platforms.")
if __name__ == "__main__":
    main()
