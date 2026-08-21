// LexoraEngine.ios.kt — Kotlin/Native C-interop bridge to liblexora-engine
package com.lexora.engine

import kotlinx.cinterop.*
import lexora.LexoraWordEntry
import lexora.LexoraSearchResults
import lexora.LexoraMemoryInfo

/**
 * iOS implementation of LexoraEngine via Kotlin/Native C-interop.
 *
 * The native library (liblexora-engine.a / .framework) is built from the C++
 * sources in /native/cpp/ via CMake. The C ABI header (c_api.h) is imported
 * via the `lexora` cinterop definition (see
 * src/nativeInterop/cinterop/lexora.def), which generates Kotlin struct
 * types (LexoraWordEntry, LexoraSearchResults, LexoraMemoryInfo).
 *
 * The C functions are bound via `@SymbolName`-annotated `external fun`
 * declarations with explicit Kotlin/Native types (COpaquePointer,
 * CPointer<ByteVar>, CValue<...>). This gives us full control over
 * nullability and pointer types instead of relying on cinterop-generated
 * top-level function stubs.
 *
 * Memory ownership:
 *  - The opaque handle is owned by this class and freed in close()/finalize().
 *  - lookup() and fuzzySearch() return C-allocated structs that must be freed
 *    via lexora_engine_free_entry / lexora_engine_free_results; we free them
 *    in a `finally` block after copying all data into Kotlin objects.
 *  - C strings are immediately copied into Kotlin Strings via toKString(),
 *    so no C memory is retained after the call returns.
 *  - For fuzzy search, lexora_engine_free_results transitively frees each
 *    LexoraWordEntry inside, so we must NOT call lexora_engine_free_entry on
 *    result entries (that would double-free).
 */
