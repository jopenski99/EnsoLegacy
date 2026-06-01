# Raw traced SVGs (not used by the app)

These are auto-traced (raster→SVG) versions of the onboarding illustrations.
They are **not** rendered by the app — they were too heavy and flat to use as
Android `VectorDrawable`s (e.g. step 2 had ~2,987 paths / 1.8 MB), and Compose
can't render SVG from `assets/` anyway.

Kept here only for reference. The shipping illustrations are **raster**
(PNG/WebP) under `app/src/main/res/drawable-nodpi/`. Safe to delete this folder
once those are in place.
