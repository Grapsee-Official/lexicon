//
//  LexiconViewModel.swift
//  Lexicon
//

import Foundation
import SwiftUI
import AVFoundation

@MainActor
final class LexiconViewModel: ObservableObject {

    private let nativeSpeech = AVSpeechSynthesizer()

    @Published var activeTab: LexiconTab = .home
    @Published var route: ScreenRoute = .home
    @Published var routeHistory: [ScreenRoute] = []

    @Published var selectedWord: WordEntry? = nil
    @Published var detailWord: WordEntry = LexiconData.serendipityEntry
    @Published var detailExpanded36: Bool = false

    @Published var searchOpen: Bool = false
    @Published var menuOpen: Bool = false

    // Native library navigation: independent of SwiftUI sheet presentation
    // timing, so users and cold-launch screenshot tests see the same routes.
    @Published var libraryOpen: Bool = false
    @Published var libraryArticleId: String? = nil

    @Published var searchQuery: String = "serendipity"
    @Published var collectionFilter: String = "All"
    @Published var selectedCategoryId: String = "vocabulary"
    @Published var searchScopeTab: String = "All"
    @Published var filterLanguage: String = "All Languages"
    @Published var filterPos: String = "All Types"
    @Published var filterDifficulty: String = "Any Level"
    @Published var filterSort: String = "Relevance"

    @Published var savedWordIds: [String] = ["serendipity", "ethereal", "tranquility"]
    @Published var favoriteWordIds: [String] = ["serendipity", "solitude", "wanderlust", "eloquence"]
    @Published var recentSearches: [String] = ["serendipity", "tranquility", "eloquence", "nostalgia", "wanderlust"]
    @Published var popularSearches: [String] = ["serendipity", "eloquence", "nostalgia", "tranquility", "wanderlust"]
    @Published var bookmarkedArticleIds: Set<String> =
        Set(UserDefaults.standard.stringArray(forKey: "lexicon.articleBookmarks") ?? [])
    @Published var recentlyViewedIds: [String] = ["ephemeral", "wanderlust", "serendipity", "limerence", "ethereal"]

    @Published var notes: [NoteItem] = LexiconData.defaultNotes
    @Published var studyLists: [StudyListItem] = LexiconData.defaultStudyLists
    @Published var templates: [VocabTemplate] = LexiconData.defaultTemplates
    @Published var todayStudiedWords: Int = 0
    @Published var todayGoalWords: Int = 10
    @Published var dayStreak: Int = 0
    @Published var wordBuilderOpen: Bool = false
    @Published var isOfflinePackReady: Bool = true
    @Published var isLoading: Bool = false
    @Published var errorBanner: String? = nil

    @Published var snapshotOutPath: String? = nil
    @Published var snapshotSyncDir: String? = nil

    lazy var engine: LexiconEngine = {
        let eng = LexiconEngine()
        _ = eng.loadDatabase(dbDir: ":embedded:")
        return eng
    }()

    private let savedKey = "lexicon.savedWordIds"
    private let recentKey = "lexicon.recentSearches"
    private let favoriteKey = "lexicon.favoriteWordIds"
    private let viewedKey = "lexicon.recentlyViewedIds"
    private let notesKey = "lexicon.notes"
    private let studyKey = "lexicon.studyLists"
    private let templatesKey = "lexicon.templates"
    private let todayKey = "lexicon.todayStudy"
    private let studyDateKey = "lexicon.studyDate"
    private let lastPracticeDateKey = "lexicon.lastPracticeDate"
    private let streakKey = "lexicon.dayStreak"

