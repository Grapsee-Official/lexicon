package com.lexora.ui

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities for Lexora — Android TalkBack compliance.
 *
 * Per spec: "Do run TalkBack/VoiceOver audits.
 * Hard gate: 100% touch target compliance."
 *
 * Minimum touch target: 48dp × 48dp (Material Design standard).
 */

object AccessibilityAudit {
    const val MIN_TOUCH_TARGET_DP = 48

    /** Content descriptions for all icon-only buttons */
    object Labels {
        const val MENU = "Open navigation menu"
        const val SEARCH = "Search for a word"
        const val NOTIFICATIONS = "Notifications"
        const val PROFILE = "Open profile"
        const val VOLUME = "Pronounce word"
        const val BOOKMARK_SAVE = "Save word"
        const val BOOKMARK_REMOVE = "Remove from saved"
        const val SHARE = "Share word"
        const val GRID = "More search options"
        const val CAMERA = "Search with camera"
        const val MIC = "Search with voice"
        const val CLOSE = "Close"
        const val BACK = "Go back"
        const val EXPLORE_MORE = "Explore more about this word"
        const val VIEW_ALL = "View all"
    }

    /** Heading levels for semantic structure */
    enum class HeadingLevel { H1, H2, H3, H4 }
}

/**
 * Modifier: enforces minimum 48dp × 48dp touch target.
 * Use on all interactive elements (buttons, icons, chips).
 */
fun Modifier.minTouchTarget(): Modifier =
    this.sizeIn(
        minWidth = 48.dp,
        minHeight = 48.dp
    )

/**
 * Modifier: adds content description for icon-only buttons.
 */
fun Modifier.contentDescription(description: String): Modifier =
    this.semantics { contentDescription = description }

/**
 * Modifier: marks element as a heading for TalkBack navigation.
 */
fun Modifier.heading(level: AccessibilityAudit.HeadingLevel = AccessibilityAudit.HeadingLevel.H2): Modifier =
    this.semantics {
        heading()
        when (level) {
            AccessibilityAudit.HeadingLevel.H1 -> {}
            AccessibilityAudit.HeadingLevel.H2 -> {}
            AccessibilityAudit.HeadingLevel.H3 -> {}
            AccessibilityAudit.HeadingLevel.H4 -> {}
        }
    }

/**
 * Modifier: marks element as a button with role semantics.
 */
fun Modifier.buttonRole(): Modifier =
    this.semantics { role = Role.Button }

/**
 * Modifier: marks element as a search field.
 */
fun Modifier.searchFieldRole(): Modifier =
    this.semantics { role = Role.SearchBox }

/**
 * Modifier: announces live region updates (for dynamic content like search results).
 */
fun Modifier.liveRegion(polite: Boolean = true): Modifier =
    this.semantics {
        liveRegion = if (polite) LiveRegionMode.Polite else LiveRegionMode.Assertive
    }

/**
 * Modifier: combines content description + button role + min touch target.
 * Use this for all icon-only interactive elements.
 */
fun Modifier.accessibleIconButton(
    description: String,
    minTouch: Boolean = true
): Modifier {
    var mod = this
        .contentDescription(description)
        .buttonRole()
    if (minTouch) mod = mod.minTouchTarget()
    return mod
}

/**
 * Modifier: marks an image as decorative (hidden from TalkBack).
 */
fun Modifier.decorative(): Modifier =
    this.semantics { invisibleToUser() }
