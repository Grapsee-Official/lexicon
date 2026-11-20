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

# Validate static illustration references across the native screen, component
# and curated-data sources. The material in these references is part of the
# design specification, so never silently substitute an unrelated stock asset.
import re

source_roots = [
    root / "native/android/app/src/main/java/com/lexicon/ui/screens",
    root / "native/android/app/src/main/java/com/lexicon/ui/components",
    root / "native/ios/Lexicon/Screens",
    root / "native/ios/Lexicon/Components",
]
data_files = [
    root / "native/android/app/src/main/java/com/lexicon/data/LexiconData.kt",
    root / "native/ios/Lexicon/Data/LexiconData.swift",
]
code_files = []
for directory in source_roots:
    code_files.extend(directory.glob("*.kt"))
    code_files.extend(directory.glob("*.swift"))
code_files.extend(data_files)
references = set()
for source in code_files:
    contents = source.read_text(encoding="utf-8")
    for raw in re.findall(r'["\x27](ill[-_][a-zA-Z0-9_-]+)["\x27]', contents):
        base = raw.replace("_", "-")
        references.add(base)
        if base not in ios or base not in android:
            missing.add(f"Unresolved image reference {raw} in {source.relative_to(root)}")

print(f"Validated {len(references)} distinct curated illustration references.")

# Require the actual two application heroes, not merely thematic headers.
for base in ("hero_light", "hero_dark", "wotd_bg_light", "wotd_bg_dark"):
    if not (android_dir / (base + ".png")).is_file():
        missing.add("Android " + base)
for base in ("hero-light", "hero-dark", "wotd-bg-light", "wotd-bg-dark"):
    if not (ios_dir / (base + ".imageset") / "Contents.json").is_file():
        missing.add("iOS " + base)

# An epigraph is a reading invitation, not a permanently repeated
# footer. The quote now belongs to the Library index only, on both platforms.
library_placements = {
    root / "native/android/app/src/main/java/com/lexicon/ui/screens/LibraryReaderSheet.kt": 1,
    root / "native/ios/Lexicon/Screens/HomeScreen.swift": 1,
}
for path, wanted in library_placements.items():
    actual = path.read_text(encoding="utf-8").count("CountrysideQuoteBanner(")
    if actual != wanted:
        missing.add(f"{path.relative_to(root)}: expected {wanted} Library epigraph, saw {actual}")
for path in android_screens + ios_screens + [
    root / "native/android/app/src/main/java/com/lexicon/ui/screens/DictionaryHomeScreen.kt",
    root / "native/ios/Lexicon/Screens/DictionaryHomeScreen.swift",
]:
    if "CountrysideQuoteBanner(" in path.read_text(encoding="utf-8"):
        missing.add(f"{path.relative_to(root)}: duplicated epigraph in a functional screen")

# The source watercolor is not a stock thumbnail to crop. Keep transparent
# petals and loose wash intact in the Library cards and reading sheet.
for path, cropped in (
    (root / "native/android/app/src/main/java/com/lexicon/ui/components/ArticleCard.kt", "ContentScale.Crop"),
    (root / "native/ios/Lexicon/Components/ArticleCard.swift", ".scaledToFill()"),
    (root / "native/android/app/src/main/java/com/lexicon/ui/screens/LibraryReaderSheet.kt", "ContentScale.Crop"),
):
    if cropped in path.read_text(encoding="utf-8"):
        missing.add(f"{path.relative_to(root)}: cropped source watercolor")

# Responsive native scenes measure the actual viewport and preserve the art.
android_common = (root / "native/android/app/src/main/java/com/lexicon/ui/components/CountrysideComponents.kt").read_text(encoding="utf-8")
ios_common = (root / "native/ios/Lexicon/Components/CountrysideComponents.swift").read_text(encoding="utf-8")
if "BoxWithConstraints(" not in android_common or "fullPaintingHeight" not in android_common:
    missing.add("Android: countryside hero must use measured width and full artwork height")
if "completePainting" not in ios_common or "measuredWidth" not in ios_common:
    missing.add("iOS: countryside hero must use measured width and full artwork height")
