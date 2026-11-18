package com.lexicon.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Distinguish an intentionally empty collection from a first launch.
    val hasSavedIds: Boolean get() = sp.contains(KEY_SAVED)
    val hasRecentSearches: Boolean get() = sp.contains(KEY_RECENT)

    fun loadFavoriteIds(): List<String>? = if (!sp.contains(KEY_FAVORITES)) null else
        sp.getString(KEY_FAVORITES, "").orEmpty().split('\n').filter { it.isNotBlank() }

    fun saveFavoriteIds(ids: List<String>) {
        sp.edit().putString(KEY_FAVORITES, ids.joinToString("\n")).apply()
    }

    fun loadViewedIds(): List<String>? = if (!sp.contains(KEY_VIEWED)) null else
        sp.getString(KEY_VIEWED, "").orEmpty().split("\n").filter { it.isNotBlank() }

    fun saveViewedIds(ids: List<String>) {
        sp.edit().putString(KEY_VIEWED, ids.joinToString("\n")).apply()
    }

    fun loadNotes(): List<NoteItem>? {
        if (!sp.contains(KEY_NOTES)) return null
        return runCatching {
            val array = JSONArray(sp.getString(KEY_NOTES, "[]") ?: "[]")
            (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)
                NoteItem(
                    id = obj.getString("id"),
                    category = runCatching { NoteCategory.valueOf(obj.getString("category")) }
                        .getOrDefault(NoteCategory.PERSONAL),
                    title = obj.getString("title"),
                    body = obj.getString("body"),
                    dateLabel = obj.getString("dateLabel"),
                    bookmarked = obj.optBoolean("bookmarked", false),
                    illustration = if (obj.isNull("illustration")) null else obj.optString("illustration"),
                )
            }
        }.getOrNull()
    }

    fun saveNotes(notes: List<NoteItem>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id", note.id)
                put("category", note.category.name)
                put("title", note.title)
                put("body", note.body)
                put("dateLabel", note.dateLabel)
                put("bookmarked", note.bookmarked)
                put("illustration", note.illustration ?: JSONObject.NULL)
            })
        }
        sp.edit().putString(KEY_NOTES, array.toString()).apply()
    }

    fun loadStudyProgress(): Map<String, Int>? {
        if (!sp.contains(KEY_STUDY)) return null
        return runCatching {
            val obj = JSONObject(sp.getString(KEY_STUDY, "{}") ?: "{}")
            obj.keys().asSequence().associateWith { key -> obj.optInt(key, 0) }
        }.getOrNull()
    }

    fun saveStudyProgress(lists: List<StudyListItem>) {
        val obj = JSONObject()
        lists.forEach { obj.put(it.id, it.completedWords) }
        sp.edit().putString(KEY_STUDY, obj.toString()).apply()
    }

    fun loadTemplates(): List<VocabTemplate>? {
        if (!sp.contains(KEY_TEMPLATES)) return null
        return runCatching {
            val array = JSONArray(sp.getString(KEY_TEMPLATES, "[]") ?: "[]")
            (0 until array.length()).map { index ->
                val obj = array.getJSONObject(index)
                val words = obj.optJSONArray("sampleWords") ?: JSONArray()
                VocabTemplate(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    subtitle = obj.getString("subtitle"),
                    category = runCatching { TemplateCategory.valueOf(obj.getString("category")) }
                        .getOrDefault(TemplateCategory.LEARNING),
                    illustration = obj.getString("illustration"),
                    starred = obj.optBoolean("starred", true),
                    sampleWords = (0 until words.length()).map { words.getString(it) },
                )
            }
        }.getOrNull()
    }

    fun saveTemplates(templates: List<VocabTemplate>) {
        val array = JSONArray()
        templates.forEach { tpl ->
            array.put(JSONObject().apply {
                put("id", tpl.id)
                put("title", tpl.title)
                put("subtitle", tpl.subtitle)
                put("category", tpl.category.name)
                put("illustration", tpl.illustration)
                put("starred", tpl.starred)
                put("sampleWords", JSONArray(tpl.sampleWords))
            })
        }
        sp.edit().putString(KEY_TEMPLATES, array.toString()).apply()
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun yesterday(): String = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time) }

    fun loadDayStreak(): Int {
        val last = sp.getString(KEY_LAST_STUDY_DAY, null)
        return if (last == today() || last == yesterday()) sp.getInt(KEY_DAY_STREAK, 0) else 0
    }

    fun recordStudyAndGetStreak(): Int {
        val previous = sp.getString(KEY_LAST_STUDY_DAY, null)
        val current = today()
        val streak = when (previous) {
            current -> sp.getInt(KEY_DAY_STREAK, 1).coerceAtLeast(1)
            yesterday() -> sp.getInt(KEY_DAY_STREAK, 0) + 1
            else -> 1
        }
        sp.edit()
            .putString(KEY_LAST_STUDY_DAY, current)
            .putInt(KEY_DAY_STREAK, streak)
            .apply()
        return streak
    }

    fun loadTodayStudied(): Int = if (sp.getString(KEY_DAY, null) == today())
        sp.getInt(KEY_TODAY_STUDIED, 0) else 0

    fun saveTodayStudied(words: Int) {
        sp.edit().putString(KEY_DAY, today()).putInt(KEY_TODAY_STUDIED, words).apply()
    }

    companion object {
        private const val PREFS_NAME = "lexicon_prefs"
        private const val KEY_SAVED  = "saved_word_ids"
        private const val KEY_RECENT = "recent_searches"
        private const val KEY_DARK   = "is_dark"
        private const val KEY_FAVORITES = "favorite_word_ids"
        private const val KEY_VIEWED = "recently_viewed_word_ids"
        private const val KEY_NOTES = "notes_json"
        private const val KEY_STUDY = "study_progress_json"
        private const val KEY_TEMPLATES = "templates_json"
        private const val KEY_DAY = "study_day"
        private const val KEY_TODAY_STUDIED = "today_studied"
        private const val KEY_LAST_STUDY_DAY = "last_study_day"
        private const val KEY_DAY_STREAK = "day_streak"
    }
}
