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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.width
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.BrandGreen
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
    homeEditorial: Boolean = false,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val editorial = isDark && homeEditorial
    val compactType = editorial && LocalConfiguration.current.fontScale < 1.5f
    val accent = if (editorial) Color(0xFFD0AD75) else if (isDark) DarkAccent else BrandGreen
    val lineColor = if (editorial) Color(0xFFB6A486).copy(alpha = 0.13f)
        else if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = if (compactType) LexiconTextStyles.SectionTitle.copy(fontSize = 14.5.sp, lineHeight = 18.sp)
                else LexiconTextStyles.SectionTitle,
            maxLines = 2,
            modifier = (if (divider && compactType) Modifier else Modifier.weight(1f)).sectionHeading(2),
        )

        if (divider) {
            Box(
                modifier = (if (compactType) Modifier.weight(1f) else Modifier.width(12.dp))
                    .height(1.dp)
                    .background(lineColor),
            )
        } else {
            Spacer(Modifier.size(0.dp))
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
                style = if (compactType) LexiconTextStyles.SectionAction.copy(fontSize = 10.5.sp)
                    else LexiconTextStyles.SectionAction,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(if (editorial) 11.dp else 13.dp),
            )
        }
    }
}
