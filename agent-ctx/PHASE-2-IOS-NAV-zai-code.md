# Task: PHASE-2-IOS-NAV — iOS Navigation Wiring + Xcode Project Config

Agent: Z.ai Code (main)
Date: 2026-08-20
Task ID: PHASE-2-IOS-NAV

## Summary

Wired up navigation between all iOS screens, updated `ContentView.swift` to
orchestrate the full root view (mirroring `src/app/page.tsx`), generated the
`Lexora.xcodeproj/project.pbxproj`, created the `Assets.xcassets/` catalogue,
hardened `Info.plist`, and rewrote the iOS `README.md` with build / font /
simulator instructions.

## Work Log

### 1. ContentView.swift — root view orchestrator

Rewrote `/native/ios/Lexora/ContentView.swift` to mirror `src/app/page.tsx`:

- **Layout**: `ZStack { background; VStack { LexoraHeader; activeScreen }; BottomNav (overlay); if vm.searchOpen { SearchModal } }`
- **Active tab switch** uses `@ViewBuilder` over `vm.activeTab`:
  - `.home` → `HomeScreen()`
  - `.dictionary` → `subHeaderScreen(title: "Dictionary", subtitle: "Browse the full lexicon") { DictionaryScreen() }`
  - `.collections` → `subHeaderScreen(title: "Collections", subtitle: "Your saved words") { CollectionsScreen() }`
  - `.more` → `subHeaderScreen(title: "Settings", subtitle: "Make Lexora yours") { MoreScreen() }`
  - `.search` → `HomeScreen()` (search opens modal on top)
- **Sub-header** (`subHeaderScreen` helper): mirrors `tabMeta[activeTab]`
  block — Fraunces display 20pt semibold title + Inter body 11pt subtitle,
  left-aligned, 16pt horizontal padding, 8pt top / 4pt bottom.
- **Animated transitions**: `.transition(.opacity.combined(with: .move(edge: .top)))`
  per tab — mirrors `motion.div initial={{opacity:0, y:6}} animate={{opacity:1, y:0}}`.
  Plus `.animation(.easeInOut(duration: 0.2), value: vm.searchOpen)` and
  `value: vm.activeTab`.
- **Word detail sheet**: `.sheet(item: $vm.selectedWord) { word in WordDetailSheet(word: word) }`
  — presented when `vm.selectedWord != nil`.
