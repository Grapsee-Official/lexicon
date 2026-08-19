//
//  LexoraViewModel.swift
//  Lexora
//
//  ObservableObject translation of the Zustand store in `src/lib/app-store.ts`.
//
//  State exposed via @Published:
//    - activeTab           (bottom-nav selection)
//    - selectedWord        (currently shown in the detail bottom sheet; nil = closed)
//    - searchOpen          (full-screen search modal)
//    - savedWordIds        (persisted to UserDefaults)
//    - recentSearches      (persisted to UserDefaults, max 8)
//
//  Persisted keys mirror the web `partialize` config: only savedWordIds
//  and recentSearches are written to disk; everything else is ephemeral.
//

import Foundation
import SwiftUI

@MainActor
final class LexoraViewModel: ObservableObject {

    // MARK: - Published state
    @Published var activeTab: LexoraTab = .home
    @Published var selectedWord: WordEntry? = nil
    @Published var searchOpen: Bool = false
    @Published var menuOpen: Bool = false

    // Persisted (loaded from UserDefaults on init).
    @Published var savedWordIds: [String] = []
    @Published var recentSearches: [String] = []

    // MARK: - Persistence keys
    private let savedKey = "lexora.savedWordIds"
    private let recentKey = "lexora.recentSearches"

    // MARK: - Init
    init() {
        loadPersisted()
    }

    // MARK: - Tab + sheet actions
    func setActiveTab(_ tab: LexoraTab) {
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

    // MARK: - Save / bookmark (mirrors toggleSave + isSaved)
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

    // MARK: - Recent searches (mirrors addRecentSearch + clearRecentSearches)
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

    // MARK: - Persistence (UserDefaults)
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