    private var todayDate: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date())
    }

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
        if let idx = args.firstIndex(of: "-lexiconSnapshotSyncDir"), idx + 1 < args.count {
            snapshotSyncDir = args[idx + 1]
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
        case "library":
            openLibrary()
        case "library_article":
            openLibrary(articleId: LexiconData.articles.first?.id)
        case "more":
            setActiveTab(.more)
        default:
            break
        }
    }

    func openLibrary(articleId: String? = nil) {
        let validArticle = articleId.flatMap { id in
            LexiconData.articles.first(where: { $0.id == id })?.id
        }
        searchOpen = false
        menuOpen = false
        libraryArticleId = validArticle
        libraryOpen = true
        activeTab = .home
        route = .home
    }

    func openLibraryArticle(_ id: String) {
        openLibrary(articleId: id)
    }

    func closeLibrary() {
        libraryOpen = false
        libraryArticleId = nil
    }

    func navigate(to target: ScreenRoute, word: WordEntry? = nil, expand36: Bool = false) {
        closeLibrary()
        let nextTab: LexiconTab
        switch target {
        case .home:
            nextTab = .home
        case .dictionaryHome, .categoryDirectory, .categoryDetail, .wordOfTheDay, .wordDetail, .searchResults, .expandedExploration:
            nextTab = .dictionary
        case .searchDiscovery:
            nextTab = .search
        case .collections, .myNotes, .studyLists, .quickTemplates:
            nextTab = .collections
        case .more:
            nextTab = .more
        }
        routeHistory = Array((routeHistory + [route]).suffix(12))
        if let w = word ?? (target == .wordOfTheDay ? LexiconData.featuredWord() : nil) {
            detailWord = w
            recentlyViewedIds = Array(([w.id] + recentlyViewedIds.filter { $0 != w.id }).prefix(8))
            UserDefaults.standard.set(recentlyViewedIds, forKey: viewedKey)
        }
        detailExpanded36 = expand36
        activeTab = nextTab
        route = target
        searchOpen = false
    }

    func navigateBack() {
        if libraryOpen {
            if libraryArticleId != nil {
                libraryArticleId = nil
            } else {
                closeLibrary()
            }
            return
        }
        let prev = routeHistory.last ?? .home
        if !routeHistory.isEmpty { routeHistory.removeLast() }
        switch prev {
        case .home: activeTab = .home
        case .dictionaryHome, .categoryDirectory, .categoryDetail, .wordOfTheDay, .wordDetail, .searchResults, .expandedExploration:
            activeTab = .dictionary
        case .searchDiscovery: activeTab = .search
        case .collections, .myNotes, .studyLists, .quickTemplates: activeTab = .collections
        case .more: activeTab = .more
        }
        searchOpen = false
        detailExpanded36 = prev == .expandedExploration
        route = prev
    }

    func setActiveTab(_ tab: LexiconTab) {
        closeLibrary()
        activeTab = tab
        routeHistory = []
        searchOpen = false
        switch tab {
        case .home: route = .home
        case .dictionary: route = .dictionaryHome
        case .search: route = .searchDiscovery
        case .collections: route = .collections
        case .more: route = .more
        }
    }

    func openCategory(_ id: String) {
        guard LexiconData.categories.contains(where: { $0.id == id }) else { return }
        selectedCategoryId = id
        navigate(to: .categoryDetail)
    }

    func lookupCategoryTerm(_ term: String) {
        searchScopeTab = "All"
        filterLanguage = "All Languages"
        filterPos = "All Types"
        filterDifficulty = "Any Level"
        filterSort = "Relevance"
        submitSearch(term)
    }

    func openCollectionFilter(_ filter: String) {
        collectionFilter = ["All", "Favorites", "Bookmarks"].contains(filter) ? filter : "All"
        navigate(to: .collections)
    }

    /// Speak the word with Apple's native pronunciation engine.
    func pronounce(_ word: String) {
        let clean = word.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty else { return }
        if nativeSpeech.isSpeaking {
            nativeSpeech.stopSpeaking(at: .immediate)
        }
        let utterance = AVSpeechUtterance(string: clean)
        utterance.voice = AVSpeechSynthesisVoice(language: "en-GB")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        nativeSpeech.speak(utterance)
    }

    func toggleArticleBookmark(_ id: String) {
        guard LexiconData.articles.contains(where: { $0.id == id }) else { return }
        if bookmarkedArticleIds.contains(id) {
            bookmarkedArticleIds.remove(id)
        } else {
            bookmarkedArticleIds.insert(id)
        }
        UserDefaults.standard.set(bookmarkedArticleIds.sorted(),
                                  forKey: "lexicon.articleBookmarks")
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
        let top = searchWords(trimmed, limit: 1).first
        selectedWord = nil
        navigate(to: .searchResults, word: top)
    }

    func cycleSearchFilter(_ which: String) {
        switch which {
        case "language":
            let opts = ["All Languages"] + Array(Set(LexiconData.dictionary.map {
                String($0.language.split(separator: "(").first ?? "").trimmingCharacters(in: .whitespaces)
            })).sorted()
            let idx = (opts.firstIndex(of: filterLanguage) ?? 0) + 1
            filterLanguage = opts[idx % opts.count]
        case "pos":
            let opts = ["All Types"] + Array(Set(LexiconData.dictionary.map { $0.partOfSpeech.displayValue })).sorted()
            let idx = (opts.firstIndex(of: filterPos) ?? 0) + 1
            filterPos = opts[idx % opts.count]
        case "difficulty":
            let opts = ["Any Level"] + Array(Set(LexiconData.dictionary.map { $0.level })).sorted()
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
        persistLibrary()
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
        persistLibrary()
    }

    func toggleNoteBookmark(_ noteId: String) {
        if let i = notes.firstIndex(where: { $0.id == noteId }) {
            notes[i].bookmarked.toggle()
        }
        persistLibrary()
    }

    func updateNote(_ noteId: String, title: String, body: String) {
        if let i = notes.firstIndex(where: { $0.id == noteId }) {
            notes[i].title = title.trimmingCharacters(in: .whitespacesAndNewlines)
            notes[i].body = body.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        persistLibrary()
    }

    func deleteNote(_ noteId: String) {
        notes.removeAll { $0.id == noteId }
        persistLibrary()
    }

    /// Mark one reviewed word, only after its definition is revealed.
    /// A blank word field draws from saved words; unknown words are not counted.
    @discardableResult
    func createStudyList(title: String, words: String) -> Bool {
        let name = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else { return false }
        let terms: [String] = words.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? savedWordIds
            : words.components(separatedBy: CharacterSet(charactersIn: ",;\n"))
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
        var ids: [String] = []
        for term in terms {
            if let word = LexiconData.dictionary.first(where: {
                $0.id.caseInsensitiveCompare(term) == .orderedSame ||
                $0.word.caseInsensitiveCompare(term) == .orderedSame
            }), !ids.contains(word.id) {
                ids.append(word.id)
            }
        }
        guard !ids.isEmpty else { return false }
        studyLists.append(StudyListItem(
            id: "sl_custom_\(UUID().uuidString)", title: name,
            totalWords: ids.count, completedWords: 0,
            thumbnail: "ill-thumb-books", wordIds: ids
        ))
        persistLibrary()
        return true
    }

    func resetStudyList(_ id: String) {
        guard let index = studyLists.firstIndex(where: { $0.id == id }) else { return }
        studyLists[index].completedWords = 0
        persistLibrary()
    }

    func deleteStudyList(_ id: String) {
        guard id.hasPrefix("sl_custom_") else { return }
        studyLists.removeAll { $0.id == id }
        persistLibrary()
    }

    func advanceStudyList(_ listId: String) {
        guard let i = studyLists.firstIndex(where: { $0.id == listId }),
              studyLists[i].completedWords < studyLists[i].wordIds.count else { return }
        studyLists[i].completedWords = min(studyLists[i].wordIds.count, studyLists[i].completedWords + 1)
        todayStudiedWords = min(todayGoalWords, todayStudiedWords + 1)
        let defaults = UserDefaults.standard
        let last = defaults.string(forKey: lastPracticeDateKey)
        let fmt = DateFormatter()
        fmt.locale = Locale(identifier: "en_US_POSIX")
        fmt.dateFormat = "yyyy-MM-dd"
        let yesterday = fmt.string(from: Calendar.current.date(byAdding: .day, value: -1, to: Date()) ?? Date())
        if last != todayDate {
            dayStreak = last == yesterday ? dayStreak + 1 : 1
            defaults.set(todayDate, forKey: lastPracticeDateKey)
        }
        persistLibrary()
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
        persistLibrary()
    }

    func toggleTemplateStar(_ id: String) {
        if let i = templates.firstIndex(where: { $0.id == id }) {
            templates[i].starred.toggle()
        }
        persistLibrary()
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
        if searchScopeTab == "Examples" {
            for entry in LexiconData.dictionary
                where entry.examples.contains(where: { $0.localizedCaseInsensitiveContains(q) }) {
                if !seen.contains(entry.id) {
                    seen.insert(entry.id)
                    results.append(entry)
                }
            }
        }
        switch searchScopeTab {
        case "Words":
            results = results.filter { $0.word.localizedCaseInsensitiveContains(q) }
        case "Meanings":
            results = results.filter { $0.definitions.contains(where: { $0.localizedCaseInsensitiveContains(q) }) }
        case "Examples":
            results = results.filter { $0.examples.contains(where: { $0.localizedCaseInsensitiveContains(q) }) }
        case "Idioms":
            results = []
        default:
            break
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
        let defaults = UserDefaults.standard
        if let saved = defaults.array(forKey: savedKey) as? [String] {
            savedWordIds = saved
        }
        if let recent = defaults.array(forKey: recentKey) as? [String] {
            recentSearches = recent
        }
        if let favorites = defaults.array(forKey: favoriteKey) as? [String] {
            favoriteWordIds = favorites
        }
        if let viewed = defaults.array(forKey: viewedKey) as? [String] {
            recentlyViewedIds = viewed
        }
        if let data = defaults.data(forKey: notesKey),
           let stored = try? JSONDecoder().decode([NoteItem].self, from: data) {
            notes = stored
        }
        if let data = defaults.data(forKey: studyKey),
           let stored = try? JSONDecoder().decode([StudyListItem].self, from: data) {
            studyLists = stored.map { list in
                var fixed = list
                fixed.totalWords = list.wordIds.count
                fixed.completedWords = list.totalWords == fixed.totalWords
                    ? min(list.completedWords, fixed.totalWords) : 0
                return fixed
            }
        }
        if let data = defaults.data(forKey: templatesKey),
           let stored = try? JSONDecoder().decode([VocabTemplate].self, from: data) {
            templates = stored
        }
        todayStudiedWords = defaults.string(forKey: studyDateKey) == todayDate
            ? defaults.integer(forKey: todayKey) : 0
        let fmt = DateFormatter()
        fmt.locale = Locale(identifier: "en_US_POSIX")
        fmt.dateFormat = "yyyy-MM-dd"
        let yesterday = fmt.string(from: Calendar.current.date(byAdding: .day, value: -1, to: Date()) ?? Date())
        let last = defaults.string(forKey: lastPracticeDateKey)
        dayStreak = last == todayDate || last == yesterday ? defaults.integer(forKey: streakKey) : 0
    }

    private func persistLibrary() {
        let defaults = UserDefaults.standard
        defaults.set(favoriteWordIds, forKey: favoriteKey)
        if let data = try? JSONEncoder().encode(notes) { defaults.set(data, forKey: notesKey) }
        if let data = try? JSONEncoder().encode(studyLists) { defaults.set(data, forKey: studyKey) }
        if let data = try? JSONEncoder().encode(templates) { defaults.set(data, forKey: templatesKey) }
        defaults.set(todayStudiedWords, forKey: todayKey)
        defaults.set(todayDate, forKey: studyDateKey)
        defaults.set(dayStreak, forKey: streakKey)
    }

    private func persistSaved() {
        UserDefaults.standard.set(savedWordIds, forKey: savedKey)
    }

    private func persistRecent() {
        UserDefaults.standard.set(recentSearches, forKey: recentKey)
    }
}
