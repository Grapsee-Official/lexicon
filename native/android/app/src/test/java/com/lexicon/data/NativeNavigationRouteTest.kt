package com.lexicon.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A route is a full native destination; the bottom navigation must always
 * reflect its parent area when the reader returns through the back stack.
 */
class NativeNavigationRouteTest {
    @Test
    fun everyNativeDestinationMapsToTheCorrectParentTab() {
        val expected = mapOf(
            ScreenRoute.HOME to TabKey.HOME,
            ScreenRoute.DICTIONARY_HOME to TabKey.DICTIONARY,
            ScreenRoute.WORD_OF_THE_DAY to TabKey.DICTIONARY,
            ScreenRoute.WORD_DETAIL to TabKey.DICTIONARY,
            ScreenRoute.SEARCH_RESULTS to TabKey.DICTIONARY,
            ScreenRoute.EXPANDED_EXPLORATION to TabKey.DICTIONARY,
            ScreenRoute.SEARCH_DISCOVERY to TabKey.SEARCH,
            ScreenRoute.COLLECTIONS to TabKey.COLLECTIONS,
            ScreenRoute.MY_NOTES to TabKey.COLLECTIONS,
            ScreenRoute.STUDY_LISTS to TabKey.COLLECTIONS,
            ScreenRoute.QUICK_TEMPLATES to TabKey.COLLECTIONS,
            ScreenRoute.MORE to TabKey.MORE,
        )
        assertEquals("Do not introduce a route without navigation semantics",
            ScreenRoute.values().toSet(), expected.keys)
        expected.forEach { (route, parentTab) ->
            assertEquals("Wrong highlighted tab after returning to $route",
                parentTab, tabForRoute(route))
        }
    }
}
