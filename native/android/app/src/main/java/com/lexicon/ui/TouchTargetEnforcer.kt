package com.lexicon.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * TouchTargetEnforcer — wraps content to enforce 48dp minimum touch target.
 *
 * Per spec: "Hard gate: 100% touch target compliance"
 * (44pt minimum for iOS, 48dp for Android Material Design)
 *
 * Usage:
 *   TouchTargetEnforcer(
 *       onClick = { ... },
 *       contentDescription = "Search for a word",
 *       modifier = Modifier...
 *   ) {
 *       Icon(Icons.Outlined.Search, ...)
 *   }
 */
@Composable
fun TouchTargetEnforcer(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    minSizeDp: Int = 48,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .sizeIn(
                minWidth = minSizeDp.dp,
                minHeight = minSizeDp.dp
            )
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
    ) {
        content()
    }
}
