# Typography — SF Pro (default) or Avenir Next

The app can be set in either of two custom typefaces, or in the device font. The whole type
scale is defined in terms of two *cuts* rather than two named fonts:

| Cut | Used for | Compose styles |
| --- | --- | --- |
| **display** | Large headings, fare amounts, timers — anything 20sp and up | `displayLarge…displaySmall`, `headlineLarge…headlineSmall`, `titleLarge`, plus call sites tagged `AppFont.display` |
| **text** | Body copy, labels, buttons, navigation, chips — below 20sp | `titleMedium`, `titleSmall`, `body*`, `label*`, and the default style every `Text` inherits |

The 20sp split is Apple's own optical-size guidance for when to switch from SF Text to SF
Display. Avenir Next has no optical-size pair, so it supplies the same files for both cuts
and the split has nothing to do — that is expected, not a fallback.

## The switch

One line, in `ui/theme/Fonts.kt`:

```kotlin
var Preferred by mutableStateOf(SanFrancisco)   // ← AppTypeface.Companion; the default
```

SF Pro is the default. Set it to `AvenirNext` for Avenir Next, or `System` for the device
font. Three ways to drive it:

```kotlin
// 1. Permanent choice — edit the default in Fonts.kt.
var Preferred by mutableStateOf(AvenirNext)

// 2. At runtime, from anywhere (a debug row, a settings toggle). It is snapshot state, so
//    the entire UI restyles on the next frame — no restart.
AppTypeface.Preferred = AppTypeface.AvenirNext

// 3. For one subtree only — a preview, a screenshot test, a side-by-side.
MetroTransitTheme(typeface = AppTypeface.AvenirNext) { … }
```

`MetroTransitTheme.fonts.typeface` reports which family actually loaded (null = platform
font), with `isSanFrancisco` / `isAvenirNext` for a quick check.

## The font files are not in this repository

SF Pro is Apple's typeface and its license permits use only for developing software for
Apple platforms. Avenir Next is licensed separately (it ships with macOS/iOS, and is sold
by Monotype for other uses). Shipping either inside an Android APK is a licensing decision
that is yours to make, so neither is committed here.

Because of that, both families are resolved **by name at runtime** instead of through
generated `R.font` constants (see `ui/theme/Fonts.kt`). The consequence:

- **Files absent** → the app falls back and looks exactly as it does today. Nothing
  crashes, nothing needs commenting out.
- **Files present** → every screen picks up the family on next launch. No code change.

The fallback order is: the family you asked for → the other custom family → the platform
sans-serif. With `SanFrancisco` as the default, dropping in only `avenir_next_*` files still
styles the app in Avenir Next rather than leaving it on the device font.

## Activating a family

Drop the files into `app/src/main/res/font/` using these exact names (lowercase and
underscores are an Android resource requirement). `.otf` and `.ttf` both work.

### SF Pro (the default)

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

Apple distributes SF Pro from <https://developer.apple.com/fonts/> as a `.dmg` of `.otf`
files, named e.g. `SF-Pro-Text-Semibold.otf` → rename to `sf_pro_text_semibold.otf`.

### Avenir Next

One prefix, since the family has no Display/Text pair:

```
app/src/main/res/font/
  avenir_next_ultralight.otf      ← optional
  avenir_next_regular.otf
  avenir_next_medium.otf
  avenir_next_demibold.otf        ← maps to FontWeight.SemiBold
  avenir_next_bold.otf
  avenir_next_heavy.otf           ← maps to FontWeight.ExtraBold
```

Avenir Next's own cut names are what the suffixes follow: UltraLight, Regular, Medium,
DemiBold, Bold, Heavy. `avenir_next_light`, `_semibold` and `_black` are accepted as
aliases if that is how your files are named. On macOS the family lives in
`/System/Library/Fonts/Avenir Next.ttc`; a `.ttc` collection has to be split into
individual `.otf`/`.ttf` files before Android can read it.

Notes for both:

- **Partial sets are fine.** Only the weights present get registered; Compose synthesises
  the rest. The app uses Normal, Medium, SemiBold, Bold, ExtraBold and Black — Avenir Next
  has no Black cut, so Heavy stands in for it.
- **One cut is enough to start.** If you supply only `sf_pro_text_*`, it stands in for
  display as well (and vice versa).

## If you need something ship-safe instead

If bundling a licensed family is not acceptable for distribution, the usual substitutes on
Android are **Inter** (metrically close to SF) or **Nunito Sans** / **Montserrat** (closer
to Avenir's geometric humanist shapes) — all under the SIL Open Font License and
redistributable. Adding one is a small change in `Fonts.kt`: give `AppTypeface` another
entry with the new prefix, and the rest of the app needs no edits.

## Deliberate exception

`ResultScreen.kt` keeps `FontFamily.Monospace` for the departure-board clock and platform
readouts. That is intentional — the tabular, fixed-width look is the point there.
