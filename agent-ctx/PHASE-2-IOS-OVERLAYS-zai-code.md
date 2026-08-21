# Work Record — PHASE-2-IOS-OVERLAYS

**Task ID:** PHASE-2-IOS-OVERLAYS
**Agent:** zai-code (iOS overlay subagent)
**Task:** Generate the 2 missing iOS overlay components for Lexora — `WordDetailSheet` (bottom sheet) and `SearchModal` (full-screen search) — translating the locked web `word-detail-sheet.tsx` and `search-modal.tsx` 1:1 to SwiftUI.

## Inputs read before coding

- `/home/z/my-project/worklog.md` (full project history — Phase 1 web build, Phase 2 iOS port, Phase 2 Android port, Phase 2 iOS screens)
- `/home/z/my-project/native/ios/Lexora/Data/LexoraViewModel.swift` — confirmed state surface: `selectedWord`, `searchOpen`, `openWord`, `closeWord`, `openSearch`, `closeSearch`, `toggleSave`, `isSaved`, `addRecentSearch`, `clearRecentSearches`, `recentSearches`
- `/home/z/my-project/native/ios/Lexora/Models/LexoraModels.swift` — `WordEntry` struct (id, word, phonetic, partOfSpeech, definitions, etymology, synonyms, antonyms, examples, language, illustration) + `PartOfSpeech.displayValue` (returns lowercase raw value)
- `/home/z/my-project/native/ios/Lexora/Data/LexoraData.swift` — `searchWords(_:limit:)` helper (case-insensitive prefix/substring/synonym match, default limit 10)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraColors.swift` — brand palette + `LexoraPalette` resolver (`lexoraPalette(scheme)` helper)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraTypography.swift` — `Font.lexoraDisplay/lexoraBody/lexoraLong` helpers (Fraunces / Inter / Lora with graceful system fallbacks)
- `/home/z/my-project/native/ios/Lexora/Theme/LexoraTheme.swift` — `lexoraPalette(_:)` View extension
- `/home/z/my-project/native/ios/Lexora/Components/WordOfTheDayCard.swift` — style reference (card radius, border, shadow, haptics, `@EnvironmentObject vm` pattern, `.buttonStyle(.plain)`)
- `/home/z/my-project/native/ios/Lexora/Components/FloatingSearchBar.swift` — capsule search-bar pattern (card bg + border + thin SF Symbols)
- `/home/z/my-project/native/ios/Lexora/ContentView.swift` — confirmed existing wiring: `.sheet(item: $vm.selectedWord) { word in WordDetailSheet(word: word) }` and `.sheet(isPresented: $vm.searchOpen) { SearchModalSheet() }`. The inline `WordDetailSheet` and `SearchModalSheet` structs in ContentView are basic placeholders that the user will remove when activating these new dedicated-file versions.
- Web references: `src/components/lexora/word-detail-sheet.tsx`, `src/components/lexora/search-modal.tsx`

## Files generated

### 1. `/home/z/my-project/native/ios/Lexora/Components/WordDetailSheet.swift`

Bottom sheet presenting the full dictionary entry for a word. Translates `word-detail-sheet.tsx` 1:1.

**Presentation:** `let word: WordEntry` parameter — matches the existing `ContentView` wiring `.sheet(item: $vm.selectedWord) { word in WordDetailSheet(word: word) }`. Sheet height set to 88% of viewport via `.presentationDetents([.fraction(0.88)])` + `.presentationDragIndicator(.visible)` + `.presentationBackground(palette.background)`, mirroring the web `max-h-[88dvh]`. Dismissal (swipe-down) automatically sets `vm.selectedWord = nil`, which is exactly `vm.closeWord()`.

**Layout (ScrollView → VStack spacing 20):**
- **Header:** `HStack(alignment: .top)` — left column (flex-1) holds the "LEXORA ENTRY" label (`sparkles` icon + Inter 10pt semibold, tracking 1.6, highlight color), word title (Fraunces 30pt semibold, foreground), and phonetic + part-of-speech row (Inter 13pt italic muted · Inter 13pt foreground@60% lowercased). Right column holds 3 circular 40×40 action buttons in an `HStack(spacing: 8)`:
  - `speaker.wave.2` — light haptic only (matches web `Volume2` + `haptic("light")`).
  - `bookmark` / `bookmark.fill` — visual from `vm.isSaved(word.id)`, tap fires `vm.toggleSave(word.id)` with medium haptic. When saved: accent-tinted background (`accent.opacity(0.15)`), accent border (`accent.opacity(0.4)`), accent icon. When unsaved: card bg, border, foreground@80% icon.
  - `square.and.arrow.up` (share) — light haptic only (matches web `Share2` placeholder).
  - All buttons use `.buttonStyle(.plain)` + accessibility labels.
