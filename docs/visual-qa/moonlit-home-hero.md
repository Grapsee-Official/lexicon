# Moonlit Home hero — integration contract

The supplied Moonlit Cottage Storybook Vignette is a 1536 × 1024 RGBA illustration. Use its original pixels, not a screenshot, generated substitute, opaque background or recolored derivative.

## Native composition

- Dark Home only: aspect-fit the scene on the trailing side, with room for the existing headline, paragraph and floating search control.
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
