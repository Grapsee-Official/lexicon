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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val chipBg = if (isDark) DarkCard else CardSurface.copy(alpha = 0.78f)
    val chipBorder = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f)
    val chipText = if (isDark) DarkForeground.copy(alpha = 0.88f) else DeepForest.copy(alpha = 0.84f)
    val gridTint = if (isDark) DarkAccent else DeepForest.copy(alpha = 0.68f)

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
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
                    .padding(horizontal = 13.dp, vertical = 5.5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tag.label,
                    color = chipText,
                    style = LexiconTextStyles.TagChip,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 28.dp)
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
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