- **Definitions:** `sectionHeader("book", "Definition"/"Definitions")` + numbered list. Each item: `HStack(alignment: .top)` with a 16pt-wide number column (Inter 12pt medium, highlight color) and the definition text (Lora 15pt, foreground@90%, lineSpacing 3).
- **Etymology:** card with `padding(16)`, `palette.card` background, 12pt continuous corner radius, 1pt border. Contains `sectionHeader("sparkles", "Etymology")` + Lora 13pt text (foreground@80%, lineSpacing 3).
- **Language origin:** `HStack` row with `globe` icon (14pt, highlight), "Origin:" label (Inter 13pt muted), and the language value (Inter 13pt foreground@90%).
- **Synonyms:** `sectionHeader("arrow.2.squarepath", "Synonyms")` + `FlowLayout` of pill tags. Each pill: Inter 12pt, foreground@80%, `secondary.opacity(0.15)` bg, `secondary.opacity(0.3)` border, capsule shape, 12×6 padding.
- **Antonyms:** `sectionHeader("nosign", "Antonyms")` + `FlowLayout` of pill tags. Same pill style but `Color.red` tint (foreground@70%, red@10% bg, red@20% border) — matches web `bg-destructive/10 border-destructive/20`.
- **Examples:** `sectionHeader("quote.opening", "In Context")` + blockquotes. Each blockquote: Lora 14pt italic text (foreground@80%, lineSpacing 3) with a 2pt left border (`highlight.opacity(0.6)`) via `.overlay(alignment: .leading)`, 12pt leading padding, 8pt trailing padding, 4pt vertical padding. Matches web `border-l-2 border-highlight/60`.

**Shared helpers:** `sectionHeader(icon:title:)` renders an SF Symbol (11pt regular, foreground@60%) + uppercase Inter label (11pt semibold, tracking 1.4, foreground@60%). `pill(text:tint:foregroundOpacity:)` renders a capsule pill. `circleButton(icon:label:saved:action:)` renders the 40×40 circular action buttons.

### 2. `/home/z/my-project/native/ios/Lexora/Components/SearchModal.swift`

Full-screen search overlay. Translates `search-modal.tsx` 1:1.

**Presentation:** Self-contained overlay — reads `vm.searchOpen` and conditionally renders. Designed to be placed at the top of the root `ZStack` (above `ContentView`) so it covers all other content. When `vm.searchOpen` becomes true, the content appears with `.transition(.move(edge: .top).combined(with: .opacity))` — matches the web's `motion.div` `initial={{ y: 24, opacity: 0 }} animate={{ y: 0, opacity: 1 }}` slide-up + fade entry. The `.animation(.easeInOut(duration: 0.2), value: vm.searchOpen)` on the ZStack drives the transition.

**Layout (VStack spacing 0):**
- **Top bar:** `HStack(spacing: 8)` with 16×12 padding, `palette.background` bg.
  - Close button: `xmark` (16pt regular, foreground@70%, 36×36 frame) — clears query, defocuses, calls `vm.closeSearch()` with light haptic.
  - Capsule search field: `HStack(spacing: 10)` with 14×10 padding, `palette.card` bg, capsule clip, 1pt border. Contains:
    - `magnifyingglass` icon (14pt, foreground@50%)
    - `TextField` bound to `$query` (Inter 14pt, foreground), `.submitLabel(.search)`, `.onSubmit` adds the trimmed query to recent searches + light haptic (matches web `handleSubmit`)
    - `mic` button (14pt, foreground@50%, 28×28, light haptic)
    - `camera` button (14pt, foreground@50%, 28×28, light haptic)
- **Content area:** `ScrollView` that switches between 3 states based on `trimmedQuery` and `results`:
  1. **Empty state (no query):** `VStack(alignment: .leading, spacing: 24)` with:
     - **Recent searches** (only if `!vm.recentSearches.isEmpty`): "RECENT" header (Inter 11pt semibold, tracking 1.4, muted) + "Clear" button (`trash` icon + "Clear" text, foreground@60%, calls `vm.clearRecentSearches()` with light haptic). List of recent items: each is a `Button` with `clock` icon (14pt muted) + recent text (Inter 14pt foreground@80%, 1-line) + `arrow.right` icon (14pt muted). Tapping sets `query = recent` + refocuses (matches web `handleRecentClick`).
     - **Suggested pills:** "SUGGESTED" header + `FlowLayout` of 6 capsule pills (Petrichor, Saudade, Komorebi, Mellifluous, Halcyon, Numinous). Each pill: Inter 12pt foreground@80%, card bg, 1pt border, capsule shape, 14×6 padding. Tapping sets `query = s` + refocuses.
  2. **Results list:** `VStack(spacing: 0)` of result rows. Each row: `Button` with `HStack(alignment: .firstTextBaseline, spacing: 12)` — word title (Fraunces 16pt semibold, foreground) + part of speech (Inter 12pt italic, muted, lowercased) + first definition (Lora 13pt, foreground@70%, 1-line truncation). Tap calls `selectWord(word)`.
  3. **No results:** `VStack(spacing: 12)` centered — `magnifyingglass` icon (28pt, muted) in a 64×64 circle (card bg, 1pt border) + "No words found" (Fraunces 18pt semibold, foreground) + message (Inter 12pt, muted, centered, maxWidth 280, with curly-quoted query).

