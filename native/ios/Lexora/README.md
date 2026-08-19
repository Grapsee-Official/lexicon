# Lexora — iOS (Swift + SwiftUI)

Native translation of the locked Lexora web prototype (Phase 1).
Built with SwiftUI, iOS 16+, no third-party dependencies.

> Source root: `/home/z/my-project/native/ios/Lexora/`
> Asset source: `/home/z/my-project/native/ios/Lexora/Resources/`

---

## 1. Build with Xcode

This folder ships as a **folder of Swift sources + PNG assets**, not a complete `.xcodeproj`. To build it locally:

1. Open Xcode → **File → New → Project…**
2. Choose **iOS → App** template.
3. Product name: **Lexora**. Interface: **SwiftUI**. Language: **Swift**.
4. Save the project anywhere (e.g. `~/Developer/Lexora`).
5. In Finder, drag every file from `native/ios/Lexora/` into the new project's `Lexora` group, preserving the folder structure:
   - `LexoraApp.swift`
   - `ContentView.swift`
   - `Theme/`        (3 files)
   - `Models/`       (1 file)
   - `Data/`         (2 files)
   - `Screens/`      (1 file)
   - `Components/`   (9 files)
   - `Info.plist`    (replace the auto-generated one)
   - `Resources/`    (all PNGs)
6. When prompted, check:
   - ✅ **Copy items if needed**
   - ✅ **Create groups**
   - ✅ **Add to target: Lexora**
7. Select a simulator (iPhone 15 / iPhone 16 Pro recommended) and press **⌘R**.

### Project settings to verify

- **Target → General → Minimum Deployments**: iOS 16.0
- **Target → General → Identity → Display Name**: `Lexora`
- **Target → Build Phases → Copy Bundle Resources** must include every PNG in `Resources/`.
  (Dragging with "Create groups" usually handles this; check if any image is missing in the list.)

---

## 2. Custom fonts (Fraunces / Inter / Lora)

The app currently **falls back to system serif / sans-serif** so it compiles
out of the box. To get the exact typography:

