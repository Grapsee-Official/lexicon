# Lexora — Android (Kotlin + Jetpack Compose)

Native Android port of the Lexora mobile-first PWA. Phase 2 of the project —
the web prototype (Phase 1) is locked and this is its 1:1 native translation.

> **Tagline** — *Words. Wisdom. Worlds.*

---

## What's inside

A single-screen home feed built with **Jetpack Compose** + **Material 3**,
using a custom `LexoraTheme` that reproduces the exact brand palette
(Cotswolds English countryside: warm cream paper, sage green, deep forest,
terracotta, dusty blue, old-wood gold) and the same component hierarchy as
the locked web build.

```
app/
├── build.gradle.kts
├── proguard-rules.pro
└── src/main/
    ├── AndroidManifest.xml
    ├── java/com/lexora/
    │   ├── MainActivity.kt                 ← entry point (setContent + LexoraTheme)
    │   ├── data/
    │   │   ├── Models.kt                   ← WordEntry, Category, Article, etc.
    │   │   ├── LexoraData.kt               ← 18 dictionary entries + categories + articles
    │   │   ├── LexoraViewModel.kt          ← StateFlow-driven ViewModel (Zustand→ViewModel)
    │   │   └── LexoraPrefs.kt              ← SharedPreferences persistence
    │   └── ui/
    │       ├── theme/                      ← Color, Type, LexoraTheme
    │       ├── components/                 ← Header, Hero, SearchBar, WOTD, etc.
    │       └── screens/HomeScreen.kt       ← composition root
    └── res/
        ├── drawable-nodpi/                 ← rasterised brand + illustration assets
        ├── mipmap-*/                       ← launcher icons (192×192)
        ├── values/                         ← colors, strings, themes
        ├── values-night/themes.xml         ← dark-mode theme overrides
        └── xml/                            ← backup + data-extraction rules
```

---

## Requirements

| Tool            | Version                |
|-----------------|------------------------|
| Android Studio  | Hedgehog (2023.1.1)+   |
| AGP             | 8.6.0                  |
| Kotlin          | 2.0.21                 |
| Compose BOM     | 2024.10.01             |
| Gradle          | 8.9 (auto-downloaded)  |
| Java JDK        | 17                     |
| `minSdk` / `targetSdk` / `compileSdk` | 24 / 34 / 34 |

The project uses the [version catalog](app/gradle/libs.versions.toml) pattern
for dependency management.

---

## Build instructions

### 1. Open the project

1. Launch Android Studio.
2. `File → Open…` and select the **`native/android/`** folder.
3. Wait for Gradle sync. If Studio complains that the Gradle wrapper jar is
   missing, accept the prompt to generate it (or run `gradle wrapper` from
   a terminal that has Gradle installed).

### 2. Generate the Gradle wrapper (one-time)

This repo intentionally does **not** ship the binary `gradle-wrapper.jar`.
Generate it locally so you have the exact version pinned in
`gradle/wrapper/gradle-wrapper.properties`:

```bash
cd native/android
gradle wrapper --gradle-version 8.9
```

(After this, the standard `./gradlew` and `gradlew.bat` scripts will appear.)

### 3. Build & run

```bash
# Debug APK
./gradlew :app:assembleDebug

# Install on a connected device / emulator
./gradlew :app:installDebug
```

Or use Android Studio's **Run ▶** button.

> The `applicationId` is `com.lexora` (debug build adds the `.debug` suffix).

---

## Brand spec — exact translations

| Web token              | Hex        | Kotlin constant         |
|------------------------|------------|-------------------------|
| `--background`         | `#FAF3E9`  | `WarmCream`             |
| `--primary`            | `#677554`  | `BrandGreen`            |
| `--deep` / foreground  | `#31462F`  | `DeepForest`            |
| `--accent`             | `#C98267`  | `Terracotta`            |
| `--highlight`          | `#A58A61`  | `OldWoodGold`           |
| `--secondary`          | `#7C9297`  | `DustyBlue`             |
| `--card`               | `#FBF7EE`  | `CardSurface`           |
| WOTD right-panel cream | `#FBF4EA`  | `WotdRightSurface`      |
| Dark `--background`    | `#1A1F28`  | `DarkBackground`        |
| Dark `--accent`        | `#D4A45A`  | `DarkAccent`            |

