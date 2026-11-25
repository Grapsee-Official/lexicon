package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
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
    val fontScale = config.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
    val compact = maxWidth < 328.dp
    val accessible = fontScale >= 1.5f || maxWidth.value / fontScale.coerceAtLeast(1f) < 250f
    val definition = word.definitions.firstOrNull().orEmpty()
    val editorial = !accessible
    val shortDefinition = editorial && definition.length <= 72
    // The reference is a compact editorial spread, not a tall two-panel tile.
    // Give unusually long entries room, while matching the regular card to
    // the reference's proportions at ordinary phone widths.
    val referenceHeight = maxWidth * (175f / 531f)
    val cardHeight = when {
        accessible -> (128.dp * config.fontScale).coerceIn(190.dp, 320.dp)
        definition.length > 105 -> maxOf(136.dp, referenceHeight)
        else -> maxOf(96.dp, referenceHeight) * config.fontScale.coerceIn(1f, 1.25f)
    }

    // Same ink family as the Home night backdrop, not a separate slate tile.
    val leftBg = if (isDark) Color.Transparent else BrandGreen
    val rightBg = if (isDark) Color.Transparent else WotdRightSurface
    val nightInk = Color(0xFF08121A)
    val cardBorder = if (isDark) Color(0xFFB6A486).copy(alpha = 0.17f) else Color(0xFFA58A61).copy(alpha = .28f)
    val labelGold = if (isDark) Color(0xFFD0AD75) else WotdGoldLight
    val wordColor = if (isDark) DarkForeground else Color.White
    val phoneticColor = if (isDark) DarkMutedForeground else Color.White.copy(alpha = 0.78f)
    val posColor = if (isDark) Color(0xFFD0AD75) else WotdGoldLight
    val defColor = if (isDark) DarkForeground.copy(alpha = 0.90f) else DeepForest.copy(alpha = 0.88f)
    val exploreColor = if (isDark) Color(0xFFD0AD75) else BrandGreen
    // Fit ordinary long words beside the speaker icon without splitting them
    // into unnatural fragments on narrow phones (e.g. "Wanderlu" / "st").
    val wordSize = when {
        word.word.length >= 15 -> 12.sp
        word.word.length >= 11 -> if (compact || accessible) 13.sp else 14.sp
        word.word.length >= 9 -> if (editorial) 18.sp else if (compact || accessible) 14.sp else 15.sp
        else -> if (compact || accessible) 17.sp else 20.sp
    }

    val shape = RoundedCornerShape(if (isDark) 12.dp else 16.dp)

    if (accessible) {
        AccessibleDailyWord(word, viewModel, isDark, definition, cardBorder, labelGold)
        return@BoxWithConstraints
    }
    Box(
        modifier = Modifier
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
            .background(if (isDark) nightInk else rightBg)
            .border(1.dp, cardBorder, shape)
            .clickable(role = Role.Button) { viewModel.navigate(com.lexicon.data.ScreenRoute.WORD_OF_THE_DAY, word = word) },
    ) {
        if (isDark) {
            WordOfDayNightScene(Modifier.matchParentSize(), accessible)
        }
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(if (accessible) 0.43f else 0.39f)
                    .fillMaxHeight()
                    .background(leftBg)
                    .padding(horizontal = if (accessible) 8.dp else 10.dp, vertical = if (editorial) 10.dp else 14.dp),
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
                            .size(if (isDark && !accessible) 23.dp else 28.dp)
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
                        style = if (editorial) LexiconTextStyles.WotdPhonetic.copy(fontSize = 9.5.sp) else LexiconTextStyles.WotdPhonetic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = word.partOfSpeech.label(),
                        color = posColor,
                        style = if (editorial) LexiconTextStyles.WotdPartOfSpeech.copy(fontSize = 10.sp) else LexiconTextStyles.WotdPartOfSpeech,
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
                    .weight(if (accessible) 0.57f else 0.61f)
                    .fillMaxHeight()
                    .background(rightBg),
            ) {
                if (!isDark) {
                    Image(
                        painter = rememberCountrysidePainter(resolveCountrysideDrawable("wotd_bg_light", false, "home-daily")),
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
                        .fillMaxWidth(if (isDark && accessible) 1f else if (editorial) 0.79f else 0.69f)
                        .padding(start = 13.dp, top = if (isDark && !accessible) 11.dp else 13.dp, bottom = if (isDark && !accessible) 10.dp else 12.dp, end = 4.dp),
                    verticalArrangement = if (shortDefinition) Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                        else Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = definition,
                        color = defColor,
                        style = if (editorial) LexiconTextStyles.WotdDefinition.copy(fontSize = 9.5.sp, lineHeight = 13.sp)
                            else LexiconTextStyles.WotdDefinition,
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
                            style = if (editorial) LexiconTextStyles.WotdExploreMore.copy(fontSize = 9.5.sp)
                                else LexiconTextStyles.WotdExploreMore,
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
    }
}

/** A full-width reading layout for enlarged text or very narrow windows.
 * The landscape has its own aspect-correct band; all text is live and wraps. */
@Composable
private fun AccessibleDailyWord(
    word: WordEntry, viewModel: LexiconViewModel, isDark: Boolean,
    definition: String, border: Color, gold: Color,
) {
    val shape = RoundedCornerShape(if (isDark) 12.dp else 16.dp)
    val ink = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else DeepForest.copy(alpha = 0.8f)
    val accent = if (isDark) gold else BrandGreen
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (isDark) Color(0xFF08121A) else WotdRightSurface)
            .border(1.dp, border, shape)
            .clickable(role = Role.Button) { viewModel.navigate(com.lexicon.data.ScreenRoute.WORD_OF_THE_DAY, word = word) },
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("WORD OF THE DAY", color = accent, style = LexiconTextStyles.WotdLabel)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(word.word, color = ink, style = LexiconTextStyles.WotdWord,
                    modifier = Modifier.weight(1f).liveAnnouncement())
                Box(Modifier.size(48.dp).iconButton(AccessibilityAudit.Descriptions.Volume)
                    .clickable(role = Role.Button) { viewModel.pronounce(word.word) },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Outlined.VolumeUp, null, tint = accent)
                }
            }
            Text(word.phonetic, color = muted, style = LexiconTextStyles.WotdPhonetic)
            Text(word.partOfSpeech.label(), color = accent, style = LexiconTextStyles.WotdPartOfSpeech)
            Text(definition, color = ink, style = LexiconTextStyles.WotdDefinition)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Explore more", color = accent, style = LexiconTextStyles.WotdExploreMore)
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = accent, modifier = Modifier.size(14.dp))
            }
        }
        if (isDark) {
            WordOfDayNightScene(Modifier.fillMaxWidth().height(128.dp), false, fullWidth = true)
        } else {
            Image(rememberCountrysidePainter(resolveCountrysideDrawable("wotd_bg_light", false, "home-daily")), null,
                Modifier.fillMaxWidth().height(128.dp), contentScale = ContentScale.Fit,
                alignment = Alignment.CenterEnd)
        }
    }
}

// One painting covers the full card. The definition and the word are native
// foreground controls; the divider no longer separates two painted surfaces.
@Composable
private fun WordOfDayNightScene(modifier: Modifier, accessible: Boolean, fullWidth: Boolean = false) {
    var sceneWidthPx by remember { mutableIntStateOf(0) }
    val fraction = if (fullWidth) 1f else if (accessible) 0.57f else 0.61f
    val artwork = rememberReferenceVector(R.drawable.ref_home_daily,
        (sceneWidthPx * fraction).roundToInt(), 323, 174)
    Canvas(modifier.onSizeChanged { sceneWidthPx = it.width }) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val width = size.width * fraction
        // Scale the drawing uniformly, align the tree and path to the lower
        // right, and clip only to the intended landscape column.
        val artWidth = maxOf(width, size.height * 323f / 174f)
        val artHeight = artWidth * 174f / 323f
        clipRect(left = size.width - width) {
            translate(left = size.width - artWidth, top = size.height - artHeight) {
                with(artwork) { draw(size = Size(artWidth, artHeight)) }
            }
        }
    }
}
