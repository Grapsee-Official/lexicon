package com.lexicon.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Ensures large text does not collapse genuine tablet layouts into a
 * wasteful one-card-per-row design, while compact phones stay readable.
 */
class CountrysideResponsiveGridTest {
    @Test
    fun compactPhonesUseSingleColumnAtEitherTextSize() {
        assertEquals(1, countrysideAdaptiveColumns(320, 1.0f, maxColumns = 2))
        assertEquals(1, countrysideAdaptiveColumns(320, 1.3f, maxColumns = 2))
        assertEquals(1, countrysideAdaptiveColumns(320, 1.0f, maxColumns = 4))
        assertEquals(1, countrysideAdaptiveColumns(320, 1.3f, maxColumns = 4))
    }

    @Test
    fun ordinaryPhoneUsesTwoColumnsOnlyWhenTheyFit() {
        assertEquals(1, countrysideAdaptiveColumns(360, 1.0f, maxColumns = 2))
        assertEquals(2, countrysideAdaptiveColumns(412, 1.0f, maxColumns = 2))
        assertEquals(1, countrysideAdaptiveColumns(412, 1.3f, maxColumns = 2))
    }

    @Test
    fun textEnlargementDoesNotForceSingleColumnOnTablet() {
        assertEquals(2, countrysideAdaptiveColumns(800, 1.3f, maxColumns = 2))
        assertEquals(2, countrysideAdaptiveColumns(800, 1.3f, maxColumns = 4))
        assertEquals(
            2,
            countrysideAdaptiveColumns(800, 1.3f, maxColumns = 4, accessibleMinCardWidthDp = 270),
        )
    }

    @Test
    fun largeScreensAndIntermediateWidthsFollowCardMinima() {
        assertEquals(4, countrysideAdaptiveColumns(800, 1.0f, maxColumns = 4))
        assertEquals(2, countrysideAdaptiveColumns(600, 1.3f, maxColumns = 4))
        assertEquals(
            1,
            countrysideAdaptiveColumns(560, 1.3f, maxColumns = 4, accessibleMinCardWidthDp = 270),
        )
    }
}
