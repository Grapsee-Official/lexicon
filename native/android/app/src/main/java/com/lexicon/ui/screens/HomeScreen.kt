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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Offset
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
            // Reference samples: header/left ink #050F18, lower gap #061019.
            0.00f to Color(0xFF050F18),
            0.28f to Color(0xFF040E17),
            0.57f to Color(0xFF061019),
            1.00f to Color(0xFF08111A),
        )
    ).drawBehind {
        // Diffuse ink wash extends from the header through the painted sky
        // and foreground. No separate image-sized rectangle or hard halo.
        val skyY = size.width * 0.38f
        drawRect(Brush.radialGradient(
            colors = listOf(Color(0xFF192A3C).copy(alpha = 0.085f), Color.Transparent),
            center = Offset(size.width * 0.78f, skyY),
            radius = size.width * 0.76f,
        ))
        drawRect(Brush.radialGradient(
            colors = listOf(Color(0xFF51412C).copy(alpha = 0.025f), Color.Transparent),
            center = Offset(size.width * 0.79f, size.width * 0.70f),
            radius = size.width * 0.46f,
        ))
    } else Modifier.background(WarmCream)

    val feedState = rememberLazyListState()
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableStateOf(0) }
    var mastheadHeightPx by remember { mutableStateOf(0) }
    var heroHeightPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().then(background),
        contentAlignment = Alignment.TopCenter,
    ) {
      Box(Modifier.widthIn(max = 640.dp).fillMaxSize().clipToBounds()) {
        // Render the painting once in the PAGE coordinate space. Its upper
        // sky can cross the header/feed boundary; it is not clipped by LazyColumn.
        // Its translation tracks the masthead, while header controls stay fixed.
        if (state.isDark && headerHeightPx > 0 && mastheadHeightPx > 0 &&
            feedState.firstVisibleItemIndex == 0) {
            HomeNightScene(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, -feedState.firstVisibleItemScrollOffset) }
                    .height(with(density) { (headerHeightPx + mastheadHeightPx).toDp() }),
                extendsBehindTags = true,
                headerInset = with(density) { headerHeightPx.toDp() },
                measuredHeroHeight = with(density) { heroHeightPx.toDp() },
            )
        }

        val scrolled = feedState.firstVisibleItemIndex != 0 || feedState.firstVisibleItemScrollOffset > 0
        Column(modifier = Modifier.fillMaxSize()) {
            // Keep the artwork below the fixed header once the feed scrolls.
            Box(Modifier.fillMaxWidth().background(
                if (state.isDark && !scrolled) Color.Transparent
                else if (state.isDark) Color(0xFF050F18) else WarmCream
            )) {
                LexiconHeader(
                    viewModel = viewModel,
                    modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
                )
            }
            HomeFeed(
                viewModel = viewModel,
                modifier = Modifier.weight(1f).clipToBounds(),
                feedState = feedState,
                onMastheadMeasured = { mastheadHeightPx = it },
                onHeroMeasured = { heroHeightPx = it },
            )
            // Real reserved space for navigation, including the raised search seal.
            BottomNav(viewModel = viewModel, modifier = Modifier
                .background(if (state.isDark) Color(0xFF08111A) else WarmCream)
                .padding(top = 8.dp))
        }
      }
    }
}

@Composable
private fun HomeFeed(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    feedState: LazyListState,
    onMastheadMeasured: (Int) -> Unit,
    onHeroMeasured: (Int) -> Unit,
) {
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
        state = feedState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item(key = "masthead") {
            // Foreground only. Dark artwork lives behind the entire page.
            Box(Modifier.fillMaxWidth().onSizeChanged { onMastheadMeasured(it.height) }) {
                Column(verticalArrangement = Arrangement.spacedBy(if (state.isDark) 5.dp else 11.dp)) {
                    Hero(viewModel = viewModel, sharedNightScene = state.isDark,
                        modifier = Modifier.onSizeChanged { onHeroMeasured(it.height) })
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
                modifier = Modifier.padding(horizontal = if (state.isDark) 18.dp else 16.dp),
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
                    modifier = Modifier.padding(horizontal = if (state.isDark) 18.dp else 16.dp),
                    homeEditorial = true,
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val fontScale = LocalConfiguration.current.fontScale
                    val gap = 8.dp
                    val inset = 16.dp
                    val count = homeShelfColumns(maxWidth.value, fontScale)
                    val width = ((maxWidth - inset * 2 - gap * (count - 1)) / count)
                        .coerceAtLeast(56.dp)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = inset),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        items(LexiconData.categories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                viewModel = viewModel,
                                modifier = Modifier.width(width),
                                shelfArtworkHeight = (width * 0.82f).coerceIn(50.dp, 96.dp),
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
                    modifier = Modifier.padding(horizontal = if (state.isDark) 18.dp else 16.dp),
                    homeEditorial = true,
                )
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = if (state.isDark) 18.dp else 16.dp),
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


/** Logical window width and text preference, not physical pixels or model. */
internal fun homeShelfColumns(widthDp: Float, fontScale: Float): Int = when {
    fontScale >= 1.8f -> if (widthDp >= 560f) 3 else 2
    fontScale >= 1.5f -> if (widthDp >= 560f) 4 else 3
    widthDp >= 560f -> 5
    widthDp < 340f && fontScale >= 1.25f -> 3
    else -> 4
}
