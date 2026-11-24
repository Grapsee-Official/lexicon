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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.BuildConfig
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import com.lexicon.data.Article
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.components.ArticleCard
import com.lexicon.ui.components.BottomNav
import com.lexicon.ui.components.CategoryCard
import com.lexicon.ui.components.Hero
import com.lexicon.ui.components.HomeNightScene
import com.lexicon.ui.components.LexiconHeader
import com.lexicon.ui.components.SectionHeader
import com.lexicon.ui.components.TrendingTagRow
import com.lexicon.ui.components.WordOfTheDayCard
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
    // One page-sized midnight field, rather than a separate hero backdrop.
    val background = if (state.isDark) Modifier.background(
        Brush.verticalGradient(
            0.00f to Color(0xFF08111A),
            0.27f to Color(0xFF040E18),
            0.57f to Color(0xFF09121B),
            1.00f to Color(0xFF08111A),
        )
    ) else Modifier.background(WarmCream)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(background),
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
    val context = LocalContext.current
    // Match the canonical Wanderlust reference during explicit debug captures;
    // ordinary users retain the correctly rotating daily-word feature.
    val isCapture = BuildConfig.DEBUG &&
        ((context as? android.app.Activity)?.intent?.hasExtra("lexicon_screen") == true)
    val homeWord = if (isCapture) LexiconData.wordOfTheDay else LexiconData.featuredWord()
    val libraryFixture = if (isCapture) (context as? android.app.Activity)
        ?.intent?.getStringExtra("lexicon_screen") else null
    var libraryOpen by remember(libraryFixture) {
        mutableStateOf(libraryFixture == "library" || libraryFixture == "library_article")
    }
    var articleToRead by remember(libraryFixture) {
        mutableStateOf<Article?>(
            if (libraryFixture == "library_article") LexiconData.articles.firstOrNull() else null
        )
    }
    val ink = if (state.isDark) DarkForeground else DeepForest
    val secondaryInk = if (state.isDark) DarkMutedForeground else WarmGrey
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item(key = "masthead") {
            // The illustration now spans the headline, search, and tag shelf.
            // The light appearance retains its original independent image.
            Box(Modifier.fillMaxWidth()) {
                if (state.isDark) {
                    HomeNightScene(Modifier.matchParentSize(), extendsBehindTags = true)
                }
                Column(verticalArrangement = Arrangement.spacedBy(if (state.isDark) 5.dp else 11.dp)) {
                    Hero(viewModel = viewModel, sharedNightScene = state.isDark)
                    TrendingTagRow(
                        viewModel = viewModel,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        sceneIntegrated = state.isDark,
                    )
                }
            }
        }

        item(key = "wotd") {
            WordOfTheDayCard(
                word = homeWord,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Ten original bookplates in canonical order; load only visible artwork.
        item(key = "categories") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Explore by Category",
                    actionLabel = "View all",
                    divider = false,
                    onAction = { viewModel.setActiveTab(TabKey.DICTIONARY) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val largeText = LocalConfiguration.current.fontScale > 1.2f
                    val readableWidth = if (largeText) {
                        (maxWidth * 0.43f).coerceIn(150.dp, 220.dp)
                    } else {
                        ((maxWidth - 62.dp) / 4f).coerceIn(if (state.isDark) 78.dp else 94.dp, 116.dp)
                    }
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(LexiconData.categories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                viewModel = viewModel,
                                modifier = Modifier.width(
                                    if (state.isDark && !largeText && category.title.length > 15)
                                        maxOf(readableWidth, 116.dp) else readableWidth
                                ),
                            )
                        }
                    }
                }
            }
        }

        item(key = "library") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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

    // A native illustrated reading sheet replaces the generic Material dialog.
    // Bookmarks persist through the shared view model, and scrollable prose
    // remains accessible on compact phones, large text, and tablets.
    if (libraryOpen) {
        LibraryReaderSheet(
            selectedArticle = articleToRead,
            articles = LexiconData.articles,
            bookmarkedArticleIds = state.bookmarkedArticleIds,
            isDark = state.isDark,
            onSelect = { articleToRead = it },
            onBack = { articleToRead = null },
            onToggleBookmark = { viewModel.toggleArticleBookmark(it) },
            onDismiss = {
                libraryOpen = false
                articleToRead = null
            },
        )
    }

}
