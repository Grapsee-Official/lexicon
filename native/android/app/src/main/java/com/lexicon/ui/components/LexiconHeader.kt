package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkHighlight
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold

/**
 * Exact header layout from the Light & Dark reference PNGs:
 *   [Hamburger] [Crest Mark] [Lexora / Words. Wisdom. Worlds.] ...... [Bell+dot] [Avatar]
 * (Zero invented extra icons in the top bar; tapping the brand crest or More->Theme switches mode.)
 */
@Composable
fun LexiconHeader(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val iconTint = if (isDark) DarkAccent.copy(alpha = 0.88f) else DeepForest.copy(alpha = 0.72f)
    val wordmarkColor = if (isDark) DarkForeground else BrandGreen
    val taglineColor = if (isDark) DarkForeground.copy(alpha = 0.76f) else DeepForest.copy(alpha = 0.70f)
    val dotColor = if (isDark) DarkHighlight else OldWoodGold
    val markRes = if (isDark) R.drawable.lexicon_mark_dark else R.drawable.lexicon_mark
    val avatarRes = if (isDark) R.drawable.avatar_dark else R.drawable.avatar_default

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Menu)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.MORE) },
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(21.dp),
            )
        }

        Spacer(Modifier.width(4.dp))

        Image(
            painter = painterResource(markRes),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clickable(role = Role.Button) { viewModel.toggleTheme() },
            contentScale = ContentScale.Fit,
        )

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.HOME) },
        ) {
            Text(
                text = "Lexicon",
                color = wordmarkColor,
                style = LexiconTextStyles.BrandWordmark,
                maxLines = 1,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "Words. Wisdom. Worlds.",
                color = taglineColor,
                style = LexiconTextStyles.BrandTagline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Bell)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp)
                    .size(5.5.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isDark) DarkAccent.copy(alpha = 0.35f) else DeepForest.copy(alpha = 0.16f),
                    shape = CircleShape,
                )
                .iconButton(AccessibilityAudit.Descriptions.Avatar)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.MORE) },
        ) {
            Image(
                painter = painterResource(avatarRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
