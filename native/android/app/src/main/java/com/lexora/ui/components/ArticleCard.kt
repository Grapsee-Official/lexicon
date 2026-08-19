package com.lexora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexora.data.Article
import com.lexora.data.LexoraViewModel
import com.lexora.ui.theme.LexoraTextStyles

/**
 * ArticleCard — HORIZONTAL layout (flex-row equivalent).
 *
 *   Image LEFT  (35% width, full height, object-cover)
 *   Text  RIGHT (65% width, Column justify-between, padding 16dp)
 *
 * Fixed height 120dp. Width 224dp (matches web's w-56).
 * Title line-clamps to 3 lines; read-time + bookmark at bottom.
 */
@Composable
fun ArticleCard(
    article: Article,
    viewModel: LexoraViewModel,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val resId = ctx.resources.getIdentifier(article.thumbnail, "drawable", ctx.packageName)

    Row(
        modifier = modifier
            .width(224.dp)
            .height(120.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* open article */ },
    ) {
        // LEFT: image — 35% width, full height, object-cover
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
        ) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // RIGHT: text — 65% width, Column space-between, padding 16dp
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Title — clamped to 3 lines
            Text(
                text = article.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = LexoraTextStyles.ArticleTitle,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            // Bottom row — read time + bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${article.readMinutes} min read",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = LexoraTextStyles.ArticleMeta,
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = "Bookmark",
                    tint = if (article.bookmarked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
