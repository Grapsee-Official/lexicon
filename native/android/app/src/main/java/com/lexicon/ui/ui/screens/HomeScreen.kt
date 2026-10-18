package com.lexicon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.components.ArticleCard
import com.lexicon.ui.components.BottomNav
import com.lexicon.ui.components.CategoryCard
import com.lexicon.ui.components.FloatingSearchBar
import com.lexicon.ui.components.Hero
import com.lexicon.ui.components.LexiconHeader
import com.lexicon.ui.components.SectionHeader
import com.lexicon.ui.components.TrendingTagRow
import com.lexicon.ui.components.WordOfTheDayCard
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.WarmCream

/**
 * HomeScreen — composes the entire home feed (header + content + bottom nav).
 *
 * Mirrors the web's src/app/page.tsx composition: Header on top, main content
 * in a scrollable Column with bottom padding to clear the floating BottomNav.
 */
@Composable
fun HomeScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsState()
    val bg = if (state.isDark) DarkBackground else WarmCream

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        // Main scrollable column (header + content)
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            LexiconHeader(viewModel = viewModel)

            when (state.activeTab) {
                TabKey.HOME -> HomeFeed(viewModel)
                TabKey.DICTIONARY -> PlaceholderTab("Dictionary", "Browse the full lexicon")
                TabKey.COLLECTIONS -> PlaceholderTab("Collections", "Your saved words")
                TabKey.MORE -> PlaceholderTab("Settings", "Make Lexicon yours")
                TabKey.SEARCH -> HomeFeed(viewModel) // never reaches here; FAB opens search
            }
        }

        // BottomNav — floating overlay anchored to the bottom
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
        contentPadding = PaddingValues(bottom = 96.dp), // clear floating nav
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Hero(viewModel = viewModel) }

        // Search bar
        item {
            FloatingSearchBar(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Trending tags
        item {
            TrendingTagRow(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Word of the Day
        item {
            WordOfTheDayCard(
                word = LexiconData.wordOfTheDay,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Categories — no divider
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "Explore by Category",
                    actionLabel = "View all",
                    divider = false,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LexiconData.categories.forEach { c ->
                        CategoryCard(category = c, viewModel = viewModel)
                    }
                }
            }
        }

        // Library — no divider
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    title = "From the Library",
                    actionLabel = "View all articles",
                    divider = false,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LexiconData.articles.forEach { a ->
                        ArticleCard(article = a, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/** Minimal stub for the secondary tabs (Dictionary / Collections / More). */
@Composable
private fun PlaceholderTab(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}
