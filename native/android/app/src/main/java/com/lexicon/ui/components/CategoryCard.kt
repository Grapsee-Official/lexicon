package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.Category
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WarmGrey

/**
 * CategoryCard — Uses Modifier.weight(1f) inside the 4-card Row so ALL FOUR
 * categories (Vocabulary, Geography, Literature, Science) fit 100% side-by-side
 * without clipping "Science", matching the Light & Dark reference PNGs!
 */
@Composable
fun CategoryCard(
    category: Category,
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val ctx = LocalContext.current
    val drawableName = if (isDark) "${category.illustration}_dark" else category.illustration
    val resId = ctx.resources.getIdentifier(drawableName, "drawable", ctx.packageName)
        .takeIf { it != 0 }
        ?: ctx.resources.getIdentifier(category.illustration, "drawable", ctx.packageName)

    val cardBg = if (isDark) DarkCard else CardSurface
    val borderColor = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.10f)
    val titleColor = if (isDark) DarkForeground else DeepForest
    val subColor = if (isDark) DarkMutedForeground else WarmGrey
    val shape = RoundedCornerShape(13.dp)

    Column(
        modifier = modifier
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = DeepForest.copy(alpha = 0.10f),
                        spotColor = DeepForest.copy(alpha = 0.10f),
                    )
                } else Modifier
            )
            .clip(shape)
            .background(cardBg)
            .border(1.dp, borderColor, shape)
            .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.DICTIONARY) }
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = "${category.title} illustration",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(9.dp)),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.5.dp),
        ) {
            Text(
                text = category.title,
                color = titleColor,
                style = LexiconTextStyles.CategoryTitle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = category.subtitle,
                color = subColor,
                style = LexiconTextStyles.CategorySubtitle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
