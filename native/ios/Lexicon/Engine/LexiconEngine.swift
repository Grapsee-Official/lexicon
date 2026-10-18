//  LexiconEngine.swift — Swift wrapper for the C++ engine via C ABI
//  Per spec: "KMM wrapper. Hard gate: Kotlin/Swift can call C++ and receive strings."
//
//  ---------------------------------------------------------------------------
//  Type-conflict fix (P4-FIX-IOS-01)
//  ---------------------------------------------------------------------------
//  Previously this file declared its own `public struct WordEntry` (with
//  `partOfSpeech: String`) that collided with the app's `struct WordEntry`
//  in `LexiconModels.swift` (which uses `partOfSpeech: PartOfSpeech` — an enum
//  — and additionally carries an optional `illustration`). The duplicate
//  definition caused "ambiguous for type lookup" errors in every Swift file
//  that referenced `WordEntry`.
//
//  Resolution:
//    * The engine's struct is renamed `EngineWordEntry`. It mirrors the C ABI
//      `LexiconWordEntry` (c_api.h) field-for-field and is used ONLY inside
//      this class — it is never returned to callers and never appears in a
//      public method signature.
//    * A new private `toAppEntry(_:)` converts `EngineWordEntry` into the
//      app's `WordEntry` (from `LexiconModels.swift`). Every lookup funnels
//      through it before leaving this class, so all UI / ViewModel / screen
//      code continues to see the app's `WordEntry` model exclusively.
//    * The lookup methods (`lookup`, `getWordById`, `fuzzySearch`,
//      `prefixSearch`) and `SearchResult` are lowered from `public` to
//      `internal` (Swift default). Reason: the app's `WordEntry` is `internal`
//      (declared without an access modifier in `LexiconModels.swift`), and
//      Swift's access-control rule forbids a `public` function from returning
//      a less-visible type ("function cannot be declared public because its
//      result type is internal"). `LexiconModels.swift` is out of scope for
//      this fix, so the methods are made `internal` instead. All callers
//      (LexiconEditMenuInteraction, LexiconDebugMenu, ContentView, the
//      ViewModel) live in the same app module, so `internal` is fully
//      reachable. Methods that do not touch `WordEntry` (`loadDatabase`,
//      `enableDawg`, `isDawgEnabled`, `getMemoryUsage`, `close`) and
//      `MemoryInfo` remain `public`.
//  ---------------------------------------------------------------------------

import Foundation

/// LexiconEngine — Swift wrapper around the C++ LexiconEngine via the C ABI.
/// This is the iOS equivalent of the Android JNI bridge.
public class LexiconEngine {
    private var handle: OpaquePointer?

    public init() {
        handle = lexicon_engine_create()
    }

    deinit {
        close()
    }

    /// Load the dictionary database from a directory.
    public func loadDatabase(dbDir: String) -> Bool {
        guard let handle = handle else { return false }
        return lexicon_engine_load(handle, dbDir) == 0
    }

    // MARK: - Lookups
    //
    // These methods return the APP's `WordEntry` (from `LexiconModels.swift`),
    // not the engine's internal `EngineWordEntry`. The conversion from the C
    // ABI's raw strings happens in `toAppEntry(_:)` before any value leaves
    // this class. They are `internal` (not `public`) because the app's
    // `WordEntry` is itself `internal` and Swift forbids a `public` function
    // from returning a less-visible type — see the file header note above.

    /// Exact lookup (NFC-normalized internally).
    func lookup(word: String) -> WordEntry? {
        guard let handle = handle else { return nil }
        guard let entryPtr = lexicon_engine_lookup(handle, word) else { return nil }
        defer { lexicon_engine_free_entry(entryPtr) }
        let engineEntry = convertEntry(entryPtr)
        return toAppEntry(engineEntry)
    }

