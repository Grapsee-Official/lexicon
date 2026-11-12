package com.lexicon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.ScreenRoute
import com.lexicon.ui.components.BotanicalSectionTitle
import com.lexicon.ui.components.CountrysideHeroHeader
import com.lexicon.ui.components.CountrysideSearchPill
import com.lexicon.ui.components.LeafSprig
import com.lexicon.ui.components.resolveCountrysideDrawable
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LongFontFamily
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.WarmCream
import com.lexicon.ui.theme.WarmGrey

/**
 * Page 1: Dictionary Home — Main dictionary dashboard:
 *   - Hero: Botanical sprig + "Dictionary / Every word has a world." + antique books & quill
 *   - Search Pill: [Search for a word... | Camera | Mic]
 *   - Word of the Day ("Solitude") split watercolour meadow cottage card
 *   - Explore Words: 6 botanical miniature cards (Categories, Languages, Phrases, Idioms, Examples, Etymology)
 *   - World of Words: Rudyard Kipling teacup-on-antique-book card
 *   - Recently Viewed (left) & Popular Words (right) side-by-side cards
 *   - Random Word + green Shuffle button + Word Builder ('A' tile) interactive banner
 */
@Composable
fun DictionaryHomeScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var draftQuery by remember { mutableStateOf("") }
    val solitude = LexiconData.solitudeEntry
    val solitudeSaved = state.savedWordIds.contains(solitude.id)

    val popularIds = listOf("serendipity", "limerence", "ephemeral", "mellifluous", "quixotic")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hero") {
                CountrysideHeroHeader(
                    title = "Dictionary",
                    subtitle = "Every word has a world.",
                    illustrationBase = "ill_hero_books",
                    isDark = isDark,
                    onHistoryClick = { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION) },
                    onLogoToggleTheme = { viewModel.toggleTheme() },
                    heroHeight = 136.dp,
                )
            }

            item(key = "search") {
                CountrysideSearchPill(
                    value = draftQuery,
                    onValueChange = { draftQuery = it },
                    placeholder = "Search for a word...",
                    isDark = isDark,
                    showCameraMic = true,
                    onSubmit = {
                        if (draftQuery.isNotBlank()) viewModel.submitSearch(draftQuery)
                        else viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Word of the Day ("Solitude") panoramic meadow cottage card
            item(key = "wotd_solitude") {
                val shape = RoundedCornerShape(18.dp)
                val wotdRes = resolveCountrysideDrawable("ill_wotd_solitude", isDark)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(152.dp)
                        .clip(shape)
                        .background(cardBg)
                        .border(1.dp, border, shape)
                        .clickable(role = Role.Button) {
                            viewModel.navigate(ScreenRoute.WORD_OF_THE_DAY, word = solitude)
                        },
                ) {
                    Image(
                        painter = painterResource(wotdRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .fillMaxWidth(0.62f),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to cardBg,
                                    0.42f to cardBg.copy(alpha = 0.95f),
                                    0.68f to Color.Transparent,
                                )
                            ),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.52f)
                            .padding(start = 15.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "WORD OF THE DAY",
                                    color = accent,
                                    style = TextStyle(
                                        fontFamily = BodyFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 8.5.sp,
                                        letterSpacing = 0.7.sp,
                                    ),
                                )
                                LeafSprig(color = accent, modifier = Modifier.size(12.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    text = solitude.word,
                                    color = fg,
                                    style = TextStyle(
                                        fontFamily = DisplayFontFamily,
                                        fontSize = 23.sp,
                                        lineHeight = 26.sp,
                                    ),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = null,
                                    tint = muted,
                                    modifier = Modifier.size(15.dp),
                                )
                            }

                            Text(
                                text = solitude.phonetic,
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp),
                            )
                            Text(
                                text = solitude.partOfSpeech.label(),
                                color = fg.copy(alpha = 0.85f),
                                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.5.sp),
                            )
                            Text(
                                text = solitude.definitions.first(),
                                color = fg.copy(alpha = 0.84f),
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp, lineHeight = 14.sp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "View details",
                                color = if (isDark) DarkAccent else BrandGreen,
                                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.5.sp),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = if (isDark) DarkAccent else BrandGreen,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }

                    Icon(
                        imageVector = if (solitudeSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Solitude",
                        tint = if (solitudeSaved) accent else fg.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(11.dp)
                            .size(20.dp)
                            .clickable(role = Role.Button) { viewModel.toggleSave(solitude.id) },
                    )
                }
            }

            // Explore Words (6 botanical miniature cards)
            item(key = "explore_words") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotanicalSectionTitle(
                        title = "Explore Words",
                        isDark = isDark,
                        sprigAfterTitle = true,
                        actionLabel = "View all",
                        onAction = { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        LexiconData.exploreTopics.forEach { topic ->
                            val iconRes = resolveCountrysideDrawable(topic.illustration, isDark)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(cardBg)
                                    .border(1.dp, border, RoundedCornerShape(11.dp))
                                    .clickable(role = Role.Button) {
                                        when (topic.id) {
                                            "languages" -> viewModel.openWordExpanded(LexiconData.serendipityEntry)
                                            "phrases", "idioms" -> {
                                                viewModel.setSearchScopeTab("Idioms")
                                                viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY)
                                            }
                                            "examples" -> viewModel.navigate(ScreenRoute.WORD_OF_THE_DAY)
                                            else -> viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION)
                                        }
                                    }
                                    .padding(horizontal = 3.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(32.dp),
                                )
                                Text(
                                    text = topic.title,
                                    color = fg,
                                    style = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 8.8.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = topic.subtitle,
                                    color = muted,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 6.2.sp, lineHeight = 8.sp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // World of Words — Rudyard Kipling teacup card
            item(key = "world_of_words") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BotanicalSectionTitle(
                        title = "World of Words",
                        isDark = isDark,
                        sprigAfterTitle = true,
                        actionLabel = null,
                    )
                    val shape = RoundedCornerShape(16.dp)
                    val cupRes = resolveCountrysideDrawable("ill_teacup_books", isDark)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(114.dp)
                            .clip(shape)
                            .background(cardBg)
                            .border(1.dp, border, shape)
                            .clickable(role = Role.Button) { viewModel.navigate(ScreenRoute.MY_NOTES) },
                    ) {
                        Image(
                            painter = painterResource(cupRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.CenterEnd,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .fillMaxWidth(0.52f),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.56f)
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "“Words are the most powerful drug used by mankind.”",
                                color = fg,
                                style = TextStyle(
                                    fontFamily = DisplayFontFamily,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp,
                                ),
                                maxLines = 3,
                            )
                            Text(
                                text = "– Rudyard Kipling",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp),
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Explore quotes",
                                    color = if (isDark) DarkAccent else BrandGreen,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isDark) DarkAccent else BrandGreen,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Dual Columns: Recently Viewed & Popular Words
            item(key = "dual_lists") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Left: Recently Viewed
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Recently Viewed",
                                color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.5.sp),
                            )
                            Text(
                                text = "View all →",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.sp),
                                modifier = Modifier.clickable { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION) },
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp)),
                        ) {
                            state.recentlyViewedIds.take(5).forEachIndexed { idx, id ->
                                val w = LexiconData.getWordById(id) ?: return@forEachIndexed
                                if (idx > 0) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openWord(w) }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    LeafSprig(color = accent, modifier = Modifier.size(11.dp))
                                    Text(
                                        text = w.word,
                                        color = fg,
                                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 11.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = w.partOfSpeech.label(),
                                        color = muted,
                                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.sp),
                                    )
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = muted, modifier = Modifier.size(11.dp))
                                }
                            }
                        }
                    }

                    // Right: Popular Words
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Popular Words",
                                color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.5.sp),
                            )
                            Text(
                                text = "View all →",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.sp),
                                modifier = Modifier.clickable { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp)),
                        ) {
                            popularIds.forEachIndexed { idx, id ->
                                val w = LexiconData.getWordById(id) ?: return@forEachIndexed
                                val saved = state.savedWordIds.contains(w.id)
                                if (idx > 0) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openWord(w) }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(accent.copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            color = fg,
                                            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 8.sp),
                                        )
                                    }
                                    Text(
                                        text = w.word,
                                        color = fg,
                                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 11.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(
                                        imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (saved) accent else muted,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { viewModel.toggleSave(w.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Banner: Random Word + Shuffle Circle + Word Builder ('A' tile)
            item(key = "random_and_builder") {
                val vaseRes = resolveCountrysideDrawable("ill_random_vase", isDark)
                val tileRes = resolveCountrysideDrawable("ill_builder_tile", isDark)
                val shape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(cardBg)
                        .border(1.dp, border, shape)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) { viewModel.discoverRandomWord() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Image(
                            painter = painterResource(vaseRes),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Column {
                            Text(
                                text = "Random Word",
                                color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                            )
                            Text(
                                text = "Discover a random word and expand your vocabulary.",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.sp, lineHeight = 10.5.sp),
                                maxLines = 2,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) DarkAccent else BrandGreen)
                            .clickable(role = Role.Button) { viewModel.discoverRandomWord() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shuffle,
                            contentDescription = "Random discovery",
                            tint = if (isDark) DarkBackground else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) { viewModel.toggleWordBuilder(true) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Word Builder",
                                color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.5.sp),
                            )
                            Text(
                                text = "Learn words through roots, prefixes and suffixes.",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.sp, lineHeight = 10.5.sp),
                                maxLines = 2,
                            )
                        }
                        Image(
                            painter = painterResource(tileRes),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
            }
        }

        // Interactive Word Builder Overlay (roots, prefixes, suffixes)
        if (state.wordBuilderOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { viewModel.toggleWordBuilder(false) },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) { }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Word Builder · Classical Roots",
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 18.sp),
                        )
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close Word Builder",
                            tint = muted,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.toggleWordBuilder(false) },
                        )
                    }
                    LexiconData.morphemeRoots.forEach { mr ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg.copy(alpha = 0.65f))
                                .border(1.dp, border, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(mr.root, color = if (isDark) DarkAccent else BrandGreen, style = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
                                Text("(${mr.origin} — “${mr.meaning}”)", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.sp))
                            }
                            Text(
                                text = mr.derivatives.joinToString(" · "),
                                color = fg,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.5.sp),
                            )
                        }
                    }
                }
            }
        }
    }
}
