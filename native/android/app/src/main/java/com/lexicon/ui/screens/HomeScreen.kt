package com.lexicon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                word = LexiconData.wordOfTheDay,
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
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LexiconData.categories.forEach { category ->
                        CategoryCard(
                            category = category,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f),
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
                        ArticleCard(article = article, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
