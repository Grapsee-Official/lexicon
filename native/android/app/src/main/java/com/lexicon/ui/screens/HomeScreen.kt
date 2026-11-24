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
import androidx.compose.ui.unit.Dp
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
import com.lexicon.ui.components.referenceHeroStacks
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
    // Sampled from the unpainted x=7..16 gutter of the 591x1280
    // reference. Register the vertical pigment to the content width so
    // rotation, tablets and long displays do not relocate the midnight sky.
    val background = if (state.isDark) Modifier.drawBehind {
        val pageWidth = minOf(size.width, 640.dp.toPx())
        val referenceHeight = maxOf(1f, pageWidth * 1280f / 591f)
        drawRect(Brush.verticalGradient(
            0.00f to Color(0xFF050F18),
            0.075f to Color(0xFF061019),
            0.117f to Color(0xFF09121B),
            0.219f to Color(0xFF040E17),
            0.312f to Color(0xFF030D17),
            0.387f to Color(0xFF050F18),
            0.570f to Color(0xFF09121B),
            0.844f to Color(0xFF08111A),
            1.00f to Color(0xFF08111A),
            startY = 0f, endY = referenceHeight,
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
      BoxWithConstraints(Modifier.widthIn(max = 640.dp).fillMaxSize().clipToBounds()) {
        val stackedHero = referenceHeroStacks(maxWidth.value, LocalConfiguration.current.fontScale)
        // Render the painting once in the PAGE coordinate space. Its upper
        // sky can cross the header/feed boundary; it is not clipped by LazyColumn.
        // Its translation tracks the masthead, while header controls stay fixed.
        if (state.isDark && !stackedHero && headerHeightPx > 0 && mastheadHeightPx > 0 &&
            feedState.firstVisibleItemIndex == 0) {
            HomeNightScene(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, -feedState.firstVisibleItemScrollOffset) }
                    .height(with(density) { (headerHeightPx + mastheadHeightPx).toDp() }),
                extendsBehindTags = true,
                headerInset = with(density) { headerHeightPx.toDp() },
                measuredHeroHeight = with(density) { heroHeightPx.toDp() },
                stackedLayout = false,
            )
        }

        val scrolled = feedState.firstVisibleItemIndex != 0 || feedState.firstVisibleItemScrollOffset > 0
        Column(modifier = Modifier.fillMaxSize()) {
            // Keep the artwork below the fixed header once the feed scrolls.
            val headerSurface = when {
                state.isDark && !scrolled -> Modifier
                state.isDark -> Modifier.background(Brush.verticalGradient(
                    listOf(Color(0xFF050F18), Color(0xFF08111A))))
                else -> Modifier.background(WarmCream)
            }
            Box(Modifier.fillMaxWidth().then(headerSurface)) {
                LexiconHeader(
                    viewModel = viewModel,
                    referencePageWidth = maxWidth,
                    modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
                )
            }
            HomeFeed(
                viewModel = viewModel,
                modifier = Modifier.weight(1f).clipToBounds(),
                feedState = feedState,
                stackedHero = stackedHero,
                pageWidth = maxWidth,
                onMastheadMeasured = { mastheadHeightPx = it },
                onHeroMeasured = { heroHeightPx = it },
            )
            // Real reserved space for navigation, including the raised search seal.
            BottomNav(viewModel = viewModel, referencePageWidth = maxWidth, modifier = Modifier
                .background(if (state.isDark) Color(0xFF08111A) else WarmCream)
                .padding(top = if (state.isDark) maxWidth * (4f / 591f) else 8.dp))
        }
      }
    }
}

