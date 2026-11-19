# Lexora — illustrated Home reference contract

This is an implementation and test contract, not a declaration of visual parity.
**Reference:** the user-provided 864 × 1821 light and 852 × 1846 nocturne Home design screenshots (19 November 2026).
**Defect evidence:** the user-marked Android comparison crop (669 × 869).

## Visual identity

The app must feel like an illustrated English countryside book rather than a standard modern dashboard:
- Faint warm old-paper ground, warm ivory raised bookplates, forest/sage ink, old-brass detailing.
- Nocturne is the *same* composition, under blue-black ink and golden lamplight, not an inverted UI.
- Preserve uncropped watercolor botanical edges; images float within bookplates rather than being forced into full-bleed square photos.
- Use bundled Playfair Display for literary display headings, Inter for short controls, and Lora for long prose. The screenshots do not embed font metadata; exact typeface identity requires human visual review.
- Thin 1 px borders, soft organic corners, and restrained shadows. Avoid generic brightly coloured cards.

## Homepage anatomy / native styling

| Region | Reference intent | Implementation |
| --- | --- | --- |
| Trending words | Airy, low-contrast pill labels with clear separation and grid access | 30+ pt visual height; horizontal scroll; buttons remain accessible |
| Word of the Day | Bookplate split approx 39/61, vivid sage or midnight left panel, scenic artwork on the right | Natural content, full pronunciation action, no truncating definition or word on compact devices |
| Explore by Category | Four small painted bookplates, watercolor edges fully visible, compact captions | Viewport-measured 78–108 pt widths, four visible on standard phones, 105 pt height; aspect-fit art |
| From the Library | Reading cards with framed illustration, legible serif titles, reading-time metadata | Compact horizontally scrollable 174 × 90 pt editorial tiles |
| Bottom navigation | Quiet cream/ink capsule with a raised search seal, legible icons and labels | Native tab semantics, independent center search, preserved scroll breathing room |
| Canvas | Pale aged-paper ivory / blue-black nocturne as in the supplied targets | Shared Android + iOS palette, editable paper material and original watercolor raster art |

## Editable artwork provenance

The original detailed countryside watercolor assets remain native source images. The following **new** reusable botanical designs are 100% path/vector editable, with no embedded PNG/JPEG:
- `artwork/vector/lexora-sprig-parchment.svg`
- `artwork/vector/lexora-sprig-midnight.svg`
- `artwork/vector/lexora-english-garden-bookplate-parchment.svg`
- `artwork/vector/lexora-english-garden-bookplate-midnight.svg`

The five-leaf sprig's path coordinates match the native `LeafSprig` (Compose Canvas) and `BotanicalSprig` (SwiftUI Canvas). These vector source files are intentionally not used as replacements for rich watercolor scenes.

## QA acceptance

- Build C++17 with its tests, Android debug native + unit tests, and SwiftUI iOS simulator. Do not publish release/APK.
- Use the approved **Wanderlust** editorial fixture only in debug screenshot mode; ordinary users retain rotating Word of the Day.
- Verify the light Home's sage panel begins around 44.3% of screenshot height with a ±3.5% device/safe-area tolerance.
- Assert the five bundled Playfair Display / Inter / Lora files are byte-identical across both platforms and registered on iOS.
- Verify that all 620 native paper-grain marks match their separate, editable light/dark material SVG coordinates.
- Capture and review **26 nonblank screenshots per platform**, across all implemented routes in light + dark mode. Blank-pixel checks prove *render presence*, not appearance fidelity.
- Check 320–432 dp phone widths and accessibility text size; cards must never clip essential content or block gestures.
- Compare screenshots side by side against the user's reference at equal viewport sizes and inspect typography, paper material, border rhythm, crop, color, nav safe areas, and available touch targets.
- Automated checks are necessary, but **100% visual parity is not certified without source-to-render evidence on every required screen**. Never label a successful compilation as visual approval.
