package com.lexicon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.Article
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.components.ArticleCard
import com.lexicon.ui.components.BottomNav
import com.lexicon.ui.components.CategoryCard
import com.lexicon.ui.components.Hero
import com.lexicon.ui.components.LexiconHeader
import com.lexicon.ui.components.SectionHeader
import com.lexicon.ui.components.TrendingTagRow
import com.lexicon.ui.components.WordOfTheDayCard
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.WarmGrey
import com.lexicon.ui.theme.WarmCream

@Composable
fun HomeScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bg = if (state.isDark) DarkBackground else WarmCream

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LexiconHeader(viewModel = viewModel)
            HomeFeed(viewModel = viewModel)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            BottomNav(viewModel = viewModel)
        }
    }
}

@Composable
private fun HomeFeed(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var libraryOpen by remember { mutableStateOf(false) }
    var articleToRead by remember { mutableStateOf<Article?>(null) }
    val ink = if (state.isDark) DarkForeground else DeepForest
    val secondaryInk = if (state.isDark) DarkMutedForeground else WarmGrey
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 86.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "hero") {
            Hero(viewModel = viewModel)
        }

        item(key = "tags") {
            TrendingTagRow(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "wotd") {
            WordOfTheDayCard(
                word = LexiconData.featuredWord(),
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // All 4 Category cards in an equal-width 4-column Row — ZERO clipping on "Science"!
        item(key = "categories") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Explore by Category",
                    actionLabel = "View all",
                    divider = false,
                    onAction = { viewModel.setActiveTab(TabKey.DICTIONARY) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LexiconData.categories.forEach { category ->
                        CategoryCard(
                            category = category,
                            viewModel = viewModel,
                            modifier = Modifier.width(94.dp),
                        )
                    }
                }
            }
        }

        item(key = "library") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "From the Library",
                    actionLabel = "View all articles",
                    divider = true,
                    onAction = { articleToRead = null; libraryOpen = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    LexiconData.articles.forEach { article ->
                        ArticleCard(
                            article = article,
                            viewModel = viewModel,
                            onOpen = { articleToRead = article; libraryOpen = true },
                        )
                    }
                }
            }
        }
    }

    // The library is a reader sheet, not a new navigation page.
    if (libraryOpen) {
        val selected = articleToRead
        AlertDialog(
            onDismissRequest = { libraryOpen = false },
            title = {
                Text(
                    selected?.title ?: "From the Library",
                    color = ink,
                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 21.sp),
                )
            },
            text = {
                Column(
                    Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    if (selected == null) {
                        Text(
                            "Three little journeys through language and time.",
                            color = secondaryInk,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                        )
                        LexiconData.articles.forEach { article ->
                            Column(
                                Modifier.fillMaxWidth().clickable(role = Role.Button) {
                                    articleToRead = article
                                }.padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(article.title, color = ink,
                                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 17.sp))
                                Text(article.excerpt, color = secondaryInk,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp))
                            }
                        }
                    } else {
                        Text(
                            selected.excerpt,
                            color = secondaryInk,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp),
                        )
                        LexiconData.articleReading(selected.id).forEach { paragraph ->
                            Text(
                                paragraph,
                                color = ink,
                                style = TextStyle(
                                    fontFamily = BodyFontFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 23.sp,
                                ),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { libraryOpen = false }) { Text("Close") }
            },
            dismissButton = {
                if (selected != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(onClick = { viewModel.toggleArticleBookmark(selected.id) }) {
                            Text(if (selected.id in state.bookmarkedArticleIds) "Saved" else "Save story")
                        }
                        TextButton(onClick = { articleToRead = null }) { Text("All articles") }
                    }
                }
            },
            containerColor = if (state.isDark) DarkCard else CardSurface,
        )
    }
}
