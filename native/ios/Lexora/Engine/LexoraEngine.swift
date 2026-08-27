//  LexiconEngine.swift — Swift wrapper for the C++ engine via C ABI
//  Per spec: "KMM wrapper. Hard gate: Kotlin/Swift can call C++ and receive strings."

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

    /// Exact lookup (NFC-normalized internally).
    public func lookup(word: String) -> WordEntry? {
        guard let handle = handle else { return nil }
        guard let entryPtr = lexicon_engine_lookup(handle, word) else { return nil }
        defer { lexicon_engine_free_entry(entryPtr) }
        return convertEntry(entryPtr)
    }

    /// Fuzzy search (Levenshtein ≤ maxDistance).
    public func fuzzySearch(query: String, maxDistance: Int = 2, limit: Int = 20) -> [SearchResult] {
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
            let entry = convertEntry(result.entry)
            results.append(SearchResult(entry: entry, editDistance: Int(result.edit_distance)))
        }

        return results
    }

    /// Prefix search via DAWG.
    public func prefixSearch(prefix: String, limit: Int = 50) -> [String] {
        return fuzzySearch(query: prefix, maxDistance: 0, limit: limit).map { $0.entry.word }
    }

    /// Get word by ID.
    public func getWordById(id: String) -> WordEntry? {
        return lookup(word: id)
    }

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
            hashTableBytes: info.pointee.hash_table_bytes,
            dawgBytes: info.pointee.dawg_bytes,
            totalBytes: info.pointee.total_bytes
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

    private func convertEntry(_ entryPtr: UnsafePointer<LexiconWordEntry>) -> WordEntry {
        let entry = entryPtr.pointee

        let definitions = stringArray(from: entry.definitions, count: Int(entry.definition_count))
        let synonyms = stringArray(from: entry.synonyms, count: Int(entry.synonym_count))
        let antonyms = stringArray(from: entry.antonyms, count: Int(entry.antonym_count))
        let examples = stringArray(from: entry.examples, count: Int(entry.example_count))

        return WordEntry(
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

// MARK: - Data Models (Swift)

public struct WordEntry {
    public let id: String
    public let word: String
    public let phonetic: String
    public let partOfSpeech: String
    public let definitions: [String]
    public let etymology: String
    public let synonyms: [String]
    public let antonyms: [String]
    public let examples: [String]
    public let language: String
}

public struct SearchResult {
    public let entry: WordEntry
    public let editDistance: Int
}

public struct MemoryInfo {
    public let hashTableBytes: UInt64
    public let dawgBytes: UInt64
    public let totalBytes: UInt64
}
