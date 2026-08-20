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
//    }
//    .sheet(item: $vm.selectedWord) { WordDetailSheet(word:) }
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @EnvironmentObject private var theme: LexoraThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ZStack {
            // MARK: - Background — warm cream (#FAF3E9) light, midnight dark.
            lexoraPalette(scheme).background
                .ignoresSafeArea()

            // MARK: - Main column (header + active screen).
            // Screens own their `.padding(.bottom, 120)` so the floating
            // BottomNav capsule never overlaps their content.
            VStack(spacing: 0) {
                LexoraHeader()

                activeScreen
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
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
        }
        .background(LexoraColors.background)
        // MARK: - Word detail sheet (presented when vm.selectedWord != nil)
        .sheet(item: $vm.selectedWord) { word in
            WordDetailSheet(word: word)
                .environmentObject(vm)
                .environmentObject(theme)
        }
        // Animate the search overlay appearance + tab transitions.
        .animation(.easeInOut(duration: 0.2), value: vm.searchOpen)
        .animation(.easeInOut(duration: 0.2), value: vm.activeTab)
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
}

// MARK: - Preview

#Preview {
    ContentView()
        .environmentObject(LexoraViewModel())
        .environmentObject(LexoraThemeState())
}