- **Search modal**: ZStack overlay `if vm.searchOpen { SearchModal() }` with
  `.transition(.opacity)` and `.zIndex(10)` — per spec ("fullScreenCover or
  ZStack overlay").
- **BottomNav**: kept as floating overlay (BottomNav already draws itself
  via internal `VStack { Spacer(); HStack { Spacer(); navCapsule } }`).
- **Background**: `lexoraPalette(scheme).background` (warm cream `#FAF3E9`
  light / midnight `#1A1F28` dark) plus `.background(LexoraColors.background)`
  for safety.
- Removed the inline `SimpleBrowseScreen`, `SearchModalSheet`, and inline
  `WordDetailSheet` definitions from the previous version — they're now
  replaced by the dedicated `DictionaryScreen`, `CollectionsScreen`,
  `MoreScreen`, `SearchModal`, `WordDetailSheet` files (created by parallel
  agents in `Screens/` and `Components/`).

### 2. LexoraApp.swift — entry point

Updated `/native/ios/Lexora/LexoraApp.swift` (was already correct, expanded
documentation comments):

- `@main struct LexoraApp: App`
- `@StateObject private var viewModel = LexoraViewModel()`
- `@StateObject private var themeState = LexoraThemeState()`
- `WindowGroup { ContentView().environmentObject(viewModel).environmentObject(themeState).lexoraTheme() }`

### 3. project.pbxproj — generated Xcode project

Created `/native/ios/Lexora.xcodeproj/project.pbxproj` (hand-authored):

- **PBXBuildFile section**: 23 Swift source build files + 19 PNG resource
  build files + 1 Assets.xcassets build file = 43 build file entries.
- **PBXFileReference section**: 1 product (Lexora.app) + 3 root files
  (Info.plist, Assets.xcassets, rasterize-assets.js omitted) + 23 Swift
  sources + 19 PNG resources.
- **PBXGroup section**: 9 groups (main, Products, Lexora, Components,
  Screens, Data, Models, Theme, Resources) — mirrors the folder structure.
- **PBXNativeTarget**: `Lexora`, `com.apple.product-type.application`,
  with 3 build phases (Sources / Frameworks / Resources).
- **PBXSourcesBuildPhase**: lists all 23 Swift build files.
- **PBXResourcesBuildPhase**: lists all 19 PNG build files + Assets.xcassets.
- **PBXFrameworksBuildPhase**: empty (SwiftUI comes from SDK).
- **PBXProject**: objectVersion 56, Xcode 14.0 compatibility, targets = [Lexora].
- **XCBuildConfiguration** (4): Project Debug, Project Release, Target Debug,
  Target Release.
  - `IPHONEOS_DEPLOYMENT_TARGET = 16.0`
  - `PRODUCT_BUNDLE_IDENTIFIER = com.lexora.app`
  - `PRODUCT_NAME = $(TARGET_NAME)` (= "Lexora")
  - `SWIFT_VERSION = 5.0`
  - `TARGETED_DEVICE_FAMILY = "1,2"` (iPhone + iPad)
  - `INFOPLIST_FILE = Lexora/Info.plist`
  - `GENERATE_INFOPLIST_FILE = NO` (we ship a custom Info.plist)
  - `ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon`
  - `ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor`
  - `CODE_SIGN_STYLE = Automatic`, `DEVELOPMENT_TEAM = ""`
  - `ENABLE_PREVIEWS = YES`
- **XCConfigurationList** (2): project config list + target config list,
  both default to Release.

### 4. Assets.xcassets — asset catalogue

Created `/native/ios/Lexora/Assets.xcassets/`:

- `Contents.json` — root catalogue metadata (author: xcode, version: 1).
- `AppIcon.appiconset/Contents.json` — single universal 1024×1024 entry
  referencing `icon-512.png` (modern Xcode 14+ single-size app icon format).
- `AppIcon.appiconset/icon-512.png` — copied from `public/icon-512.png`.
- `AppIcon.appiconset/icon-192.png` — copied from `public/icon-192.png`
  (kept as fallback reference; Xcode 14+ uses the single-size entry).
- `AccentColor.colorset/Contents.json` — brand green `#677554` for light
  mode + `#1D2A3A` for dark mode (matches `LexoraColors.primaryDark`).

### 5. Info.plist — hardened

Updated `/native/ios/Lexora/Info.plist`:

- **CFBundleDisplayName**: `Lexora` ✓
- **UILaunchScreen**: dict with `UIColorName = AccentColor` — uses the brand
  green as the launch screen background (matches the warm-cream feel since
  AccentColor is set in Assets.xcassets).
- **UISupportedInterfaceOrientations**: portrait-only on iPhone ✓
- **UISupportedInterfaceOrientations~ipad**: all 4 orientations ✓
- **UIAppFonts**: commented array with 8 entries (Fraunces/Inter/Lora × 3
  weights + Lora Italic) — user uncomments when adding `.ttf` files ✓
- **UIStatusBarStyle**: `UIStatusBarStyleDefault`
- **UIViewControllerBasedStatusBarAppearance**: `true`
- **UIRequiresFullScreen**: `false` (multi-window friendly)
- **ITSAppUsesNonExemptEncryption**: `false`
- **CFBundleURLTypes**: empty array (no URL schemes yet)
- Added commented-out NSCameraUsageDescription, NSMicrophoneUsageDescription,
  NSPhotoLibraryUsageDescription — reserved for Phase 3 camera/voice search.

### 6. README.md — full build guide

Rewrote `/native/ios/Lexora/README.md` with 10 sections:

1. **Open the project in Xcode** — Option A (use generated `.xcodeproj`)
   + Option B (start fresh in Xcode). Includes note about Xcode's "fix up"
   prompt and how to manually re-add files if the pbxproj gets out of sync.
2. **Run on the simulator** — recommended simulators (iPhone 15, 16 Pro, SE),
   keyboard shortcuts (⌘R, ⇧⌘A), and steps for physical-device builds.
3. **Custom fonts** — Step 1 (download Fraunces/Inter/Lora) + Step 2 (drag
   `.ttf` into Resources/) + Step 3 (uncomment UIAppFonts in Info.plist)
   + Step 4 (build & run). Includes a font-weight role table.
4. **Asset catalogue** — tables for Assets.xcassets and Resources/ PNGs.
5. **Project structure** — full tree with all 23 Swift sources + 19 PNGs +
   Assets.xcassets.
6. **State management** — Zustand → LexoraViewModel mapping table.
7. **Navigation architecture** — ContentView ZStack layout diagram + tab
   metadata table.
8. **Brand tokens** — exact hex table.
9. **Troubleshooting** — common build issues and fixes (no SwiftUI module,
   missing icon, fonts not loading, sheets not appearing).
10. **Known follow-ups (Phase 3)** — camera/mic permissions pre-commented,
    rasterize-assets.js is a Node helper.

## Files Modified / Created

### Modified
- `/home/z/my-project/native/ios/Lexora/ContentView.swift` (rewritten)
- `/home/z/my-project/native/ios/Lexora/LexoraApp.swift` (expanded docs)
- `/home/z/my-project/native/ios/Lexora/Info.plist` (hardened)
- `/home/z/my-project/native/ios/Lexora/README.md` (rewritten)

### Created
- `/home/z/my-project/native/ios/Lexora.xcodeproj/project.pbxproj` (new, ~330 lines)
- `/home/z/my-project/native/ios/Lexora/Assets.xcassets/Contents.json`
- `/home/z/my-project/native/ios/Lexora/Assets.xcassets/AppIcon.appiconset/Contents.json`
- `/home/z/my-project/native/ios/Lexora/Assets.xcassets/AppIcon.appiconset/icon-512.png` (copied)
- `/home/z/my-project/native/ios/Lexora/Assets.xcassets/AppIcon.appiconset/icon-192.png` (copied)
- `/home/z/my-project/native/ios/Lexora/Assets.xcassets/AccentColor.colorset/Contents.json`

## Notes for Parallel Agents

- `ContentView.swift` references `DictionaryScreen()`, `CollectionsScreen()`,
  `MoreScreen()`, `WordDetailSheet(word:)`, `SearchModal()` — these are
  created by parallel agents. The pbxproj already lists them in the Sources
  build phase, so they'll compile as soon as the files exist.
- All these views are expected to use `@EnvironmentObject private var vm:
  LexoraViewModel` (and optionally `theme: LexoraThemeState`).
- `WordDetailSheet` is presented via `.sheet(item: $vm.selectedWord)` so it
  must accept a `word: WordEntry` initializer parameter.
- `SearchModal` is presented as a ZStack overlay (not a sheet) so it should
  fill the screen and manage its own dismiss via `vm.closeSearch()`.

## Stage Summary

- iOS navigation architecture is fully wired: HomeScreen → DictionaryScreen →
  CollectionsScreen → MoreScreen via `vm.activeTab`.
- Overlays wired: `WordDetailSheet` via `.sheet(item:)` + `SearchModal` via
  ZStack overlay.
- Xcode project file generated with all 23 Swift sources + 19 PNG resources +
  Assets.xcassets listed; iOS 16 deployment target; bundle ID `com.lexora.app`.
- Asset catalogue created with AccentColor (brand green #677554) + AppIcon
  (icon-512.png as universal 1024×1024 entry).
- Info.plist hardened with launch screen (using AccentColor), portrait-only
  orientations, commented UIAppFonts, CFBundleDisplayName=Lexora.
- README rewritten with full build / font / simulator instructions +
  troubleshooting section.
- Did not run any Xcode/Swift toolchain (per task spec).
- Did not write test code (per task spec).
- Did not modify existing component files (LexoraHeader, BottomNav, etc.) —
  per task spec.
