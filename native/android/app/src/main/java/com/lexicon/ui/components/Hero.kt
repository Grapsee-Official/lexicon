package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
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
    val subtextColor = if (isDark) DarkMutedForeground else DeepForest.copy(alpha = 0.78f)
    val heroArt = if (isDark) R.drawable.hero_dark else R.drawable.hero_light

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(238.dp),
    ) {
        Image(
            painter = painterResource(heroArt),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.71f)
                .height(238.dp)
                .offset(x = 8.dp, y = 6.dp)
                .clickable(role = Role.Button) { viewModel.openSearch() },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.50f)
                .padding(start = 18.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.sectionHeading(1)) {
                Text(
                    text = "Discover the",
                    color = headlineMain,
                    style = LexiconTextStyles.HeroHeadline,
                )
                Text(
                    text = "World of",
                    color = headlineMain,
                    style = LexiconTextStyles.HeroHeadline,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Words",
                        color = headlineWords,
                        style = LexiconTextStyles.HeroHeadline,
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
                maxLines = 4,
            )
        }

        FloatingSearchBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 2.dp)
                .fillMaxWidth(0.78f),
        )
    }
}
