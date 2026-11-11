package com.lexicon.ui

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

object AccessibilityAudit {
    const val MIN_TOUCH_TARGET_DP = 48

    object Labels {
        const val MENU = "Open navigation menu"
        const val SEARCH = "Search for a word"
        const val SEARCH_FAB = "Search for a word"
        const val NOTIFICATIONS = "Notifications, new items available"
        const val PROFILE = "Open profile"
        const val VOLUME = "Pronounce word"
        const val BOOKMARK_SAVE = "Save word"
        const val BOOKMARK_REMOVE = "Remove from saved"
        const val SHARE = "Share word"
        const val GRID = "More search options"
        const val CAMERA = "Search with camera"
        const val MIC = "Search with voice"
        const val CLOSE = "Close"
        const val CLEAR_RECENT = "Clear recent searches"
        const val BACK = "Go back"
        const val EXPLORE_MORE = "Explore more about this word"
        const val VIEW_ALL = "View all"
    }

    object Descriptions {
        const val Menu = Labels.MENU
        const val Search = Labels.SEARCH
        const val SearchFab = Labels.SEARCH_FAB
        const val Bell = Labels.NOTIFICATIONS
        const val Avatar = Labels.PROFILE
        const val Volume = Labels.VOLUME
        const val BookmarkSave = Labels.BOOKMARK_SAVE
        const val BookmarkRemove = Labels.BOOKMARK_REMOVE
        const val BookmarkOn = Labels.BOOKMARK_REMOVE
        const val BookmarkOff = Labels.BOOKMARK_SAVE
        const val Share = Labels.SHARE
        const val Grid = Labels.GRID
        const val Camera = Labels.CAMERA
        const val Mic = Labels.MIC
        const val Close = Labels.CLOSE
        const val Clear = Labels.CLEAR_RECENT
        const val ClearRecent = Labels.CLEAR_RECENT
    }

    enum class HeadingLevel { H1, H2, H3, H4 }
}

fun Modifier.minTouchTarget(): Modifier =
    this.sizeIn(minWidth = 48.dp, minHeight = 48.dp)

fun Modifier.enforceTouchTarget(): Modifier = minTouchTarget()

fun Modifier.contentDescription(description: String): Modifier =
    this.semantics { contentDescription = description }

fun Modifier.iconButton(description: String): Modifier =
    this.semantics {
        contentDescription = description
        role = Role.Button
    }

fun Modifier.heading(level: AccessibilityAudit.HeadingLevel = AccessibilityAudit.HeadingLevel.H2): Modifier =
    this.semantics { heading() }

fun Modifier.sectionHeading(level: Int = 2): Modifier =
    this.semantics { heading() }

fun Modifier.buttonRole(): Modifier =
    this.semantics { role = Role.Button }

fun Modifier.liveRegion(polite: Boolean = true): Modifier =
    this.semantics {
        liveRegion = if (polite) LiveRegionMode.Polite else LiveRegionMode.Assertive
    }

fun Modifier.liveAnnouncement(polite: Boolean = true): Modifier = liveRegion(polite)

fun Modifier.accessibleIconButton(description: String, minTouch: Boolean = true): Modifier {
    var mod = this.contentDescription(description).buttonRole()
    if (minTouch) mod = mod.minTouchTarget()
    return mod
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
fun Modifier.decorative(): Modifier =
    this.semantics { invisibleToUser() }
