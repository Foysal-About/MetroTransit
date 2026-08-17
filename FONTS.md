# Typography — San Francisco (SF Pro)

The app's entire type scale is defined in terms of two families:

| Family | Used for | Compose styles |
| --- | --- | --- |
| **SF Pro Display** | Large headings, fare amounts, timers — anything 20sp and up | `displayLarge…displaySmall`, `headlineLarge…headlineSmall`, `titleLarge`, plus call sites tagged `AppFont.display` |
| **SF Pro Text** | Body copy, labels, buttons, navigation, chips — below 20sp | `titleMedium`, `titleSmall`, `body*`, `label*`, and the default style every `Text` inherits |

The 20sp split is Apple's own optical-size guidance for when to switch from SF Text to
SF Display.

## The font files are not in this repository

SF Pro is Apple's typeface, and its license permits use only for designing and developing
software for Apple platforms. Shipping the files inside an Android APK is outside that
license, so they are deliberately **not** committed here — that decision is yours to make.

Because of that, the app resolves the fonts **by name at runtime** instead of through
generated `R.font` constants (see `ui/theme/Fonts.kt`). The consequence:

- **Files absent** → the app falls back to the platform sans-serif and looks exactly as it
  does today. Nothing crashes, nothing needs commenting out.
- **Files present** → every screen picks up San Francisco on next launch. No code change.

`MetroTransitTheme.fonts.isSanFrancisco` tells you which of the two is live, if you want to
assert it in a test or surface it on a debug screen.

## Activating SF Pro

Drop the files into `app/src/main/res/font/` using these exact names (lowercase and
underscores are an Android resource requirement). `.otf` and `.ttf` both work.

```
app/src/main/res/font/
  sf_pro_display_light.otf        ← optional
  sf_pro_display_regular.otf
  sf_pro_display_medium.otf
  sf_pro_display_semibold.otf
  sf_pro_display_bold.otf
  sf_pro_display_heavy.otf        ← maps to FontWeight.ExtraBold
  sf_pro_display_black.otf
  sf_pro_text_light.otf           ← optional
  sf_pro_text_regular.otf
  sf_pro_text_medium.otf
  sf_pro_text_semibold.otf
  sf_pro_text_bold.otf
  sf_pro_text_heavy.otf
  sf_pro_text_black.otf
```

Notes:

- **Partial sets are fine.** Only the weights present get registered; Compose synthesises
  the rest. The app uses Normal, Medium, SemiBold, Bold, ExtraBold and Black.
- **One family is enough to start.** If you supply only `sf_pro_text_*`, it stands in for
  Display as well (and vice versa).
- Apple distributes SF Pro from <https://developer.apple.com/fonts/> as a `.dmg` of `.otf`
  files, named e.g. `SF-Pro-Text-Semibold.otf` → rename to `sf_pro_text_semibold.otf`.

## If you need something ship-safe instead

If bundling SF Pro is not acceptable for distribution, the usual substitute on Android is
**Inter** (SIL Open Font License, redistributable) — it is metrically close to SF and was
designed for the same purpose. Swapping it in is a one-line change in
`Fonts.kt`: add the `inter_*` files and use them as the fallback in `appFontFamilies()`
instead of `FontFamily.SansSerif`.

## Deliberate exception

`ResultScreen.kt` keeps `FontFamily.Monospace` for the departure-board clock and platform
readouts. That is intentional — the tabular, fixed-width look is the point there.
