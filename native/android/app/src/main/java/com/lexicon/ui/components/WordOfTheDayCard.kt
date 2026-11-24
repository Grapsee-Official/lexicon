package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.WordEntry
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.iconButton
import com.lexicon.ui.liveAnnouncement
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WotdGoldLight
import com.lexicon.ui.theme.WotdRightSurface

@Composable
fun WordOfTheDayCard(
    word: WordEntry,
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val config = LocalConfiguration.current
    val compact = config.screenWidthDp < 360
    val accessible = config.fontScale > 1.2f
    val cardHeight = if (accessible) 186.dp else if (compact) 144.dp else 118.dp

    // Same ink family as the Home night backdrop, not a separate slate tile.
    val leftBg = if (isDark) Color(0xFF0B141C) else BrandGreen
    val rightBg = if (isDark) Color(0xFF0C151D) else WotdRightSurface
    val cardBorder = if (isDark) Color(0xFFB6A486).copy(alpha = 0.19f) else DeepForest.copy(alpha = 0.12f)
    val labelGold = if (isDark) DarkAccent else WotdGoldLight
    val wordColor = if (isDark) DarkForeground else Color.White
    val phoneticColor = if (isDark) DarkMutedForeground else Color.White.copy(alpha = 0.78f)
    val posColor = if (isDark) DarkAccent else WotdGoldLight
    val defColor = if (isDark) DarkForeground.copy(alpha = 0.90f) else DeepForest.copy(alpha = 0.88f)
    val exploreColor = if (isDark) DarkAccent else BrandGreen
    // Fit ordinary long words beside the speaker icon without splitting them
    // into unnatural fragments on narrow phones (e.g. "Wanderlu" / "st").
    val wordSize = when {
        word.word.length >= 15 -> 12.sp
        word.word.length >= 11 -> if (compact || accessible) 13.sp else 14.sp
        word.word.length >= 9 -> if (compact || accessible) 14.sp else 15.sp
        else -> if (compact || accessible) 17.sp else 20.sp
    }

    val shape = RoundedCornerShape(if (isDark) 12.dp else 16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 3.dp,
                        shape = shape,
                        ambientColor = DeepForest.copy(alpha = 0.12f),
                        spotColor = DeepForest.copy(alpha = 0.12f),
                    )
                } else Modifier
            )
            .clip(shape)
            .background(rightBg)
            .border(1.dp, cardBorder, shape)
            .clickable(role = Role.Button) { viewModel.navigate(com.lexicon.data.ScreenRoute.WORD_OF_THE_DAY, word = word) },
    ) {
        Column(
            modifier = Modifier
                .weight(0.39f)
                .fillMaxHeight()
                .background(leftBg)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "WORD OF THE DAY",
                    color = labelGold,
                    style = LexiconTextStyles.WotdLabel,
                    maxLines = 1,
                )
                if (!compact) {
                    LeafSprig(
                        color = labelGold,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = word.word,
                    color = wordColor,
                    style = LexiconTextStyles.WotdWord.copy(fontSize = wordSize),
                    maxLines = if (word.word.length > 14 || accessible) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .liveAnnouncement(),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .iconButton(AccessibilityAudit.Descriptions.Volume)
                        .clickable(role = Role.Button) { viewModel.pronounce(word.word) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = labelGold,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = word.phonetic,
                    color = phoneticColor,
                    style = LexiconTextStyles.WotdPhonetic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = word.partOfSpeech.label(),
                    color = posColor,
                    style = LexiconTextStyles.WotdPartOfSpeech,
                    maxLines = 1,
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(if (isDark) cardBorder else DeepForest.copy(alpha = 0.08f)),
        )

        Box(
            modifier = Modifier
                .weight(0.61f)
                .fillMaxHeight()
                .background(rightBg),
        ) {
            if (isDark) {
                val artwork = painterResource(R.drawable.wotd_home_dark)
                val grade = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.88f) })
                // Isolate the illustration so the alpha masks reveal the card's
                // midnight ink, rather than painting a hard scrim over the art.
                Canvas(Modifier.matchParentSize().graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }) {
                    val intrinsic = artwork.intrinsicSize
                    val aspect = if (intrinsic.width > 0 && intrinsic.height > 0)
                        intrinsic.width / intrinsic.height else 1.5f
                    val width = maxOf(size.width, size.height * aspect)
                    val height = width / aspect
                    clipRect {
                        translate(left = size.width - width, top = (size.height - height) / 2f) {
                            with(artwork) {
                                draw(size = Size(width, height), alpha = 0.94f, colorFilter = grade)
                            }
                        }
                    }
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.18f to Color.Black.copy(alpha = 0.04f),
                            0.38f to Color.Black.copy(alpha = 0.16f),
                            0.56f to Color.Black.copy(alpha = 0.52f),
                            0.76f to Color.Black.copy(alpha = 0.94f),
                            0.95f to Color.Black,
                            1f to Color.Black.copy(alpha = 0.86f),
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.65f),
                            0.12f to Color.Black.copy(alpha = 0.94f),
                            0.78f to Color.Black,
                            1f to Color.Black.copy(alpha = 0.66f),
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.wotd_bg_light),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight().fillMaxWidth(0.96f),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            0.0f to rightBg.copy(alpha = 0.96f),
                            0.38f to rightBg.copy(alpha = 0.72f),
                            0.83f to Color.Transparent,
                        )
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isDark && accessible) 0.88f else 0.69f)
                    .padding(start = 13.dp, top = 13.dp, bottom = 12.dp, end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = word.definitions.firstOrNull().orEmpty(),
                    color = defColor,
                    style = LexiconTextStyles.WotdDefinition,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Explore more",
                        color = exploreColor,
                        style = LexiconTextStyles.WotdExploreMore,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = exploreColor,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
