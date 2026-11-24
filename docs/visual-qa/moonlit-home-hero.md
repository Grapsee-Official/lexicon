# Moonlit Home hero — integration contract

The supplied Moonlit Cottage Storybook Vignette is a 1536 × 1024 RGBA illustration. Use its original pixels, not a screenshot, generated substitute, opaque background or recolored derivative.

## Native composition

- Dark Home only: aspect-fit the scene on the trailing side, with room for the existing headline, paragraph and floating search control.
- Use the shared midnight-paper canvas (#07121C) and a restrained blue ambient gradient behind the painting. Fade into the canvas at the header and feed boundary. Preserve the original warm gold, moon and botanical details.
- Use a continuous reading scrim rather than a separate rectangular panel; keep synthetic stars out of this hero.
- Keep light mode, feed order, and search navigation unchanged.

## Install the exact asset

```sh
python3 scripts/install_moonlit_home_hero.py "/path/to/Moonlit Cottage Storybook Vignette(2).png"
python3 scripts/install_moonlit_home_hero.py --check
```

The installer verifies the exact approved SHA-256 and copies the original bytes into Android's drawable and both iOS resource locations. It does not modify the web artwork.

## Verification status

Native composition source implemented. The supplied PNG is **not yet in GitHub**: the current connector cannot transfer the uploaded binary. Do not treat the old placeholder asset or successful source commit as completed visual integration.

After installation, build and capture dark and light Home on both native platforms (320–432 pt/dp, tablet and enlarged text), review moon/cottage/book visibility, readable copy, no hard seams, and search accessibility. Visual sign-off requires rendered native screenshots, not a static mockup.