    /// Fuzzy search (Levenshtein ≤ maxDistance).
    func fuzzySearch(query: String, maxDistance: Int = 2, limit: Int = 20) -> [SearchResult] {
        guard let handle = handle else { return [] }
        guard let resultsPtr = lexicon_engine_fuzzy_search(handle, query, Int32(maxDistance), UInt32(limit)) else {
            return []
        }
        defer { lexicon_engine_free_results(resultsPtr) }

        let count = Int(resultsPtr.pointee.count)
        var results: [SearchResult] = []
        results.reserveCapacity(count)

        for i in 0..<count {
            let result = resultsPtr.pointee.results![i]
            let engineEntry = convertEntryEntry(result.entry)
            let appEntry = toAppEntry(engineEntry)
            results.append(SearchResult(entry: appEntry, editDistance: Int(result.edit_distance)))
        }

        return results
    }

    /// Prefix search via DAWG.
    func prefixSearch(prefix: String, limit: Int = 50) -> [String] {
        return fuzzySearch(query: prefix, maxDistance: 0, limit: limit).map { $0.entry.word }
    }

    /// Get word by ID.
    func getWordById(id: String) -> WordEntry? {
        return lookup(word: id)
    }

    // MARK: - Engine controls

    /// Enable/disable DAWG (for RAM pressure management).
    public func enableDawg(_ enabled: Bool) {
        guard let handle = handle else { return }
        lexicon_engine_enable_dawg(handle, enabled ? 1 : 0)
    }

    public func isDawgEnabled() -> Bool {
        guard let handle = handle else { return false }
        return lexicon_engine_is_dawg_enabled(handle) != 0
    }

    /// Get memory usage info.
    public func getMemoryUsage() -> MemoryInfo {
        guard let handle = handle else {
            return MemoryInfo(hashTableBytes: 0, dawgBytes: 0, totalBytes: 0)
        }
        let info = lexicon_engine_get_memory_usage(handle)
        return MemoryInfo(
            hashTableBytes: info.hash_table_bytes,
            dawgBytes: info.dawg_bytes,
            totalBytes: info.total_bytes
        )
    }

    /// Close and release all resources.
    public func close() {
        if let handle = handle {
            lexicon_engine_close(handle)
            self.handle = nil
        }
    }

    // MARK: - Private helpers

    /// Convert the C ABI `LexiconWordEntry` (see `c_api.h`) into the engine's
    /// intermediate `EngineWordEntry`. This is a straight 1:1 lift of the C
    /// struct fields into Swift `String` / `[String]` — no semantic mapping
    /// happens here; that lives in `toAppEntry(_:)`.
    private func convertEntryEntry(_ entry: LexiconWordEntry) -> EngineWordEntry {
        return EngineWordEntry(
            id: stringFromC(entry.id),
            word: stringFromC(entry.word),
            phonetic: stringFromC(entry.phonetic),
            partOfSpeech: stringFromC(entry.part_of_speech),
            definitions: stringArray(from: entry.definitions, count: Int(entry.definition_count)),
            etymology: stringFromC(entry.etymology),
            synonyms: stringArray(from: entry.synonyms, count: Int(entry.synonym_count)),
            antonyms: stringArray(from: entry.antonyms, count: Int(entry.antonym_count)),
            examples: stringArray(from: entry.examples, count: Int(entry.example_count)),
            language: stringFromC(entry.language)
        )
    }

    private func convertEntry(_ entryPtr: UnsafePointer<LexiconWordEntry>) -> EngineWordEntry {
        let entry = entryPtr.pointee

        let definitions = stringArray(from: entry.definitions, count: Int(entry.definition_count))
        let synonyms = stringArray(from: entry.synonyms, count: Int(entry.synonym_count))
        let antonyms = stringArray(from: entry.antonyms, count: Int(entry.antonym_count))
        let examples = stringArray(from: entry.examples, count: Int(entry.example_count))

        return EngineWordEntry(
            id: stringFromC(entry.id),
            word: stringFromC(entry.word),
            phonetic: stringFromC(entry.phonetic),
            partOfSpeech: stringFromC(entry.part_of_speech),
            definitions: definitions,
            etymology: stringFromC(entry.etymology),
            synonyms: synonyms,
            antonyms: antonyms,
            examples: examples,
            language: stringFromC(entry.language)
        )
    }

