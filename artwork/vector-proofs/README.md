# Lexora artwork vector proofs

This is an offline design experiment, not a production asset migration. The 12
original PNGs in `All real art/` remain the canonical paintings.

From the repository root:

```sh
python -m pip install pillow numpy opencv-python scikit-image
python scripts/trace_home_artwork.py
```

Use `--only Vocabulary` for a single motif, or tune `--width` and `--colors`.
The output contains actual SVG path geometry and opacity; no embedded PNGs.
The converter traces the original pixels, including their alpha coverage.
It does not generate replacement artwork.

## Acceptance before native integration

* Compare at actual hero and category display sizes over the native midnight
  palette and at enlarged sizes. Compare the painting against the source and
  the design reference. Look for contour seams, posterization, lost foliage
  and changed highlights.
* Automatic tracing is lossy. SVG itself does not fix composition, background
  integration or responsiveness. Preserve the painting if tracing loses the
  intended texture.
* Avoid parsing millions of vector coordinates during Home startup.
  Android VectorDrawable supports only a subset of SVG. iOS asset catalogs
  have their own SVG restrictions. Export only reviewed, compatible paths;
  retain original raster paintings where faithful vectors are impractical.
* Light and dark artwork are separate design assets. Do not recolor the
  upcoming light-mode set by default.

The script intentionally writes to a separate proof directory and does not
modify any native app, build workflow or original artwork.
