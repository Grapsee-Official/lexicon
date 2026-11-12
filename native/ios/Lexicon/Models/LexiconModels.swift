//
//  LexiconModels.swift
//  Lexicon
//

import Foundation

enum PartOfSpeech: String, Codable, CaseIterable {
    case noun, verb, adjective, adverb
    case pronoun, preposition, conjunction, interjection

    var displayValue: String { rawValue }
}

struct WordTranslation: Codable, Identifiable, Hashable {
    var id: String { code }
    let language: String
    let code: String
    let translated: String
    var transliteration: String = ""
}

struct WordEntry: Codable, Identifiable, Hashable {
    let id: String
    let word: String
    let phonetic: String
    let partOfSpeech: PartOfSpeech
    let definitions: [String]
    let etymology: String
    let synonyms: [String]
    let antonyms: [String]
    let examples: [String]
    let language: String
    var illustration: String? = nil
    var originPeriod: String = "Late 18th c."
    var frequency: String = "High"
    var level: String = "Intermediate"
    var relatedWords: [String] = []
    var didYouKnow: String = ""
}

struct LexiconCategory: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let subtitle: String
    let illustration: String
}

struct ExploreTopic: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let subtitle: String
    let illustration: String
}

struct Article: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let excerpt: String
    let readMinutes: Int
    let thumbnail: String
    var bookmarked: Bool = false
}

struct TrendingTag: Codable, Identifiable, Hashable {
    var id: String { wordId }
    let label: String
    let wordId: String
}

enum LexiconTab: String, CaseIterable, Codable {
    case home, dictionary, search, collections, more
}

enum NoteCategory: String, Codable, CaseIterable {
    case personal = "Personal"
    case wordNote = "Word Note"
    case study = "Study"
    case idea = "Idea"
}

struct NoteItem: Codable, Identifiable, Hashable {
    let id: String
    var category: NoteCategory
    var title: String
    var body: String
    var dateLabel: String
    var bookmarked: Bool = false
    var illustration: String? = nil
}

struct StudyListItem: Codable, Identifiable, Hashable {
    let id: String
    var title: String
    var totalWords: Int
    var completedWords: Int
    var thumbnail: String
    var wordIds: [String]

    var percentComplete: Int {
        guard totalWords > 0 else { return 0 }
        return min(100, max(0, Int((Double(completedWords) * 100.0) / Double(totalWords))))
    }
}

enum TemplateCategory: String, Codable, CaseIterable {
    case learning = "Learning"
    case writing = "Writing"
    case exam = "Exam"
    case dailyLife = "Daily Life"
}

struct VocabTemplate: Codable, Identifiable, Hashable {
    let id: String
    var title: String
    var subtitle: String
    var category: TemplateCategory
    var illustration: String
    var starred: Bool = true
    var sampleWords: [String] = []
}

struct IdiomPhrase: Codable, Identifiable, Hashable {
    let id: String
    let phrase: String
    let meaning: String
    let example: String
    let kind: String
}

struct MorphemeRoot: Codable, Identifiable, Hashable {
    var id: String { root }
    let root: String
    let meaning: String
    let origin: String
    let derivatives: [String]
}

enum ScreenRoute: String, CaseIterable, Codable {
    case home                = "home"
    case dictionaryHome      = "dictionary_home"      // Page 1
    case wordOfTheDay        = "word_of_the_day"      // Page 2
    case wordDetail          = "word_detail"          // Page 3
    case collections         = "collections"          // Page 4
    case myNotes             = "my_notes"             // Page 5
    case studyLists          = "study_lists"          // Page 6
    case quickTemplates      = "quick_templates"      // Page 7
    case searchDiscovery     = "search_discovery"     // Page 8
    case searchResults       = "search_results"       // Page 9
    case expandedExploration = "expanded_exploration" // Page 10
    case more                = "more"
}
