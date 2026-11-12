//
//  ContentView.swift
//  Lexicon
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var toastMessage: String? = nil

    private var selectedWordBinding: Binding<WordEntry?> {
        Binding(
            get: { vm.selectedWord },
            set: { vm.selectedWord = $0 }
        )
    }

    var body: some View {
        ZStack {
            lexiconPalette(scheme).background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                if vm.route == .home || vm.route == .more {
                    LexiconHeader()
                }

                activeScreen
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .lexiconEditMenu(engine: vm.engine)
            }

            BottomNav()
                .environmentObject(vm)
                .environmentObject(theme)
                .zIndex(2)

            if vm.searchOpen {
                SearchModal()
                    .environmentObject(vm)
                    .environmentObject(theme)
                    .transition(.opacity)
                    .zIndex(10)
            }

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
                    .padding(.bottom, 140)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .zIndex(20)
                .allowsHitTesting(false)
            }
        }
        .background(lexiconPalette(scheme).background)
        .sheet(item: selectedWordBinding) { (word: WordEntry) in
            WordDetailSheet(word: word)
                .lexiconTheme()
                .environmentObject(vm)
                .environmentObject(theme)
        }
        .animation(.easeInOut(duration: 0.18), value: vm.route)
        .animation(.easeInOut(duration: 0.25), value: toastMessage)
        .task(id: toastMessage) {
            guard toastMessage != nil else { return }
            try? await Task.sleep(nanoseconds: 2_500_000_000)
            toastMessage = nil
        }
        .onReceive(NotificationCenter.default.publisher(for: .lexiconWordSelected)) { note in
            handleWordSelected(note)
        }
        .onReceive(NotificationCenter.default.publisher(for: .lexiconWordNotFound)) { note in
            handleWordNotFound(note)
        }
        .task {
            if let allDir = vm.snapshotAllDir {
                await captureAllScreensSequence(toDir: allDir)
                return
            }
            guard let outPath = vm.snapshotOutPath else { return }
            try? await Task.sleep(nanoseconds: 1_500_000_000)
            await MainActor.run {
                captureSelfWindow(to: outPath)
            }
        }
    }

    @MainActor
    private func captureAllScreensSequence(toDir dir: String) async {
        try? FileManager.default.createDirectory(atPath: dir, withIntermediateDirectories: true)
        let sequence: [(String, String, String)] = [
            ("light", "1",  "ios-01-dictionary-home-light.png"),
            ("light", "2",  "ios-02-word-of-the-day-light.png"),
            ("light", "3",  "ios-03-word-detail-light.png"),
            ("light", "4",  "ios-04-collections-light.png"),
            ("light", "5",  "ios-05-my-notes-light.png"),
            ("light", "6",  "ios-06-study-lists-light.png"),
            ("light", "7",  "ios-07-quick-templates-light.png"),
            ("light", "8",  "ios-08-search-discovery-light.png"),
            ("light", "9",  "ios-09-search-results-light.png"),
            ("light", "10", "ios-10-expanded-exploration-light.png"),
            ("dark",  "1",  "ios-01-dictionary-home-dark.png"),
            ("dark",  "3",  "ios-03-word-detail-dark.png"),
        ]
        try? await Task.sleep(nanoseconds: 1_200_000_000)
        for (mode, scr, fileName) in sequence {
            theme.theme = mode
            vm.applyScreenName(scr)
            try? await Task.sleep(nanoseconds: 550_000_000)
            let outPath = (dir as NSString).appendingPathComponent(fileName)
            captureSelfWindow(to: outPath)
        }
        let donePath = (dir as NSString).appendingPathComponent("_DONE")
        try? "ok".write(toFile: donePath, atomically: true, encoding: .utf8)
    }

    @MainActor
    private func captureSelfWindow(to outPath: String) {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first
        else { return }
        let format = UIGraphicsImageRendererFormat()
        format.scale = window.screen.scale
        format.opaque = true
        let renderer = UIGraphicsImageRenderer(size: window.bounds.size, format: format)
        let img = renderer.image { _ in
            window.drawHierarchy(in: window.bounds, afterScreenUpdates: true)
        }
        if let data = img.pngData() {
            let url = URL(fileURLWithPath: outPath)
            try? FileManager.default.createDirectory(
                at: url.deletingLastPathComponent(),
                withIntermediateDirectories: true
            )
            try? data.write(to: url)
            print("[LexiconSnapshot] Saved native iOS window PNG to \(outPath) (\(data.count) bytes)")
        }
    }

    @ViewBuilder
    private var activeScreen: some View {
        switch vm.route {
        case .home:
            HomeScreen()
        case .dictionaryHome:
            DictionaryHomeScreen()
        case .wordOfTheDay:
            WordOfTheDayScreen()
        case .wordDetail:
            WordDetailProfileScreen()
        case .collections:
            CollectionsHubScreen()
        case .myNotes:
            MyNotesScreen()
        case .studyLists:
            StudyListsScreen()
        case .quickTemplates:
            QuickTemplatesScreen()
        case .searchDiscovery:
            SearchDiscoveryScreen()
        case .searchResults:
            SearchResultsScreen()
        case .expandedExploration:
            ExpandedExplorationScreen()
        case .more:
            VStack(spacing: 0) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Settings")
                        .font(.lexiconDisplay(20, weight: .semibold))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                    Text("Make Lexicon yours")
                        .font(.lexiconBody(11))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 4)

                MoreScreen()
            }
        }
    }

    private func handleWordSelected(_ note: Notification) {
        guard let raw = note.userInfo?["word"] else {
            showToast("Word not found in Lexicon")
            return
        }
        if let wordString = extractWordString(from: raw),
           let resolved = LexiconData.dictionary.first(where: { (entry: WordEntry) in
               entry.word.lowercased() == wordString.lowercased()
           }) {
            vm.openWord(resolved)
            return
        }
        let displayWord = extractWordString(from: raw) ?? "that word"
        showToast("\"\(displayWord)\" isn't in Lexicon yet")
    }

    private func handleWordNotFound(_ note: Notification) {
        let word = (note.userInfo?["word"] as? String) ?? "that word"
        showToast("\"\(word)\" isn't in Lexicon yet")
    }

    private func extractWordString(from raw: Any) -> String? {
        if let s = raw as? String { return s }
        let mirror = Mirror(reflecting: raw)
        for child in mirror.children where child.label == "word" {
            if let s = child.value as? String { return s }
        }
        return nil
    }

    private func showToast(_ message: String) {
        toastMessage = message
    }
}
