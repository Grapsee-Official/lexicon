package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.Article
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.WarmGrey

@Composable
fun ArticleCard(
    article: Article,
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val ctx = LocalContext.current
    val thumbName = if (isDark) "${article.thumbnail}_dark" else article.thumbnail
    val resId = ctx.resources.getIdentifier(thumbName, "drawable", ctx.packageName)
        .takeIf { it != 0 }
        ?: ctx.resources.getIdentifier(article.thumbnail, "drawable", ctx.packageName)

    val cardBg = if (isDark) DarkCard else CardSurface
    val borderColor = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.10f)
    val titleColor = if (isDark) DarkForeground else DeepForest
    val metaColor = if (isDark) DarkMutedForeground else WarmGrey
    val iconColor = if (isDark) DarkAccent else OldWoodGold
    val shape = RoundedCornerShape(13.dp)

    Row(
        modifier = modifier
            .width(196.dp)
            .height(94.dp)
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
            .clickable(role = Role.Button) { },
    ) {
        Box(
            modifier = Modifier
                .weight(0.36f)
                .fillMaxHeight()
                .padding(5.dp)
                .clip(RoundedCornerShape(9.dp)),
        ) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Column(
            modifier = Modifier
                .weight(0.64f)
                .fillMaxHeight()
                .padding(start = 6.dp, end = 10.dp, top = 9.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = article.title,
                color = titleColor,
                style = LexiconTextStyles.ArticleTitle,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${article.readMinutes} min read",
                        color = metaColor,
                        style = LexiconTextStyles.ArticleMeta,
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