1. Download **Fraunces**, **Inter**, **Lora** from [Google Fonts](https://fonts.google.com).
2. Drag the `.ttf` files into the `Resources/` group in Xcode
   (check "Copy items if needed" + "Add to target: Lexora").
3. Open `Info.plist` and uncomment the entries under `UIAppFonts`:

```xml
<key>UIAppFonts</key>
<array>
    <string>Fraunces-Regular.ttf</string>
    <string>Fraunces-Medium.ttf</string>
    <string>Fraunces-SemiBold.ttf</string>
    <string>Inter-Regular.ttf</string>
    <string>Inter-Medium.ttf</string>
    <string>Inter-SemiBold.ttf</string>
    <string>Lora-Regular.ttf</string>
    <string>Lora-Italic.ttf</string>
</array>
```

4. Build & run — `LexoraTypography.swift` will pick up the custom PostScript
   names automatically and resolve `Font.lexoraDisplay/lexoraBody/lexoraLong`
   to the real fonts.

> Tip: Use [Google Fonts Helper](https://gwfh.mranftl.com) to grab every weight
> in one zip.

---

## 3. Asset catalogue

All assets ship as **PNG** in `Resources/`:

| File                                | Used by                              |
| ----------------------------------- | ------------------------------------ |
| `lexora-mark.png` (+@2x, @3x)       | `LexoraHeader` brand mark            |
| `lexora-countryside.png` (+@2x, @3x)| `Hero` right column                  |
| `wotd-bg-light.png`                 | `WordOfTheDayCard` right-panel image |
| `wotd-bg-dark.png`                  | `WordOfTheDayCard` (dark mode)       |
| `cat-vocabulary.png`                | `CategoryCard`                       |
| `cat-geography.png`                 | `CategoryCard`                       |
| `cat-literature.png`                | `CategoryCard`                       |
| `cat-science.png`                   | `CategoryCard`                       |
| `article-1.png` / `article-2.png` / `article-3.png` | `ArticleCard`     |
| `avatar-default.png`                | `LexoraHeader` profile avatar        |
| `hero-light.png` / `hero-dark.png`  | reserved (Phase 3)                   |
| `paper-texture.png`                 | reserved (Phase 3 — paper noise)     |

`lexora-mark.svg` and `lexora-countryside-day.svg` were rasterized to PNG via
`sharp` at 1x/2x/3x using `rasterize-assets.js` (kept alongside this README
for reproducibility).

> To regenerate assets:
> ```bash
> cd /home/z/my-project && node native/ios/Lexora/rasterize-assets.js
> ```

---

## 4. Project structure

```
Lexora/
├── LexoraApp.swift             ← @main entry, owns ViewModel + ThemeState
├── ContentView.swift           ← Root: Header + active tab + BottomNav + sheets
├── Info.plist
├── Theme/
│   ├── LexoraColors.swift      ← Color(hex:) + exact brand palette (light/dark)
│   ├── LexoraTypography.swift  ← Fraunces / Inter / Lora + fallbacks
│   └── LexoraTheme.swift       ← ThemeState + LexoraThemeModifier
├── Models/
│   └── LexoraModels.swift      ← WordEntry, Category, Article, TrendingTag, Tab
├── Data/
│   ├── LexoraData.swift        ← All 18 dictionary entries + helpers
│   └── LexoraViewModel.swift   ← ObservableObject (Zustand → @MainActor)
├── Components/
│   ├── LexoraHeader.swift
│   ├── Hero.swift
│   ├── FloatingSearchBar.swift
│   ├── TrendingTagRow.swift
│   ├── WordOfTheDayCard.swift
│   ├── CategoryCard.swift
│   ├── ArticleCard.swift
│   ├── SectionHeader.swift
│   └── BottomNav.swift
├── Screens/
│   └── HomeScreen.swift        ← Composes all home components in a ScrollView
└── Resources/
    └── (all PNG assets)
```

---

## 5. State management

`LexoraViewModel` is the single source of truth — an `@MainActor ObservableObject`
that translates the Zustand store 1:1:

| Zustand state / action    | Swift equivalent                              |
| ------------------------- | --------------------------------------------- |
| `activeTab`               | `@Published var activeTab: LexoraTab`         |
| `selectedWord`            | `@Published var selectedWord: WordEntry?`     |
| `searchOpen`              | `@Published var searchOpen: Bool`             |
| `menuOpen`                | `@Published var menuOpen: Bool`               |
| `savedWordIds`            | `@Published var savedWordIds: [String]`       |
| `recentSearches`          | `@Published var recentSearches: [String]`     |
| `toggleSave` / `isSaved`  | `toggleSave(_:)` / `isSaved(_:)`              |
| `addRecentSearch`         | `addRecentSearch(_:)`                         |
| `clearRecentSearches`     | `clearRecentSearches()`                       |
| `persist` middleware      | `UserDefaults` (keys: `lexora.savedWordIds`, `lexora.recentSearches`) |

Injected at the App root via `.environmentObject(viewModel)` and read
in any view with `@EnvironmentObject`.

---

## 6. Brand tokens (locked)

From `src/app/globals.css` — exact hex:

| Token              | Hex       | Used for                          |
| ------------------ | --------- | --------------------------------- |
| `--background`     | `#FAF3E9` | Warm cream main bg                |
| `--primary`        | `#677554` | Brand green (WOTD left, accents)  |
| `--deep`/`--foreground` | `#31462F` | Body text                    |
| `--accent`         | `#C98267` | Terracotta (bell dot, etc.)       |
| `--highlight`      | `#A58A61` | Old-wood gold (View all →)        |
| `--secondary`      | `#7C9297` | Dusty blue                        |
| `--card`           | `#FBF4EA` | Card surface                      |

Dark-mode variants live in `LexoraColors` (midnight blue + antique gold).

---

## 7. Known follow-ups (Phase 3)

The home screen + all 10 components are translated 1:1. The full
Dictionary/Collections/More screens from the web build are intentionally
lean here (`SimpleBrowseScreen`) — they can be ported next once the user
confirms the home build matches the web prototype.
