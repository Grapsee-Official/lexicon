package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.semantics.Role
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
import com.lexicon.ui.theme.DarkMutedForeground
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
    val subtextColor = if (isDark) DarkMutedForeground else DeepForest.copy(alpha = 0.90f)
    val heroArt = if (isDark) R.drawable.hero_dark else R.drawable.hero_light
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 360
    val expanded = configuration.screenWidthDp >= 600
    val largeText = configuration.fontScale > 1.2f
    val heroHeight = when {
        expanded && largeText -> 410.dp
        expanded -> 350.dp
        largeText -> 310.dp
        compact -> 246.dp
        else -> 215.dp
    }
    val textWidth = if (compact || largeText) 0.60f else 0.52f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight),
    ) {
        Image(
            painter = painterResource(heroArt),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(if (compact) 0.65f else 0.71f)
                .height(heroHeight)
                .offset(x = 8.dp, y = 6.dp),
        )

        if (isDark) {
            NocturneAtmosphere(Modifier.fillMaxSize())
        }

        // A soft parchment/night-paper wash preserves readable typography while
        // the original full-detail painting remains visible on the right.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.70f)
                .height(heroHeight)
                .background(
                    Brush.horizontalGradient(
                        0.00f to (if (isDark) DarkBackground else WarmCream).copy(alpha = 0.99f),
                        0.55f to (if (isDark) DarkBackground else WarmCream).copy(alpha = 0.94f),
                        1.00f to Color.Transparent,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(textWidth)
                .padding(start = 18.dp, top = 4.dp),
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
                modifier = Modifier.fillMaxWidth(0.88f),
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
