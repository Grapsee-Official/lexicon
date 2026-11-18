#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
a={p.stem.replace("_","-") for p in (root/"native/android/app/src/main/res/drawable-nodpi").glob("ill_*.png")}
folder=root/"native/ios/Lexicon/Assets.xcassets"
b={p.name.removesuffix(".imageset") for p in folder.glob("ill-*.imageset")}
missing=a^b
missing|={x for x in a if (x.endswith("-dark") and x[:-5] not in a) or (not x.endswith("-dark") and x+"-dark" not in a)}
missing|={x for x in b if not (folder/(x+".imageset")/"Contents.json").is_file()}
if missing:
    print("Countryside illustration parity failed: "+", ".join(sorted(missing)))
    sys.exit(1)
print(f"Lexicon native countryside art parity passed ({len(a)} illustration variants).")
