package com.lexicon.engine

import android.content.ComponentCallbacks2
import android.content.Context

/**
 * Android JNI bridge to C++ liblexicon-engine.so.
 * Mirrors native/kmm/src/androidMain/kotlin/com/lexicon/engine/LexiconEngine.android.kt
 * so :app compiles standalone and links directly against liblexicon-engine.so.
 */
class LexiconEngine : MemoryPressureListener {

    companion object {
        private var nativeLoaded = false

        init {
            try {
                System.loadLibrary("lexicon-engine")
                nativeLoaded = true
            } catch (_: UnsatisfiedLinkError) {
                nativeLoaded = false
            }
        }
    }

    private var handle: Long = 0L

    private external fun nativeCreate(): Long
    private external fun nativeLoad(handle: Long, dbDir: String): Boolean
    private external fun nativeLookup(handle: Long, word: String): Long
    private external fun nativeFuzzySearch(handle: Long, query: String, maxDist: Int, limit: Int): Long
    private external fun nativeFreeEntry(entryPtr: Long)
    private external fun nativeFreeResults(resultsPtr: Long)
    private external fun nativeEnableDawg(handle: Long, enabled: Boolean)
    private external fun nativeIsDawgEnabled(handle: Long): Boolean
    private external fun nativeGetMemoryUsage(handle: Long): LongArray
    private external fun nativeClose(handle: Long)

    private external fun nativeGetEntryFields(entryPtr: Long): Array<String>
    private external fun nativeGetEntryDefinitions(entryPtr: Long): Array<String>
    private external fun nativeGetEntrySynonyms(entryPtr: Long): Array<String>
    private external fun nativeGetEntryAntonyms(entryPtr: Long): Array<String>
    private external fun nativeGetEntryExamples(entryPtr: Long): Array<String>

    private external fun nativeGetResultCount(resultsPtr: Long): Int
    private external fun nativeGetResultEntry(resultsPtr: Long, index: Int): Long
    private external fun nativeGetResultDistance(resultsPtr: Long, index: Int): Int

    init {
        if (nativeLoaded) {
            handle = runCatching { nativeCreate() }.getOrDefault(0L)
        }
    }

    fun loadDatabase(dbDir: String): Boolean {
        if (handle == 0L) return false
        return nativeLoad(handle, dbDir)
    }

    fun lookup(word: String): EngineWordEntry? {
        if (handle == 0L) return null
        val entryPtr = nativeLookup(handle, word)
        if (entryPtr == 0L) return null
        return try {
            convertEntry(entryPtr)
        } finally {
            nativeFreeEntry(entryPtr)
        }
    }

    fun fuzzySearch(query: String, maxDistance: Int = 2, limit: Int = 20): List<SearchResult> {
        if (handle == 0L) return emptyList()
        val resultsPtr = nativeFuzzySearch(handle, query, maxDistance, limit)
        if (resultsPtr == 0L) return emptyList()
        return try {
            val count = nativeGetResultCount(resultsPtr)
            (0 until count).map { i ->
                val entryPtr = nativeGetResultEntry(resultsPtr, i)
                val distance = nativeGetResultDistance(resultsPtr, i)
                SearchResult(convertEntry(entryPtr), distance)
            }
        } finally {
            nativeFreeResults(resultsPtr)
        }
    }

    fun prefixSearch(prefix: String, limit: Int = 50): List<String> =
        fuzzySearch(prefix, 0, limit).map { it.entry.word }

    fun getWordById(id: String): EngineWordEntry? = lookup(id)

    fun enableDawg(enabled: Boolean) {
        if (handle != 0L) nativeEnableDawg(handle, enabled)
    }

    fun isDawgEnabled(): Boolean =
        if (handle != 0L) nativeIsDawgEnabled(handle) else false

    fun getMemoryUsage(): MemoryInfo {
        if (handle == 0L) return MemoryInfo(0L, 0L, 0L)
        val arr = nativeGetMemoryUsage(handle)
        return MemoryInfo(
            hashTableBytes = arr.getOrElse(0) { 0L },
            dawgBytes = arr.getOrElse(1) { 0L },
            totalBytes = arr.getOrElse(2) { 0L },
        )
    }

    fun close() {
        if (handle != 0L) {
            nativeClose(handle)
            handle = 0L
        }
    }

    private fun convertEntry(entryPtr: Long): EngineWordEntry {
        val fields = nativeGetEntryFields(entryPtr)
        return EngineWordEntry(
            id = fields.getOrElse(0) { "" },
            word = fields.getOrElse(1) { "" },
            phonetic = fields.getOrElse(2) { "" },
            partOfSpeech = fields.getOrElse(3) { "" },
            definitions = nativeGetEntryDefinitions(entryPtr).toList(),
            etymology = fields.getOrElse(4) { "" },
            synonyms = nativeGetEntrySynonyms(entryPtr).toList(),
            antonyms = nativeGetEntryAntonyms(entryPtr).toList(),
            examples = nativeGetEntryExamples(entryPtr).toList(),
            language = fields.getOrElse(5) { "" },
        )
    }

    override fun onTrimMemory(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.LOW -> Unit
            MemoryPressureLevel.MODERATE,
            MemoryPressureLevel.CRITICAL -> enableDawg(false)
        }
    }

    fun registerMemoryPressure(context: Context) {
        context.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(config: android.content.res.Configuration) {}
            override fun onLowMemory() = onTrimMemory(MemoryPressureLevel.CRITICAL)
            override fun onTrimMemory(level: Int) {
                val mpLevel = when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> MemoryPressureLevel.CRITICAL
                    level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE -> MemoryPressureLevel.MODERATE
                    else -> MemoryPressureLevel.LOW
                }
                onTrimMemory(mpLevel)
            }
        })
    }
}

data class EngineWordEntry(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definitions: List<String>,
    val etymology: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val examples: List<String>,
    val language: String,
)

data class SearchResult(
    val entry: EngineWordEntry,
    val editDistance: Int,
)

data class MemoryInfo(
    val hashTableBytes: Long,
    val dawgBytes: Long,
    val totalBytes: Long,
)

interface MemoryPressureListener {
    fun onTrimMemory(level: MemoryPressureLevel)
}

enum class MemoryPressureLevel {
    LOW,
    MODERATE,
    CRITICAL,
}
