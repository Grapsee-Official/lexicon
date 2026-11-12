//
//  LexiconViewModel.swift
//  Lexicon
//

import Foundation
import SwiftUI

@MainActor
final class LexiconViewModel: ObservableObject {

    @Published var activeTab: LexiconTab = .home
    @Published var route: ScreenRoute = .dictionaryHome
    @Published var routeHistory: [ScreenRoute] = []

    @Published var selectedWord: WordEntry? = nil
    @Published var detailWord: WordEntry = LexiconData.serendipityEntry
    @Published var detailExpanded36: Bool = false

    @Published var searchOpen: Bool = false
    @Published var menuOpen: Bool = false

    @Published var searchQuery: String = "serendipity"
    @Published var searchScopeTab: String = "All"
    @Published var filterLanguage: String = "All Languages"
    @Published var filterPos: String = "All Types"
    @Published var filterDifficulty: String = "Any Level"
    @Published var filterSort: String = "Relevance"

    @Published var savedWordIds: [String] = ["serendipity", "ethereal", "tranquility"]
    @Published var favoriteWordIds: [String] = ["serendipity", "solitude", "wanderlust", "eloquence"]
    @Published var recentSearches: [String] = ["serendipity", "tranquility", "eloquence", "nostalgia", "wanderlust"]
    @Published var popularSearches: [String] = ["serendipity", "eloquence", "nostalgia", "tranquility", "wanderlust"]
    @Published var recentlyViewedIds: [String] = ["ephemeral", "wanderlust", "serendipity", "limerence", "ethereal"]

    @Published var notes: [NoteItem] = LexiconData.defaultNotes
    @Published var studyLists: [StudyListItem] = LexiconData.defaultStudyLists
    @Published var templates: [VocabTemplate] = LexiconData.defaultTemplates
    @Published var todayStudiedWords: Int = 6
    @Published var todayGoalWords: Int = 10
    @Published var dayStreak: Int = 12
    @Published var wordBuilderOpen: Bool = false
    @Published var isOfflinePackReady: Bool = true
    @Published var isLoading: Bool = false
    @Published var errorBanner: String? = nil

    @Published var snapshotOutPath: String? = nil
    @Published var snapshotAllDir: String? = nil

    lazy var engine: LexiconEngine = {
        let eng = LexiconEngine()
        _ = eng.loadDatabase(dbDir: ":embedded:")
        return eng
    }()

    private let savedKey = "lexicon.savedWordIds"
    private let recentKey = "lexicon.recentSearches"

    init() {
        loadPersisted()
        applyLaunchArguments()
    }

    private func applyLaunchArguments() {
        let args = ProcessInfo.processInfo.arguments
        if let idx = args.firstIndex(of: "-lexoraTab"), idx + 1 < args.count {
            let tabArg = args[idx + 1].lowercased()
            switch tabArg {
            case "home": setActiveTab(.home)
            case "dictionary": setActiveTab(.dictionary)
            case "search": setActiveTab(.search)
            case "collections": setActiveTab(.collections)
            case "more": setActiveTab(.more)
            default: break
            }
        }
        if let idx = args.firstIndex(of: "-lexiconScreen"), idx + 1 < args.count {
            let scr = args[idx + 1].lowercased()
            applyScreenName(scr)
        }
        if let idx = args.firstIndex(of: "-lexoraWord"), idx + 1 < args.count {
            let wordArg = args[idx + 1]
            if let match = searchWords(wordArg, limit: 1).first {
                detailWord = match
                navigate(to: .wordDetail, word: match)
            }
        }
        if let idx = args.firstIndex(of: "-lexoraSnapshotOut"), idx + 1 < args.count {
            snapshotOutPath = args[idx + 1]
        }
        if let idx = args.firstIndex(of: "-lexiconSnapshotAllDir"), idx + 1 < args.count {
            snapshotAllDir = args[idx + 1]
        }
    }

