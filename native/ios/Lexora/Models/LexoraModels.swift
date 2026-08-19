//
//  LexoraModels.swift
//  Lexora
//
//  Native Swift translations of the TypeScript types in `src/lib/lexora-data.ts`.
//  All structs are Codable + Identifiable so they can be used in SwiftUI lists,
//  encoded to UserDefaults, and diffed with ForEach.
//

import Foundation

/// Part-of-speech enum — matches the web `PartOfSpeech` union exactly.
enum PartOfSpeech: String, Codable, CaseIterable {
    case noun, verb, adjective, adverb
    case pronoun, preposition, conjunction, interjection

    /// Lowercased display form (matches `word.partOfSpeech.toLowerCase()` in web).
    var displayValue: String { rawValue }
}

/// Master dictionary entry — 1:1 translation of `WordEntry`.
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
    /// Optional illustration resource name (PNG in Resources/).
    var illustration: String? = nil
}

/// Category tile — 1:1 translation of `Category`.
struct LexoraCategory: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let subtitle: String
    /// Resource name (PNG in Resources/).
    let illustration: String
}

/// Article preview tile — 1:1 translation of `Article`.
struct Article: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let excerpt: String
    let readMinutes: Int
    /// Resource name (PNG in Resources/).
    let thumbnail: String
    var bookmarked: Bool = false
}

/// Trending tag pill — links a label to a dictionary entry.
struct TrendingTag: Codable, Identifiable, Hashable {
    var id: String { wordId }   // one tag per word
    let label: String
    let wordId: String
}

/// Bottom-nav tab keys (matches `TabKey`).
enum LexoraTab: String, CaseIterable, Codable {
    case home, dictionary, search, collections, more
}