### Fonts

The web build uses **Fraunces** (display), **Inter** (body), **Lora**
(long-form dictionary text). For an offline-buildable first cut, this port
uses the system `Serif` / `SansSerif` families with the **same visual
hierarchy** (serif headline, sans-serif body, serif long-form).

To upgrade to the real Google Fonts:

1. Download Fraunces, Inter, Lora TTFs from <https://fonts.google.com>.
2. Drop them into `app/src/main/res/font/` — lowercase + underscore, e.g.
   `fraunces_regular.ttf`, `inter_medium.ttf`, `lora_italic.ttf`.
3. In `ui/theme/Type.kt`, replace each `FontFamily.Serif` / `SansSerif`
   constant with a `FontFamily(Font(R.font.fraunces_regular), …)`
   constructor — uncomment the helper families already declared at the top
   of that file.

No other code changes required.

---

## Component → Web mapping

| Android component                         | Web component                              |
|-------------------------------------------|--------------------------------------------|
| `LexoraHeader.kt`                         | `src/components/lexora/header.tsx`         |
| `Hero.kt` + `LeafSprig.kt`                | `src/components/lexora/hero.tsx`           |
| `FloatingSearchBar.kt`                    | `src/components/lexora/floating-search-bar.tsx` |
| `TrendingTagRow.kt`                       | `src/components/lexora/trending-tag-row.tsx` |
| `WordOfTheDayCard.kt`                     | `src/components/lexora/word-of-the-day-card.tsx` |
| `CategoryCard.kt`                         | `src/components/lexora/category-card.tsx`  |
| `ArticleCard.kt`                          | `src/components/lexora/article-card.tsx`   |
| `SectionHeader.kt`                        | `src/components/lexora/section-header.tsx` |
| `BottomNav.kt`                            | `src/components/lexora/bottom-nav.tsx`     |
| `HomeScreen.kt`                           | `src/app/page.tsx` (home tab branch)       |
| `LexoraViewModel.kt`                      | `src/lib/app-store.ts` (Zustand store)     |
| `LexoraData.kt`                           | `src/lib/lexora-data.ts`                   |

### Hard requirements honoured

- **Tagline** `Words. Wisdom. Worlds.` is rendered **title-case**, not upper-cased.
- **WOTD card** is a **strict 2-column split** (40% left / 60% right), both
  sides solid colour (no gradient blending between panels). The
  watercolour illustration is masked with `linear-gradient(to right,
  transparent 0%, black 35%)` exactly as in CSS — implemented via an
  offscreen Compose layer + `BlendMode.DstIn`.
- **Article cards** are **horizontal** (`Row`) with a fixed 120 dp height
  and 224 dp width.
- **Category cards** are borderless / seamless.
- **Bottom nav** is a floating pill with a 40 dp centre Search FAB.
- **All icons** use the `Icons.Outlined.*` family — the thinnest standard
  Material line weight, matching the web's `strokeWidth={0.8}` aesthetic.
- **State** is persisted via SharedPreferences (`lexora_prefs.xml`):
  saved word IDs, recent searches, dark-mode toggle.

---

## Known follow-ups

- **Search modal & detail sheet**: the web build has a `SearchModal` and
  `WordDetailSheet` (vaul-based bottom sheets). These are out-of-scope for
  this Phase 2 cut — clicking the search bar / WOTD card updates the
  ViewModel state but does not yet render an overlay. A `ModalBottomSheet`
  Compose replacement is the natural next step.
- **Dictionary / Collections / More** tabs render a placeholder title for
  now; the data layer (`LexoraData.getAlphabetSections`,
  `searchWords`, `getWordById`) is already in place to populate them.
- **Launcher icon**: a single 192×192 PNG is reused across all density
  buckets. For production, generate proper per-density PNGs or an adaptive
  icon (foreground + background layers).

---

## License

Project-internal — matches the rest of the Lexora monorepo.