    func applyScreenName(_ scr: String) {
        switch scr {
        case "1", "dictionary_home", "dict_home":
            navigate(to: .dictionaryHome)
        case "2", "wotd", "word_of_the_day":
            navigate(to: .wordOfTheDay, word: LexiconData.serendipityEntry)
        case "3", "word_detail", "detail":
            navigate(to: .wordDetail, word: LexiconData.serendipityEntry)
        case "4", "collections":
            navigate(to: .collections)
        case "5", "notes", "my_notes":
            navigate(to: .myNotes)
        case "6", "study_lists", "study":
            navigate(to: .studyLists)
        case "7", "templates", "quick_templates":
            navigate(to: .quickTemplates)
        case "8", "search", "search_discovery":
            navigate(to: .searchDiscovery)
        case "9", "results", "search_results":
            searchQuery = "serendipity"
            navigate(to: .searchResults, word: LexiconData.serendipityEntry)
        case "10", "expanded", "expanded_exploration":
            navigate(to: .expandedExploration, word: LexiconData.serendipityEntry, expand36: false)
        case "10_expanded", "translations":
            navigate(to: .expandedExploration, word: LexiconData.serendipityEntry, expand36: true)
        case "home":
            setActiveTab(.home)
        default:
            break
        }
    }

    func navigate(to target: ScreenRoute, word: WordEntry? = nil, expand36: Bool = false) {
        let nextTab: LexiconTab
        switch target {
        case .home:
            nextTab = .home
        case .dictionaryHome, .wordOfTheDay, .wordDetail, .searchResults, .expandedExploration:
            nextTab = .dictionary
        case .searchDiscovery:
            nextTab = .search
        case .collections, .myNotes, .studyLists, .quickTemplates:
            nextTab = .collections
        case .more:
            nextTab = .more
        }
        routeHistory = Array((routeHistory + [route]).suffix(12))
        if let w = word {
            detailWord = w
            recentlyViewedIds = Array(([w.id] + recentlyViewedIds.filter { $0 != w.id }).prefix(8))
        }
        detailExpanded36 = expand36
        activeTab = nextTab
        route = target
        searchOpen = false
    }

    func navigateBack() {
        let prev = routeHistory.last ?? .dictionaryHome
        if !routeHistory.isEmpty { routeHistory.removeLast() }
        route = prev
    }

    func setActiveTab(_ tab: LexiconTab) {
        activeTab = tab
        searchOpen = false
        switch tab {
        case .home: route = .home
        case .dictionary: route = .dictionaryHome
        case .search: route = .searchDiscovery
        case .collections: route = .collections
        case .more: route = .more
        }
    }

    func openWord(_ word: WordEntry) {
        navigate(to: .wordDetail, word: word, expand36: false)
    }

    func openWordExpanded(_ word: WordEntry) {
        navigate(to: .expandedExploration, word: word, expand36: true)
    }

    func closeWord() {
        selectedWord = nil
    }

    func openSearch() {
        navigate(to: .searchDiscovery)
    }

    func closeSearch() {
        searchOpen = false
    }

    func submitSearch(_ query: String) {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        addRecentSearch(trimmed)
        searchQuery = trimmed
        let top = searchWords(trimmed, limit: 1).first ?? LexiconData.serendipityEntry
        navigate(to: .searchResults, word: top)
    }

    func cycleSearchFilter(_ which: String) {
        switch which {
        case "language":
            let opts = ["All Languages", "English", "Japanese", "Portuguese"]
            let idx = (opts.firstIndex(of: filterLanguage) ?? 0) + 1
            filterLanguage = opts[idx % opts.count]
        case "pos":
            let opts = ["All Types", "noun", "adjective"]
            let idx = (opts.firstIndex(of: filterPos) ?? 0) + 1
            filterPos = opts[idx % opts.count]
        case "difficulty":
            let opts = ["Any Level", "Intermediate", "Advanced"]
            let idx = (opts.firstIndex(of: filterDifficulty) ?? 0) + 1
            filterDifficulty = opts[idx % opts.count]
        default:
            let opts = ["Relevance", "Alphabetical"]
            let idx = (opts.firstIndex(of: filterSort) ?? 0) + 1
            filterSort = opts[idx % opts.count]
        }
    }

    func discoverRandomWord() {
        let current = detailWord.id
        let next = LexiconData.dictionary.filter { $0.id != current }.randomElement() ?? LexiconData.serendipityEntry
        navigate(to: .wordDetail, word: next)
    }

    func toggleSave(_ id: String) {
        if let idx = savedWordIds.firstIndex(of: id) {
            savedWordIds.remove(at: idx)
        } else {
            savedWordIds.insert(id, at: 0)
        }
        persistSaved()
    }

