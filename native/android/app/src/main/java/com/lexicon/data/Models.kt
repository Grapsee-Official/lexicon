package com.lexicon.data

/**
 * Data models — direct translation of /home/z/my-project/src/lib/lexicon-data.ts.
 *
 * All types are immutable Kotlin data classes so they work cleanly inside a
 * StateFlow-driven ViewModel.
 */

enum class PartOfSpeech {
    NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION;

    /** Pretty label in lowercase — matches the web `lowercase` rendering. */
    fun label(): String = name.lowercase()
}

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
    /** Optional drawable resource name for the detail-sheet header. */
    val illustration: String? = null,
)

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    /** Drawable resource name (without extension) under res/drawable-nodpi/. */
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

/** A grouped alphabetical section for the Dictionary screen. */
data class AlphabetSection(
    val letter: String,
    val entries: List<WordEntry>,
)
