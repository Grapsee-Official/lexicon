// LexoraEngine.iosMain — Swift C-interop bridge
package com.lexora.engine

import kotlinx.cinterop.*
import platform.Foundation.NSString
import platform.Foundation.stringWithCString
import platform.Foundation.stringUsingEncoding
import platform.darwin.NSObject

/**
 * iOS implementation of LexoraEngine via Swift C-interop.
 * 
 * The native library (LexoraEngine.framework) is built from the C++ sources
 * in /native/cpp/ via CMake. The C ABI header (c_api.h) is imported via
 * a bridging header in the Xcode project.
 */
actual class LexoraEngine actual constructor() : MemoryPressureListener {

    private var handle: COpaquePointer? = null
    private var loaded: Boolean = false

    init {
        handle = lexora_engine_create()
    }

    actual fun loadDatabase(dbDir: String): Boolean {
        dbDir.useContents { cstr ->
            loaded = lexora_engine_load(handle, cstr) == 0
            return loaded
        }
    }

    actual fun lookup(word: String): WordEntry? {
        if (!loaded) return null
        word.useContents { cstr ->
            val entryPtr = lexora_engine_lookup(handle, cstr)
            if (entryPtr == null) return null
            return convertEntry(entryPtr)
        }
    }

    actual fun fuzzySearch(query: String, maxDistance: Int, limit: Int): List<SearchResult> {
        if (!loaded) return emptyList()
        query.useContents { cstr ->
            val resultsPtr = lexora_engine_fuzzy_search(handle, cstr, maxDistance, limit)
            if (resultsPtr == null) return emptyList()

            return memScoped {
                val count = resultsPtr.pointed.count.toInt()
                (0 until count).map { i ->
                    val result = resultsPtr.pointed.results!![i]
                    val entry = convertEntry(result.entry)
                    SearchResult(entry, result.edit_distance)
                }
            }
        }
    }

    actual fun prefixSearch(prefix: String, limit: Int): List<String> {
        return fuzzySearch(prefix, 0, limit).map { it.entry.word }
    }

    actual fun getWordById(id: String): WordEntry? {
        return lookup(id)
    }

    actual fun enableDawg(enabled: Boolean) {
        lexora_engine_enable_dawg(handle, if (enabled) 1 else 0)
    }

    actual fun isDawgEnabled(): Boolean {
        return lexora_engine_is_dawg_enabled(handle) != 0
    }

    actual fun getMemoryUsage(): MemoryInfo {
        val info = lexora_engine_get_memory_usage(handle)
        return MemoryInfo(
            hashTableBytes = info.pointed.hash_table_bytes,
            dawgBytes = info.pointed.dawg_bytes,
            totalBytes = info.pointed.total_bytes
        )
    }

    actual fun close() {
        if (handle != null) {
            lexora_engine_close(handle)
            handle = null
        }
    }

    protected fun finalize() {
        close()
    }

    override fun onTrimMemory(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.LOW -> { }
            MemoryPressureLevel.MODERATE -> enableDawg(false)
            MemoryPressureLevel.CRITICAL -> enableDawg(false)
        }
    }

    private fun convertEntry(entryPtr: CPointer<LexoraWordEntry>): WordEntry {
        val entry = entryPtr.pointed
        return WordEntry(
            id = entry.id?.toKString() ?: "",
            word = entry.word?.toKString() ?: "",
            phonetic = entry.phonetic?.toKString() ?: "",
            partOfSpeech = entry.part_of_speech?.toKString() ?: "",
            definitions = stringArrayToList(entry.definitions, entry.definition_count),
            etymology = entry.etymology?.toKString() ?: "",
            synonyms = stringArrayToList(entry.synonyms, entry.synonym_count),
            antonyms = stringArrayToList(entry.antonyms, entry.antonym_count),
            examples = stringArrayToList(entry.examples, entry.example_count),
            language = entry.language?.toKString() ?: ""
        )
    }

    private fun stringArrayToList(arr: CPointer<CPointerVar<ByteVar>>?, count: UInt): List<String> {
        if (arr == null || count == 0u) return emptyList()
        return (0 until count.toInt()).map { i ->
            arr[i]?.toKString() ?: ""
        }
    }
}
