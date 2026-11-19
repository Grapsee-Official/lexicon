# Lexicon native countryside fidelity — implementation audit
Date: 2026-11-19
Target: **pure native** Jetpack Compose + SwiftUI applications with the shared C++17 engine. Next.js files are not a product target.

## Visual source of truth
The owner's supplied images: day and nocturnal Home, Word of the Day / Word Detail, Dictionary and Search states, Notes, Study Lists, Quick Templates, color boards, and original illustrated scene studies. Rendered reference screenshots are more authoritative than the earlier mockup's legacy brand label **Lexora**. Production brand is **Lexicon**.

**Core rule:** The app must behave like a usable, accessible native reader while retaining an old English Cotswolds book's atmosphere. Warm parchment, imperfect watercolor edges, honey-stone villages, meadow daisies, ink-green typography, and a quiet lantern-lit night. Never replace organic painting with generic opaque cards, modern gradients, gradients over text, arbitrary crops, raster-wrapped "SVG", or fake interactive controls.

**Reference palettes:** light #F3EBDD / #657D5A / #31462F / #7C9297 / #C98267 / #A58A61. Night #0F1622 / #1D2A3A / #1A1F28 / #334A5E / #D4A45A / #F0D89A. Use semantic contrast for real text and controls, not exact palette samples as a substitute for accessibility.

## Findings — source inspected at main c9a6c31ddb524f1c104eb0d32c03642c7a1ec213

