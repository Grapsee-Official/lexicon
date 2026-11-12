package com.lexicon.data

enum class PartOfSpeech {
    NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION;

    fun label(): String = name.lowercase()
}

data class WordTranslation(
    val language: String,
    val code: String,
    val translated: String,
    val transliteration: String = "",
)

data class WordEntry(
    val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: PartOfSpeech,
    val definitions: List<String>,
    val etymology: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val examples: List<String>,
    val language: String,
    val illustration: String? = null,
    val originPeriod: String = "Late 18th c.",
    val frequency: String = "High",
    val level: String = "Intermediate",
    val relatedWords: List<String> = emptyList(),
    val didYouKnow: String = "",
)

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val illustration: String,
)

data class ExploreTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val illustration: String,
)

data class Article(
    val id: String,
    val title: String,
    val excerpt: String,
    val readMinutes: Int,
    val thumbnail: String,
    val bookmarked: Boolean = false,
)

data class TrendingTag(
    val label: String,
    val wordId: String,
)

data class AlphabetSection(
    val letter: String,
    val entries: List<WordEntry>,
)

enum class NoteCategory(val label: String) {
    PERSONAL("Personal"),
    WORD_NOTE("Word Note"),
    STUDY("Study"),
    IDEA("Idea"),
}

data class NoteItem(
    val id: String,
    val category: NoteCategory,
    val title: String,
    val body: String,
    val dateLabel: String,
    val bookmarked: Boolean = false,
    val illustration: String? = null,
)

data class StudyListItem(
    val id: String,
    val title: String,
    val totalWords: Int,
    val completedWords: Int,
    val thumbnail: String,
    val wordIds: List<String>,
) {
    val percentComplete: Int
        get() = if (totalWords <= 0) 0 else ((completedWords * 100f) / totalWords).toInt().coerceIn(0, 100)
}

enum class TemplateCategory(val label: String) {
    LEARNING("Learning"),
    WRITING("Writing"),
    EXAM("Exam"),
    DAILY_LIFE("Daily Life"),
}

data class VocabTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: TemplateCategory,
    val illustration: String,
    val starred: Boolean = true,
    val sampleWords: List<String> = emptyList(),
)

data class IdiomPhrase(
    val id: String,
    val phrase: String,
    val meaning: String,
    val example: String,
    val kind: String, // "Idiom" or "Phrase"
)

data class MorphemeRoot(
    val root: String,
    val meaning: String,
    val origin: String,
    val derivatives: List<String>,
)

/**
 * Full navigation destinations covering all 10 UI concepts + Home & Settings.
 * Pages 3, 9, and 10 share a unified WordEntry architecture with progressive expansion!
 */
enum class ScreenRoute {
    HOME,                   // Original Cotswolds Scholarly Home
    DICTIONARY_HOME,        // Page 1: Dictionary Home (Solitude WOTD, Explore Words, Kipling quote, Random & Word Builder)
    WORD_OF_THE_DAY,        // Page 2: Dedicated Word of the Day screen
    WORD_DETAIL,            // Page 3: Full Word Profile (Serendipity + 4 metadata cards + Did You Know?)
    COLLECTIONS,            // Page 4: Collections Hub (Favorites, Bookmarks, My Notes, Study List)
    MY_NOTES,               // Page 5: My Notes (2x2 grid + search/filters + Quick Templates)
    STUDY_LISTS,            // Page 6: Study Lists (stats, Today's Study 6/10 ring, list progress)
    QUICK_TEMPLATES,        // Page 7: Quick Templates (2x2 illustrated templates + Create Your Own)
    SEARCH_DISCOVERY,       // Page 8: Search & Discovery (tabs, Recent Searches, 4 Search Filters)
    SEARCH_RESULTS,         // Page 9: Search Results / Dictionary Search inline card + matched entries
    EXPANDED_EXPLORATION,   // Page 10: Dictionary Lookup Hub + Expanded 36-Language Word Exploration
    MORE,                   // Settings & Appearance
}
