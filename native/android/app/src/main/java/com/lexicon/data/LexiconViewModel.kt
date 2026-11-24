package com.lexicon.data

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import com.lexicon.engine.EngineWordEntry
import com.lexicon.engine.LexiconEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TabKey { HOME, DICTIONARY, SEARCH, COLLECTIONS, MORE }

data class LexiconUiState(
    val activeTab: TabKey = TabKey.HOME,
    val route: ScreenRoute = ScreenRoute.HOME,
    val routeHistory: List<ScreenRoute> = emptyList(),
    val selectedWord: WordEntry? = null,
    val detailWord: WordEntry = LexiconData.serendipityEntry,
    val detailExpanded36: Boolean = false,
    val searchOpen: Boolean = false,
    val menuOpen: Boolean = false,
    val searchQuery: String = "serendipity",
    val collectionFilter: String = "All",
    val searchScopeTab: String = "All",
    val filterLanguage: String = "All Languages",
    val filterPos: String = "All Types",
    val filterDifficulty: String = "Any Level",
    val filterSort: String = "Relevance",
    val savedWordIds: List<String> = listOf("serendipity", "ethereal", "tranquility"),
    val favoriteWordIds: List<String> = listOf("serendipity", "solitude", "wanderlust", "eloquence"),
    val recentSearches: List<String> = listOf("serendipity", "tranquility", "eloquence", "nostalgia", "wanderlust"),
    val popularSearches: List<String> = listOf("serendipity", "eloquence", "nostalgia", "tranquility", "wanderlust"),
    val bookmarkedArticleIds: Set<String> = emptySet(),
    val recentlyViewedIds: List<String> = listOf("ephemeral", "wanderlust", "serendipity", "limerence", "ethereal"),
    val notes: List<NoteItem> = LexiconData.defaultNotes,
    val studyLists: List<StudyListItem> = LexiconData.defaultStudyLists,
    val templates: List<VocabTemplate> = LexiconData.defaultTemplates,
    val todayStudiedWords: Int = 0,
    val todayGoalWords: Int = 10,
    val dayStreak: Int = 0,
    val wordBuilderOpen: Boolean = false,
    val isOfflinePackReady: Boolean = true,
    val isLoading: Boolean = false,
    val errorBanner: String? = null,
    val isDark: Boolean = false,
)

class LexiconViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LexiconUiState())
    val uiState: StateFlow<LexiconUiState> = _uiState.asStateFlow()

    private var prefs: LexiconPrefs? = null
    private var speechContext: Context? = null
    private var nativeSpeech: TextToSpeech? = null
    private var speechReady = false
    private var pendingWord: String? = null
    val engine: LexiconEngine = LexiconEngine()

    init {
        runCatching { engine.loadDatabase(":embedded:") }
    }

    fun attachPrefs(context: Context) {
        speechContext = context.applicationContext
        if (prefs == null) {
            prefs = LexiconPrefs(context.applicationContext)
            val store = prefs!!
            val (saved, recent, isDark) = store.load()
            val progress = store.loadStudyProgress()
            _uiState.update { s ->
                s.copy(
                    savedWordIds = if (store.hasSavedIds) saved else s.savedWordIds,
                    recentSearches = if (store.hasRecentSearches) recent else s.recentSearches,
                    recentlyViewedIds = store.loadViewedIds() ?: s.recentlyViewedIds,
                    favoriteWordIds = store.loadFavoriteIds() ?: s.favoriteWordIds,
                    bookmarkedArticleIds = store.loadArticleBookmarks(),
                    notes = store.loadNotes() ?: s.notes,
                    studyLists = store.loadStudyLists() ?: if (progress == null) s.studyLists else s.studyLists.map { list ->
                        list.copy(completedWords = (progress[list.id] ?: list.completedWords).let { old ->
                            if (old > list.wordIds.size) 0 else old.coerceIn(0, list.wordIds.size)
                        })
                    },
                    templates = store.loadTemplates() ?: s.templates,
                    todayStudiedWords = store.loadTodayStudied(),
                    dayStreak = store.loadDayStreak(),
                    isDark = isDark,
                )
            }
            val externalDb = File(context.filesDir, "lexicon")
            if (File(externalDb, "lexicon.ht").exists()) {
                runCatching { engine.loadDatabase(externalDb.absolutePath) }
            }
        }
    }

    fun navigate(to: ScreenRoute, word: WordEntry? = null, expand36: Boolean = false) {
        _uiState.update { s ->
            val nextTab = when (to) {
                ScreenRoute.HOME -> TabKey.HOME
                ScreenRoute.DICTIONARY_HOME,
                ScreenRoute.WORD_OF_THE_DAY,
                ScreenRoute.WORD_DETAIL,
                ScreenRoute.SEARCH_RESULTS,
                ScreenRoute.EXPANDED_EXPLORATION -> TabKey.DICTIONARY
                ScreenRoute.SEARCH_DISCOVERY -> TabKey.SEARCH
                ScreenRoute.COLLECTIONS,
                ScreenRoute.MY_NOTES,
                ScreenRoute.STUDY_LISTS,
                ScreenRoute.QUICK_TEMPLATES -> TabKey.COLLECTIONS
                ScreenRoute.MORE -> TabKey.MORE
            }
            val resolvedWord = word ?: if (to == ScreenRoute.WORD_OF_THE_DAY) LexiconData.featuredWord() else s.detailWord
            val nextViewed = if (word != null) {
                (listOf(word.id) + s.recentlyViewedIds.filter { it != word.id }).take(8)
            } else s.recentlyViewedIds
            s.copy(
                activeTab = nextTab,
                route = to,
                routeHistory = (s.routeHistory + s.route).takeLast(12),
                detailWord = resolvedWord,
                detailExpanded36 = expand36,
                recentlyViewedIds = nextViewed,
                searchOpen = false,
            )
        }
        if (word != null) prefs?.saveViewedIds(_uiState.value.recentlyViewedIds)
    }

    fun navigateBack() {
        _uiState.update { s ->
            val prev = s.routeHistory.lastOrNull() ?: ScreenRoute.HOME
            val previousTab = when (prev) {
                ScreenRoute.HOME -> TabKey.HOME
                ScreenRoute.DICTIONARY_HOME, ScreenRoute.WORD_OF_THE_DAY, ScreenRoute.WORD_DETAIL,
                ScreenRoute.SEARCH_RESULTS, ScreenRoute.EXPANDED_EXPLORATION -> TabKey.DICTIONARY
                ScreenRoute.SEARCH_DISCOVERY -> TabKey.SEARCH
                ScreenRoute.COLLECTIONS, ScreenRoute.MY_NOTES, ScreenRoute.STUDY_LISTS,
                ScreenRoute.QUICK_TEMPLATES -> TabKey.COLLECTIONS
                ScreenRoute.MORE -> TabKey.MORE
            }
            s.copy(
                route = prev,
                activeTab = previousTab,
                routeHistory = s.routeHistory.dropLast(1),
                searchOpen = false,
                detailExpanded36 = prev == ScreenRoute.EXPANDED_EXPLORATION,
            )
        }
    }

    fun setDarkMode(dark: Boolean) {
        _uiState.update { s ->
            prefs?.saveIsDark(dark)
            s.copy(isDark = dark)
        }
    }

    fun setActiveTab(tab: TabKey) {
        val targetRoute = when (tab) {
            TabKey.HOME -> ScreenRoute.HOME
            TabKey.DICTIONARY -> ScreenRoute.DICTIONARY_HOME
            TabKey.SEARCH -> ScreenRoute.SEARCH_DISCOVERY
            TabKey.COLLECTIONS -> ScreenRoute.COLLECTIONS
            TabKey.MORE -> ScreenRoute.MORE
        }
        _uiState.update {
            it.copy(
                activeTab = tab,
                route = targetRoute,
                routeHistory = emptyList(),
                searchOpen = false,
            )
        }
    }

    fun openCollectionFilter(filter: String) {
        _uiState.update { it.copy(collectionFilter = if (filter in setOf("All", "Favorites", "Bookmarks")) filter else "All") }
        navigate(ScreenRoute.COLLECTIONS)
    }

    /** Speak an English dictionary word using the device's native voice. */
    fun pronounce(word: String) {
        val clean = word.trim()
        if (clean.isEmpty()) return
        val existing = nativeSpeech
        if (existing != null && speechReady) {
            existing.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "lexicon-dictionary-word")
            return
        }
        pendingWord = clean
        if (existing == null) {
            val ctx = speechContext ?: return
            nativeSpeech = TextToSpeech(ctx) { status ->
                speechReady = status == TextToSpeech.SUCCESS
                if (speechReady) {
                    nativeSpeech?.language = Locale.UK
                    pendingWord?.let { term ->
                        nativeSpeech?.speak(
                            term, TextToSpeech.QUEUE_FLUSH, null, "lexicon-dictionary-word"
                        )
                    }
                }
                pendingWord = null
            }
        }
    }

    fun toggleArticleBookmark(id: String) {
        if (LexiconData.articles.none { it.id == id }) return
        _uiState.update { s ->
            s.copy(bookmarkedArticleIds = if (id in s.bookmarkedArticleIds)
                s.bookmarkedArticleIds - id else s.bookmarkedArticleIds + id)
        }
        prefs?.saveArticleBookmarks(_uiState.value.bookmarkedArticleIds)
    }

    fun openWord(word: WordEntry) {
        navigate(ScreenRoute.WORD_DETAIL, word = word, expand36 = false)
    }

    fun openWordExpanded(word: WordEntry) {
        navigate(ScreenRoute.EXPANDED_EXPLORATION, word = word, expand36 = true)
    }

    fun closeWord() {
        _uiState.update { it.copy(selectedWord = null) }
    }

    fun openSearch() {
        navigate(ScreenRoute.SEARCH_DISCOVERY)
    }

    fun closeSearch() {
        _uiState.update { it.copy(searchOpen = false) }
    }

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun submitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        addRecentSearch(trimmed)
        val topMatch = searchWords(trimmed, limit = 1).firstOrNull()
        _uiState.update { current ->
            current.copy(
                searchQuery = trimmed,
                detailWord = topMatch ?: current.detailWord,
                selectedWord = null,
                route = ScreenRoute.SEARCH_RESULTS,
                routeHistory = (current.routeHistory + current.route).takeLast(12),
                activeTab = TabKey.DICTIONARY,
            )
        }
    }

    fun setSearchScopeTab(scope: String) {
        _uiState.update { it.copy(searchScopeTab = scope) }
    }

    fun cycleSearchFilter(which: String) {
        _uiState.update { s ->
            when (which) {
                "language" -> {
                    val opts = listOf("All Languages") + LexiconData.dictionary
                        .map { it.language.substringBefore(" (") }.distinct().sorted()
                    s.copy(filterLanguage = opts[(opts.indexOf(s.filterLanguage) + 1) % opts.size])
                }
                "pos" -> {
                    val opts = listOf("All Types") + LexiconData.dictionary
                        .map { it.partOfSpeech.label() }.distinct().sorted()
                    s.copy(filterPos = opts[(opts.indexOf(s.filterPos) + 1) % opts.size])
                }
                "difficulty" -> {
                    val opts = listOf("Any Level") + LexiconData.dictionary
                        .map { it.level }.distinct().sorted()
                    s.copy(filterDifficulty = opts[(opts.indexOf(s.filterDifficulty) + 1) % opts.size])
                }
                else -> {
                    val opts = listOf("Relevance", "Alphabetical")
                    s.copy(filterSort = opts[(opts.indexOf(s.filterSort) + 1) % opts.size])
                }
            }
        }
    }

    fun discoverRandomWord() {
        val pool = LexiconData.dictionary
        val current = _uiState.value.detailWord.id
        val next = pool.filter { it.id != current }.randomOrNull() ?: LexiconData.serendipityEntry
        navigate(ScreenRoute.WORD_DETAIL, word = next)
    }

    fun toggleWordBuilder(open: Boolean) {
        _uiState.update { it.copy(wordBuilderOpen = open) }
    }

    fun toggleSave(id: String) {
        _uiState.update { s ->
            val next = if (s.savedWordIds.contains(id)) s.savedWordIds - id else listOf(id) + s.savedWordIds
            prefs?.saveSavedIds(next)
            s.copy(savedWordIds = next)
        }
    }

    fun toggleFavorite(id: String) {
        _uiState.update { s ->
            val next = if (s.favoriteWordIds.contains(id)) s.favoriteWordIds - id else listOf(id) + s.favoriteWordIds
            s.copy(favoriteWordIds = next)
        }
        prefs?.saveFavoriteIds(_uiState.value.favoriteWordIds)
    }

    fun isSaved(id: String): Boolean = _uiState.value.savedWordIds.contains(id)
    fun isFavorite(id: String): Boolean = _uiState.value.favoriteWordIds.contains(id)

    fun addNote(category: NoteCategory, title: String, body: String) {
        val cleanTitle = title.trim().ifEmpty { "Untitled Note" }
        val cleanBody = body.trim().ifEmpty { "Captured during countryside study." }
        val today = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
        val item = NoteItem(
            id = "note_${System.currentTimeMillis()}",
            category = category,
            title = cleanTitle,
            body = cleanBody,
            dateLabel = today,
            bookmarked = false,
            illustration = when (category) {
                NoteCategory.WORD_NOTE -> "ill_note_flowers"
                NoteCategory.STUDY -> "ill_col_bookmarks"
                NoteCategory.PERSONAL -> "ill_col_notes"
                NoteCategory.IDEA -> "ill_note_lantern"
            },
        )
        _uiState.update { it.copy(notes = listOf(item) + it.notes) }
        prefs?.saveNotes(_uiState.value.notes)
    }

    fun toggleNoteBookmark(noteId: String) {
        _uiState.update { s ->
            s.copy(notes = s.notes.map { if (it.id == noteId) it.copy(bookmarked = !it.bookmarked) else it })
        }
        prefs?.saveNotes(_uiState.value.notes)
    }

    fun updateNote(noteId: String, title: String, body: String) {
        _uiState.update { state ->
            state.copy(notes = state.notes.map { note ->
                if (note.id == noteId) note.copy(title = title.trim(), body = body.trim()) else note
            })
        }
        prefs?.saveNotes(_uiState.value.notes)
    }

    fun deleteNote(noteId: String) {
        _uiState.update { s -> s.copy(notes = s.notes.filterNot { it.id == noteId }) }
        prefs?.saveNotes(_uiState.value.notes)
    }

    /** Only actual offline dictionary entries can form a practice list. */
    fun createStudyList(title: String, words: String): Boolean {
        val name = title.trim()
        if (name.isBlank()) return false
        val tokens = if (words.isBlank()) _uiState.value.savedWordIds
            else words.split(',', ';', '\n').map { it.trim() }
        val ids = tokens.mapNotNull { token ->
            LexiconData.dictionary.firstOrNull {
                it.id.equals(token, ignoreCase = true) || it.word.equals(token, ignoreCase = true)
            }?.id
        }.distinct()
        if (ids.isEmpty()) return false
        val list = StudyListItem(
            id = "sl_custom_" + java.util.UUID.randomUUID().toString(),
            title = name, totalWords = ids.size, completedWords = 0,
            thumbnail = "ill_thumb_books", wordIds = ids,
        )
        _uiState.update { it.copy(studyLists = it.studyLists + list) }
        prefs?.saveStudyLists(_uiState.value.studyLists)
        return true
    }

    fun resetStudyList(id: String) {
        _uiState.update { s ->
            s.copy(studyLists = s.studyLists.map {
                if (it.id == id) it.copy(completedWords = 0) else it
            })
        }
        prefs?.saveStudyLists(_uiState.value.studyLists)
    }

    fun deleteStudyList(id: String) {
        if (!id.startsWith("sl_custom_")) return
        _uiState.update { s -> s.copy(studyLists = s.studyLists.filterNot { it.id == id }) }
        prefs?.saveStudyLists(_uiState.value.studyLists)
    }

    /** Only records a successfully revealed vocabulary card, never a navigation tap. */
    fun advanceStudyList(listId: String) {
        val current = _uiState.value.studyLists.firstOrNull { it.id == listId } ?: return
        if (current.completedWords >= current.wordIds.size || current.wordIds.isEmpty()) return
        val streak = prefs?.recordStudyAndGetStreak() ?: _uiState.value.dayStreak.coerceAtLeast(1)
        _uiState.update { s ->
            s.copy(
                studyLists = s.studyLists.map { sl ->
                    if (sl.id == listId) sl.copy(completedWords = (sl.completedWords + 1).coerceAtMost(sl.wordIds.size)) else sl
                },
                todayStudiedWords = (s.todayStudiedWords + 1).coerceAtMost(s.todayGoalWords),
                dayStreak = streak,
            )
        }
        prefs?.saveStudyProgress(_uiState.value.studyLists)
        prefs?.saveStudyLists(_uiState.value.studyLists)
        prefs?.saveTodayStudied(_uiState.value.todayStudiedWords)
    }

    fun createCustomTemplate(title: String, subtitle: String, category: TemplateCategory) {
        val tpl = VocabTemplate(
            id = "tpl_${System.currentTimeMillis()}",
            title = title.trim().ifEmpty { "Countryside Journal" },
            subtitle = subtitle.trim().ifEmpty { "Custom literary word study." },
            category = category,
            illustration = "ill_tpl_write",
            starred = true,
        )
        _uiState.update { it.copy(templates = listOf(tpl) + it.templates) }
        prefs?.saveTemplates(_uiState.value.templates)
    }

    fun toggleTemplateStar(id: String) {
        _uiState.update { s ->
            s.copy(templates = s.templates.map { if (it.id == id) it.copy(starred = !it.starred) else it })
        }
        prefs?.saveTemplates(_uiState.value.templates)
    }

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

    fun clearPopularSearches() {
        _uiState.update { it.copy(popularSearches = emptyList()) }
    }

    fun restorePopularSearches() {
        _uiState.update {
            it.copy(popularSearches = listOf("serendipity", "eloquence", "nostalgia", "tranquility", "wanderlust"))
        }
    }

    fun toggleTheme() {
        _uiState.update { s ->
            val next = !s.isDark
            prefs?.saveIsDark(next)
            s.copy(isDark = next)
        }
    }

    fun searchWords(query: String, limit: Int = 12): List<WordEntry> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val s = _uiState.value
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
        if (s.searchScopeTab == "Examples") {
            LexiconData.dictionary.filter { w ->
                w.examples.any { it.contains(q, ignoreCase = true) }
            }.forEach { out.putIfAbsent(it.id, it) }
        }

        var list = out.values.toList()
        list = when (s.searchScopeTab) {
            "Words" -> list.filter { it.word.contains(q, ignoreCase = true) }
            "Meanings" -> list.filter { it.definitions.any { d -> d.contains(q, ignoreCase = true) } }
            "Examples" -> list.filter { it.examples.any { e -> e.contains(q, ignoreCase = true) } }
            "Idioms" -> emptyList()
            else -> list
        }
        if (s.filterLanguage != "All Languages") {
            list = list.filter { it.language.contains(s.filterLanguage, ignoreCase = true) }
        }
        if (s.filterPos != "All Types") {
            list = list.filter { it.partOfSpeech.label().equals(s.filterPos, ignoreCase = true) }
        }
        if (s.filterDifficulty != "Any Level") {
            list = list.filter { it.level.equals(s.filterDifficulty, ignoreCase = true) }
        }
        if (s.filterSort == "Alphabetical") {
            list = list.sortedBy { it.word.lowercase() }
        }
        return list.take(limit)
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
        nativeSpeech?.stop()
        nativeSpeech?.shutdown()
        nativeSpeech = null
        runCatching { engine.close() }
        super.onCleared()
    }
}
