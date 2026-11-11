package com.lexicon.data

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lexicon.engine.EngineWordEntry
import com.lexicon.engine.LexiconEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

enum class TabKey { HOME, DICTIONARY, SEARCH, COLLECTIONS, MORE }

data class LexiconUiState(
    val activeTab: TabKey = TabKey.HOME,
    val selectedWord: WordEntry? = null,
    val searchOpen: Boolean = false,
    val menuOpen: Boolean = false,
    val savedWordIds: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isDark: Boolean = false,
)

class LexiconViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LexiconUiState())
    val uiState: StateFlow<LexiconUiState> = _uiState.asStateFlow()

    private var prefs: LexiconPrefs? = null
    val engine: LexiconEngine = LexiconEngine()

    init {
        runCatching { engine.loadDatabase(":embedded:") }
    }

    fun attachPrefs(context: Context) {
        if (prefs == null) {
            prefs = LexiconPrefs(context.applicationContext)
            val (saved, recent, isDark) = prefs!!.load()
            _uiState.update {
                it.copy(
                    savedWordIds = saved,
                    recentSearches = recent,
                    isDark = isDark,
                )
            }
            val externalDb = File(context.filesDir, "lexicon")
            if (File(externalDb, "lexicon.ht").exists()) {
                runCatching { engine.loadDatabase(externalDb.absolutePath) }
            }
        }
    }

    fun setDarkMode(dark: Boolean) {
        _uiState.update { s ->
            prefs?.saveIsDark(dark)
            s.copy(isDark = dark)
        }
    }

    fun setActiveTab(tab: TabKey) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun openWord(word: WordEntry) {
        _uiState.update { it.copy(selectedWord = word) }
    }

    fun closeWord() {
        _uiState.update { it.copy(selectedWord = null) }
    }

    fun openSearch() {
        _uiState.update { it.copy(searchOpen = true) }
    }

    fun closeSearch() {
        _uiState.update { it.copy(searchOpen = false) }
    }

    fun openMenu() {
        _uiState.update { it.copy(menuOpen = true) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(menuOpen = false) }
    }

    fun toggleSave(id: String) {
        _uiState.update { s ->
            val next = if (s.savedWordIds.contains(id)) s.savedWordIds - id else s.savedWordIds + id
            prefs?.saveSavedIds(next)
            s.copy(savedWordIds = next)
        }
    }

    fun isSaved(id: String): Boolean = _uiState.value.savedWordIds.contains(id)

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { s ->
            val next = (listOf(trimmed) + s.recentSearches.filter {
                !it.equals(trimmed, ignoreCase = true)
            }).take(8)
            prefs?.saveRecentSearches(next)
            s.copy(recentSearches = next)
        }
    }

    fun clearRecentSearches() {
        _uiState.update { s ->
            prefs?.saveRecentSearches(emptyList())
            s.copy(recentSearches = emptyList())
        }
    }

    fun toggleTheme() {
        _uiState.update { s ->
            val next = !s.isDark
            prefs?.saveIsDark(next)
            s.copy(isDark = next)
        }
    }

    /**
     * Live search routed through the C++17 engine (exact + DAWG prefix + Levenshtein ≤ 2)
     * merged with full LexiconData entries so typo queries like "wandrlust" resolve!
     */
    fun searchWords(query: String, limit: Int = 8): List<WordEntry> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val out = LinkedHashMap<String, WordEntry>()

        runCatching {
            engine.lookup(q)?.let { e ->
                val mapped = mapEngineEntry(e)
                out[mapped.id] = mapped
            }
            engine.fuzzySearch(q, maxDistance = 2, limit = limit).forEach { sr ->
                val mapped = mapEngineEntry(sr.entry)
                out.putIfAbsent(mapped.id, mapped)
            }
        }

        LexiconData.searchWords(q, limit = limit).forEach { w ->
            out.putIfAbsent(w.id, w)
        }

        return out.values.take(limit)
    }

    private fun mapEngineEntry(e: EngineWordEntry): WordEntry {
        LexiconData.getWordById(e.id)?.let { return it }
        val pos = PartOfSpeech.entries.firstOrNull {
            it.label().equals(e.partOfSpeech, ignoreCase = true)
        } ?: PartOfSpeech.NOUN
        return WordEntry(
            id = e.id,
            word = e.word,
            phonetic = e.phonetic,
            partOfSpeech = pos,
            definitions = e.definitions,
            etymology = e.etymology,
            synonyms = e.synonyms,
            antonyms = e.antonyms,
            examples = e.examples,
            language = e.language,
        )
    }

    override fun onCleared() {
        runCatching { engine.close() }
        super.onCleared()
    }
}
