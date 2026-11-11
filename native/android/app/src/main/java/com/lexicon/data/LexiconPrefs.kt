package com.lexicon.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Tiny SharedPreferences wrapper used by [LexiconViewModel] to persist
 * saved word IDs, recent searches, and the dark-mode toggle.
 *
 * Stores comma-separated strings to keep the value types simple and
 * match the simplicity of the web's `localStorage` JSON blob.
 */
class LexiconPrefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns (savedWordIds, recentSearches, isDark). */
    fun load(): Triple<List<String>, List<String>, Boolean> {
        val saved = sp.getString(KEY_SAVED, "")
            ?.split(',')
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val recent = sp.getString(KEY_RECENT, "")
            ?.split('\n')
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val isDark = sp.getBoolean(KEY_DARK, false)
        return Triple(saved, recent, isDark)
    }

    fun saveSavedIds(ids: List<String>) {
        sp.edit().putString(KEY_SAVED, ids.joinToString(",")).apply()
    }

    fun saveRecentSearches(list: List<String>) {
        sp.edit().putString(KEY_RECENT, list.joinToString("\n")).apply()
    }

    fun saveIsDark(isDark: Boolean) {
        sp.edit().putBoolean(KEY_DARK, isDark).apply()
    }

    companion object {
        private const val PREFS_NAME = "lexicon_prefs"
        private const val KEY_SAVED  = "saved_word_ids"
        private const val KEY_RECENT = "recent_searches"
        private const val KEY_DARK   = "is_dark"
    }
}