**Auto-focus:** `@FocusState private var isFocused: Bool` bound to the `TextField` via `.focused($isFocused)`. On open (via `.onAppear` + `.onChange(of: vm.searchOpen)`), a 250ms delayed `isFocused = true` is scheduled — matches the web `setTimeout(() => inputRef.current?.focus(), 250)`. On close, `query = ""` + `isFocused = false` clears state.

**Live search:** `results` computed property calls `LexoraData.searchWords(query, limit: 8)` — matches web `searchWords(query, 8)`. Recomputed on every keystroke (SwiftUI re-evaluates the body when `@State query` changes).

**Select word flow:** `selectWord(_:)` fires medium haptic → `vm.addRecentSearch(word.word)` → `vm.closeSearch()` → clears query + defocuses → 200ms delay → `vm.openWord(word)`. Matches web `selectWord` 1:1 (including the 200ms `setTimeout` gap so the search dismiss transition completes before the detail sheet presents).

## Notes / decisions

- **ContentView integration (KNOWN):** The existing `ContentView.swift` defines inline `WordDetailSheet` (basic) and `SearchModalSheet` (basic) structs. The new `WordDetailSheet` in `Components/WordDetailSheet.swift` has the same name + signature (`word: WordEntry` parameter) — it is a **drop-in replacement**. The user must remove the inline `WordDetailSheet` struct from `ContentView.swift` to avoid a duplicate-definition compile error. For `SearchModal`, the existing `ContentView` uses `SearchModalSheet()` (different name) — no conflict, but the user must swap `SearchModalSheet()` → `SearchModal()` and change the presentation from `.sheet` to a root `ZStack` overlay (so the custom opacity + move-edge transition works). Both changes are documented in the file headers. Per "Do NOT modify existing files", `ContentView.swift` was not touched.
- **SF Symbol weights:** `.regular` throughout — matches the existing iOS components (`WordOfTheDayCard`, `FloatingSearchBar`, `BottomNav`, `SectionHeader`, `DictionaryScreen`, `CollectionsScreen`, `MoreScreen`). The previous iOS screens agent made the same choice; consistency across the app.
- **Brand colors:** every color flows through `lexoraPalette(scheme)` so light/dark both render correctly. The only non-palette color is `Color.red` for antonym pill tinting (matches web `destructive` token, which maps to a standard red).
- **Haptics:** light for taps/row opens/close/recent-click/suggestion-pill/submit, medium for word selection and bookmark toggle — matches web `haptic("light")` / `haptic("medium")` calls exactly.
- **FlowLayout:** A private `FlowLayout: Layout` struct (iOS 16+ Layout protocol) is included in **both** files (file-scoped `private`, so no symbol conflict). It wraps pill subviews to the next line when they exceed the available width — mirrors CSS `flex-wrap: wrap`. Used for synonym/antonym pills in `WordDetailSheet` and suggested-search pills in `SearchModal`.
- **`presentationBackground`:** Added `.presentationBackground(palette.background)` on `WordDetailSheet` so the sheet background matches the app background (iOS 16.4+). Falls back gracefully on older iOS.
- **`#Preview` blocks:** Both files include SwiftUI `#Preview` macros (development aids, not test code). `WordDetailSheet` preview uses `LexoraData.wordOfTheDay`. `SearchModal` preview pre-sets `vm.searchOpen = true` so the overlay renders in its open state.
- **iOS version:** Targets iOS 16+ (matches the existing project). Uses `Layout` protocol (iOS 16+), `.presentationDetents` (iOS 16.4+), `@FocusState` (iOS 15+), `.onChange(of:perform:)` (iOS 14+ deprecated form — compiles on iOS 16/17 with a deprecation warning on 17).

## Stage Summary

2 new Swift files written, ~650 LoC of pure SwiftUI:

```
/home/z/my-project/native/ios/Lexora/Components/
├── WordDetailSheet.swift   (bottom sheet — header + definitions + etymology + origin + synonyms + antonyms + examples)
└── SearchModal.swift       (full-screen overlay — top bar + live search + recents + suggestions + results + no-results)
```

Both components:
- Are pure SwiftUI, use `@EnvironmentObject` for `vm`.
- Use `LexoraColors` + `LexoraTypography` helpers exclusively (no raw hex / font names inline).
- Use SF Symbols (`.regular` weight) — `sparkles`, `speaker.wave.2`, `bookmark`/`bookmark.fill`, `square.and.arrow.up`, `book`, `globe`, `arrow.2.squarepath`, `nosign`, `quote.opening`, `xmark`, `magnifyingglass`, `mic`, `camera`, `clock`, `arrow.right`, `trash`.
- React to dark mode through `lexoraPalette(scheme)`.
- Translate the locked web `word-detail-sheet.tsx` and `search-modal.tsx` 1:1 (layout, spacing, colors, typography, haptics, interaction flows).
- Do not modify any existing files.
- Do not include test code.

No Xcode/Swift toolchain was invoked — files are written for the user to compile in Xcode.
