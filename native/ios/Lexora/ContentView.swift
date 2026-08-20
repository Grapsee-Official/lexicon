//
//  ContentView.swift
//  Lexora
//
//  Root view orchestrator — translates `src/app/page.tsx`:
//    <div flex min-h-screen flex-col bg-background>
//      <LexoraHeader />
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
//      VStack { LexoraHeader; activeScreen }   // screens own their bottom padding
//      BottomNav()                              // floating capsule overlay
//      if vm.searchOpen { SearchModal() }       // full-screen search overlay
//      if toastMessage != nil { ToastOverlay }  // brief "word not found" toast
//    }
//    .sheet(item: $vm.selectedWord) { WordDetailSheet(word:) }
//    .lexoraEditMenu(engine: engine)            // iOS 16+ edit-menu integration
//    .onReceive(.lexoraWordSelected) → vm.openWord
//    .onReceive(.lexoraWordNotFound) → showToast
//
//  LexoraEditMenuInteraction wiring (P3-FIX-09):
//  ----------------------------------------------
//  The `LexoraEditMenuInteraction` (UIEditMenuInteractionDelegate, iOS 16+)
//  and its `LexoraEngine` dependency live alongside this file in
//  `Engine/LexoraEditMenuInteraction.swift` + `Engine/LexoraEngine.swift`.
//  Both are part of the same Lexora app target — Swift has no per-file
//  `import` for same-module sources, so the symbols (`lexoraEditMenu`,
//  `.lexoraWordSelected`, `.lexoraWordNotFound`, `LexoraEngine`) are
//  resolved at compile time as long as those two files are listed in the
//  Xcode target's "Compile Sources" build phase.
//
//  NOTE on `vm.engine`: the task spec calls for `.lexoraEditMenu(engine:
//  vm.engine)`, but `LexoraViewModel` does not currently expose an
//  `engine` property and we cannot modify that file (task constraint:
//  "Do NOT change any other file"). We hold a stable `LexoraEngine`
//  instance in `@State` here instead — the engine is cheap to construct
//  and safe to retain for the view's lifetime, and this is the minimal
//  deviation that keeps the wiring functional. If `LexoraViewModel` is
//  later extended with an `engine` property, swap `engine: engine` →
//  `engine: vm.engine` and delete the local `@State`.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @EnvironmentObject private var theme: LexoraThemeState
    @Environment(\.colorScheme) private var scheme

    // MARK: - Lexora EditMenu engine
    //
    // Stable `LexoraEngine` instance used by the UIEditMenuInteraction
    // delegate to resolve selected words. Held in `@State` so the same
    // engine (and its underlying C++ handle) survives SwiftUI re-renders.
    //
    // See file header for why this lives here rather than on `vm`.
    @State private var engine = LexoraEngine()

    // MARK: - Toast state (for .lexoraWordNotFound)
    //
    // Briefly shown above the floating BottomNav when the user selects a
    // word that isn't in the Lexora dictionary. Auto-dismisses after 2.5s
    // via the `.task(id: toastMessage)` modifier on the ZStack — a new
    // toast cancels any pending dismissal so the latest message stays
    // visible for its full duration.
    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack {
            // MARK: - Background — warm cream (#FAF3E9) light, midnight dark.
            lexoraPalette(scheme).background
                .ignoresSafeArea()

            // MARK: - Main column (header + active screen).
            // Screens own their `.padding(.bottom, 120)` so the floating
            // BottomNav capsule never overlaps their content.
            //
            // `.lexoraEditMenu(engine:)` is attached to `activeScreen` so
            // the UIEditMenuInteraction anchor sits inside the responder
            // chain of whichever screen currently owns the user's text
            // selection. (The modifier inserts a 0×0 anchor UIView via
            // `LexoraEditMenuAnchor` — see `LexoraEditMenuInteraction.swift`.)
            VStack(spacing: 0) {
                LexoraHeader()

                activeScreen
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .lexoraEditMenu(engine: engine)
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
                            .font(.lexoraBody(13))
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .foregroundStyle(lexoraPalette(scheme).background)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(lexoraPalette(scheme).foreground.opacity(0.92))
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
        .background(LexoraColors.background)
        // MARK: - Word detail sheet (presented when vm.selectedWord != nil)
        //
        // Driven by `vm.selectedWord` — set both by direct UI taps (trending
        // tags, search results) and by the `.lexoraWordSelected` notification
        // handler below (EditMenu lookup hit).
        .sheet(item: $vm.selectedWord) { word in
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
        // MARK: - Lexora EditMenu notification listeners
        //
        // Posted by `LexoraEditMenuInteraction.performLookup(word:)`:
        //   - .lexoraWordSelected  → userInfo["word"] = engine.lookup result
        //   - .lexoraWordNotFound  → userInfo["word"] = String (original query)
        .onReceive(
            NotificationCenter.default.publisher(for: .lexoraWordSelected)
        ) { note in
            handleWordSelected(note)
        }
        .onReceive(
            NotificationCenter.default.publisher(for: .lexoraWordNotFound)
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
                            subtitle: "Make Lexora yours") {
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
                    .font(.lexoraDisplay(20, weight: .semibold))
                    .foregroundStyle(lexoraPalette(scheme).foreground)
                Text(subtitle)
                    .font(.lexoraBody(11))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
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

    // MARK: - Lexora EditMenu notification handlers
    //
    // The engine wrapper (`LexoraEngine.swift`) ships its own `WordEntry`
    // struct (`partOfSpeech: String`), distinct from the model
    // `WordEntry` in `LexoraModels.swift` (`partOfSpeech: PartOfSpeech`).
    // `LexoraEditMenuInteraction.performLookup` posts the engine's
    // `WordEntry` in `.lexoraWordSelected`'s userInfo. To bridge the two
    // types without modifying either file (task constraint), we:
    //
    //   1. Try a direct cast to the model `WordEntry` — works if the
    //      engine's `WordEntry` is later removed/renamed or if the engine
    //      is rewritten to return the model type.
    //   2. Fall back to `Mirror` reflection on the payload to extract the
    //      `word` string field from any struct, then re-resolve via
    //      `LexoraData.dictionary` to obtain the model `WordEntry` (with
    //      the `PartOfSpeech` enum + any illustration asset).
    //   3. If resolution fails entirely, surface a "not found" toast so
    //      the user gets feedback either way.

    private func handleWordSelected(_ note: Notification) {
        guard let raw = note.userInfo?["word"] else {
            showToast("Word not found in Lexora")
            return
        }

        // (1) Direct cast — model `WordEntry`.
        if let entry = raw as? WordEntry {
            vm.openWord(entry)
            return
        }

        // (2) Reflect on the payload to extract the `word` string, then
        //     re-resolve via LexoraData (case-insensitive match on the
        //     word title — LexoraData stores canonical entries for all 18
        //     words the engine knows about).
        if let wordString = extractWordString(from: raw),
           let resolved = LexoraData.dictionary.first(where: {
               $0.word.lowercased() == wordString.lowercased()
           }) {
            vm.openWord(resolved)
            return
        }

        // (3) Couldn't resolve — toast.
        let displayWord = extractWordString(from: raw) ?? "that word"
        showToast("\"\(displayWord)\" isn't in Lexora yet")
    }

    private func handleWordNotFound(_ note: Notification) {
        // `.lexoraWordNotFound` posts userInfo["word"] as the original
        // String query (see LexoraEditMenuInteraction.performLookup).
        let word = (note.userInfo?["word"] as? String) ?? "that word"
        showToast("\"\(word)\" isn't in Lexora yet")
    }

    /// Extract the `word` string field from any payload.
    ///
    /// Works for both the engine's `WordEntry` and the model `WordEntry`
    /// without compile-time knowledge of either type, by introspecting
    /// the value's children via `Mirror`. Also accepts a plain `String`
    /// (used by `.lexoraWordNotFound`).
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
        .environmentObject(LexoraViewModel())
        .environmentObject(LexoraThemeState())
}
