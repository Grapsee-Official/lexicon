//
//  ContentView.swift
//  Lexora
//
//  Root view — hosts the LexoraHeader + active screen + BottomNav,
//  plus a basic search modal + word-detail sheet wired to the ViewModel.
//
//  Mirrors the structure of `src/app/page.tsx`:
//    <div flex min-h-screen flex-col bg-background>
//      <LexoraHeader />
//      <main flex-1 ...> activeTab switch </main>
//      <BottomNav />
//      <WordDetailSheet /> <SearchModal />
//    </div>
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @EnvironmentObject private var theme: LexoraThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        VStack(spacing: 0) {
            LexoraHeader()

            // Sub-header for non-home tabs.
            if let meta = tabMeta(vm.activeTab) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(meta.title)
                        .font(.lexoraDisplay(20, weight: .semibold))
                        .foregroundStyle(lexoraPalette(scheme).foreground)
                    Text(meta.subtitle)
                        .font(.lexoraBody(11))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 4)
            }

            // Active screen.
            Group {
                switch vm.activeTab {
                case .home:        HomeScreen()
                case .dictionary:  SimpleBrowseScreen(title: "Dictionary",
                                                      subtitle: "Browse the full lexicon")
                case .collections: SimpleBrowseScreen(title: "Collections",
                                                      subtitle: "Your saved words",
                                                      source: vm.savedWordIds.compactMap { LexoraData.getWordById($0) },
                                                      emptyMessage: "No saved words yet — tap the bookmark on a word to collect it.")
                case .more:        SimpleBrowseScreen(title: "Settings",
                                                      subtitle: "Make Lexora yours",
                                                      isSettings: true)
                case .search:      HomeScreen() // search opens modal; show home behind
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            BottomNav()
        }
        .background(LexoraColors.background)
        .ignoresSafeArea(.keyboard)
        // Search modal (sheet)
        .sheet(isPresented: $vm.searchOpen) {
            SearchModalSheet()
                .environmentObject(vm)
        }
        // Word detail (sheet)
        .sheet(item: $vm.selectedWord) { word in
            WordDetailSheet(word: word)
                .environmentObject(vm)
        }
    }

    private func tabMeta(_ tab: LexoraTab) -> (title: String, subtitle: String)? {
        switch tab {
        case .dictionary:  return ("Dictionary", "Browse the full lexicon")
        case .collections: return ("Collections", "Your saved words")
        case .more:        return ("Settings", "Make Lexora yours")
        default:           return nil
        }
    }
}

// MARK: - Minimal secondary screens (kept lean — main focus is Home).

/// Lightweight list screen for the Dictionary/Collections/More tabs.
/// The Dictionary and Collections tabs are intentionally simple here —
/// Phase 2 scope is the home screen + component library 1:1; the full
/// Dictionary/Collections/More screens from the web build can be ported
/// in a follow-up pass once the user verifies the home build.
struct SimpleBrowseScreen: View {
    let title: String
    let subtitle: String
    var source: [WordEntry] = LexoraData.dictionary
    var emptyMessage: String = "No entries."
    var isSettings: Bool = false

    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if isSettings {
                    settingsList
                } else if source.isEmpty {
                    Text(emptyMessage)
                        .font(.lexoraBody(13))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                        .padding(.top, 40)
                        .frame(maxWidth: .infinity)
                } else {
                    ForEach(source) { word in
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            vm.openWord(word)
                        } label: {
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(word.word)
                                        .font(.lexoraDisplay(16, weight: .semibold))
                                        .foregroundStyle(lexoraPalette(scheme).foreground)
                                    Spacer()
                                    Image(systemName: vm.isSaved(word.id) ? "bookmark.fill" : "bookmark")
                                        .font(.system(size: 14, weight: .regular))
                                        .foregroundStyle(lexoraPalette(scheme).highlight)
                                }
                                Text(word.phonetic)
                                    .font(.lexoraBody(11))
                                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                                Text(word.definitions.first ?? "")
                                    .font(.lexoraLong(12))
                                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.8))
                                    .lineLimit(2)
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(lexoraPalette(scheme).card)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 120)
        }
        .background(LexoraColors.background)
    }

    private var settingsList: some View {
        VStack(alignment: .leading, spacing: 8) {
            ForEach(["Theme", "Notifications", "Reading list", "About Lexora"], id: \.self) { row in
                HStack {
                    Text(row)
                        .font(.lexoraBody(14))
                        .foregroundStyle(lexoraPalette(scheme).foreground)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                }
                .padding(.vertical, 12)
                .padding(.horizontal, 14)
                .background(lexoraPalette(scheme).card)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            }
        }
    }
}

