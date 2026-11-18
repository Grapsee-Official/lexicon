package com.lexicon.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Release-gating regressions for our actual offline dictionary seed data.
 * These run on the JVM without requiring an Android emulator or screenshot.
 */
class NativeDictionaryContractTest {
    @Test
    fun allStudyListsCountOnlyWordsThatExist() {
        assertFalse(LexiconData.defaultStudyLists.isEmpty())
        LexiconData.defaultStudyLists.forEach { list ->
            assertEquals(
                "Study count must equal real vocabulary IDs: ${list.id}",
                list.wordIds.size, list.totalWords
            )
            assertEquals(
                "Fresh lists must not pretend a learner completed reviews",
                0, list.completedWords
            )
            list.wordIds.forEach { id ->
                assertNotNull("Missing study word: $id", LexiconData.getWordById(id))
            }
        }
    }

    @Test
    fun unknownSearchMustNotInventAWord() {
        assertTrue(LexiconData.searchWords("zzz_no_such_country_word_9274").isEmpty())
        assertTrue(LexiconData.searchWords("   ").isEmpty())
    }

    @Test
    fun dictionaryEntriesHaveMeaningfulContent() {
        assertFalse(LexiconData.dictionary.isEmpty())
        LexiconData.dictionary.forEach { entry ->
            assertTrue("Missing word for ${entry.id}", entry.word.isNotBlank())
            assertTrue(
                "Missing definition for ${entry.id}",
                entry.definitions.any { it.isNotBlank() }
            )
        }
    }
}
