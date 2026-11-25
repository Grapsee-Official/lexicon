package com.lexicon.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.*
import com.lexicon.ui.components.*
import com.lexicon.ui.theme.*

/** Dictionary Home from the supplied Cotswolds watercolor reference. */
@Composable
fun DictionaryHomeScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dark = state.isDark
    val bg = if (dark) DarkBackground else WarmCream
    val card = if (dark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (dark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (dark) DarkForeground else DeepForest
    val muted = if (dark) DarkMutedForeground else WarmGrey
    val highlight = if (dark) DarkAccent else BrandGreen
    var draft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            CountrysideHeroHeader(
                title = "Dictionary", subtitle = "Look up any word, anytime.",
                illustrationBase = "ill_hero_cottage", isDark = dark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                onLogoToggleTheme = { viewModel.toggleTheme() }, heroHeight = 148.dp,
            )
        }
        item {
            CountrysideSearchPill(
                value = draft, onValueChange = { draft = it },
                placeholder = "Search for a word...", isDark = dark,
                showFilter = true,
                onFilterClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                onSubmit = {
                    if (draft.isBlank()) viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY)
                    else viewModel.submitSearch(draft)
                }, modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Popular Searches", isDark = dark,
                    actionLabel = if (state.popularSearches.isEmpty()) "Restore" else "Clear All",
                    isClearAction = state.popularSearches.isNotEmpty(),
                    onAction = {
                        if (state.popularSearches.isEmpty()) viewModel.restorePopularSearches()
                        else viewModel.clearPopularSearches()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.popularSearches.isEmpty()) {
                        Text("Restore suggestions or search above.", color = muted)
                    }
                    state.popularSearches.forEach { word ->
                        Box(
                            Modifier.clip(RoundedCornerShape(28.dp)).background(card)
                                .border(1.dp, border, RoundedCornerShape(28.dp))
                                .clickable(role = Role.Button) { viewModel.submitSearch(word) }
                                .padding(horizontal = 13.dp, vertical = 9.dp),
                        ) {
                            Text(word, color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 12.5.sp))
                        }
                    }
                }
            }
        }
        item {
            CountrysideQuoteBanner(
                quote = "Words are doors to new worlds.", attribution = "Lexora",
                illustrationBase = "ill_books_daisies", isDark = dark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Recently Searched", isDark = dark,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(17.dp)).background(card)
                        .border(1.dp, border, RoundedCornerShape(17.dp)),
                ) {
                    if (state.recentSearches.isEmpty()) {
                        Text("Search for a word to begin your journey.", color = muted,
                            modifier = Modifier.padding(16.dp))
                    }
                    state.recentSearches.take(3).forEachIndexed { index, word ->
                        if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                        val entry = LexiconData.getWordById(word)
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable(role = Role.Button) { viewModel.submitSearch(word) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = null,
                                tint = highlight, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text(word, color = fg,
                                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    entry?.let { it.partOfSpeech.label() + " · " + it.phonetic }
                                        ?: "Search again for details",
                                    color = muted, maxLines = 1,
                                    style = TextStyle(fontFamily = BodyFontFamily,
                                        fontSize = 10.5.sp, fontStyle = FontStyle.Italic),
                                )
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null,
                                tint = fg, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp)).background(card)
                    .border(1.dp, border, RoundedCornerShape(18.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = highlight, modifier = Modifier.size(19.dp))
                    Text("Quick Access", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 17.sp))
                }
                Text("Jump to useful tools and features.", color = muted,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
                val columns = if (fontScale >= 1.4f) 2 else 4
                val tiles =
                    listOf(
                        Triple("Word of the Day", Icons.AutoMirrored.Outlined.MenuBook, ScreenRoute.WORD_OF_THE_DAY),
                        Triple("Favourite Words", Icons.Outlined.StarOutline, ScreenRoute.COLLECTIONS),
                        Triple("Bookmarks", Icons.Outlined.BookmarkBorder, ScreenRoute.COLLECTIONS),
                        Triple("History", Icons.Outlined.History, ScreenRoute.SEARCH_DISCOVERY),
                    )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tiles.chunked(columns).forEach { group ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.forEach { (title, icon, route) ->
                        Column(
                            Modifier.weight(1f).heightIn(min = 62.dp)
                                .clip(RoundedCornerShape(14.dp)).background(bg.copy(alpha = 0.55f))
                                .border(1.dp, border, RoundedCornerShape(14.dp))
                                .clickable(role = Role.Button) {
                                    when (title) {
                                        "Favourite Words" -> viewModel.openCollectionFilter("Favorites")
                                        "Bookmarks" -> viewModel.openCollectionFilter("Bookmarks")
                                        else -> viewModel.navigate(route)
                                    }
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(icon, contentDescription = null, tint = highlight,
                                modifier = Modifier.size(23.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(title, color = fg, maxLines = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = TextStyle(fontFamily = BodyFontFamily,
                                    fontSize = 10.sp, lineHeight = 12.sp))
                        }
                    }
                    }
                    }
                }
            }
        }
        item {
            // Give the original 420x182 panorama its own responsive bookplate.
            // A fixed 76dp height made it a tiny orphan on tablets; cropping
            // it to fill a flat strip would discard the original painting.
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val paintingWidth = maxWidth.coerceAtMost(640.dp)
                Image(
                    painter = rememberCountrysidePainter(resolveCountrysideDrawable("ill_banner_church", dark, "dictionary-panorama"), paintingWidth),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(paintingWidth)
                        .aspectRatio(758f / 132f)
                        .clip(RoundedCornerShape(15.dp))
                        .background(card)
                        .border(1.dp, border, RoundedCornerShape(15.dp)),
                )
            }
        }
    }
}


