# Lexora native watercolor fidelity — ongoing issue ledger

Source of truth: user-supplied 10 reference UI images (19 November 2026),
the user-marked Android screenshots, and direct inspection of original native
PNG files. This document is **not** a declaration of 100% visual fidelity.

## Non-negotiable rules

- Original detailed watercolor art is the design foundation on every affected
  screen. Do not crop a complete painting, hide it with cream circles, erase
  it behind icons, or substitute inferior scene approximations.
- Respect naturally feathered watercolor edges. Icons and letters go on top of
  art without artificial opaque background cutouts where the art should remain.
- Keep search-bar shape and placement; the user's red markup targeted paintings,
  not a requested redesign of the search control.
- Keep the last illustration scrollable completely clear of floating navigation.
- Use actual light/dark screenshots and compact/tablet layouts before visual
  sign-off. Green builds alone do not prove fidelity.
- No release publication or APK signing in artwork-repair work.

## Shared-component and screen-level tasks (OPEN until screenshot sign-off)

- [ ] Hero scenes: verify complete countryside compositions throughout Home,
      Dictionary, Search, Collections, Notes, Study List, Quick Templates and
      word-detail routes. Shared sizing/fit code was changed previously.
- [ ] Banner continuity: confirm no lower cream strip or artificial cut line on
      all bridge/landscape banners. The damaged bridge source now has only its
      effectively transparent 24-pixel bottom tail removed, while its watercolor
      pigment is retained.
- [ ] Collection cards: verify no circular cutout behind action arrows on
      Favorites, Bookmarks, My Notes and Study List. Four light/dark source
      paintings were restored using detailed neighboring watercolor strokes.
- [ ] Other artwork controls/cards: confirm controls remain legible without
      solid patches or clipping painted compositions; particularly Home WOTD,
      Etymology, Usage Examples, note cards, study cards, templates and search.
- [ ] Word-detail source scene: verify repaired cottage/bridge sky and the
      enlarged etymology illustration render at useful scale without cropping.
- [ ] Bottom scroll clearance: verify every final artwork and content section
      scrolls above floating nav in light/dark, compact/standard/tablet, and
      enlarged-text layout.
- [ ] Verify original UI references at matching aspect ratios; document any
      remaining divergences before asserting artistic fidelity.

## Actual source-art findings and corrections

- **Fixed in PNG source, awaiting native screenshot validation:** circular
  transparent holes in four collection cards, cottage hero and bridge hero,
  and the floral decoration; faded screenshot-label fragments in Bookmarks and
  floral artwork. Both platforms share identical repaired PNG bytes.
- **Fixed in PNG source, awaiting screenshot validation:** extra nearly fully
  transparent bottom rows of the bridge quote banner (not painted pigment).
- **Not a visible source defect:** black status-bar/history/search glyph RGB
  appears in some original hero PNGs, but its alpha is exactly zero. The
  artwork-restoration process intentionally leaves these pixels alone rather
  than inpainting valid scenes to solve an invisible issue.
- **Limit:** missing painted pixels were absent from the supplied originals.
  The narrowly localized restoration borrows original watercolor brushwork;
  it is not lossless recovery of the unavailable master artwork.

## Follow-up defects found in native screenshot review

- **Android Home WOTD word wrapping:** run 44542164484 visibly split
  "Wanderlust" between lines, unlike the original UI reference. Responsive
  type sizing was corrected; check the next emulator captures before closing.
- **Collections quad-card arrows:** run 44542164484 rendered arrows too
  faint/small on detailed floral backgrounds. Both platforms now use
  higher-contrast arrow glyphs without opaque circular background patches;
  screenshot confirmation remains pending.

- **Word-detail Etymology painting overlap:** the prior green-run Android
  and iOS screenshots visibly place the books illustration over the lower
  etymology text. Both cards in the paired layout now reserve more vertical
  space (including an accessible-text variant); screenshot acceptance remains
  OPEN until the original text and painting are separately readable.

- **iOS build regression (run 44547551579):** Swift failed to type-check
  the enlarged word-detail screen after a Dynamic Type height change.
  WordDetailProfileScreen now explicitly reads dynamicTypeSize and separates
  the etymology/usage cards into a smaller view expression. This correction
  requires a fresh native iOS compile and screenshots before approval.

## Responsiveness audit — newly confirmed tablet issue

- **Android 800dp tablet at fontScale 1.3:** the former single-column
  short-circuit ignored available width, producing oversized one-card rows on
  Collections, My Notes, and Quick Templates. Grid column counts now account
  for both the minimum readable bookplate width and actual screen width.
- **Compact phones:** preserve single-column layout where two illustrated
  cards cannot fit. Maintain legible titles, captions, and original painting
  proportions rather than forcing a cramped grid.
- **JVM regression tests:** cover 320dp phone, typical phone, enlarged-text
  phone, 600dp intermediate device, and 800dp tablet, in normal and enlarged
  text configurations.
- **Expanded native evidence:** responsive debug-only QA now captures 13
  illustrated screens in each light/dark mode for both compact and tablet
  profiles (52 captures instead of 28). Screen-content fidelity and final
  bottom-scroll clearance remain OPEN until actual image review.

## Illustrated bookplate composition — new responsive correction

- **Dictionary panorama:** the 800dp Android tablet evidence (run 44542164484)
  showed the bottom stone-bridge watercolor as a small orphan image in
  otherwise empty space. Android fixed its image frame to only 76dp high;
  iOS used a similarly short scaledToFill/clipped treatment. Both now display
  the entire original 420:182 panorama in a centred responsive bookplate,
  capped at 640dp wide rather than cropping or stretching its watercolor.
- **Cross-screen quote banners:** formerly a universal 92–94dp height made
  paintings diminish on tablets. Shared Android and iOS components now give
  wide screens a more generous illustration area, while preserving familiar
  phone proportions and supporting larger text.
- [ ] Review light/dark phone, tablet, enlarged-type and final-scroll renders
      for each banner after the current native CI completes. No 100% fidelity
      claim is made from source edits alone.

## Readable ink and free watercolor inside note cards

- **Confirmed from run 44552228803 tablet evidence:** after fixing
  the two-column layout, My Notes text runs across the illustrated books,
  flowers and quill, obscuring both content and original artwork.
- **Android note cards:** reserve the left half for words and the right for
  the full uncropped watercolor scene. No painted region is covered by
  a cream text plate.
- **iOS note cards:** measure each card and allocate separate text and painting
  widths, with multi-line titles and full-fit paintings on compact devices.
- [ ] Recheck all Notes artwork, text and bookmark controls in light/dark,
      phone, 1.3×-text tablet, and iOS dynamic text screenshots.

## Verification and completion rule

Native Android, iOS and C++ must build/test on the merged source commit.
Review screenshots/artifacts for each affected screen and compare to the
supplied reference images before checking these boxes. Never interpret an
image-data edit or a green CI result as automatic 100% visual parity.
