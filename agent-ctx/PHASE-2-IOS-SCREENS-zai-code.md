# Work Record — PHASE-2-IOS-SCREENS

**Task ID:** PHASE-2-IOS-SCREENS
**Agent:** zai-code (iOS native subagent)
**Task:** Generate the 3 missing iOS secondary screens (Dictionary, Collections, More) for Lexora, translating the locked web `dictionary-screen.tsx`, `collections-screen.tsx`, and `more-screen.tsx` 1:1 to SwiftUI.

## Inputs read before coding

- `/home/z/my-project/worklog.md` (full Phase-1 web + Phase-2 iOS / Android history)
- `/home/z/my-project/native/ios/Lexora/Screens/HomeScreen.swift` (screen pattern)
- `/home/z/my-project/native/ios/Lexora/Data/LexoraViewModel.swift` (state pattern — @MainActor ObservableObject, UserDefaults persistence, no `isDark` field)
- `/home/z/my-project/native/ios/Lexora/Models/LexoraModels.swift` (WordEntry, PartOfSpeech, etc.)
- `/home/z/my-project/native/ios/Lexora/Data/LexoraData.swift` (18 dictionary entries, `getAlphabetSections()`, `searchWords()`, `getWordById()`)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraColors.swift` (exact brand palette + `LexoraPalette` resolver)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraTypography.swift` (Fraunces / Inter / Lora helpers)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraTheme.swift` (`LexoraThemeState` ObservableObject — the next-themes equivalent; toggle() + @AppStorage("lexora.theme"))
- `/home/z/my-project/native/ios/Lexora/Components/SectionHeader.swift` (shared header pattern)
- `/home/z/my-project/native/ios/Lexora/ContentView.swift` (root wiring — confirmed `LexoraThemeState` is already injected as `@EnvironmentObject`)
- `/home/z/my-project/native/ios/Lexora/Components/WordOfTheDayCard.swift` (existing card / shadow / border conventions to match)
- Web references: `dictionary-screen.tsx`, `collections-screen.tsx`, `more-screen.tsx`

## Files generated

1. **`/home/z/my-project/native/ios/Lexora/Screens/DictionaryScreen.swift`**
   - `ScrollViewReader` wraps the whole screen so the alphabet index buttons can call `proxy.scrollTo(letter, anchor: .top)`.
   - Sticky-feel A–Z quick index at the top — horizontal `ScrollView` of 26-px letter circles. Active letter is filled with brand-primary and uses `primaryForeground` text; inactive letters are `mutedForeground` with a hairline border.
   - Active-letter tracking uses `GeometryReader` + a custom `SectionTopPreferenceKey`. Each section reports its `minY` in the named coordinate space `"dictScroll"`. The `onPreferenceChange` callback picks the section with the greatest `minY` that is still ≤ an 80pt threshold (i.e. the topmost visible section), updating `activeLetterIndex` with a 0.15s ease-out animation.
   - Each section is a `DictionarySection` view: header row = large serif letter (brand green) + horizontal divider + count ("N words"), then a grouped card (`bg card`, `border`, `shadow-card`, 12pt corner radius) containing the word rows.
   - `DictionaryRow` matches the web 1:1: word title (Fraunces 14pt semibold) · part of speech (Inter 10pt italic, muted, lowercased) · definition (Lora 12pt, foreground @ 70%, 1 line) · `speaker.wave.2` SF Symbol on the right (12pt, regular weight, muted). Tapping fires `UIImpactFeedbackGenerator(.light)` + `vm.openWord(entry)`.
   - Row dividers handled with a top-overlay Rectangle (1pt, border color, 12pt horizontal inset) — skipped for the first row in each group.

2. **`/home/z/my-project/native/ios/Lexora/Screens/CollectionsScreen.swift`**
   - Computed `savedWords` filters `LexoraData.dictionary` by `vm.savedWordIds` (Set-based lookup) and alpha-sorts. Reactivity flows through `@Published var savedWordIds`.
   - Empty state (`EmptyCollectionState`): vertical layout with a soft `Circle` glow behind a 160-pt `cat-literature` image (rounded 16pt corners, opacity 0.85 light / 0.60 dark to match web `fade-edges opacity-80 dark:opacity-60`). A filled `bookmark.fill` badge (44-pt circle, card surface, border, shadow) sits in the bottom-right corner offset by 8pt. Below: "Your collection awaits" headline (Fraunces 20pt semibold) + description (Inter 13pt, muted, centered, 3pt line-spacing) + "Saved words sync offline across all your devices" pill (`sparkles` icon + Inter 11pt, capsule shape with border).
   - List state: "Saved Words" title (Fraunces 24pt semibold) + count (Inter 11pt muted), then a single grouped card (16pt corners, border, shadow) of `SavedRow` views.
   - `SavedRow`: filled `bookmark.fill` (highlight color) · word title (Fraunces 15pt semibold) · part of speech (Inter 10pt italic, muted, lowercase) · definition (Lora 12pt, 1 line) · `speaker.wave.2` icon · `trash` button (28-pt tap target, muted @ 60%). Row tap → `vm.openWord(entry)`; trash tap → `vm.toggleSave(entry.id)` with light haptic + stop-propagation via separate `.buttonStyle(.plain)` button nested inside.
   - Staggered mount-in animation: each row starts at `opacity(0)` + `offset(x: -8)` and animates to `opacity(1)` + `offset(0)` with `.easeOut(0.25).delay(idx * 0.04)`, driven by a `@State var appeared` toggled in `.onAppear`.
   - Container cross-fades between empty and list states via `.transition(.opacity.combined(with: .move(edge: .top)))` + `.animation(.easeInOut(0.25), value: savedWords.count)`.

3. **`/home/z/my-project/native/ios/Lexora/Screens/MoreScreen.swift`**
   - Profile header card: 64-pt circular avatar (`avatar-default` resource) with 2-pt primary-@-30% ring · "Curious Soul" headline (Fraunces 18pt semibold) · "Lexora Companion · since 2026" subtitle (Inter 12pt muted) · `saved` + `recent` stat row (Inter 11pt with semibold value).
   - Four `SettingsGroup`s, each with an uppercase muted header (`tracking(1.4)`, Inter 11pt semibold) and a 16-pt-corner card containing rows separated by hairline dividers:
     - **APPEARANCE**: Theme (sun/moon icon, value "Dark"/"Light", tap → `theme.toggle()` + medium haptic) · Text size (eye icon, "Default") · Notifications (bell icon, "On")
     - **DICTIONARY**: Language pairs (globe, "36 installed") · Storage (externaldrive, "412 MB") · Download language packs (arrow.down.circle, empty value)
     - **PRIVACY & DATA**: Clear recent searches (doc.text, dynamic value "\(N) items" or "Empty", tap → `vm.clearRecentSearches()` + medium haptic) · Clear cache (externaldrive, "412 MB")
     - **ABOUT**: About Lexora (info.circle, "v1.0.0") · Privacy policy (doc.text) · Open source licences (chevron.left.slash.chevron.right)
   - `SettingsRow` matches web: icon in a 32-pt rounded square (8-pt corner radius, primary @ 10% fill, primary icon stroke) · label (Inter 14pt) · value (Inter 12pt muted, optional) · `chevron.right` (12pt regular, muted @ 60%). Whole row is a `.plain` button.
   - Theme toggle: uses `@EnvironmentObject` for `LexoraThemeState` (the next-themes equivalent). `isDark` is computed locally — mirrors web's `(resolvedTheme ?? theme) === "dark"`: stored pref "dark"/"light" wins, otherwise falls back to the current `colorScheme`. Tap calls `theme.toggle()`.
   - Footer: filled heart (accent color, 11pt regular) + "Crafted in the spirit of the Cotswolds" (Inter 11pt muted) + "Lexora · Words. Wisdom. Worlds." (Inter 10pt, muted @ 60%), all centered.

## Notes / decisions

- **`vm.isDark` does not exist.** The ViewModel was intentionally kept lean by the previous iOS agent (mirroring the Zustand store, which has no `isDark` field — that lives in `next-themes`). The theme is owned by `LexoraThemeState`, which is already injected via `@EnvironmentObject` at the App root. `MoreScreen` reads `LexoraThemeState` directly and calls `theme.toggle()` — this is the 1:1 architectural translation of the web's `useTheme()` + `setTheme(isDark ? "light" : "dark")`. No existing files were modified.
- **SF Symbol weights**: all icons use `.regular` weight to match the existing iOS components (`WordOfTheDayCard`, `BottomNav`, `SectionHeader`); micro arrows use `.semibold` to match the existing `SectionHeader` arrow pattern. The task allowed `.thin` or `.regular`.
- **Brand colors**: every color goes through `lexoraPalette(scheme)` (appearance-aware) so light/dark both work without extra branches — except the WOTD-style hero backgrounds which the existing screens hard-code (out of scope here).
- **LazyVStack** used in DictionaryScreen for performance — preferences from off-screen sections simply aren't reported, which is fine because the active-letter search only cares about visible sections.
- **Haptics**: light for taps/row opens, medium for the theme toggle and the destructive "clear recent searches" action — matches the web's `haptic("light")` / `haptic("medium")` calls.
- **ContentView wiring**: the user previously had `ContentView` switch on `.dictionary`/`.collections`/`.more` and render the placeholder `SimpleBrowseScreen`. The new screens are not auto-wired in (per "Do NOT modify existing files"). The user will swap `SimpleBrowseScreen(...)` → `DictionaryScreen()`, `CollectionsScreen()`, `MoreScreen()` in `ContentView.swift` when they want to activate them — both VM and ThemeState are already in the environment.

## Stage Summary

3 new Swift files written, totalling ~700 LoC of pure SwiftUI:

```
/home/z/my-project/native/ios/Lexora/Screens/
├── DictionaryScreen.swift   (alphabetical browse, sticky A–Z, active-letter tracking)
├── CollectionsScreen.swift  (empty state + saved words list with staggered animation)
├── MoreScreen.swift         (profile header + 4 settings groups + footer credit)
└── HomeScreen.swift         (existing — unchanged)
```

All 3 screens:
- Are pure SwiftUI, use `@EnvironmentObject` for `vm` (and `LexoraThemeState` where needed).
- Use `LexoraColors` + `LexoraTypography` helpers (no raw hex / font names inline).
- Use SF Symbols (mostly `.regular` weight) — `speaker.wave.2`, `bookmark.fill`, `trash`, `sparkles`, `heart.fill`, `chevron.right`, `moon`/`sun`, `eye`, `bell`, `globe`, `externaldrive`, `arrow.down.circle`, `info.circle`, `doc.text`, `chevron.left.slash.chevron.right`.
- React to dark mode through `lexoraPalette(scheme)`.
- Do not modify any existing files.
- Do not include test code.

No Xcode/Swift toolchain was invoked — files are written for the user to compile in Xcode.
