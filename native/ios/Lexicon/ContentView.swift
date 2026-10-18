//
//  ContentView.swift
//  Lexicon
//
//  Root view orchestrator — translates `src/app/page.tsx`:
//    <div flex min-h-screen flex-col bg-background>
//      <LexiconHeader />
//      <main flex-1>
//        {tabMeta[activeTab] && <sub-header />}
//        <AnimatePresence mode="wait"> activeTab switch </AnimatePresence>
//      </main>
//      <BottomNav />          // floating overlay
//      <WordDetailSheet />    // .sheet(item: selectedWord)
//      <SearchModal />        // ZStack overlay when vm.searchOpen
//    </div>
//
//  Layout:
//    ZStack {
//      background
//      VStack { LexiconHeader; activeScreen }   // screens own their bottom padding
//      BottomNav()                              // floating capsule overlay
//      if vm.searchOpen { SearchModal() }       // full-screen search overlay
//      if toastMessage != nil { ToastOverlay }  // brief "word not found" toast
//    }
//    .sheet(item: selectedWordBinding) { WordDetailSheet(word:) }   // see selectedWordBinding below
//    .lexiconEditMenu(engine: engine)            // iOS 16+ edit-menu integration
//    .onReceive(.lexiconWordSelected) → vm.openWord
//    .onReceive(.lexiconWordNotFound) → showToast
//
//  LexiconEditMenuInteraction wiring (P3-FIX-09):
//  ----------------------------------------------
//  The `LexiconEditMenuInteraction` (UIEditMenuInteractionDelegate, iOS 16+)
//  and its `LexiconEngine` dependency live alongside this file in
//  `Engine/LexiconEditMenuInteraction.swift` + `Engine/LexiconEngine.swift`.
//  Both are part of the same Lexicon app target — Swift has no per-file
//  `import` for same-module sources, so the symbols (`lexiconEditMenu`,
//  `.lexiconWordSelected`, `.lexiconWordNotFound`, `LexiconEngine`) are
//  resolved at compile time as long as those two files are listed in the
//  Xcode target's "Compile Sources" build phase.
//
//  NOTE on `vm.engine`: the task spec calls for `.lexiconEditMenu(engine:
//  vm.engine)`, but `LexiconViewModel` does not currently expose an
//  `engine` property and we cannot modify that file (task constraint:
//  "Do NOT change any other file"). We hold a stable `LexiconEngine`
//  instance in `@State` here instead — the engine is cheap to construct
//  and safe to retain for the view's lifetime, and this is the minimal
//  deviation that keeps the wiring functional. If `LexiconViewModel` is
//  later extended with an `engine` property, swap `engine: engine` →
//  `engine: vm.engine` and delete the local `@State`.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    // MARK: - Lexicon EditMenu engine
    //
    // Stable `LexiconEngine` instance used by the UIEditMenuInteraction
    // delegate to resolve selected words. Held in `@State` so the same
    // engine (and its underlying C++ handle) survives SwiftUI re-renders.
    //
    // See file header for why this lives here rather than on `vm`.
    @State private var engine = LexiconEngine()

    // MARK: - Toast state (for .lexiconWordNotFound)
    //
    // Briefly shown above the floating BottomNav when the user selects a
    // word that isn't in the Lexicon dictionary. Auto-dismisses after 2.5s
    // via the `.task(id: toastMessage)` modifier on the ZStack — a new
    // toast cancels any pending dismissal so the latest message stays
    // visible for its full duration.
    @State private var toastMessage: String? = nil

    // MARK: - Selected-word binding for `.sheet(item:)`
    //
    // `LexiconViewModel` is `@MainActor`-isolated, and `$vm.selectedWord`
    // (the `EnvironmentObject.Wrapper`'s `subscript(dynamicMember:)`
    // projection) doesn't reliably produce a usable `Binding<WordEntry?>`
    // under stricter Swift concurrency checks — the wrapper's get/set
    // closures don't carry the MainActor isolation needed to touch
    // `selectedWord`, so the compiler emits:
    //   - "cannot call value of non-function type 'Binding<Subject>''
    //   - "referencing subscript 'subscript(dynamicMember:)' requires
    //      wrapper 'EnvironmentObject<LexiconViewModel>.Wrapper'"
    // at the `.sheet(item: $vm.selectedWord)` call site.
    //
    // Constructing the binding explicitly here makes the actor hop
    // explicit: the `get`/`set` closures inherit `ContentView`'s
    // MainActor isolation (SwiftUI `View`s are `@MainActor`-isolated
    // by default), so touching `vm.selectedWord` is provably on the
    // main actor and the wrapper subscript is bypassed entirely.
    // `.sheet(item:)` then resolves cleanly against `Binding<WordEntry?>`,
    // and the trailing closure's `word` parameter can be inferred (we
    // still annotate it explicitly below as a belt-and-suspenders fix
    // for the "cannot infer type of closure parameter" error).
    private var selectedWordBinding: Binding<WordEntry?> {
        Binding(
            get: { vm.selectedWord },
            set: { vm.selectedWord = $0 }
        )
    }

    var body: some View {
        ZStack {
            // MARK: - Background — warm cream (#FAF3E9) light, midnight dark.
            lexiconPalette(scheme).background
                .ignoresSafeArea()

            // MARK: - Main column (header + active screen).
            // Screens own their `.padding(.bottom, 120)` so the floating
            // BottomNav capsule never overlaps their content.
            //
            // `.lexiconEditMenu(engine:)` is attached to `activeScreen` so
            // the UIEditMenuInteraction anchor sits inside the responder
            // chain of whichever screen currently owns the user's text
            // selection. (The modifier inserts a 0×0 anchor UIView via
            // `LexiconEditMenuAnchor` — see `LexiconEditMenuInteraction.swift`.)
            VStack(spacing: 0) {
                LexiconHeader()

                activeScreen
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .lexiconEditMenu(engine: engine)
            }

            // MARK: - Floating bottom nav overlay.
            // BottomNav positions itself at the bottom-center via its internal
            // VStack { Spacer(); HStack { Spacer(); navCapsule } }.
            BottomNav()
                .environmentObject(vm)
                .environmentObject(theme)
                .zIndex(2)

            // MARK: - Search modal overlay (when vm.searchOpen).
            if vm.searchOpen {
                SearchModal()
                    .environmentObject(vm)
                    .environmentObject(theme)
                    .transition(.opacity)
                    .zIndex(10)
            }

            // MARK: - Toast overlay (brief "word not found" message).
            //
            // Rendered above the BottomNav (zIndex 20) so it isn't covered
            // by the floating capsule. `allowsHitTesting(false)` so taps
            // pass through to the underlying content. Auto-dismisses after
            // 2.5s via the `.task(id: toastMessage)` modifier below.
            if let toastMessage = toastMessage {
                VStack {
                    Spacer()
                    HStack(spacing: 8) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 14, weight: .regular))
                        Text(toastMessage)
                            .font(.lexiconBody(13))
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .foregroundStyle(lexiconPalette(scheme).background)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(lexiconPalette(scheme).foreground.opacity(0.92))
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.15), radius: 8, y: 2)
                    .padding(.horizontal, 24)
                    .padding(.bottom, 140)  // clear the floating BottomNav
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .zIndex(20)
                .allowsHitTesting(false)
            }
        }
        .background(LexiconColors.background)
        // MARK: - Word detail sheet (presented when vm.selectedWord != nil)
        //
        // Driven by `vm.selectedWord` — set both by direct UI taps (trending
        // tags, search results) and by the `.lexiconWordSelected` notification
        // handler below (EditMenu lookup hit).
        .sheet(item: selectedWordBinding) { (word: WordEntry) in
            WordDetailSheet(word: word)
                .environmentObject(vm)
                .environmentObject(theme)
        }
        // Animate the search overlay appearance + tab transitions + toast.
        .animation(.easeInOut(duration: 0.2), value: vm.searchOpen)
        .animation(.easeInOut(duration: 0.2), value: vm.activeTab)
        .animation(.easeInOut(duration: 0.25), value: toastMessage)
        // MARK: - Toast auto-dismiss
        //
        // `.task(id:)` re-runs whenever `toastMessage` changes — a new
        // toast cancels any pending dismissal so it stays visible for the
        // full 2.5s. Setting `toastMessage = nil` here triggers another
        // id change, but the guard short-circuits it.
        .task(id: toastMessage) {
            guard toastMessage != nil else { return }
            try? await Task.sleep(nanoseconds: 2_500_000_000)  // 2.5s
            toastMessage = nil
        }
        // MARK: - Lexicon EditMenu notification listeners
        //
        // Posted by `LexiconEditMenuInteraction.performLookup(word:)`:
        //   - .lexiconWordSelected  → userInfo["word"] = engine.lookup result
        //   - .lexiconWordNotFound  → userInfo["word"] = String (original query)
        .onReceive(
            NotificationCenter.default.publisher(for: .lexiconWordSelected)
        ) { note in
            handleWordSelected(note)
        }
        .onReceive(
            NotificationCenter.default.publisher(for: .lexiconWordNotFound)
        ) { note in
            handleWordNotFound(note)
        }
    }

    // MARK: - Active screen switch
    //
    // Mirrors the AnimatePresence block in page.tsx:
    //   {activeTab === "home"       && <motion.div key="home">...</motion.div>}
    //   {activeTab === "dictionary" && <motion.div key="dictionary"><DictionaryScreen/></motion.div>}
    //   {activeTab === "collections"&& <motion.div key="collections"><CollectionsScreen/></motion.div>}
    //   {activeTab === "more"       && <motion.div key="more"><MoreScreen/></motion.div>}
    //
    // Non-home tabs get a sub-header (title + subtitle) above the screen —
    // mirrors the `tabMeta[activeTab]` block in page.tsx.

    @ViewBuilder
    private var activeScreen: some View {
        switch vm.activeTab {
        case .home:
            HomeScreen()
                .transition(activeTabTransition)

        case .dictionary:
            subHeaderScreen(title: "Dictionary",
                            subtitle: "Browse the full lexicon") {
                DictionaryScreen()
            }
            .transition(activeTabTransition)

        case .collections:
            subHeaderScreen(title: "Collections",
                            subtitle: "Your saved words") {
                CollectionsScreen()
            }
            .transition(activeTabTransition)

        case .more:
            subHeaderScreen(title: "Settings",
                            subtitle: "Make Lexicon yours") {
                MoreScreen()
            }
            .transition(activeTabTransition)

        case .search:
            // Search opens a modal; show home behind.
            HomeScreen()
                .transition(activeTabTransition)
        }
    }

    // MARK: - Sub-header + content wrapper (non-home tabs)
    //
    // Mirrors `tabMeta[activeTab]` block + screen content:
    //   <div className="px-4 pb-1 pt-2">
    //     <motion.h1 font-display text-xl font-semibold text-foreground>{title}</motion.h1>
    //     <p font-body text-[11px] text-muted-foreground>{subtitle}</p>
    //   </div>
    //   <motion.div key={tab} className="px-4"> <Screen/> </motion.div>

    @ViewBuilder
    private func subHeaderScreen<Content: View>(
        title: String,
        subtitle: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.lexiconDisplay(20, weight: .semibold))
                    .foregroundStyle(lexiconPalette(scheme).foreground)
                Text(subtitle)
                    .font(.lexiconBody(11))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 4)

            content()
        }
    }

    // MARK: - Transitions
    //
    // AnimatePresence(mode: "wait") equivalent — opacity + vertical slide.
    // Matches `motion.div initial={{opacity:0, y:6}} animate={{opacity:1, y:0}}`.

    private var activeTabTransition: AnyTransition {
        .asymmetric(
            insertion: .opacity.combined(with: .move(edge: .top)),
            removal: .opacity.combined(with: .move(edge: .top))
        )
    }

    // MARK: - Lexicon EditMenu notification handlers
    //
    // The engine wrapper (`LexiconEngine.swift`) ships its own `WordEntry`
    // struct (`partOfSpeech: String`), distinct from the model
    // `WordEntry` in `LexiconModels.swift` (`partOfSpeech: PartOfSpeech`).
    // `LexiconEditMenuInteraction.performLookup` posts the engine's
    // `WordEntry` in `.lexiconWordSelected`'s userInfo. Both types share
    // the same unqualified name `WordEntry` at module scope, so an
    // explicit `raw as? WordEntry` cast is ambiguous and trips the
    // "cannot infer type of closure parameter" / "subscript(dynamicMember:)
    // requires wrapper" cascade at the call sites below. We resolve the
    // payload purely by reflection — extract the `word` string field from
    // any struct (engine `WordEntry`, model `WordEntry`, or a plain
    // `String`), then re-resolve via `LexiconData.dictionary` to obtain
    // the canonical model `WordEntry` (with the `PartOfSpeech` enum + any
    // illustration asset). If resolution fails entirely, we surface a
    // "not found" toast so the user gets feedback either way.
    //
    // The explicit `(entry: WordEntry)` annotation on the `first(where:)`
    // closure pins the parameter to the model `WordEntry` (the element
    // type of `LexiconData.dictionary`), so the compiler doesn't have to
    // infer it through the ambiguous module-scope `WordEntry` name —
    // this is the same belt-and-suspenders pattern used on the
    // `.sheet(item:)` closure above.

    private func handleWordSelected(_ note: Notification) {
        guard let raw = note.userInfo?["word"] else {
            showToast("Word not found in Lexicon")
            return
        }

        // Reflect on the payload to extract the `word` string, then
        // re-resolve via LexiconData (case-insensitive match on the
        // word title — LexiconData stores canonical entries for all 18
        // words the engine knows about).
        if let wordString = extractWordString(from: raw),
           let resolved = LexiconData.dictionary.first(where: { (entry: WordEntry) in
               entry.word.lowercased() == wordString.lowercased()
           }) {
            vm.openWord(resolved)
            return
        }

        // Couldn't resolve — toast.
        let displayWord = extractWordString(from: raw) ?? "that word"
        showToast("\"\(displayWord)\" isn't in Lexicon yet")
    }

    private func handleWordNotFound(_ note: Notification) {
        // `.lexiconWordNotFound` posts userInfo["word"] as the original
        // String query (see LexiconEditMenuInteraction.performLookup).
        let word = (note.userInfo?["word"] as? String) ?? "that word"
        showToast("\"\(word)\" isn't in Lexicon yet")
    }

    /// Extract the `word` string field from any payload.
    ///
    /// Works for both the engine's `WordEntry` and the model `WordEntry`
    /// without compile-time knowledge of either type, by introspecting
    /// the value's children via `Mirror`. Also accepts a plain `String`
    /// (used by `.lexiconWordNotFound`).
    private func extractWordString(from raw: Any) -> String? {
        if let s = raw as? String { return s }
        let mirror = Mirror(reflecting: raw)
        for child in mirror.children where child.label == "word" {
            if let s = child.value as? String { return s }
        }
        return nil
    }

    /// Show a brief toast message above the floating BottomNav.
    ///
    /// Auto-dismisses after 2.5 seconds via the `.task(id: toastMessage)`
    /// modifier on the ZStack. A new toast cancels any pending dismissal
    /// so the latest message stays visible for its full duration.
    private func showToast(_ message: String) {
        toastMessage = message
    }
}

// MARK: - Preview

#Preview {
    ContentView()
        .environmentObject(LexiconViewModel())
        .environmentObject(LexiconThemeState())
}
