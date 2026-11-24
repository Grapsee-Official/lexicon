package com.lexicon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.WarmCream
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkSage
import com.lexicon.ui.theme.DarkSecondary
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
        compact -> if (isDark) 270.dp else 290.dp
        else -> if (isDark) 258.dp else 278.dp
    }
    val textWidth = if (compact || largeText) 0.60f else if (isDark) 0.49f else 0.52f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight),
    ) {
        if (isDark) {
            // Render the complete source on the page canvas, rather than fitting
            // its transparent padding inside a separate right-hand image box.
            val painting = painterResource(R.drawable.hero_dark)
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(DarkSecondary.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.78f, size.height * 0.48f),
                        radius = maxOf(size.width, size.height) * 0.68f,
                    ),
                )
                val intrinsic = painting.intrinsicSize
                val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f) {
                    intrinsic.width / intrinsic.height
                } else 1.5f
                // The source has transparent outer margins. Deliberate 8%
                // trailing overscan removes the empty strip at the screen edge.
                val artWidth = minOf(
                    size.width * (if (largeText) 0.90f else 1.06f),
                    size.height * aspect * 1.12f,
                )
                val artHeight = artWidth / aspect
                val x = size.width - artWidth * 0.92f
                val y = size.height - artHeight + size.height * 0.035f
                clipRect {
                    translate(left = x, top = y) {
                        with(painting) {
                            draw(size = Size(artWidth, artHeight), alpha = 0.91f)
                        }
                    }
                }
            }
            // Keep the words readable, but allow the distant trees to emerge
            // organically beside them. The book continues behind the search.
            val readingEdge = if (largeText) 0.60f else if (compact) 0.53f else 0.46f
            Box(
                Modifier.matchParentSize().background(
                    Brush.horizontalGradient(
                        0.00f to DarkBackground,
                        0.25f to DarkBackground.copy(alpha = 0.98f),
                        (readingEdge - 0.09f) to DarkBackground.copy(alpha = 0.91f),
                        readingEdge to DarkBackground.copy(alpha = 0.62f),
                        (readingEdge + 0.12f) to DarkBackground.copy(alpha = 0.18f),
                        (readingEdge + 0.22f) to Color.Transparent,
                        1.00f to Color.Transparent,
                    ),
                ),
            )
            // Feather the sky and foreground into the shared page ink.
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        0.00f to DarkBackground,
                        0.045f to DarkBackground.copy(alpha = 0.35f),
                        0.13f to Color.Transparent,
                        0.79f to Color.Transparent,
                        0.92f to DarkBackground.copy(alpha = 0.20f),
                        1.00f to DarkBackground,
                    ),
                ),
            )
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

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(textWidth)
                .padding(start = if (isDark) 22.dp else 18.dp, top = if (isDark && !largeText) 22.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.sectionHeading(1)) {
                Text(
                    text = "Discover the",
                    color = headlineMain,
                    style = if (expanded) LexiconTextStyles.HeroHeadline.copy(
                        fontSize = 36.sp, lineHeight = 42.sp,
                    ) else LexiconTextStyles.HeroHeadline,
                )
                Text(
                    text = "World of",
                    color = headlineMain,
                    style = if (expanded) LexiconTextStyles.HeroHeadline.copy(
                        fontSize = 36.sp, lineHeight = 42.sp,
                    ) else LexiconTextStyles.HeroHeadline,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Words",
                        color = headlineWords,
                        style = if (expanded) LexiconTextStyles.HeroHeadline.copy(
                        fontSize = 36.sp, lineHeight = 42.sp,
                    ) else LexiconTextStyles.HeroHeadline,
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
                style = LexiconTextStyles.HeroParagraph,
                modifier = Modifier.fillMaxWidth(if (isDark && !largeText) 0.84f else 0.88f),
                maxLines = if (largeText) 8 else 7,
            )
        }

        FloatingSearchBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 2.dp)
                .fillMaxWidth(if (compact) 0.90f else 0.75f),
        )
    }
}