    func toggleFavorite(_ id: String) {
        if let idx = favoriteWordIds.firstIndex(of: id) {
            favoriteWordIds.remove(at: idx)
        } else {
            favoriteWordIds.insert(id, at: 0)
        }
    }

    func isSaved(_ id: String) -> Bool { savedWordIds.contains(id) }
    func isFavorite(_ id: String) -> Bool { favoriteWordIds.contains(id) }

    func addNote(category: NoteCategory, title: String, body: String) {
        let cleanTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanBody = body.trimmingCharacters(in: .whitespacesAndNewlines)
        let df = DateFormatter()
        df.locale = Locale(identifier: "en_US_POSIX")
        df.dateFormat = "MMM d, yyyy"
        let art: String
        switch category {
        case .wordNote: art = "ill-note-flowers"
        case .study: art = "ill-col-bookmarks"
        case .personal: art = "ill-col-notes"
        case .idea: art = "ill-note-lantern"
        }
        let item = NoteItem(
            id: "note_\(Int(Date().timeIntervalSince1970 * 1000))",
            category: category,
            title: cleanTitle.isEmpty ? "Untitled Note" : cleanTitle,
            body: cleanBody.isEmpty ? "Captured during countryside study." : cleanBody,
            dateLabel: df.string(from: Date()),
            bookmarked: false,
            illustration: art
        )
        notes.insert(item, at: 0)
    }

    func toggleNoteBookmark(_ noteId: String) {
        if let i = notes.firstIndex(where: { $0.id == noteId }) {
            notes[i].bookmarked.toggle()
        }
    }

    func deleteNote(_ noteId: String) {
        notes.removeAll { $0.id == noteId }
    }

    func advanceStudyList(_ listId: String) {
        if let i = studyLists.firstIndex(where: { $0.id == listId }) {
            studyLists[i].completedWords = min(studyLists[i].totalWords, studyLists[i].completedWords + 2)
        }
        todayStudiedWords = min(todayGoalWords, todayStudiedWords + 1)
    }

    func createCustomTemplate(title: String, subtitle: String, category: TemplateCategory) {
        let tpl = VocabTemplate(
            id: "tpl_\(Int(Date().timeIntervalSince1970 * 1000))",
            title: title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Countryside Journal" : title,
            subtitle: subtitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Custom literary word study." : subtitle,
            category: category,
            illustration: "ill-tpl-write",
            starred: true
        )
        templates.insert(tpl, at: 0)
    }

    func toggleTemplateStar(_ id: String) {
        if let i = templates.firstIndex(where: { $0.id == id }) {
            templates[i].starred.toggle()
        }
    }

    func addRecentSearch(_ q: String) {
        let trimmed = q.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        let lower = trimmed.lowercased()
        let filtered = recentSearches.filter { $0.lowercased() != lower }
        recentSearches = Array(([trimmed] + filtered).prefix(8))
        persistRecent()
    }

    func clearRecentSearches() {
        recentSearches = []
        persistRecent()
    }

    func clearPopularSearches() {
        popularSearches = []
    }

    func restorePopularSearches() {
        popularSearches = ["serendipity", "eloquence", "nostalgia", "tranquility", "wanderlust"]
    }

    func searchWords(_ query: String, limit: Int = 12) -> [WordEntry] {
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

        if filterLanguage != "All Languages" {
            results = results.filter { $0.language.localizedCaseInsensitiveContains(filterLanguage) }
        }
        if filterPos != "All Types" {
            results = results.filter { $0.partOfSpeech.displayValue.caseInsensitiveCompare(filterPos) == .orderedSame }
        }
        if filterDifficulty != "Any Level" {
            results = results.filter { $0.level.caseInsensitiveCompare(filterDifficulty) == .orderedSame }
        }
        if filterSort == "Alphabetical" {
            results.sort { $0.word.lowercased() < $1.word.lowercased() }
        }

        return Array(results.prefix(limit))
    }

    private func loadPersisted() {
        if let saved = UserDefaults.standard.array(forKey: savedKey) as? [String], !saved.isEmpty {
            savedWordIds = saved
        }
        if let recent = UserDefaults.standard.array(forKey: recentKey) as? [String], !recent.isEmpty {
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