    /// Convert the engine's intermediate `EngineWordEntry` (raw strings from
    /// the C ABI) into the app's `WordEntry` model (declared in
    /// `LexiconModels.swift`) that is consumed by the ViewModel and SwiftUI
    /// screens. This is the single boundary between the C++ layer and the app
    /// layer — every lookup result funnels through here.
    private func toAppEntry(_ engineEntry: EngineWordEntry) -> WordEntry {
        // Map the raw part-of-speech string to the app's `PartOfSpeech` enum.
        // The enum's raw values are lowercase ("noun", "verb", "adjective",
        // "adverb", "pronoun", "preposition", "conjunction", "interjection"),
        // matching the strings the C++ engine emits. Lowercase defensively in
        // case the ABI ever yields mixed case, and fall back to `.noun` so we
        // always produce a valid app model entry — the app's `WordEntry`
        // requires a non-optional `PartOfSpeech`.
        let partOfSpeech = PartOfSpeech(rawValue: engineEntry.partOfSpeech.lowercased()) ?? .noun

        return WordEntry(
            id: engineEntry.id,
            word: engineEntry.word,
            phonetic: engineEntry.phonetic,
            partOfSpeech: partOfSpeech,
            definitions: engineEntry.definitions,
            etymology: engineEntry.etymology,
            synonyms: engineEntry.synonyms,
            antonyms: engineEntry.antonyms,
            examples: engineEntry.examples,
            language: engineEntry.language
            // `illustration` is not surfaced by the C ABI; the app's
            // `WordEntry` defaults it to `nil` via its memberwise initializer.
        )
    }

    private func stringFromC(_ cString: UnsafePointer<CChar>?) -> String {
        guard let cString = cString else { return "" }
        return String(cString: cString)
    }

    private func stringArray(from ptr: UnsafePointer<UnsafePointer<CChar>?>?, count: Int) -> [String] {
        guard let ptr = ptr, count > 0 else { return [] }
        return (0..<count).compactMap { i in
            ptr[i].map { String(cString: $0) }
        }
    }
}

// MARK: - Engine data models
//
// `EngineWordEntry` is the intermediate representation produced directly from
// the C ABI `LexiconWordEntry` struct (c_api.h). It mirrors that C struct
// field-for-field with Swift types and is used ONLY inside `LexiconEngine` —
// by `convertEntry(_:)` (C ABI → EngineWordEntry) and `toAppEntry(_:)`
// (EngineWordEntry → app WordEntry). It is never handed back to callers and
// never appears in any method signature visible outside this file, hence the
// `internal` (default) access level.
//
// (Renamed from `WordEntry` to resolve the "ambiguous for type lookup"
// collision with the app's `WordEntry` in `LexiconModels.swift`.)
struct EngineWordEntry {
    let id: String
    let word: String
    let phonetic: String
    let partOfSpeech: String
    let definitions: [String]
    let etymology: String
    let synonyms: [String]
    let antonyms: [String]
    let examples: [String]
    let language: String
}

// `SearchResult` wraps an app `WordEntry` (already converted from the engine's
// `EngineWordEntry` via `toAppEntry(_:)`) together with its Levenshtein edit
// distance. `entry` is the app model so SwiftUI / ViewModel code can consume
// it directly without a second conversion. `internal` (default) because it
// carries the `internal` `WordEntry` — see the file header note.
struct SearchResult {
    let entry: WordEntry
    let editDistance: Int
}

// `MemoryInfo` is returned by the public `getMemoryUsage()` API and contains
// only primitive `UInt64` fields, so it can safely stay `public` with no
// access-control entanglement with the (internal) `WordEntry` model.
public struct MemoryInfo {
    public let hashTableBytes: UInt64
    public let dawgBytes: UInt64
    public let totalBytes: UInt64
}
