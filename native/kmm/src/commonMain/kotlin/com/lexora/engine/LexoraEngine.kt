// LexoraEngine.kt — KMM commonMain expect/actual declarations
// Shared interface for the C++ engine, implemented per-platform.
package com.lexora.engine

/**
 * LexoraEngine — cross-platform dictionary engine.
 * 
 * expect/actual pattern: the common interface lives here,
 * Android implements via JNI, iOS implements via Swift C-interop.
 */
expect class LexoraEngine {
    fun loadDatabase(dbDir: String): Boolean
    fun lookup(word: String): WordEntry?
    fun fuzzySearch(query: String, maxDistance: Int = 2, limit: Int = 20): List<SearchResult>
    fun prefixSearch(prefix: String, limit: Int = 50): List<String>
    fun getWordById(id: String): WordEntry?
    fun enableDawg(enabled: Boolean)
    fun isDawgEnabled(): Boolean
    fun getMemoryUsage(): MemoryInfo
    fun close()
}

data class WordEntry(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definitions: List<String>,
    val etymology: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val examples: List<String>,
    val language: String
)

data class SearchResult(
    val entry: WordEntry,
    val editDistance: Int
)

data class MemoryInfo(
    val hashTableBytes: Long,
    val dawgBytes: Long,
    val totalBytes: Long
)

/**
 * RAM pressure callback — called by the platform when memory is low.
 * The engine should unload the DAWG to free memory.
 */
interface MemoryPressureListener {
    fun onTrimMemory(level: MemoryPressureLevel)
}

enum class MemoryPressureLevel {
    LOW,      // Release non-critical resources
    MODERATE, // Unload DAWG
    CRITICAL  // Unload DAWG + flush caches
}
