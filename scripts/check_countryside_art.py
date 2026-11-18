#!/usr/bin/env python3
"""Native Lexicon screenshot-reference guardrails (no screenshots are captured).

Checks that every illustrated light/dark asset has an Android and iOS twin and
that all ten already-designed countryside screen families still exist on both
platforms. This is a source inventory check, not a pixel-fidelity assertion.
"""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
android_dir = root / "native/android/app/src/main/res/drawable-nodpi"
ios_dir = root / "native/ios/Lexicon/Assets.xcassets"

android = {item.stem.replace("_", "-") for item in android_dir.glob("ill_*.png")}
ios = {item.name.removesuffix(".imageset") for item in ios_dir.glob("ill-*.imageset")}
missing = android ^ ios
missing |= {
    name for name in android
    if (name.endswith("-dark") and name[:-5] not in android)
    or (not name.endswith("-dark") and f"{name}-dark" not in android)
}
missing |= {
    name for name in ios
    if not (ios_dir / (name + ".imageset") / "Contents.json").is_file()
}

# These screen families correspond to the 10 original approved UI concepts.
screens = (
    ("Dictionary Home", "DictionaryHomeScreen"),
    ("Word of the Day", "WordOfTheDayScreen"),
    ("Word Detail", "WordDetailProfileScreen"),
    ("Collections", "CollectionsHubScreen"),
    ("My Notes", "MyNotesScreen"),
    ("Study Lists", "StudyListsScreen"),
    ("Quick Templates", "QuickTemplatesScreen"),
    ("Search & Discovery", "SearchDiscoveryScreen"),
    ("Search Results", "SearchResultsScreen"),
    ("Expanded Word Exploration", "ExpandedExplorationScreen"),
)
android_screens = [
    root / "native/android/app/src/main/java/com/lexicon/ui/screens/DictionaryHomeScreen.kt",
    root / "native/android/app/src/main/java/com/lexicon/ui/screens/CountrysideScreens.kt",
    root / "native/android/app/src/main/java/com/lexicon/ui/screens/WordEntryScreens.kt",
]
ios_screens = [
    root / "native/ios/Lexicon/Screens/DictionaryHomeScreen.swift",
    root / "native/ios/Lexicon/Screens/CountrysideScreens.swift",
    root / "native/ios/Lexicon/Screens/WordEntryScreens.swift",
]
android_text = "\n".join(p.read_text(encoding="utf-8") for p in android_screens)
ios_text = "\n".join(p.read_text(encoding="utf-8") for p in ios_screens)
for title, symbol in screens:
    if f"fun {symbol}(" not in android_text:
        missing.add(f"Android {title} screen")
    if f"struct {symbol}: View" not in ios_text:
        missing.add(f"iOS {title} screen")

# Require the actual two application heroes, not merely thematic headers.
for base in ("hero_light", "hero_dark", "wotd_bg_light", "wotd_bg_dark"):
    if not (android_dir / (base + ".png")).is_file():
        missing.add("Android " + base)
for base in ("hero-light", "hero-dark", "wotd-bg-light", "wotd-bg-dark"):
    if not (ios_dir / (base + ".imageset") / "Contents.json").is_file():
        missing.add("iOS " + base)

if missing:
    print("Lexicon visual source parity check failed:")
    for entry in sorted(missing):
        print(" - " + entry)
    sys.exit(1)

print(
    f"Lexicon source parity passed: {len(screens)} native screen families, "
    f"{len(android)} matched countryside illustration variants, both day/night heroes."
)
