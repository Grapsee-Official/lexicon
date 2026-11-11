package com.lexicon.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String = "View all",
    onAction: () -> Unit = {},
    divider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else OldWoodGold
    val lineColor = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = LexiconTextStyles.SectionTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sectionHeading(2),
        )

        if (divider) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(lineColor),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .clickable(role = Role.Button) { onAction() }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = actionLabel,
                color = accent,
                style = LexiconTextStyles.SectionAction,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}
