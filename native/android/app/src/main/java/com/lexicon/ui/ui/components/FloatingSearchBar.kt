package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.LexiconTextStyles

/**
 * FloatingSearchBar — pill-shaped search affordance.
 *
 * Layout: [search icon] [placeholder text] [camera icon] [mic icon]
 *
 * Stroke-equivalent: thin — using Icons.Outlined.* which gives a delicate
 * line weight similar to the web's strokeWidth 0.8.
 *
 * Accessibility:
 *   - The whole bar is a single Button (role + contentDescription
 *     "Search for a word") that opens the search modal.
 *   - The pill visual is wrapped in an outer [Box] that owns the click +
 *     enforces a 48dp × 48dp minimum touch target. The visual height is
 *     ~36dp (16dp icon + 10dp vertical padding × 2) — without the wrapper,
 *     the touch area would only be the inner 16dp icon strip, failing the
 *     WCAG 2.5.5 / Material 48dp minimum.
 *   - The camera & mic glyphs are decorative here (they are NOT separately
 *     interactive — the SearchModal owns their clickable equivalents), so
 *     their contentDescription is null and they don't pollute the merged
 *     search-button label.
 */
@Composable
fun FloatingSearchBar(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for a word…",
) {
    // Outer Box = the touch target / accessibility button.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .iconButton(AccessibilityAudit.Descriptions.Search)
            .enforceTouchTarget()
            .clickable(role = Role.Button) { viewModel.openSearch() },
        contentAlignment = Alignment.Center,
    ) {
        // Inner Row = the visual pill (shadow + clip + background + padding).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50),
                    clip = false,
                )
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp),
            )

            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = LexiconTextStyles.SearchPlaceholder,
                // The outer Box owns the accessibility label
                // ("Search for a word" via .iconButton above); suppress the
                // placeholder text's own semantics so TalkBack doesn't read
                // both the spec wording AND the placeholder wording.
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { },
            )

            Spacer(Modifier.size(0.dp))

            // Decorative glyphs — see class kdoc.
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