actual class LexoraEngine actual constructor() : MemoryPressureListener {

    private var handle: COpaquePointer? = null
    private var loaded: Boolean = false

    // ---- External C function declarations matching c_api.h ----
    // Bound to the native symbols via @SymbolName. We use COpaquePointer for
    // the opaque LexoraEngineHandle* and CPointer<ByteVar> for const char*,
    // which are the idiomatic Kotlin/Native types for these C signatures.

    @SymbolName("lexora_engine_create")
    private external fun cCreate(): COpaquePointer?

    @SymbolName("lexora_engine_load")
    private external fun cLoad(handle: COpaquePointer?, dbDir: CPointer<ByteVar>?): Int

    @SymbolName("lexora_engine_close")
    private external fun cClose(handle: COpaquePointer?)

    @SymbolName("lexora_engine_lookup")
    private external fun cLookup(
        handle: COpaquePointer?,
        word: CPointer<ByteVar>?
    ): CPointer<LexoraWordEntry>?

    @SymbolName("lexora_engine_fuzzy_search")
    private external fun cFuzzySearch(
        handle: COpaquePointer?,
        query: CPointer<ByteVar>?,
        maxDistance: Int,
        limit: UInt
    ): CPointer<LexoraSearchResults>?

    @SymbolName("lexora_engine_free_entry")
    private external fun cFreeEntry(entry: CPointer<LexoraWordEntry>?)

    @SymbolName("lexora_engine_free_results")
    private external fun cFreeResults(results: CPointer<LexoraSearchResults>?)

    @SymbolName("lexora_engine_enable_dawg")
    private external fun cEnableDawg(handle: COpaquePointer?, enabled: Int)

    @SymbolName("lexora_engine_is_dawg_enabled")
    private external fun cIsDawgEnabled(handle: COpaquePointer?): Int

    @SymbolName("lexora_engine_get_memory_usage")
    private external fun cGetMemoryUsage(handle: COpaquePointer?): CValue<LexoraMemoryInfo>

    init {
        handle = cCreate()
    }

    actual fun loadDatabase(dbDir: String): Boolean {
        val h = handle ?: return false
        // Allocate a C string inside a memScoped block so it stays alive for
        // the duration of the C call. The pointer is invalid after the block
        // exits, but cLoad completes synchronously inside it.
        return memScoped {
            val cDbDir = dbDir.cstr.getPointer(this)
            loaded = cLoad(h, cDbDir) == 0
            loaded
        }
    }

    actual fun lookup(word: String): WordEntry? {
        if (!loaded) return null
        val h = handle ?: return null
        return memScoped {
            val cWord = word.cstr.getPointer(this)
            val entryPtr = cLookup(h, cWord) ?: return@memScoped null
            // Free the C-allocated entry after copying all data into Kotlin
            // objects. toKString() copies the char* contents into a Kotlin
            // String, so the WordEntry is fully independent of C memory.
            try {
                convertEntry(entryPtr.pointed)
            } finally {
                cFreeEntry(entryPtr)
            }
        }
    }

    actual fun fuzzySearch(query: String, maxDistance: Int, limit: Int): List<SearchResult> {
        if (!loaded) return emptyList()
        val h = handle ?: return emptyList()
        return memScoped {
            val cQuery = query.cstr.getPointer(this)
            val resultsPtr = cFuzzySearch(h, cQuery, maxDistance, limit.toUInt())
                ?: return@memScoped emptyList<SearchResult>()
            try {
                val pointed = resultsPtr.pointed
                val count = pointed.count.toInt()
                val resultsArray = pointed.results
                if (resultsArray == null || count == 0) {
                    emptyList()
                } else {
                    // Index into the C array via pointer arithmetic. Each
                    // element is a LexoraSearchResult containing a by-value
                    // LexoraWordEntry. We pass the entry view to convertEntry,
                    // which copies all strings into Kotlin Strings before
                    // returning.
                    (0 until count).map { i ->
                        val result = (resultsArray + i).pointed
                        val entry = convertEntry(result.entry)
                        SearchResult(entry, result.edit_distance)
                    }
                }
            } finally {
                // cFreeResults transitively frees each entry inside, so we
                // must NOT call cFreeEntry on individual result entries.
                cFreeResults(resultsPtr)
            }
        }
    }

    actual fun prefixSearch(prefix: String, limit: Int): List<String> {
        // Not directly exposed via C ABI — use fuzzy search with distance 0.
        return fuzzySearch(prefix, 0, limit).map { it.entry.word }
    }

    actual fun getWordById(id: String): WordEntry? {
        // C ABI doesn't expose ID-based lookup; reuse lookup().
        return lookup(id)
    }

    actual fun enableDawg(enabled: Boolean) {
        val h = handle ?: return
        cEnableDawg(h, if (enabled) 1 else 0)
    }

    actual fun isDawgEnabled(): Boolean {
        val h = handle ?: return false
        return cIsDawgEnabled(h) != 0
    }

    actual fun getMemoryUsage(): MemoryInfo {
        val h = handle ?: return MemoryInfo(0L, 0L, 0L)
        // cGetMemoryUsage returns LexoraMemoryInfo by value (not a pointer),
        // so we use CValue<LexoraMemoryInfo> and access fields via useContents.
        return cGetMemoryUsage(h).useContents {
            MemoryInfo(
                hashTableBytes = hash_table_bytes.toLong(),
                dawgBytes = dawg_bytes.toLong(),
                totalBytes = total_bytes.toLong()
            )
        }
    }

    actual fun close() {
        val h = handle
        if (h != null) {
            cClose(h)
            handle = null
            loaded = false
        }
    }

    @Suppress("DEPRECATION")
    protected fun finalize() {
        close()
    }

    override fun onTrimMemory(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.LOW -> { /* keep DAWG loaded */ }
            MemoryPressureLevel.MODERATE -> enableDawg(false)
            MemoryPressureLevel.CRITICAL -> enableDawg(false)
        }
    }

    /**
     * Reads the C `LexoraWordEntry` struct [entry] (a cinterop-generated view
     * into C memory) and copies all strings into Kotlin Strings. Safe to call
     * after the returned [WordEntry] is created — no references to C memory
     * are retained.
     */
    private fun convertEntry(entry: LexoraWordEntry): WordEntry {
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

    /**
     * Copies a `const char**` array of length [count] into a Kotlin
     * `List<String>`. Handles null pointer and zero count gracefully.
     * `arr[i]` returns `CPointer<ByteVar>?` (i.e. `const char*`), which may
     * itself be null if the C code stored a null slot.
     */
    private fun stringArrayToList(
        arr: CPointer<CPointerVar<ByteVar>>?,
        count: UInt
    ): List<String> {
        if (arr == null || count == 0u) return emptyList()
        return (0 until count.toInt()).map { i ->
            arr[i]?.toKString() ?: ""
        }
    }
}
