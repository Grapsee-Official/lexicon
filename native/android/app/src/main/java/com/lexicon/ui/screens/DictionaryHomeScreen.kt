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
    val card = if (dark) DarkCard else CardSurface
    val border = if (dark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (dark) DarkForeground else DeepForest
    val muted = if (dark) DarkMutedForeground else WarmGrey
    val highlight = if (dark) DarkAccent else BrandGreen
    var draft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(bg),
        contentPadding = PaddingValues(bottom = 162.dp),
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
                Text("✧  Quick Access", color = fg,
                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 17.sp))
                Text("Jump to useful tools and features.", color = muted,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        Triple("Word of the Day", Icons.AutoMirrored.Outlined.MenuBook, ScreenRoute.WORD_OF_THE_DAY),
                        Triple("Favourite Words", Icons.Outlined.StarOutline, ScreenRoute.COLLECTIONS),
                        Triple("Bookmarks", Icons.Outlined.BookmarkBorder, ScreenRoute.COLLECTIONS),
                        Triple("History", Icons.Outlined.History, ScreenRoute.SEARCH_DISCOVERY),
                    ).forEach { (title, icon, route) ->
                        Column(
                            Modifier.width(96.dp).height(88.dp)
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
        item {
            Image(
                painter = painterResource(resolveCountrysideDrawable("ill_banner_bridge", dark)),
                contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .height(76.dp).clip(RoundedCornerShape(15.dp)),
            )
        }
    }
}