1. **P1 — Loss of watercolor edges in Collections empty state.** Android `native/android/app/src/main/java/com/lexicon/ui/screens/CollectionsScreen.kt` used `ContentScale.Crop` plus a radial alpha/DstIn mask and an opaque circular bookmark disc. SwiftUI `native/ios/Lexicon/Screens/CollectionsScreen.swift` used `scaledToFill`, clip, and an opaque circle. This is a confirmed artwork-preservation bug, not a subjective taste issue. **Fix in this branch:** use `Fit` / `scaledToFit`, preserve transparent silhouette, remove opaque badge underlay.
2. **P1 — Tiny interactive bookmarks in note cards.** `CountrysideScreens.kt` made note bookmark gestures 14dp; SwiftUI `CountrysideScreens.swift` used an 11.5pt glyph with no explicit hit size. **Fix in this branch:** transparent 44dp/pt buttons with labels. The Android note card also gains enough height for 2-line titles and 3-line body previews.
3. **P0 — False-positive iOS simulator screenshot accepted as app.** The completed-success `Lexicon Native Visual QA` workflow run [44567896288](https://github.com/Grapsee-Official/lexicon/actions/runs/44567896288) contains `light-dictionary_home.png` showing pink/violet iOS SpringBoard instead of Lexicon. The old `scripts/check_capture_quality.py` incorrectly reported `rendered (white=0.9%, color=97.9%)`, so `check_screen_diversity.py` did not catch this single wrong-route frame. **Fix proposed in this branch:** strengthen pink launcher detection and require a tiny SwiftUI-only, debug-only fuchsia marker to prove the actual app view is foregrounded before accepting any iOS capture. The new gate must pass CI and the screenshot must be reexamined.
4. **P1 — Device-rendered parity is not proved.** The QA workflow captures screenshots and checks blankness, screen diversity, texture/font geometry, and some home regions. It does not have a source-to-rendered comparison against every supplied approved screenshot at normalized viewport sizes. **Still open:** collect reference pixels in repository-controlled QA fixtures, compare subject framing, region bounds, glyph wraps, colors, and crop at multiple densities, and have a human inspect contact sheets before calling a visual match approved.
5. **P2 — Header sizing needs window-based review.** `native/ios/Lexicon/Components/CountrysideComponents.swift` uses `UIScreen.main.bounds` to select responsive hero height, which can differ from the actual SwiftUI scene width in split view, Stage Manager, and resizeable iPad layouts. Compose uses `LocalConfiguration`; verify actual container constraints for landscape, 320pt compact phones, 390pt default phones, accessibility sizes, and tablets.
6. **P2 — Banner scaling and bottom nav safety need visual evidence.** `CountrysideQuoteBanner` uses `Fit` but fixed or computed heights. This avoids cropping, but doesn't establish full painted scene use on all aspect ratios. Scroll screens generally reserve 160pt / 162dp below floating nav; verify overlays, interactive bottom rows, and safe-area insets against captured screenshot evidence.
7. **P1 — Feature completeness is separate from visual polish.** `docs/LEXICON_PHASE_1_4_IMPLEMENTATION.md` explicitly says full 36-language packs, camera OCR, microphone STT, connected writing workflows, verified real audio, advanced spaced repetition, and full accessibility UI tests were not completed. Keep unimplemented affordances discoverable only if they lead to honest permission/unsupported states, not fake success. Audit C++ engine data semantics and persistence for each route.
8. **P2 — Painting editability and fidelity are different.** Existing cottage, bridge, notes, thumbnails, category and banner artwork in Android `drawable-nodpi` and iOS `Assets.xcassets` remains raster. The newly added `artwork/vector/cotswolds-village-parchment.svg` and `-midnight.svg` are path/circle/ellipse-based draft *source* scenes; they have not replaced the original watercolor screenshots, nor have they been native-rendered or visually approved.
9. **P2 — Brand-label cleanup.** Multiple comments, strings, and design contracts still say `Lexora`; the project's current desired production name is `Lexicon`. Change **user-visible** labels in a separate parity-controlled pass and verify accessibility, package naming, and release metadata rather than doing a blind global find/replace.
10. **P2 — Repository hygiene.** Root `.env` is tracked despite `.gitignore` ignoring it. Audit it privately for credentials; do not put secret values into QA logs, issues, or pull requests.

## Native acceptance matrix — strict, not ceremonial

For every route and both themes: capture actual Android emulator and iOS simulator frames, include home/dictionary/search/word/collections/notes/study/templates/reader, and check:
- **Illustration:** source alpha and organic edges, full cottages, bridges, daisies and books; no square crops, color-matched cream patches, haloes or text covering key painted subjects. `Fit` by itself is not proof of suitable composition.
- **Typography:** registered Playfair/Lora/Inter faces, intended hierarchy, readable definitions, dynamic type, long words, phonetic text, wrapping, and multi-line translations.
- **Layout:** phone width 320–432dp/pt, landscape, tablets, magnified content, bottom pill/search seal clearances, status and navigation bars, keyboard, drag/scroll gestures, full-screen back navigation.
- **Interaction:** each displayed filter changes results, note CRUD persists, bookmarks and favorites persist across launches, search history is real, list progress matches actual data, no shortcut implies a nonexistent feature.
- **Architecture:** native Compose/SwiftUI source, C++ engine tests, reliable CI compile and screenshot artifacts. Never claim compilation certifies pixels or behavior.
- **Accessibility:** 44x44pt minimum iOS and comparable Android targets wherever practical, roles/labels, VoiceOver/TalkBack ordering, contrast and motion sensitivity.

## Verification status

This branch contains targeted source fixes and original editable vector draft sources. A pull-request-triggered native screenshot workflow is proposed so future iterations can be tested **before merge**. **Current status: unverified visually and not certified 100%.** Source inspection is not an emulator capture. C++ and Android/iOS test results must be taken from the specific GitHub Actions run for this branch; do not imply a pass without those results.

**Never publish/re-sign an APK or merge unverified native changes just because a source edit was committed.** Owner-approved visual fidelity is the release gate.

## Continued line-level review — 2026-11-20

### Confirmed defects corrected on draft branch
- **Search presentation:** submitting a query on either platform no longer automatically sets the full word sheet's selected entry while navigating to the dedicated Results screen. Repeating the same route does not duplicate the back stack.
- **Bottom navigation correctness:** when using Back, the active tab is derived from the restored nested route; Android's system Back from a directly opened non-Home tab falls back to Home.
- **Real C++ prefix suggestions:** previously Android, KMM Android, and iOS labeled exact-distance-0 fuzzy search as "prefix search." Added a memory-owned UTF-8 string-array C ABI and connected the existing C++ DAWG prefix implementation to JNI and Swift. A C++ ABI test asserts `seren` → `serendipity`, unknown-prefix empty, and safe release.
- **Responsive SwiftUI header:** use measured SwiftUI container width instead of `UIScreen.main.bounds`, preserving split view and resized window support. Android/iOS header controls have 44dp/pt transparent hit targets.
- **Note card readability:** the compact two-column layout now reserves more ink width, limits short-body preview to a readable excerpt, and provides adequate height for the bookmark hit target and larger type. Needs visual verification to balance card proportions against the owner's precise mockup.
- **Saved item identity:** new Notes and Templates use UUIDs rather than millisecond timestamps on both platforms.
- **Honest affordances:** removed inert camera/voice-search buttons from the search overlay. The pictured glyphs remain decorative until permissioned recognition/OCR is actually implemented.
- **Native header semantics:** a clock formerly routed to unrelated destinations (Collections, Templates, Dictionary); the same illustrated header now renders destination-specific symbols with appropriate VoiceOver/TalkBack labels.

### Still open — never equate compilation with approval
1. **Full rendering fidelity:** compare actual iOS and Android daytime/nighttime frames against owner assets, per scene and per viewport, including illustrations with uninterrupted transparent boundaries and book-paper/ink tones. No images from the reference are assumed safe to crop.
2. **Dense layout and accessibility:** physically inspect 320pt/360pt/390pt/432pt phones, tablet split-screen, landscape, iOS Dynamic Type and Android font scales, keyboard focus, bottom navigation, note-card long titles, multiline etymologies, and 44pt accessibility. Header and note-card changes remain visually unverified until new screenshot galleries arrive.
3. **Feature completeness:** camera OCR, microphone recognition, verified speech voices, full language packs, advanced learning systems, and full accessibility behavior still require product and native test work; decorative camera/mic glyphs must not masquerade as live controls.
4. **Architecture:** verify database integrity and full offline data search beyond the embedded starter lexicon; C++ unit tests alone cannot prove the Android/iOS JNI/Swift runtime behavior.
5. **QA gates:** current screenshot gate verifies appearance/presence and foreground provenance; it cannot certify pixel-for-pixel parity or interaction sequences. Require owner visual review before merge/release.

The standalone `artwork/vector/cotswolds-village-*.svg` files are editable concept artwork, **not** approved replacements for the raster watercolor assets in production.

## Owner's real-device screenshots — quote epigraph and hero composition correction

Evidence: four owner-supplied screenshots with three narrow quote bookplates and the cropped-looking Search cottage header. The old component fixed mobile banners to ~92–120 dp/pt, forced quotes into a 58% region with a two-line limit, and rendered each hero within ~142–158 dp/pt regardless of the illustration's roughly 508×345 composition. The previous gradient washed over the full art width. These are structural layout defects, not a request to regenerate the paintings.

**Changes on review branch**
- Quote epigraphs are removed from the ends of Collections, Notes, Study Lists, Quick Templates, Dictionary, Search, Word Detail, and Expanded Exploration. The single remaining quote lives **inside the Library's article-index entrance**, before readers choose an article; it does not interrupt article prose.
- Both native epigraph components now size from the real available width, wrap quotations naturally rather than truncating them, and reserve uncompressed art space. For accessibility-sized text the painting sits below the words; its original transparent edges remain intact.
- Shared cottage/notes header scenes now reserve natural painting height based on measured container width rather than squeezing them into a short header or using physical-device dimensions. They use full-width `Fit` / `scaledToFit` with the protective ink wash restricted to the left reading margin; right-hand cottages, bridges and flowers are not bleached or artificially cropped.
- `ArticleCard` and the Android `LibraryReaderSheet` thumbnail avoid `Crop`/`scaledToFill` on original watercolor illustrations.
- The source inventory check now enforces the single-Library epigraph rule, safeguards article paintings, and verifies width-aware header contracts.

**Acceptance still open:** device screenshots at multiple phone widths and tablet layouts, portrait/landscape, light/night, larger text; compare full painted cottage/church/flowers, quote wraps, paper texture and floating search/navigation overlap with owner's originals. Changes are proposed source fixes, not proof of 100% pixel fidelity. Do not silently substitute the experimental SVG draft for approved original artwork.
