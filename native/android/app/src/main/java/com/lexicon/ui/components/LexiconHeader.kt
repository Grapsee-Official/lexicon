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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.LexiconData
import com.lexicon.data.ScreenRoute
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
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkCard

/**
 * Exact header layout from the Light & Dark reference PNGs:
 *   [Hamburger] [Crest Mark] [Lexicon / Words. Wisdom. Worlds.] ...... [Bell+dot] [Avatar]
 * (Zero invented extra icons in the top bar; tapping the brand crest or More->Theme switches mode.)
 */
@Composable
fun LexiconHeader(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val homeNight = isDark && state.route == ScreenRoute.HOME
    val homeEditorial = state.route == ScreenRoute.HOME
    var showDailyLetter by remember { mutableStateOf(false) }

    val iconTint = if (homeNight) Color(0xFFD0AD75) else if (isDark) DarkAccent.copy(alpha = 0.88f) else DeepForest.copy(alpha = 0.72f)
    val wordmarkColor = if (isDark) DarkForeground else BrandGreen
    val taglineColor = if (homeNight) Color(0xFFC9BDA9) else if (isDark) DarkForeground.copy(alpha = 0.76f) else DeepForest.copy(alpha = 0.70f)
    val dotColor = if (isDark) DarkHighlight else OldWoodGold
    val markRes = if (homeNight) R.drawable.ref_home_crest else if (isDark) R.drawable.lexicon_mark_dark else R.drawable.lexicon_mark
    val avatarRes = if (homeNight) R.drawable.ref_home_avatar else if (isDark) R.drawable.avatar_dark else R.drawable.avatar_default

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
                .size(if (homeEditorial) 36.dp else 42.dp)
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
                text = "Lexora",
                color = wordmarkColor,
                style = if (homeEditorial) LexiconTextStyles.BrandWordmark.copy(fontSize = 19.sp, lineHeight = 22.sp) else LexiconTextStyles.BrandWordmark,
                maxLines = 1,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "Words. Wisdom. Worlds.",
                color = taglineColor,
                style = if (homeEditorial) LexiconTextStyles.BrandTagline.copy(fontSize = 8.sp, lineHeight = 10.sp) else LexiconTextStyles.BrandTagline,
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
                .clickable(role = Role.Button) { showDailyLetter = true },
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
                .size(if (homeEditorial) 32.dp else 36.dp)
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

    if (showDailyLetter) {
        val word = LexiconData.featuredWord()
        AlertDialog(
            onDismissRequest = { showDailyLetter = false },
            title = {
                Text("A letter from Lexora", color = if (isDark) DarkForeground else DeepForest)
            },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your word for today",
                        color = if (isDark) DarkAccent else BrandGreen,
                    )
                    Text(word.word, color = if (isDark) DarkForeground else DeepForest,
                        style = LexiconTextStyles.BrandWordmark)
                    Text(word.definitions.firstOrNull().orEmpty(),
                        color = if (isDark) DarkForeground else DeepForest)
                    Text("A quiet little discovery from the countryside library.",
                        color = if (isDark) DarkForeground.copy(alpha = 0.7f) else DeepForest.copy(alpha = 0.7f))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDailyLetter = false
                    viewModel.navigate(ScreenRoute.WORD_OF_THE_DAY, word = word)
                }) { Text("Explore the word") }
            },
            dismissButton = {
                TextButton(onClick = { showDailyLetter = false }) { Text("Close") }
            },
            containerColor = if (isDark) DarkCard else CardSurface,
        )
    }
}
