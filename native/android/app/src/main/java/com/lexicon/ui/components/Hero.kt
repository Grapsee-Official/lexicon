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
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val headlineMain = if (isDark) DarkForeground else DeepForest
    val headlineWords = if (isDark) DarkSage else BrandGreen
    val sprigColor = if (isDark) DarkAccent else OldWoodGold
    val subtextColor = if (isDark) Color(0xFFD3C6B2) else DeepForest.copy(alpha = 0.90f)
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 360
    val expanded = configuration.screenWidthDp >= 600
    val largeText = configuration.fontScale > 1.2f
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
        modifier = modifier
            .fillMaxWidth()
            .then(if (isDark) Modifier.heightIn(min = heroHeight) else Modifier.height(heroHeight)),
    ) {
        if (isDark) {
            // The original illustration remains unmodified. Compose only its
            // painted pixels over the shared Home night, never an opaque tile.
            val painting = painterResource(R.drawable.hero_dark)
            val nightGrade = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                0.77f, 0.12f, 0.03f, 0f, 0f,
                0.05f, 0.85f, 0.03f, 0f, 0f,
                0.05f, 0.13f, 0.82f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )))
            val readingEdge = if (largeText) 0.61f else if (compact) 0.51f else 0.45f
            // DstIn changes the painting's alpha, not the page below it.
            // The isolated layer is essential: the same night continues under
            // the header, the book, the search capsule and the feed.
            Canvas(
                Modifier.matchParentSize().graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            ) {
                val intrinsic = painting.intrinsicSize
                val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f) {
                    intrinsic.width / intrinsic.height
                } else 1.5f
                val artWidth = if (largeText) {
                    minOf(size.width * 0.91f, size.height * aspect)
                } else {
                    minOf(size.width * 1.09f, size.height * aspect * 1.14f)
                }
                val artHeight = artWidth / aspect
                val x = maxOf(size.width * 0.63f - artWidth * 0.54f,
                    size.width - artWidth * 0.94f)
                val y = if (largeText) size.height - artHeight else
                    size.height * 0.065f - artHeight * 0.17f
                clipRect {
                    translate(left = x, top = y) {
                        with(painting) {
                            draw(size = Size(artWidth, artHeight), colorFilter = nightGrade)
                        }
                    }
                }
                // A wide, continuous mask opens into the distant trees,
                // preserves the book and turns bright edge foliage into shadow.
                drawRect(
                    brush = Brush.horizontalGradient(
                        0.00f to Color.Transparent,
                        (readingEdge - 0.23f) to Color.Transparent,
                        (readingEdge - 0.10f) to Color.Black.copy(alpha = 0.16f),
                        readingEdge to Color.Black.copy(alpha = 0.58f),
                        (readingEdge + 0.12f).coerceAtMost(0.83f) to Color.Black.copy(alpha = 0.94f),
                        0.82f to Color.Black,
                        0.94f to Color.Black.copy(alpha = 0.78f),
                        1.00f to Color.Black.copy(alpha = 0.54f),
                    ),
                    blendMode = BlendMode.DstIn,
                )
                // Sky and roof are quieter than the illuminated foreground.
                // Keep the warm pages present under the search capsule.
                drawRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.035f to Color.Black.copy(alpha = 0.46f),
                        0.085f to Color.Black.copy(alpha = 0.70f),
                        0.36f to Color.Black.copy(alpha = 0.70f),
                        0.59f to Color.Black.copy(alpha = 0.94f),
                        0.76f to Color.Black,
                        0.90f to Color.Black.copy(alpha = 0.88f),
                        0.97f to Color.Black.copy(alpha = 0.28f),
                        1.00f to Color.Transparent,
                    ),
                    blendMode = BlendMode.DstIn,
                )
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
                    modifier = Modifier.fillMaxWidth(if (isDark && !largeText) 0.94f else 0.88f),
                    maxLines = if (isDark) Int.MAX_VALUE else if (largeText) 8 else 7,
                )
            }
        }

        if (isDark) {
            // Native vertical flow reserves real space for the full paragraph.
            // Enlarged text increases the hero instead of hiding behind Search.
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(min = heroHeight).padding(bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                heroCopy(
                    Modifier.fillMaxWidth(textWidth)
                        .padding(start = 20.dp, top = if (largeText) 8.dp else 15.dp),
                )
                Spacer(Modifier.height(10.dp))
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