// MARK: - Search modal sheet (basic — uses LexoraData.searchWords)
struct SearchModalSheet: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dismiss) private var dismiss

    @State private var query: String = ""

    private var results: [WordEntry] {
        LexoraData.searchWords(query)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.5))
                    TextField("Search for a word...", text: $query)
                        .font(.lexoraBody(13))
                        .submitLabel(.search)
                        .onSubmit {
                            if !query.isEmpty {
                                vm.addRecentSearch(query)
                            }
                        }
                    if !query.isEmpty {
                        Button {
                            query = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                        }
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(lexoraPalette(scheme).card)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(lexoraPalette(scheme).border, lineWidth: 1))
                .padding(16)

                if query.isEmpty && !vm.recentSearches.isEmpty {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Recent")
                                .font(.lexoraDisplay(13, weight: .semibold))
                                .foregroundStyle(lexoraPalette(scheme).foreground)
                            ForEach(vm.recentSearches, id: \.self) { recent in
                                Button {
                                    query = recent
                                } label: {
                                    HStack {
                                        Image(systemName: "clock.arrow.circlepath")
                                            .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                                        Text(recent)
                                            .font(.lexoraBody(13))
                                            .foregroundStyle(lexoraPalette(scheme).foreground)
                                        Spacer()
                                    }
                                    .padding(.vertical, 8)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                } else if results.isEmpty {
                    Text("No words match \"\(query)\".")
                        .font(.lexoraBody(13))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                        .padding(.top, 40)
                        .frame(maxWidth: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 8) {
                            ForEach(results) { word in
                                Button {
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    vm.addRecentSearch(query.isEmpty ? word.word : query)
                                    dismiss()
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                        vm.openWord(word)
                                    }
                                } label: {
                                    VStack(alignment: .leading, spacing: 4) {
                                        HStack {
                                            Text(word.word)
                                                .font(.lexoraDisplay(15, weight: .semibold))
                                                .foregroundStyle(lexoraPalette(scheme).foreground)
                                            Text(word.phonetic)
                                                .font(.lexoraBody(11))
                                                .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                                            Spacer()
                                            Text(word.partOfSpeech.displayValue)
                                                .font(.lexoraBody(10))
                                                .foregroundStyle(lexoraPalette(scheme).highlight)
                                        }
                                        Text(word.definitions.first ?? "")
                                            .font(.lexoraLong(12))
                                            .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.8))
                                            .lineLimit(2)
                                    }
                                    .padding(12)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(lexoraPalette(scheme).card)
                                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .background(LexoraColors.background)
            .navigationTitle("Search")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                        .font(.lexoraBody(13, weight: .medium))
                        .foregroundStyle(lexoraPalette(scheme).highlight)
                }
            }
        }
    }
}

// MARK: - Word detail sheet (basic — opens when a word is selected)
struct WordDetailSheet: View {
    let word: WordEntry
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Header
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(alignment: .firstTextBaseline) {
                            Text(word.word)
                                .font(.lexoraDisplay(28, weight: .semibold))
                                .foregroundStyle(lexoraPalette(scheme).foreground)
                            Spacer()
                            Button {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                vm.toggleSave(word.id)
                            } label: {
                                Image(systemName: vm.isSaved(word.id) ? "bookmark.fill" : "bookmark")
                                    .font(.system(size: 18, weight: .regular))
                                    .foregroundStyle(lexoraPalette(scheme).highlight)
                            }
                        }
                        Text(word.phonetic)
                            .font(.lexoraBody(13))
                            .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                        Text(word.partOfSpeech.displayValue + " · " + word.language)
                            .font(.lexoraBody(11))
                            .foregroundStyle(lexoraPalette(scheme).highlight)
                    }

                    divider

                    // Definitions
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Definitions")
                            .font(.lexoraDisplay(15, weight: .semibold))
                            .foregroundStyle(lexoraPalette(scheme).foreground)
                        ForEach(Array(word.definitions.enumerated()), id: \.offset) { idx, def in
                            Text("\(idx + 1). \(def)")
                                .font(.lexoraLong(13))
                                .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.9))
                        }
                    }

                    divider

                    // Etymology
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Etymology")
                            .font(.lexoraDisplay(15, weight: .semibold))
                            .foregroundStyle(lexoraPalette(scheme).foreground)
                        Text(word.etymology)
                            .font(.lexoraLong(13))
                            .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.85))
                    }

                    if !word.examples.isEmpty {
                        divider
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Examples")
                                .font(.lexoraDisplay(15, weight: .semibold))
                                .foregroundStyle(lexoraPalette(scheme).foreground)
                            ForEach(Array(word.examples.enumerated()), id: \.offset) { _, ex in
                                Text("\"\(ex)\"")
                                    .font(.lexoraLong(13).italic())
                                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.85))
                            }
                        }
                    }

                    if !word.synonyms.isEmpty {
                        divider
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Synonyms")
                                .font(.lexoraDisplay(15, weight: .semibold))
                                .foregroundStyle(lexoraPalette(scheme).foreground)
                            Text(word.synonyms.joined(separator: ", "))
                                .font(.lexoraLong(13))
                                .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.85))
                        }
                    }
                }
                .padding(16)
                .padding(.bottom, 40)
            }
            .background(LexoraColors.background)
            .navigationTitle("Word")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close") { dismiss() }
                        .font(.lexoraBody(13, weight: .medium))
                        .foregroundStyle(lexoraPalette(scheme).highlight)
                }
            }
        }
    }

    private var divider: some View {
        Rectangle()
            .fill(lexoraPalette(scheme).border)
            .frame(height: 1)
    }
}
