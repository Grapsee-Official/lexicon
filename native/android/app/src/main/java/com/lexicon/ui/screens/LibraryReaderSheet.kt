package com.lexicon.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexicon.data.Article
import com.lexicon.data.LexiconData
import com.lexicon.ui.components.CountrysideQuoteBanner
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.WarmGrey

/**
 * The Library is a native illustrated reading destination, not a material
 * alert box. A single scrollable sheet adapts to narrow phones and tablets
 * while retaining readable paragraph measure, scene art and back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryReaderSheet(
    selectedArticle: Article?,
    articles: List<Article>,
    bookmarkedArticleIds: Set<String>,
    isDark: Boolean,
    onSelect: (Article) -> Unit,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ink = if (isDark) DarkForeground else DeepForest
    val secondary = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else BrandGreen
    val surface = if (isDark) DarkCard else CardSurface
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = surface,
        scrimColor = Color.Black.copy(alpha = 0.38f),
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = accent.copy(alpha = 0.40f)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedArticle != null) {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = accent)
                        Spacer(Modifier.width(5.dp))
                        Text("All articles", color = accent)
                    }
                } else {
                    Text(
                        "From the Library",
                        color = ink,
                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 23.sp),
                    )
                }
                TextButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close library", tint = accent)
                }
            }

            if (selectedArticle == null) {
                Text(
                    "Three little journeys through language and time.",
                    color = secondary,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                )
                // The Library alone opens with a generous painted bookplate.
                // Tool pages stay focused on their content and interactions.
                CountrysideQuoteBanner(
                    quote = "A collection of words is a collection of worlds.",
                    illustrationBase = "ill_banner_bridge",
                    isDark = isDark,
                )
                articles.forEach { article ->
                    val shape = RoundedCornerShape(15.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (isDark) Color(0xFF1A2532) else Color(0xFFFFFBF5))
                            .border(1.dp, accent.copy(alpha = 0.15f), shape)
                            .clickable(role = Role.Button) { onSelect(article) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val drawable = if (isDark) "${article.thumbnail}_dark" else article.thumbnail
                        val id = context.resources.getIdentifier(drawable, "drawable", context.packageName)
                            .takeIf { it != 0 }
                            ?: context.resources.getIdentifier(article.thumbnail, "drawable", context.packageName)
                        if (id != 0) {
                            Image(
                                painter = painterResource(id),
                                contentDescription = null,
                                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(10.dp)),
                                // Watercolor transparent borders are part of the painting.
                                contentScale = ContentScale.Fit,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                article.title,
                                color = ink,
                                style = TextStyle(
                                    fontFamily = DisplayFontFamily,
                                    fontSize = 17.sp,
                                    lineHeight = 22.sp,
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${article.readMinutes} min read",
                                color = secondary,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                            )
                        }
                    }
                }
            } else {
                Text(
                    selectedArticle.title,
                    color = ink,
                    style = TextStyle(
                        fontFamily = DisplayFontFamily,
                        fontSize = 29.sp,
                        lineHeight = 35.sp,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${selectedArticle.readMinutes} min read",
                            color = secondary,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                        )
                    }
                    TextButton(onClick = { onToggleBookmark(selectedArticle.id) }) {
                        Icon(
                            if (selectedArticle.id in bookmarkedArticleIds)
                                Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = accent,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (selectedArticle.id in bookmarkedArticleIds) "Saved" else "Save story",
                            color = accent,
                        )
                    }
                }
                val drawable = selectedArticle.thumbnail +
                    if (isDark) "_dark" else ""
                val id = context.resources.getIdentifier(drawable, "drawable", context.packageName)
                    .takeIf { it != 0 }
                    ?: context.resources.getIdentifier(selectedArticle.thumbnail, "drawable", context.packageName)
                if (id != 0) {
                    Image(
                        painter = painterResource(id),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(175.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(
                    selectedArticle.excerpt,
                    color = secondary,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 25.sp,
                    ),
                )
                LexiconData.articleReading(selectedArticle.id).forEach { paragraph ->
                    Text(
                        paragraph,
                        color = ink,
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                        ),
                    )
                }
            }
        }
    }
}
