package com.lexora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexora.ui.enforceTouchTarget
import com.lexora.ui.sectionHeading
import com.lexora.ui.theme.LexoraTextStyles
import com.lexora.ui.theme.OldWoodGold
import com.lexora.ui.theme.DarkAccent
import com.lexora.ui.theme.DarkBackground

/**
 * SectionHeader — title left + "View all →" right.
 *
 * Optional divider line between title and action — matches web `divider` prop.
 *
 * Action uses the highlight colour (OldWoodGold light / DarkAccent dark).
 */
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String = "View all",
    onAction: () -> Unit = {},
    divider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = LexoraTextStyles.SectionTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // Section titles are h2 headings — TalkBack announces "Heading"
            // before each, letting screen-reader users jump between sections.
            modifier = Modifier.sectionHeading(2),
        )

        if (divider) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .enforceTouchTarget()
                .clickable(role = Role.Button) { onAction() }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val accent = if (MaterialTheme.colorScheme.isDark()) DarkAccent else OldWoodGold
            Text(
                text = actionLabel,
                color = accent,
                style = LexoraTextStyles.SectionAction,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

/** Quick dark-mode check — works against our Lexora colour scheme. */
private fun androidx.compose.material3.ColorScheme.isDark(): Boolean =
    background == DarkBackground
