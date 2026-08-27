# Lexicon — iOS (Swift + SwiftUI)

Native translation of the locked Lexicon web prototype (Phase 1).
Built with SwiftUI, iOS 16+, no third-party dependencies.

> Source root: `/native/ios/Lexicon/`
> Xcode project: `/native/ios/Lexicon.xcodeproj/project.pbxproj`
> Asset source: `/native/ios/Lexicon/Resources/` + `Assets.xcassets/`

---

## 1. Open the project in Xcode

This folder ships with a **generated `.xcodeproj`** at
`/native/ios/Lexicon.xcodeproj/`.

### Option A — Use the generated project file

1. Launch **Xcode 15.0+** (Xcode 14 also works on iOS 16 targets).
2. **File → Open…** and navigate to `/native/ios/Lexicon.xcodeproj`.
3. Xcode may offer to "fix up" the project — accept any auto-fix prompts
   (it sometimes rewrites the `project.pbxproj` to a newer object format).
4. Select a simulator (iPhone 15 / iPhone 16 Pro recommended) and press **⌘R**.

> **Note**: the `project.pbxproj` was hand-generated from a template. If Xcode
> refuses to open the project or reports "missing files":
> - In Xcode, select the **Lexicon** target → **Build Phases** →
>   **Compile Sources** and **Copy Bundle Resources**.
> - Use the **+** button to add any missing `.swift` / `.png` files from
>   `/native/ios/Lexicon/`.
> - This is a one-time fix-up — Xcode will save the corrected `pbxproj`.

### Option B — Start fresh in Xcode (recommended if Option A fails)

1. **File → New → Project…**
2. Choose **iOS → App** template.
3. Product name: **Lexicon**. Interface: **SwiftUI**. Language: **Swift**.
4. Save the project at `/native/ios/` (overwrite the existing `.xcodeproj`).
5. In Finder, drag every file from `/native/ios/Lexicon/` into the new
   project's `Lexicon` group, preserving the folder structure:
   - `LexiconApp.swift`
   - `ContentView.swift`
   - `Info.plist` (replace the auto-generated one)
   - `Assets.xcassets`
   - `Theme/`, `Models/`, `Data/`, `Screens/`, `Components/`, `Resources/`
6. When prompted, check:
   - ✅ **Copy items if needed** (only if you didn't save the project at
     the same location)
   - ✅ **Create groups**
   - ✅ **Add to target: Lexicon**
7. Press **⌘R**.

### Project settings to verify

- **Target → General → Minimum Deployments**: `iOS 16.0`
- **Target → General → Identity → Display Name**: `Lexicon`
- **Target → General → Identity → Bundle Identifier**: `com.lexicon.app`
- **Target → Build Phases → Compile Sources** must include every `.swift` file
  (the generated `pbxproj` lists all 23 sources).
- **Target → Build Phases → Copy Bundle Resources** must include every PNG in
  `Resources/` plus `Assets.xcassets`.

---

## 2. Run on the simulator

