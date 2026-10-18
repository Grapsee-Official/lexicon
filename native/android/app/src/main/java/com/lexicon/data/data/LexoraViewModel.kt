package com.lexicon.data

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * LexiconViewModel — direct translation of /home/z/my-project/src/lib/app-store.ts
 * (Zustand store) into an Android ViewModel + StateFlow.
 *
 * Persisted state (savedWordIds, recentSearches) is written to
 * SharedPreferences so it survives process death.
 *
 * State:
 *   activeTab        — currently-selected bottom-nav tab
 *   selectedWord     — word shown in the detail bottom-sheet (null = closed)
 *   searchOpen       — full-screen search modal visible
 *   menuOpen         — hamburger side menu visible
 *   savedWordIds     — bookmarked word IDs (persisted)
 *   recentSearches   — recent search queries (persisted, max 8)
 */

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

    /** Call once from MainActivity to attach persistence. */
    fun attachPrefs(context: Context) {
        prefs = LexiconPrefs(context.applicationContext)
        val (saved, recent, isDark) = prefs!!.load()
        _uiState.update { it.copy(
            savedWordIds = saved,
            recentSearches = recent,
            isDark = isDark,
        ) }
    }

    // ---- Tab navigation --------------------------------------------------
    fun setActiveTab(tab: TabKey) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ---- Word detail sheet ----------------------------------------------
    fun openWord(word: WordEntry) {
        _uiState.update { it.copy(selectedWord = word) }
    }
    fun closeWord() {
        _uiState.update { it.copy(selectedWord = null) }
    }

    // ---- Search modal ----------------------------------------------------
    fun openSearch() {
        _uiState.update { it.copy(searchOpen = true) }
    }
    fun closeSearch() {
        _uiState.update { it.copy(searchOpen = false) }
    }

    // ---- Side menu -------------------------------------------------------
    fun openMenu() {
        _uiState.update { it.copy(menuOpen = true) }
    }
    fun closeMenu() {
        _uiState.update { it.copy(menuOpen = false) }
    }

    // ---- Saved words -----------------------------------------------------
    fun toggleSave(id: String) {
        _uiState.update { s ->
            val next = if (s.savedWordIds.contains(id)) {
                s.savedWordIds - id
            } else {
                s.savedWordIds + id
            }
            prefs?.saveSavedIds(next)
            s.copy(savedWordIds = next)
        }
    }
    fun isSaved(id: String): Boolean = _uiState.value.savedWordIds.contains(id)

    // ---- Recent searches -------------------------------------------------
    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { s ->
            val next = listOf(trimmed) + s.recentSearches.filter {
                it.lowercase() != trimmed.lowercase()
            }
            val capped = next.take(8)
            prefs?.saveRecentSearches(capped)
            s.copy(recentSearches = capped)
        }
    }
    fun clearRecentSearches() {
        _uiState.update { s ->
            prefs?.saveRecentSearches(emptyList())
            s.copy(recentSearches = emptyList())
        }
    }

    // ---- Theme -----------------------------------------------------------
    fun toggleTheme() {
        _uiState.update { s ->
            val next = !s.isDark
            prefs?.saveIsDark(next)
            s.copy(isDark = next)
        }
    }
}
