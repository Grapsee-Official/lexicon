package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.data.TrendingTag
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles

@Composable
fun TrendingTagRow(
    viewModel: LexiconViewModel,
    tags: List<TrendingTag> = LexiconData.trendingTags,
    modifier: Modifier = Modifier,
    sceneIntegrated: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val integrated = isDark && sceneIntegrated
    val compact = integrated && LocalConfiguration.current.fontScale <= 1.2f
    val chipBg = if (integrated) Color(0xFF0B151D).copy(alpha = 0.88f)
        else if (isDark) DarkCard else CardSurface.copy(alpha = 0.78f)
    val chipBorder = if (integrated) Color(0xFFB6A486).copy(alpha = 0.12f)
        else if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f)
    val chipText = if (integrated) Color(0xFFD8C7A8) else if (isDark) DarkForeground.copy(alpha = 0.88f) else DeepForest.copy(alpha = 0.84f)
    val gridTint = if (integrated) Color(0xFFD0AD75) else if (isDark) DarkAccent else DeepForest.copy(alpha = 0.68f)

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(chipBg)
                    .border(1.dp, chipBorder, CircleShape)
                    .clickable(role = Role.Button) {
                        LexiconData.getWordById(tag.wordId)?.let { viewModel.openWord(it) }
                    }
                    .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 6.dp else 8.5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tag.label,
                    color = chipText,
                    style = if (compact) LexiconTextStyles.TagChip.copy(fontSize = 10.5.sp, lineHeight = 14.sp)
                        else LexiconTextStyles.TagChip,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(width = if (compact) 30.dp else 34.dp, height = if (compact) 28.dp else 34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(chipBg)
                .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                .iconButton(AccessibilityAudit.Descriptions.Grid)
                .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.DICTIONARY) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.GridView,
                contentDescription = null,
                tint = gridTint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
