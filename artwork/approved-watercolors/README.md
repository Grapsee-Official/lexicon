# Approved Lexora watercolor artwork

Each new illustration is an independent transparent light-mode painting. No UI
labels, buttons, panel colors or screenshots are embedded in the artwork.
Install the reviewed artwork pack by extracting it at the repository root.
Android will prefer `paint_light_<scene>.webp` in drawable-nodpi when present;
the current editable SVG and VectorDrawable sources remain intact as fallback.
Dark mode continues to use its independently authored vectors.

| Resource stem | Reference placement |
| --- | --- |
| paint_light_search_hero | Search header cottage and fence |
| paint_light_search_quote | Search quote: stone bridge and church |
| paint_light_daily_bridge | Word of the Day: cottage and bridge |
| paint_light_detail_meadow | Dictionary entry: fence and daisies |
| paint_light_detail_quote | Dictionary entry: church panorama |
| paint_light_notes_hero | Notes: books, feather, inkwell and vase |
| paint_light_notes_quote | Notes: meadow path and church |
| paint_light_templates_hero | Templates: cottage and lantern |
| paint_light_study_hero | Study List: cottage and garden |
| paint_light_study_quote | Study List: double-arch bridge panorama |

The approved paintings are raster watercolor masters, not editable vector
paths. Keep their original RGBA files in the source pack; do not wrap a bitmap
in an SVG and claim it is vector artwork. Native text, controls and cards remain
separate editable Compose elements. Render the full alpha silhouette using
ContentScale.Fit; do not apply additional edge masks or crop the art.

