# Lexicon — Countryside artwork & native implementation audit
**Reference:** the owner's light/day and starry dark-mode screen designs shared November 18, 2026. This document audits **source, asset presence, layout decisions and native parity**, not screenshots or pixel-accurate device rendering. Final visual judgment belongs to the owner.

## Canonical look
- Brand: **Lexicon**, not the legacy mockup label Lexora.
- Day: honey-stone cottages, clouded watercolor skies, meadow daisies, worn books, botanical ink; deep forest #31462F, sage #657D5A, warm parchment #F3EBDD, watercolour blue #7C9297, terracotta #C98267 and old gold #A58A61.
- Night: midnight blue #1D2A3A, deep ink #0F1622, midnight paper #1A1F28, dusky blue #334A5E, lantern #D4A45A and firefly #F0D89A. Warm cottage windows, stars, restrained gold linework.
- Hierarchy: illustrations are decorative *around* text, never on top of it. Keep crisp native text, scrollable cards, accessible touch targets and the signature raised Search navigation.
- Source: paintings remain raster PNG until real layered illustrator files exist. An SVG containing `<image href="data:image/png;base64,...">` is a **raster wrapper, not editable vector painting**. The new `artwork/vector/nocturne_fireflies.svg` is genuinely path/circle-based and editable, with corresponding normalized native Canvas rendering.

## Ten screen families (each has Compose and SwiftUI)
| UI | Artwork / atmosphere target | Implementation / content check |
|---|---|---|
| Dictionary Home | village cottage, botanical books, gentle daylight and dark versions | Browse vocabulary, phrases, discovery, featured words. Shared illustrations must avoid text overlay |
| Word of the Day | cottage bridge, river, daisies and opened literature | Actual daily date/word, pronunciation, etymology, example, bookmark |
| Word Detail | bridge hero, antique books, seaside Serendip origin card | Definitions, synonyms, antonyms, examples, origin; placed art behind native text |
| Collections | village cottage and four illustrated saved-item categories | Search, filters, list counts and selected word navigation |
| My Notes | quill, old books, floral ink, four note cards | Search, category filters, create/edit/delete notes and templates |
| Study Lists | cottage landscape, herbarium, progress ring | Real list counts, progress, study actions; no fabricated completion statistics |
| Quick Templates | cottage lane, notebook, travel bag, books | Filter, select preset, create a custom template |
| Search & Discovery | cottage, village church, botanical search controls | Search suggestions, filters, recent searches, empty result state |
| Search Results | cottage backdrop, manuscript-like word detail card | Real dictionary results and accessible save actions |
| Expanded Word Exploration | pale book illustrations and word/translation cards | Natural-language nuance, pronunciation and **only actually available** offline translations |

## Source mapping
- Android: `native/android/app/src/main/java/com/lexicon/ui/screens/`, `native/android/app/src/main/java/com/lexicon/ui/components/`, `native/android/app/src/main/res/drawable-nodpi/`
- iOS: `native/ios/Lexicon/Screens/`, `native/ios/Lexicon/Components/`, `native/ios/Lexicon/Assets.xcassets/`
- Shared token parity: `ui/theme/Color.kt` and `Theme/LexiconColors.swift`.
- Common Cotswolds painting groups are `ill_hero_cottage`, `ill_hero_bridge`, `ill_hero_notes`, `ill_col_*`, `ill_note_*`, `ill_tpl_*`, `ill_banner_*`; corresponding iOS asset names use hyphens.
- Home day/night: Android `hero_light.png` and `hero_dark.png`; iOS `hero-light.imageset` and `hero-dark.imageset`.
- Existing painted category and article art: `cat_*` and `article_*` (matching iOS imagesets).
- Native decorative vector: `NocturneAtmosphere` in both `CountrysideComponents` files. Editable SVG design source: `artwork/vector/nocturne_fireflies.svg`.

## Already corrected in native source
1. Sky/firefly vectors respond proportionally to the available screen size in both Kotlin and Swift.
2. Hero headings, source art placement and shared heights are more closely aligned.
3. Category cards scroll horizontally instead of compressing into overly narrow fixed columns.
4. Floating center Search is raised above the nav capsule on both platforms.
5. Word Detail and Word of the Day backgrounds are layered behind native text, preserving readability.
6. Existing light/dark illustration asset-name parity is checked by `scripts/check_countryside_art.py`.

## Important limits — do not call these complete without more evidence
- **Image quality parity:** naming parity is not pixel parity. A named light/dark file may still contain poorly composed or mismatched painting. It requires original layered files or owner review.
- **Fully editable painting:** most original watercolor illustrations are PNGs. They are not 100%-editable SVGs; do not convert them into fake SVG wrappers.
- **Platform fidelity:** successful Android/iOS Release compilation does not prove 100% matching appearance at different device sizes.
- **Approval:** only the owner performs final device-based visual review.
