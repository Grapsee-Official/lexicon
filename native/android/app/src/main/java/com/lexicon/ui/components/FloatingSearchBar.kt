package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBorder
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DarkSearchPill
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.SearchPillLight

@Composable
fun FloatingSearchBar(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for a word...",
    sceneIntegrated: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val integrated = isDark && sceneIntegrated
    val compactType = integrated && LocalConfiguration.current.fontScale <= 1.2f
    val bg = if (integrated) Color(0xFF0B161C).copy(alpha = 0.97f)
        else if (isDark) DarkSearchPill.copy(alpha = 0.95f) else SearchPillLight
    val borderColor = if (integrated) Color(0xFFB6A486).copy(alpha = 0.19f)
        else if (isDark) DarkBorder else DeepForest.copy(alpha = 0.08f)
    val iconColor = if (integrated) Color(0xFFD0AD75) else if (isDark) DarkAccent else OldWoodGold
    val textColor = if (isDark) DarkMutedForeground else OldWoodGold.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .iconButton(AccessibilityAudit.Descriptions.Search)
            .clickable(role = Role.Button) { viewModel.openSearch() },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!isDark) {
                        Modifier.shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = DeepForest.copy(alpha = 0.14f),
                            spotColor = DeepForest.copy(alpha = 0.14f),
                        )
                    } else Modifier
                )
                .clip(RoundedCornerShape(50))
                .then(if (integrated) Modifier.background(Brush.verticalGradient(
                    listOf(Color(0xFF10191E).copy(alpha = 0.97f), bg)
                )) else Modifier.background(bg))
                .border(1.dp, borderColor, RoundedCornerShape(50))
                .padding(horizontal = 15.dp, vertical = if (integrated) 11.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )

            Text(
                text = placeholder,
                color = textColor,
                style = if (compactType) LexiconTextStyles.SearchPlaceholder.copy(fontSize = 11.5.sp)
                    else LexiconTextStyles.SearchPlaceholder,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { },
            )

            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )

            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
