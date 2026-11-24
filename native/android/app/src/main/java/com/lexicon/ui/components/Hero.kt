package com.lexicon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.WarmCream
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkSage
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold

@Composable
fun Hero(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    sharedNightScene: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val headlineMain = if (isDark) DarkForeground else DeepForest
    val headlineWords = if (isDark) DarkSage else BrandGreen
    val sprigColor = if (isDark) DarkAccent else OldWoodGold
    val subtextColor = if (isDark) Color(0xFFD3C6B2) else DeepForest.copy(alpha = 0.90f)
    val configuration = LocalConfiguration.current
    val largeText = configuration.fontScale > 1.2f

    // Use the actual content width, including split-screen and tablet windows.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        val expanded = maxWidth >= 600.dp
        val heroHeight = when {
            expanded && largeText -> 425.dp
            expanded -> 365.dp
            largeText -> 330.dp
            compact -> if (isDark) 250.dp else 290.dp
            else -> if (isDark) 242.dp else 278.dp
        }
        val textWidth = when {
            isDark && largeText -> 0.70f
            isDark -> 0.51f
            compact || largeText -> 0.60f
            else -> 0.52f
        }
        val headlineStyle = when {
            expanded -> LexiconTextStyles.HeroHeadline.copy(fontSize = 36.sp, lineHeight = 42.sp)
            isDark && !largeText -> LexiconTextStyles.HeroHeadline.copy(fontSize = if (compact) 24.sp else 25.sp, lineHeight = 29.sp)
            else -> LexiconTextStyles.HeroHeadline
        }
        val paragraphStyle = if (isDark && !largeText) {
            LexiconTextStyles.HeroParagraph.copy(fontSize = 10.5.sp, lineHeight = 14.5.sp)
        } else LexiconTextStyles.HeroParagraph

        Box(
            modifier = Modifier.fillMaxWidth()
                .then(if (isDark) Modifier.heightIn(min = heroHeight) else Modifier.height(heroHeight)),
        ) {
            if (isDark) {
                if (!sharedNightScene) {
                    HomeNightScene(Modifier.matchParentSize())
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.hero_light),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth(if (compact) 0.82f else 0.88f)
                        .height(heroHeight),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth(0.60f)
                        .height(heroHeight)
                        .background(
                            Brush.horizontalGradient(
                                0.00f to WarmCream.copy(alpha = 0.94f),
                                0.42f to WarmCream.copy(alpha = 0.72f),
                                1.00f to Color.Transparent,
                            ),
                        ),
                )
            }

            val heroCopy: @Composable (Modifier) -> Unit = { copyModifier ->
                Column(
                    modifier = copyModifier,
                    verticalArrangement = Arrangement.spacedBy(if (isDark) 6.dp else 8.dp),
                ) {
                    Column(modifier = Modifier.sectionHeading(1)) {
                        Text(
                            text = "Discover the",
                            color = headlineMain,
                            style = headlineStyle,
                        )
                        Text(
                            text = "World of",
                            color = headlineMain,
                            style = headlineStyle,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Words",
                                color = headlineWords,
                                style = headlineStyle,
                            )
                            Spacer(Modifier.width(8.dp))
                            LeafSprig(
                                color = sprigColor,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }

                    Text(
                        text = "A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word.",
                        color = subtextColor,
                        style = paragraphStyle,
                        modifier = Modifier.fillMaxWidth(if (isDark && largeText) 1f else 0.88f),
                        maxLines = if (isDark) Int.MAX_VALUE else if (largeText) 8 else 7,
                    )
                }
            }

            if (isDark) {
                // Native vertical flow reserves real space for the full paragraph.
                // Enlarged text increases the hero instead of hiding behind Search.
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = heroHeight).padding(bottom = 4.dp),
                    // Exactly two children: extra space goes between copy and search,
                    // while the copy's bottom padding guarantees the minimum gap.
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    heroCopy(
                        Modifier.fillMaxWidth(textWidth)
                            .padding(start = 20.dp, top = if (largeText) 8.dp else 15.dp, bottom = 10.dp),
                    )
                    FloatingSearchBar(
                        viewModel = viewModel,
                        sceneIntegrated = true,
                        modifier = Modifier.padding(start = 20.dp)
                            .fillMaxWidth(if (largeText) 0.85f else 0.73f),
                    )
                }
            } else {
                heroCopy(
                    Modifier.align(Alignment.TopStart).fillMaxWidth(textWidth)
                        .padding(start = 18.dp, top = 4.dp),
                )
                FloatingSearchBar(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 2.dp)
                        .fillMaxWidth(if (compact) 0.90f else 0.75f),
                )
            }
        }
    }
}

// The painting belongs to the masthead, not the text or search layout.
// Home supplies a layer spanning the hero and the trending controls.
@Composable
fun HomeNightScene(modifier: Modifier = Modifier, extendsBehindTags: Boolean = false) {
    val largeText = LocalConfiguration.current.fontScale > 1.2f
    val painting = painterResource(R.drawable.hero_dark)
    val grade = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.70f, 0.10f, 0.02f, 0f, 0f,
        0.04f, 0.79f, 0.03f, 0f, 0f,
        0.04f, 0.11f, 0.81f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )))
    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val intrinsic = painting.intrinsicSize
        val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f)
            intrinsic.width / intrinsic.height else 1.5f
        val artWidth = minOf(size.width * (if (largeText) 0.96f else 1.08f),
            size.height * aspect * 1.08f)
        val artHeight = artWidth / aspect
        val artX = maxOf(size.width * 0.63f - artWidth * 0.54f,
            size.width - artWidth * 0.95f)
        // Source landmarks: moon ~54%,17%; book center ~62%,69%.
        // Reserve the extra tag area for the fading foreground, not extra sky.
        val moonY = size.height * (if (extendsBehindTags) 0.075f else 0.09f)
        val artY = if (largeText) size.height * 0.90f - artHeight
            else moonY - artHeight * 0.17f
        clipRect {
            translate(left = artX, top = artY) {
                with(painting) { draw(size = Size(artWidth, artHeight), colorFilter = grade) }
            }
        }

        // Two-dimensional soft shadow around the reading column, with a
        // stronger reveal at the lower foreground. Only artwork alpha changes.
        val readingEdge = if (largeText) 0.66f else if (size.width < 360.dp.toPx()) 0.49f else 0.45f
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                (readingEdge - 0.24f) to Color.Transparent,
                (readingEdge - 0.11f) to Color.Black.copy(alpha = 0.18f),
                readingEdge to Color.Black.copy(alpha = 0.60f),
                (readingEdge + 0.13f) to Color.Black.copy(alpha = 0.96f),
                0.84f to Color.Black,
                0.96f to Color.Black.copy(alpha = 0.83f),
                1f to Color.Black.copy(alpha = 0.73f),
            ),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.035f to Color.Black.copy(alpha = 0.54f),
                0.085f to Color.Black.copy(alpha = 0.86f),
                0.35f to Color.Black.copy(alpha = 0.81f),
                0.58f to Color.Black,
                0.78f to Color.Black.copy(alpha = 0.96f),
                0.91f to Color.Black.copy(alpha = 0.57f),
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }
}