if "maxLines = if (config.fontScale >= 1.3f) 3 else 2" in android_common:
    missing.add("Android: an epigraph must never be truncated at two lines")
if ".lineLimit(dynamicTypeSize.isAccessibilitySize ? 3 : 2)" in ios_common[ios_common.find("struct CountrysideQuoteBanner: View"):ios_common.find("/// Responsive night-sky ornament")]:
    missing.add("iOS: epigraph must wrap naturally, not truncate")

# The owner's approved Word Detail reference integrates the watercolor
# BEHIND the left manuscript column and pairs Etymology with Usage Examples.
# Never regress to the experimental separated picture / stacked full-width
# leaves. Artwork remains scaled to fit, not cropped.
for src, tokens in (
    (
        root / "native/android/app/src/main/java/com/lexicon/ui/screens/WordEntryScreens.kt",
        ["item(key = \"wotd_hero_card\")", ".aspectRatio(ratio)",
         "item(key = \"detail_hero\")", "Modifier.matchParentSize().offset(y = (-10).dp)",
         "item(key = \"etym_usage\")", "fun EtymologyCard(",
         "fun UsageCard(", "item(key = \"did_you_know\")"],
    ),
    (
        root / "native/ios/Lexicon/Screens/WordEntryScreens.swift",
        ["struct WordOfTheDayScreen: View", "private func etymologyUsageCards",
         "referenceEtymologyCard", "referenceUsageCard",
         ".background(alignment: .topTrailing)", ".scaledToFit()",
         "let artHeight = (heroWidth - 32) / artRatio"],
    ),
    (
        root / "native/android/app/src/main/java/com/lexicon/ui/components/CountrysideComponents.kt",
        ["fun SearchDreamscapeWash(", ".height(paintedHeight)"],
    ),
    (
        root / "native/ios/Lexicon/Components/CountrysideComponents.swift",
        ["struct SearchDreamscapeWash: View", ".frame(height: naturalArtHeight)"],
    ),
    (
        root / "native/android/app/src/main/java/com/lexicon/ui/screens/CountrysideScreens.kt",
        ["SearchDreamscapeWash(isDark, Modifier.matchParentSize())"],
    ),
    (
        root / "native/ios/Lexicon/Screens/CountrysideScreens.swift",
        ["SearchDreamscapeWash(isDark: isDark)"],
    ),
    (
        root / "native/android/app/src/main/java/com/lexicon/ui/screens/MoreScreen.kt",
        ["private fun MoreJourneyPage", "state.savedWordIds", "state.recentSearches"],
    ),
    (
        root / "native/ios/Lexicon/Screens/MoreScreen.swift",
        ["case about, privacy, licenses, journey", "if page == .journey"],
    ),
):
    source = src.read_text(encoding="utf-8")
    for required in tokens:
        if required not in source:
            missing.add(f"{src.relative_to(root)}: missing native editorial or journey invariant: {required}")

# Both colorways have independently editable geometry, mirroring the native
# Compose/SwiftUI Search atmosphere; never flatten the source as one bitmap.
for theme in ("day", "night"):
    path = root / "artwork/vector" / f"search-cotswolds-dreamscape-{theme}.svg"
    if not path.is_file():
        missing.add(f"Missing original Search dreamscape source {path.relative_to(root)}")
    else:
        from xml.etree import ElementTree as ET
        svg = ET.parse(path).getroot()
        ns = "{http://www.w3.org/2000/svg}"
        for part in ("distant-hills", "near-hills"):
            if svg.find(f".//{ns}path[@id='{part}']") is None:
                missing.add(f"{path.relative_to(root)}: no editable {part}")
        if len(svg.findall(f".//{ns}g[@id='wildflowers']/{ns}g")) != 6:
            missing.add(f"{path.relative_to(root)}: the six painted flower clusters differ from native source")

if missing:
    print("Lexicon visual source parity check failed:")
    for entry in sorted(missing):
        print(" - " + entry)
    sys.exit(1)

print(
    f"Lexicon source parity passed: {len(screens)} native screen families, "
    f"{len(android)} matched countryside illustration variants, both day/night heroes."
)
