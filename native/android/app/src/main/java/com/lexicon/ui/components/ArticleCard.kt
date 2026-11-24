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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.lexicon.R
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.Article
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
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
    shelfWidth: Dp? = null,
    shelfHeight: Dp? = null,
    onOpen: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val isBookmarked = article.id in state.bookmarkedArticleIds

    val ctx = LocalContext.current
    val thumbName = if (isDark) "${article.thumbnail}_dark" else article.thumbnail
    val resId = ctx.resources.getIdentifier(thumbName, "drawable", ctx.packageName)
        .takeIf { it != 0 }
        ?: ctx.resources.getIdentifier(article.thumbnail, "drawable", ctx.packageName)

    val cardBg = if (isDark) Color(0xFF0A131B) else CardSurface
    val borderColor = if (isDark) Color(0xFFB6A486).copy(alpha = 0.14f) else DeepForest.copy(alpha = 0.10f)
    val titleColor = if (isDark) DarkForeground else DeepForest
    val metaColor = if (isDark) DarkMutedForeground else WarmGrey
    val iconColor = if (isDark) DarkAccent else OldWoodGold
    val shape = RoundedCornerShape(if (isDark) 10.dp else 13.dp)
    val fontScale = LocalConfiguration.current.fontScale
    val largeText = fontScale >= 1.5f
    // The illustrated Home shelf is allowed to grow vertically; keep the
    // reference geometry only for ordinary text in a measured viewport.
    val width = shelfWidth ?: if (largeText) (210.dp * (fontScale / 1.5f).coerceIn(1f, 1.45f))
        else if (isDark) 150.dp * fontScale.coerceIn(1f, 1.20f) else 174.dp
    val imageFraction = if (isDark && shelfWidth != null && article.id != "a1") 0.41f else 0.35f
    val height = shelfHeight ?: if (largeText) 132.dp * (fontScale / 1.5f).coerceAtLeast(1f)
        else if (isDark) 78.dp * fontScale.coerceIn(1f, 1.25f) else 90.dp
    val titleSize = shelfWidth?.value?.let { ((it / if (article.id == "a1") 240f else 180f) * 14f).coerceIn(10f, 14f) } ?: 10.5f
    val metaSize = shelfWidth?.value?.let { ((it / if (article.id == "a1") 240f else 180f) * 10f).coerceIn(8f, 10f) } ?: 8f

    Box(
        modifier = modifier
            .width(width)
            .height(height)
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
            .clickable(role = Role.Button) { onOpen() },
    ) {
        if (isDark) {
            val vector = when (article.id) {
                "a1" -> R.drawable.ref_home_library_origin
                "a2" -> R.drawable.ref_home_library_lost
                "a3" -> R.drawable.ref_home_library_nature
                else -> 0
            }
            if (vector != 0) {
                Image(
                    painter = painterResource(vector), contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.align(Alignment.CenterStart)
                        .fillMaxHeight().fillMaxWidth(imageFraction).padding(3.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            } else if (resId != 0) {
                Image(painter = rememberPreparedArtwork(resId, ArtworkTreatment.STORY),
                    contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().fillMaxWidth(imageFraction))
            }
        }
        Row(Modifier.fillMaxSize()) {
            if (isDark) {
                Spacer(Modifier.weight(imageFraction))
            } else {
                Box(
                    modifier = Modifier.weight(0.33f).fillMaxHeight()
                        .padding(5.dp).clip(RoundedCornerShape(9.dp)),
                ) {
                    if (resId != 0) {
                        Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

        Column(
            modifier = Modifier
                .weight(if (isDark) 1f - imageFraction else 0.67f)
                .fillMaxHeight()
                .padding(start = 5.dp, end = if (isDark) 7.dp else 9.dp, top = if (isDark) 8.dp else 10.dp, bottom = if (isDark) 7.dp else 9.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = article.title,
                color = titleColor,
                style = if (isDark && !largeText) LexiconTextStyles.ArticleTitle.copy(fontSize = titleSize.sp, lineHeight = (titleSize * 1.28f).sp) else LexiconTextStyles.ArticleTitle,
                maxLines = if (largeText) 5 else 3,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
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
                        style = if (isDark && !largeText) LexiconTextStyles.ArticleMeta.copy(fontSize = metaSize.sp, lineHeight = (metaSize * 1.25f).sp) else LexiconTextStyles.ArticleMeta,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(3.dp))
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        }
    }
}