@Composable
private fun HomeFeed(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    feedState: LazyListState,
    stackedHero: Boolean,
    pageWidth: Dp,
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
    // Every editorial section shares the reference's 30/591 page inset.
    val pageInset = if (state.isDark) (pageWidth * (30f / 591f)).coerceIn(12.dp, 32.dp) else 16.dp
    val referenceGap = (pageWidth * (20f / 591f)).coerceIn(9.dp, 18.dp)
    LazyColumn(
        state = feedState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = if (state.isDark) pageWidth * (10f / 591f) else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (state.isDark) referenceGap else 11.dp),
    ) {
        item(key = "masthead") {
            // Foreground only. Dark artwork lives behind the entire page.
            Box(Modifier.fillMaxWidth().onSizeChanged { onMastheadMeasured(it.height) }) {
                Column(verticalArrangement = Arrangement.spacedBy(if (state.isDark) 10.dp else 11.dp)) {
                    Hero(viewModel = viewModel, sharedNightScene = state.isDark && !stackedHero,
                        stackedLayout = stackedHero,
                        modifier = Modifier.onSizeChanged { onHeroMeasured(it.height) })
                    TrendingTagRow(
                        viewModel = viewModel,
                        modifier = Modifier.padding(horizontal = pageInset),
                        sceneIntegrated = state.isDark,
                        referencePageWidth = if (state.isDark) pageWidth else null,
                    )
                }
            }
        }

        item(key = "wotd") {
            WordOfTheDayCard(
                word = homeWord,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = pageInset),
            )
        }

        // Ten original bookplates in canonical order; load only visible artwork.
        item(key = "categories") {
            Column(modifier = Modifier.padding(top = if (state.isDark) pageWidth * (10f / 591f) else 0.dp),
                verticalArrangement = Arrangement.spacedBy(if (state.isDark) pageWidth * (12f / 591f) else 9.dp)) {
                SectionHeader(
                    title = "Explore by Category",
                    actionLabel = "View all",
                    divider = false,
                    onAction = { viewModel.setActiveTab(TabKey.DICTIONARY) },
                    modifier = Modifier.padding(horizontal = pageInset),
                    homeEditorial = true,
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val fontScale = LocalConfiguration.current.fontScale
                    val gap = if (state.isDark) (maxWidth * (14f / 591f)).coerceIn(6.dp, 16.dp) else 8.dp
                    val inset = pageInset
                    val count = homeShelfColumns(maxWidth.value, fontScale)
                    // Four full bookplates and a small continuation on the right.
                    val peek = if (state.isDark && fontScale < 1.5f) maxWidth * (21f / 591f) else 0.dp
                    val width = ((maxWidth - inset * 2 - gap * (count - 1) - peek) / count)
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
                                shelfArtworkHeight = (width * (if (state.isDark) 85f / 117f else 0.82f)).coerceIn(46.dp, 96.dp),
                                referenceShelfWidth = if (state.isDark) width else null,
                            )
                        }
                    }
                }
            }
        }

        item(key = "library") {
            Column(modifier = Modifier.padding(top = if (state.isDark) pageWidth * (10f / 591f) else 0.dp),
                verticalArrangement = Arrangement.spacedBy(if (state.isDark) pageWidth * (14f / 591f) else 10.dp)) {
                SectionHeader(
                    title = "From the Library",
                    actionLabel = "View all articles",
                    divider = true,
                    onAction = { articleToRead = null; libraryOpen = true },
                    modifier = Modifier.padding(horizontal = pageInset),
                    homeEditorial = true,
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val scale = LocalConfiguration.current.fontScale
                    val editorial = state.isDark && scale < 1.5f
                    val cardHeight = (maxWidth * (122f / 591f)).coerceIn(68.dp, 132.dp) * scale.coerceIn(1f, 1.4f)
                    val firstWidth = (maxWidth * (240f / 591f)).coerceIn(130.dp, 260.dp)
                    val followingWidth = (maxWidth * (180f / 591f)).coerceIn(112.dp, 200.dp)
                    LazyRow(contentPadding = PaddingValues(horizontal = pageInset),
                        horizontalArrangement = Arrangement.spacedBy(if (editorial) maxWidth * (13f / 591f) else 8.dp)) {
                        items(LexiconData.articles, key = { it.id }) { article ->
                            ArticleCard(article = article, viewModel = viewModel,
                                onOpen = { articleToRead = article; libraryOpen = true },
                                shelfWidth = if (editorial) (if (article.id == "a1") firstWidth else followingWidth) else null,
                                shelfHeight = if (editorial) cardHeight else null)
                        }
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
    widthDp < 290f || (widthDp < 400f && widthDp / fontScale < 275f) -> 3
    else -> 4
}
