# Lexicon — Four-Phase Native UI Implementation Review

Updated: 2026-11-18. Scope: Android Jetpack Compose, iOS SwiftUI and the existing ten countryside screen families. The user's own original Cotswolds day/night watercolor mockups define the artistic intent. **No new application pages or Git branches** were created.

## Phase 1 — Home & Dictionary
- Both ViewModels now start on Home rather than selecting Home while rendering Dictionary; tab changes reset back history, and back navigation has a Home fallback.
- Dictionary's six illustrated discovery cards scroll horizontally at readable widths. They no longer compress text to 6–9pt on narrow phones.
- Blank Search does not generate a lookup for a different word.
- Source components retain botanical iconography, native editable sprigs, watercolor artwork and consistent theme tokens.

## Phase 2 — Collections, Notes, Study Lists & Templates
- Persistent notes, favorites, bookmarks, templates and collections reuse the same underlying native data models.
- Notes now support editing and deletion; tapping note starter templates opens an editor rather than silently creating placeholder content.
- Templates now open a prefilled editable note, mapping template type to a relevant note category. No hidden note creation on card tap.
- Study seed data previously claimed 50/120/80/60 items while listing four word IDs. Corrected to four entries per list with zero earned progress. Old inflated progress is migrated safely.
- Study action now opens an actual card, revealing the definition first; only confirmed learning advances progress by one. Today's reviewed count and day streak persist on both platforms.
- On narrow devices, note/collection/template grids adapt to single column or adaptive two-column layouts; font sizes and minimum card heights improved.
- Search filter icons for Collections, Notes and Templates now cycle their actual categories. The former Study Lists filter icon incorrectly opened Templates; it was removed rather than exposing a misleading action.

## Phase 3 — Search, Results & Expanded Exploration
- Search query, language/POS/difficulty/sort filters, and scope tabs are consistent in Android and iOS.
- Words, Meanings, Examples and Idioms now filter against corresponding source content; idiom searches show actual phrases and examples, not unrelated dictionary entries.
- Empty and unmatched searches no longer invent a default Serendipity result, pronunciation or grammatical part of speech.
- The available filter values come from the included dictionary data, not speculative language options.
- Recently viewed word IDs persist between launches.
- Expanded Exploration displays only translations that are present; complete 36-language coverage is **not** yet available. Do not manufacture placeholders.

## Phase 4 — Responsiveness, atmosphere & release
- Shared countryside headers are responsive at widths below 390dp/pt; the headline gains width and a soft parchment/ink gradient protects legibility when a painted scene overlaps text.
- The existing raised circular Search control and pill-shaped bottom navigation remain consistent.
- Four-across Study statistics and Search filter tiles scroll horizontally and retain readable label sizes instead of shrinking.
- Matching light/dark illustration filenames are checked by `scripts/check_countryside_art.py`.
- Genuine editable vector stars/fireflies in Compose Canvas and SwiftUI Canvas correspond to `artwork/vector/nocturne_fireflies.svg`. The existing detailed cottage/bridge/wildflower watercolor artworks remain **raster PNGs**, not fully editable SVGs.
- GitHub Actions tests and compiles Android Release and iOS Release simulator builds before signing and updating a single GitHub Android APK release (tag `lexicon-latest`). No debug APK or automated screenshot audit is requested.
- iOS compile success is **not** equivalent to signed iOS device distribution.

## Final owner review — do not confuse compilation with visual certification
The owner alone audits finished on-device appearance. Source inspection and successful native Release builds **do not** establish pixel-perfect parity with image mockups.

Please review on a compact phone, modern standard phone, large phone and tablets where applicable, for **both** themes:
1. Home first launch and bottom navigation; heading legibility and hero composition.
2. Dictionary landing, scrolling Explore tiles and Word of the Day day/night artwork.
3. Word Details, synonyms/antonyms/etymology, long definitions and multi-line text.
4. Collections searches/favorites/bookmarks and empty states.
5. Notes create/edit/delete, narrow grid, persistence after restart.
6. Study reveal → mark learned → return, progress counted once, reset at midnight, streak after next-day practice.
7. Quick Templates select/edit/save note and custom template creation.
8. Search scopes (including idioms), filters, no-match states and recent searches.
9. Search Results and Expanded Exploration with genuine available translations only.
10. Light-to-dark mode switch across any route without losing current data.

## Remaining work; do not claim completion
- Actual watercolor paintings are detailed raster artwork. Complete layered/vector recreation, more complex night-sky paintings and absolute visual match need dedicated original art source and visual review.
- Full 36-language dictionary/translation packs, substantive offline search coverage, true camera/OCR, microphone speech-to-text and connected writing features are separate engineering tasks not completed by this pass. Their decorative shortcut icons should not be described as implemented workflows.
- Real pronunciation speech/audio playback, app-wide accessibility testing, automated UI instrumentation and on-device rendering parity are not established by these source changes.
- Advanced spaced-repetition scheduling and reviewing completed study lists beyond the initial listed words need a richer practice data model.

Commit history on `main` is the source of truth. Build/run statuses should always be checked before advertising an updated APK.