// Editorial lookup prompts, not an exhaustive subject index.
private val categoryLookupTerms = mapOf(
    "vocabulary" to listOf("serendipity", "eloquence", "ephemeral", "limerence", "ethereal", "solitude"),
    "geography" to listOf("archipelago", "estuary", "peninsula", "longitude", "latitude", "watershed"),
    "literature" to listOf("metaphor", "allegory", "sonnet", "prologue", "elegy", "alliteration"),
    "science" to listOf("photosynthesis", "osmosis", "gravity", "molecule", "entropy", "ecosystem"),
    "history" to listOf("civilization", "chronicle", "dynasty", "renaissance", "archaeology", "antiquity"),
    "mythology" to listOf("myth", "oracle", "phoenix", "labyrinth", "titan", "legend"),
    "philosophy" to listOf("ethics", "epistemology", "ontology", "stoicism", "virtue", "reason"),
    "art_architecture" to listOf("fresco", "perspective", "arch", "colonnade", "mosaic", "facade"),
    "nature" to listOf("petrichor", "meadow", "hedgerow", "canopy", "flora", "fauna"),
    "culture_traditions" to listOf("heritage", "folklore", "ritual", "custom", "festival", "tradition"),
)

@Composable
fun CategoryDirectoryScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
        val columns = when {
            fontScale >= 1.6f || maxWidth < 280.dp -> 1
            maxWidth >= 540.dp && fontScale < 1.3f -> 3
            else -> 2
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                CountrysideHeroHeader("Explore Categories", "Wander through the world of words.",
                    "ill_hero_cottage", state.isDark,
                    onHistoryClick = { viewModel.openSearch() }, onBack = { viewModel.navigateBack() })
            }
            LexiconData.categories.chunked(columns).forEach { row ->
                item(key = row.first().id) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { category ->
                            CategoryCard(category, viewModel, Modifier.weight(1f),
                                shelfArtworkHeight = if (columns == 1) 112.dp else 90.dp)
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDetailScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val category = LexiconData.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: LexiconData.categories.first()
    val dark = state.isDark
    val ink = if (dark) DarkForeground else DeepForest
    val muted = if (dark) DarkMutedForeground else WarmGrey
    val accent = if (dark) DarkAccent else BrandGreen
    val surface = if (dark) DarkCard else Color(0xFFFFF8EA).copy(alpha = .55f)
    val border = if (dark) DarkChipBorder else OldWoodGold.copy(alpha = .27f)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val darkArt = if (dark) ctx.resources.getIdentifier("ref_home_" + category.id, "drawable", ctx.packageName) else 0
    val res = if (darkArt != 0) darkArt else resolveCountrysideDrawable(category.illustration, dark, "home-" + category.id.replace('_', '-'))
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clickable(role = Role.Button) { viewModel.navigateBack() },
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ChevronLeft, "Back", tint = accent)
                    }
                    Text(category.title, modifier = Modifier.weight(1f), color = ink,
                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 26.sp))
                }
                Text(category.subtitle, color = muted, modifier = Modifier.padding(start = 10.dp),
                    style = TextStyle(fontFamily = LongFontFamily, fontSize = 14.sp))
                Image(rememberCountrysidePainter(res, 220.dp), null,
                    Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Fit)
            }
        }
        item {
            BotanicalSectionTitle("Words to discover", dark, actionLabel = null,
                modifier = Modifier.padding(horizontal = 16.dp))
        }
        categoryLookupTerms[category.id].orEmpty().forEach { term ->
            item(key = term) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp)).background(surface)
                    .border(1.dp, border, RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button) { viewModel.lookupCategoryTerm(term) }
                    .heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LeafSprig(accent, Modifier.size(18.dp))
                    Text(term, Modifier.weight(1f), color = ink,
                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 17.sp))
                    Icon(Icons.Outlined.ChevronRight, null, tint = accent)
                }
            }
        }
        item {
            Text("Suggested terms to look up in the dictionary.", color = muted,
                modifier = Modifier.padding(horizontal = 20.dp),
                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp))
        }
    }
}
