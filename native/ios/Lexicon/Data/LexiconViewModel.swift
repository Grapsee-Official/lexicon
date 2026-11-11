//
//  LexiconViewModel.swift
//  Lexicon
//

import Foundation
import SwiftUI

@MainActor
final class LexiconViewModel: ObservableObject {

    @Published var activeTab: LexiconTab = .home
    @Published var selectedWord: WordEntry? = nil
    @Published var searchOpen: Bool = false
    @Published var menuOpen: Bool = false

    @Published var savedWordIds: [String] = []
    @Published var recentSearches: [String] = []

    let engine = LexiconEngine()

    private let savedKey = "lexicon.savedWordIds"
    private let recentKey = "lexicon.recentSearches"

    init() {
        _ = engine.loadDatabase(dbDir: ":embedded:")
        loadPersisted()
        applyLaunchArguments()
    }

    private func applyLaunchArguments() {
        let args = ProcessInfo.processInfo.arguments
        if let idx = args.firstIndex(of: "-lexoraTab"), idx + 1 < args.count {
            let tabArg = args[idx + 1].lowercased()
            switch tabArg {
            case "dictionary": activeTab = .dictionary
            case "collections": activeTab = .collections
            case "more": activeTab = .more
            default: activeTab = .home
            }
        }
    }

    func setActiveTab(_ tab: LexiconTab) {
        activeTab = tab
    }

    func openWord(_ word: WordEntry) {
        selectedWord = word
    }

    func closeWord() {
        selectedWord = nil
    }

    func openSearch() {
        searchOpen = true
    }

    func closeSearch() {
        searchOpen = false
    }

    func openMenu() {
        menuOpen = true
    }

    func closeMenu() {
        menuOpen = false
    }

    func toggleSave(_ id: String) {
        if let idx = savedWordIds.firstIndex(of: id) {
            savedWordIds.remove(at: idx)
        } else {
            savedWordIds.append(id)
        }
        persistSaved()
    }

    func isSaved(_ id: String) -> Bool {
        savedWordIds.contains(id)
    }

    func addRecentSearch(_ q: String) {
        let trimmed = q.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        let lower = trimmed.lowercased()
        let filtered = recentSearches.filter { $0.lowercased() != lower }
        recentSearches = ([trimmed] + filtered).prefix(8).map { $0 }
        persistRecent()
    }

    func clearRecentSearches() {
        recentSearches = []
        persistRecent()
    }

    /// Live search routed through the C++17 engine (exact + DAWG prefix + Levenshtein <= 2)
    /// merged with canonical LexiconData entries.
    func searchWords(_ query: String, limit: Int = 8) -> [WordEntry] {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return [] }

        var seen = Set<String>()
        var results: [WordEntry] = []

        if let exact = engine.lookup(word: q) {
            let resolved = LexiconData.getWordById(exact.id) ?? exact
            seen.insert(resolved.id)
            results.append(resolved)
        }

        for sr in engine.fuzzySearch(query: q, maxDistance: 2, limit: limit) {
            let resolved = LexiconData.getWordById(sr.entry.id) ?? sr.entry
            if !seen.contains(resolved.id) {
                seen.insert(resolved.id)
                results.append(resolved)
            }
        }

        for entry in LexiconData.searchWords(q, limit: limit) {
            if !seen.contains(entry.id) {
                seen.insert(entry.id)
                results.append(entry)
            }
        }

        return Array(results.prefix(limit))
    }

    private func loadPersisted() {
        if let saved = UserDefaults.standard.array(forKey: savedKey) as? [String] {
            savedWordIds = saved
        }
        if let recent = UserDefaults.standard.array(forKey: recentKey) as? [String] {
            recentSearches = recent
        }
    }

    private func persistSaved() {
        UserDefaults.standard.set(savedWordIds, forKey: savedKey)
    }

    private func persistRecent() {
        UserDefaults.standard.set(recentSearches, forKey: recentKey)
    }
}
