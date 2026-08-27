// LexiconEngine.androidMain — JNI bridge to C++ liblexicon-engine.so
package com.lexicon.engine

import android.content.Context
import android.content.ComponentCallbacks2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of LexiconEngine via JNI.
 * 
 * The native library (liblexicon-engine.so) is built from the C++ sources
 * in /native/cpp/ via CMake + Android NDK.
 */
actual class LexiconEngine actual constructor() : MemoryPressureListener {

    companion object {
        init {
            System.loadLibrary("lexicon-engine")
        }
    }

    private var handle: Long = 0L

    // JNI declarations
    private external fun nativeCreate(): Long
    private external fun nativeLoad(handle: Long, dbDir: String): Boolean
    private external fun nativeLookup(handle: Long, word: String): Long // pointer to LexiconWordEntry
    private external fun nativeFuzzySearch(handle: Long, query: String, maxDist: Int, limit: Int): Long
    private external fun nativeFreeEntry(entryPtr: Long)
    private external fun nativeFreeResults(resultsPtr: Long)
    private external fun nativeEnableDawg(handle: Long, enabled: Boolean)
    private external fun nativeIsDawgEnabled(handle: Long): Boolean
    private external fun nativeGetMemoryUsage(handle: Long): LongArray // [ht_bytes, dawg_bytes, total]
    private external fun nativeClose(handle: Long)

    // Entry field accessors (JNI reads from the C struct)
    private external fun nativeGetEntryFields(entryPtr: Long): Array<String> // [id, word, phonetic, pos, etymology, language]
    private external fun nativeGetEntryDefinitions(entryPtr: Long): Array<String>
    private external fun nativeGetEntrySynonyms(entryPtr: Long): Array<String>
    private external fun nativeGetEntryAntonyms(entryPtr: Long): Array<String>
    private external fun nativeGetEntryExamples(entryPtr: Long): Array<String>

    // Result array accessors
    private external fun nativeGetResultCount(resultsPtr: Long): Int
    private external fun nativeGetResultEntry(resultsPtr: Long, index: Int): Long
    private external fun nativeGetResultDistance(resultsPtr: Long, index: Int): Int

    init {
        handle = nativeCreate()
    }

    actual fun loadDatabase(dbDir: String): Boolean {
        return nativeLoad(handle, dbDir)
    }

    actual fun lookup(word: String): WordEntry? {
        val entryPtr = nativeLookup(handle, word)
        if (entryPtr == 0L) return null
        return try {
            convertEntry(entryPtr)
        } finally {
            nativeFreeEntry(entryPtr)
        }
    }

    actual fun fuzzySearch(query: String, maxDistance: Int, limit: Int): List<SearchResult> {
        val resultsPtr = nativeFuzzySearch(handle, query, maxDistance, limit)
        if (resultsPtr == 0L) return emptyList()
        return try {
            val count = nativeGetResultCount(resultsPtr)
            (0 until count).map { i ->
                val entryPtr = nativeGetResultEntry(resultsPtr, i)
                val distance = nativeGetResultDistance(resultsPtr, i)
                val entry = convertEntry(entryPtr)
                SearchResult(entry, distance)
            }
        } finally {
            nativeFreeResults(resultsPtr)
        }
    }

    actual fun prefixSearch(prefix: String, limit: Int): List<String> {
        // Not directly exposed via C ABI — use fuzzy search with distance 0
        return fuzzySearch(prefix, 0, limit).map { it.entry.word }
    }

    actual fun getWordById(id: String): WordEntry? {
        // Would query SQLite via JNI — for now, use lookup
        return lookup(id)
    }

    actual fun enableDawg(enabled: Boolean) {
        nativeEnableDawg(handle, enabled)
    }

    actual fun isDawgEnabled(): Boolean {
        return nativeIsDawgEnabled(handle)
    }

    actual fun getMemoryUsage(): MemoryInfo {
        val arr = nativeGetMemoryUsage(handle)
        return MemoryInfo(arr[0], arr[1], arr[2])
    }

    actual fun close() {
        if (handle != 0L) {
            nativeClose(handle)
            handle = 0L
        }
    }

    protected fun finalize() {
        close()
    }

    private fun convertEntry(entryPtr: Long): WordEntry {
        val fields = nativeGetEntryFields(entryPtr)
        return WordEntry(
            id = fields[0],
            word = fields[1],
            phonetic = fields[2],
            partOfSpeech = fields[3],
            definitions = nativeGetEntryDefinitions(entryPtr).toList(),
            etymology = fields[4],
            synonyms = nativeGetEntrySynonyms(entryPtr).toList(),
            antonyms = nativeGetEntryAntonyms(entryPtr).toList(),
            examples = nativeGetEntryExamples(entryPtr).toList(),
            language = fields[5]
        )
    }

    // MemoryPressureListener — called by ComponentCallbacks2
    override fun onTrimMemory(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.LOW -> { /* keep DAWG loaded */ }
            MemoryPressureLevel.MODERATE -> enableDawg(false)
            MemoryPressureLevel.CRITICAL -> {
                enableDawg(false)
                // Could also flush hash table caches here
            }
        }
    }

    /**
     * Register this engine with the Android Application context to receive
     * ComponentCallbacks2 memory pressure events.
     */
    fun registerMemoryPressure(context: Context) {
        context.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(config: android.content.res.Configuration) {}
            override fun onLowMemory() { onTrimMemory(MemoryPressureLevel.CRITICAL) }
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