1. Open the project in Xcode (see §1).
2. Pick a simulator scheme from the toolbar dropdown:
   - **iPhone 15** (recommended — matches the web prototype's mobile width)
   - **iPhone 16 Pro**
   - **iPhone SE (3rd generation)** — for small-screen testing
3. Press **⌘R** (or click the ▶ Play button in the toolbar).
4. The simulator launches, installs Lexicon, and opens it.
5. To switch between light/dark mode in the simulator:
   - **Features → Toggle Appearance** (or **⇧⌘A**)
   - Or use the Lexicon header's moon/sun toggle (top-right).

### Run on a physical device

1. Plug in your iPhone (iOS 16+ required).
2. In Xcode, select your device from the toolbar dropdown.
3. Open **Target → Signing & Capabilities → Team** and pick your Apple ID.
   - If you don't have an Apple Developer account, use your personal Apple ID
     (free — limited to 3 apps, 7-day signing).
4. Press **⌘R**.

---

## 3. Custom fonts (Fraunces / Inter / Lora)

The app currently **falls back to system serif / sans-serif** so it compiles
out of the box. To get the exact typography from the web prototype:

### Step 1 — Download the fonts

1. Visit [Google Fonts — Fraunces](https://fonts.google.com/specimen/Fraunces).
2. Click **Download family** → unzip.
3. Repeat for **[Inter](https://fonts.google.com/specimen/Inter)** and
   **[Lora](https://fonts.google.com/specimen/Lora)**.

> Tip: Use [Google Fonts Helper](https://gwfh.mranftl.com) to grab every
> weight in one zip.

You need at least these weights:

| Font      | Files                                  | Role                         |
| --------- | -------------------------------------- | ---------------------------- |
| Fraunces  | `Fraunces-Regular.ttf`                 | Display / headings           |
|           | `Fraunces-Medium.ttf`                  | Display / medium headings    |
|           | `Fraunces-SemiBold.ttf`                | Display / semibold headings  |
| Inter     | `Inter-Regular.ttf`                    | UI body                      |
|           | `Inter-Medium.ttf`                     | UI body / medium             |
|           | `Inter-SemiBold.ttf`                   | UI body / semibold           |
| Lora      | `Lora-Regular.ttf`                     | Long-form dictionary text    |
|           | `Lora-Italic.ttf`                      | Italic examples              |

### Step 2 — Add them to the project

1. Drag all 8 `.ttf` files into the `Lexicon/Resources/` group in Xcode.
2. When prompted, check:
   - ✅ **Copy items if needed**
   - ✅ **Add to target: Lexicon**
3. Verify the files appear under **Target → Build Phases → Copy Bundle Resources**.

### Step 3 — Register them in `Info.plist`

Open `Lexicon/Info.plist` and find the `UIAppFonts` array. Uncomment the
entries so it looks like:

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

### Step 4 — Build & run

`LexiconTypography.swift` will pick up the custom PostScript names
automatically (via `Font.custom(_:size:relativeTo:)`) and resolve
`Font.lexiconDisplay/lexiconBody/lexiconLong` to the real Google Fonts.

---

## 4. Asset catalogue

### `Assets.xcassets/`

| Entry                          | Used for                                        |
| ------------------------------ | ----------------------------------------------- |
| `AccentColor.colorset`         | Tint colour — brand green `#677554` (light) / `#1D2A3A` (dark) |
| `AppIcon.appiconset`           | App icon — single 1024×1024 entry referencing `icon-512.png` |

### `Resources/` (loose PNGs)

All illustrations ship as **PNG** in `Resources/`:

| File                                | Used by                              |
| ----------------------------------- | ------------------------------------ |
| `lexicon-mark.png` (+@2x, @3x)       | `LexiconHeader` brand mark            |
| `lexicon-countryside.png` (+@2x, @3x)| `Hero` right column                  |
| `wotd-bg-light.png`                 | `WordOfTheDayCard` right-panel image |
| `wotd-bg-dark.png`                  | `WordOfTheDayCard` (dark mode)       |
| `cat-vocabulary.png`                | `CategoryCard`                       |
| `cat-geography.png`                 | `CategoryCard`                       |
| `cat-literature.png`                | `CategoryCard`                       |
| `cat-science.png`                   | `CategoryCard`                       |
| `article-1.png` / `article-2.png` / `article-3.png` | `ArticleCard`     |
| `avatar-default.png`                | `LexiconHeader` profile avatar        |
| `hero-light.png` / `hero-dark.png`  | reserved (Phase 3 — theme variants)  |
| `paper-texture.png`                 | reserved (Phase 3 — paper noise)     |

`lexicon-mark.svg` and `lexicon-countryside-day.svg` were rasterized to PNG via
`sharp` at 1x/2x/3x using `rasterize-assets.js` (kept alongside this README
for reproducibility).

> To regenerate assets:
> ```bash
> cd /home/z/my-project && node native/ios/Lexicon/rasterize-assets.js
> ```

---

## 5. Project structure

```
native/ios/
├── Lexicon.xcodeproj/
│   └── project.pbxproj         ← Generated Xcode project (23 sources + 19 PNGs)
└── Lexicon/
    ├── LexiconApp.swift         ← @main entry, owns ViewModel + ThemeState
    ├── ContentView.swift       ← Root: Header + active tab + BottomNav + sheets
    ├── Info.plist              ← App metadata, fonts, launch screen, orientations
    ├── Assets.xcassets/
    │   ├── Contents.json
    │   ├── AccentColor.colorset/   ← Brand green #677554 tint
    │   └── AppIcon.appiconset/     ← App icon (icon-512.png)
    ├── Theme/
    │   ├── LexiconColors.swift      ← Color(hex:) + exact brand palette (light/dark)
    │   ├── LexiconTypography.swift  ← Fraunces / Inter / Lora + fallbacks
    │   └── LexiconTheme.swift       ← ThemeState + LexiconThemeModifier
    ├── Models/
    │   └── LexiconModels.swift      ← WordEntry, Category, Article, TrendingTag, Tab
    ├── Data/
    │   ├── LexiconData.swift        ← All 18 dictionary entries + helpers
    │   └── LexiconViewModel.swift   ← ObservableObject (Zustand → @MainActor)
    ├── Components/
    │   ├── LexiconHeader.swift
    │   ├── Hero.swift
    │   ├── FloatingSearchBar.swift
    │   ├── TrendingTagRow.swift
    │   ├── WordOfTheDayCard.swift
    │   ├── CategoryCard.swift
    │   ├── ArticleCard.swift
    │   ├── SectionHeader.swift
    │   ├── BottomNav.swift
    │   ├── WordDetailSheet.swift   ← Bottom sheet — opens when vm.selectedWord != nil
    │   └── SearchModal.swift       ← Full-screen overlay — opens when vm.searchOpen
    ├── Screens/
    │   ├── HomeScreen.swift
    │   ├── DictionaryScreen.swift
    │   ├── CollectionsScreen.swift
    │   └── MoreScreen.swift
    └── Resources/                  ← 19 PNG illustrations + brand mark
```

---

## 6. State management

`LexiconViewModel` is the single source of truth — an `@MainActor ObservableObject`
that translates the Zustand store 1:1:

| Zustand state / action    | Swift equivalent                              |
| ------------------------- | --------------------------------------------- |
| `activeTab`               | `@Published var activeTab: LexiconTab`         |
| `selectedWord`            | `@Published var selectedWord: WordEntry?`     |
| `searchOpen`              | `@Published var searchOpen: Bool`             |
| `menuOpen`                | `@Published var menuOpen: Bool`               |
| `savedWordIds`            | `@Published var savedWordIds: [String]`       |
| `recentSearches`          | `@Published var recentSearches: [String]`     |
| `toggleSave` / `isSaved`  | `toggleSave(_:)` / `isSaved(_:)`              |
| `addRecentSearch`         | `addRecentSearch(_:)`                         |
| `clearRecentSearches`     | `clearRecentSearches()`                       |
| `persist` middleware      | `UserDefaults` (keys: `lexicon.savedWordIds`, `lexicon.recentSearches`) |

Injected at the App root via `.environmentObject(viewModel)` and read
in any view with `@EnvironmentObject`.

---

## 7. Navigation architecture

The root view orchestrator (`ContentView.swift`) mirrors `src/app/page.tsx`:

```
ZStack {
    background                                   // warm cream #FAF3E9
    VStack {
        LexiconHeader()                           // always rendered
        activeScreen                              // tab-switched
            .transition(.opacity.combined(with: .move(edge: .top)))
    }
    BottomNav()                                  // floating overlay
    if vm.searchOpen { SearchModal() }           // ZStack overlay
}
.sheet(item: $vm.selectedWord) { WordDetailSheet(word:) }
```

Tabs (mirrors the web `tabMeta` map):

| Tab            | Sub-header title | Sub-header subtitle       | Screen             |
| -------------- | ---------------- | ------------------------- | ------------------ |
| `home`         | —                | —                         | `HomeScreen`       |
| `dictionary`   | Dictionary       | Browse the full lexicon   | `DictionaryScreen` |
| `collections`  | Collections      | Your saved words          | `CollectionsScreen`|
| `more`         | Settings         | Make Lexicon yours         | `MoreScreen`       |
| `search`       | —                | —                         | `HomeScreen` (behind modal) |

---

## 8. Brand tokens (locked)

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

Dark-mode variants live in `LexiconColors` (midnight blue + antique gold).

---

## 9. Troubleshooting

### "No such module 'SwiftUI'"
- Make sure the target's iOS deployment target is `iOS 16.0` or later.
- Check that **Target → Build Settings → Frameworks Search Paths** includes
  the iOS SDK path (Xcode fills this in automatically).

### App icon missing
- Open `Assets.xcassets → AppIcon` in Xcode and verify the icon-512.png
  shows up in the 1024×1024 slot.
- For a perfect icon, replace `icon-512.png` with a real 1024×1024 PNG.

### Fonts not loading
- Verify all `.ttf` files appear under **Build Phases → Copy Bundle Resources**.
- Verify `Info.plist`'s `UIAppFonts` array is uncommented.
- If `LexiconFont.customFontsAvailable` returns `false`, the PostScript names
  in `LexiconTypography.swift` may need updating — open the `.ttf` in Font Book
  (macOS) to find the exact PostScript name.

### `.sheet` / `SearchModal` not appearing
- The `.sheet(item:)` modifier is attached to the root `ZStack` in
  `ContentView`. If you move it, make sure it's on the topmost view.
- The `SearchModal` overlay is gated on `vm.searchOpen`. Tap the center
  **Search** button in `BottomNav` to set that flag.

---

## 10. Known follow-ups (Phase 3)

- The home screen + all 11 components + 4 screens + 2 overlays are translated 1:1.
- `rasterize-assets.js` is a Node.js helper, not part of the iOS build.
- Camera / microphone permissions are pre-commented in `Info.plist` for
  future search-by-camera and voice-search features.
