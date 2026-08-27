package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bell
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Moon
import androidx.compose.material.icons.outlined.Sun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.Terracotta

/**
 * LexiconHeader — Top app bar.
 *
 * Layout (LEFT-aligned per locked web spec):
 *   [hamburger] [logo mark] [Lexicon wordmark + tagline, weight-left]
 *                                            [theme toggle] [bell] [avatar]
 *
 * - Transparent background (flows into hero)
 * - Thin stroke icons (Icons.Outlined)
 * - Tagline "Words. Wisdom. Worlds." — title case, NOT uppercase.
 */
@Composable
fun LexiconHeader(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val isDark = viewModel.uiState.value.isDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Hamburger
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Menu)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { viewModel.openMenu() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }

        // Logo mark (rasterised SVG → PNG in drawable-nodpi)
        Image(
            painter = painterResource(R.drawable.lexicon_mark),
            contentDescription = "Lexicon",
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(Modifier.width(8.dp))

        // Wordmark + tagline (weight-left so the trailing icons push right)
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "Lexicon",
                color = MaterialTheme.colorScheme.onBackground,
                style = LexiconTextStyles.BrandWordmark,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "Words. Wisdom. Worlds.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = LexiconTextStyles.BrandTagline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Theme toggle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .iconButton(if (isDark) "Switch to light mode" else "Switch to dark mode")
                .enforceTouchTarget()
                .clickable(role = Role.Button) { viewModel.toggleTheme() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isDark) Icons.Outlined.Sun else Icons.Outlined.Moon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }

        // Bell with terracotta dot
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Bell)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { /* TODO: open notifications */ },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Bell,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
            // Notification dot (top-right inside the 36dp box)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Terracotta),
            )
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Avatar)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { /* TODO: open profile */ },
        ) {
            Image(
                painter = painterResource(R.drawable.avatar_default),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
