# Moonlit Home hero — integration contract

The supplied Moonlit Cottage Storybook Vignette is a 1536 × 1024 RGBA illustration. Use its original pixels, not a screenshot, generated substitute, opaque background or recolored derivative.

## Native composition

- Dark Home only: paint the scene on a viewport-sized native canvas with responsive landmark alignment: aim for the source moon at 63% viewport width, move the art past the right boundary, and carry the foreground behind the search pill. Grade saturation at render time (0.83), preserving the original PNG bytes. Anchor the foreground behind the search control, with the headline in a separate readable left region.
- Use the shared midnight-paper canvas (#07121C) and a restrained blue ambient gradient behind the painting. Fade into the canvas at the header and feed boundary. Preserve the original warm gold, moon and botanical details.
- Use a continuous reading scrim rather than a separate rectangular panel; keep synthetic stars out of this hero.
- Keep light mode, feed order, and search navigation unchanged.

## Install the exact asset

```sh
python3 scripts/install_moonlit_home_hero.py
python3 scripts/install_moonlit_home_hero.py --check
```

The installer verifies byte-for-byte identity with `All real art/Moonlit Cottage Storybook Vignette.png` and copies the original bytes into Android's drawable and both iOS resource locations. It does not modify the web artwork.

## Verification status

Native composition implemented and the exact user-uploaded artwork is installed at all three native resource paths using the same Git blob as the approved original. Android test APK publication is gated on source parity and packaged hero dimensions. Device-level visual sign-off remains pending.

After installation, build and capture dark and light Home on both native platforms (320–432 pt/dp, tablet and enlarged text), review moon/cottage/book visibility, readable copy, no hard seams, and search accessibility. Visual sign-off requires rendered native screenshots, not a static mockup.

## Continuous night composition

Home uses a single page-sized midnight gradient (`#040E18` → `#08121B` → `#09131C`) behind the transparent header and feed. Android uses an isolated Compose canvas with two alpha masks (`DstIn`); iOS uses two SwiftUI masks. The original source PNG remains byte-identical. The sky/roof are deliberately quieter than the foreground, the outer foliage dissolves, and the search capsule uses the same ink family. Reference screenshot sampling informed the grade; this is not device-rendered sign-off. The reference contains a different original painting composition, so exact pixel equivalence cannot be achieved through positioning alone.
