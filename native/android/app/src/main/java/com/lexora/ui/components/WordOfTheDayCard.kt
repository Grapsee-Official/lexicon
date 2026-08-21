package com.lexora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexora.R
import com.lexora.data.LexoraViewModel
import com.lexora.data.WordEntry
import com.lexora.ui.AccessibilityAudit
import com.lexora.ui.enforceTouchTarget
import com.lexora.ui.iconButton
import com.lexora.ui.liveAnnouncement
import com.lexora.ui.theme.BrandGreen
import com.lexora.ui.theme.DarkBackground
import com.lexora.ui.theme.LexoraTextStyles
import com.lexora.ui.theme.WotdRightSurface

/**
 * WordOfTheDayCard — STRICT 2-COLUMN split (40% left / 60% right).
 *
 *   LEFT (40%):  solid BrandGreen bg, white/cream text.
 *                - "WORD OF THE DAY" badge (single line, no wrap)
 *                - word title + small volume button
 *                - phonetic (italic)
 *                - part of speech (lowercase)
 *
 *   RIGHT (60%): solid cream surface, BrandGreen text.
 *                - definition (Lora font)
 *                - "Explore more →" right-aligned
 *                - watercolour image absolutely pinned to top/bottom/right,
 *                  45% width, masked by a horizontal gradient
 *                  (transparent 0% → opaque 35%) to blend into the cream.
 *
 * Card height 140dp.
 */
@Composable
fun WordOfTheDayCard(
    word: WordEntry,
    viewModel: LexoraViewModel,
    modifier: Modifier = Modifier,
) {
    val isDark = viewModel.uiState.value.isDark
    val rightBg = if (isDark) DarkBackground else WotdRightSurface
    val imageRes = if (isDark) R.drawable.wotd_bg_dark else R.drawable.wotd_bg_light

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button) { viewModel.openWord(word) },
    ) {
        // ===================== LEFT PANEL (40%) =====================
        Column(
            modifier = Modifier
                .weight(0.40f)
                .fillMaxHeight()
                .background(BrandGreen)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // WOTD label — single line (whitespace-nowrap equivalent: maxLines=1)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(10.dp),
                )
                Text(
                    text = "WORD OF THE DAY",
                    color = Color.White.copy(alpha = 0.8f),
                    style = LexoraTextStyles.WotdLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Word + volume button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = word.word,
                    color = Color.White,
                    style = LexoraTextStyles.WotdWord,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        // Live region: when the WOTD rotates (future daily-refresh
                        // feature), TalkBack announces the new word automatically
                        // without the user having to swipe back to the card.
                        .liveAnnouncement(),
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .iconButton(AccessibilityAudit.Descriptions.Volume)
                        .enforceTouchTarget()
                        .clickable(role = Role.Button) { /* TODO: play pronunciation */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = word.phonetic,
                color = Color.White.copy(alpha = 0.6f),
                style = LexoraTextStyles.WotdPhonetic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = word.partOfSpeech.label(),
                color = Color.White.copy(alpha = 0.7f),
                style = LexoraTextStyles.WotdPartOfSpeech,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ===================== RIGHT PANEL (60%) =====================
        // Box with the masked image absolutely pinned right, and a Row on top
        // holding the text (70% width) and an empty spacer (30% width).
        Box(
            modifier = Modifier
                .weight(0.60f)
                .fillMaxHeight()
                .background(rightBg),
        ) {
            // Image — 45% width, full height, pinned to right edge.
            // Masked horizontally: transparent at left edge → opaque at 35%.
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0.0f to Color.Transparent,
                                0.35f to Color.Black,
                                1.0f to Color.Black,
                                startX = 0f,
                                endX = size.width,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
            )

            // Foreground text — Row holding a 70%-width text column + 30% spacer.
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.70f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = word.definitions.first(),
                        color = BrandGreen,
                        style = LexoraTextStyles.WotdDefinition,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Explore more",
                            color = BrandGreen,
                            style = LexoraTextStyles.WotdExploreMore,
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                // Empty 30% spacer — the image visually overlays this area.
                Spacer(Modifier.weight(0.30f))
            }
        }
    }
}
