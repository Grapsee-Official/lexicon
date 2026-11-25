package com.lexicon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.ScreenRoute
import com.lexicon.data.WordEntry
import com.lexicon.ui.components.BotanicalSectionTitle
import com.lexicon.ui.components.isApprovedCountrysidePainting
import com.lexicon.ui.components.CountrysideHeroHeader
import com.lexicon.ui.components.CountrysidePainting
import com.lexicon.ui.components.CountrysideQuoteBanner
import com.lexicon.ui.components.CountrysideSearchPill
import com.lexicon.ui.components.LeafSprig
import com.lexicon.ui.components.NocturneAtmosphere
import com.lexicon.ui.components.resolveCountrysideDrawable
import com.lexicon.ui.components.rememberCountrysidePainter
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkHighlight
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LongFontFamily
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.WarmCream
import com.lexicon.ui.theme.WarmGrey

/**
 * Shared Word-Entry Architecture powering:
 *   - Page 2: [WordOfTheDayScreen] (Daily featured word, cottage-bridge vignette, About the Word, 6 Explore Further tiles)
 *   - Page 3: [WordDetailProfileScreen] (Full word profile, 4 metadata tiles, Meanings, Synonyms, Antonyms, Etymology + Usage, Did You Know?)
 *   - Page 9: [SearchResultsScreen] (Dictionary Search inline word card + Synonyms/Antonyms/Etymology/Related Words + Add to Favorites)
 *   - Page 10: [ExpandedExplorationScreen] (Dictionary Lookup Hub + progressively expandable 36-Language Translations Explorer)
 */

@Composable
fun WordOfTheDayScreen(viewModel: LexiconViewModel) {
    IllustratedDailyWordScreen(viewModel)
}

// The dictionary route is a reading page in its own right, not the daily
// edition with a misleading Word of the Day badge.
@Composable
fun WordDetailProfileScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val word = state.detailWord
    val dark = state.isDark
    val ink = if (dark) DarkForeground else Color(0xFF243B37)
    val accent = if (dark) DarkAccent else BrandGreen
    val card = if (dark) DarkCard.copy(alpha = .45f) else Color.Transparent
    val rule = if (dark) DarkChipBorder else OldWoodGold.copy(alpha = .34f)
    val shape = RoundedCornerShape(14.dp)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item(key = "dictionary_heading") {
            Box {
                CountrysideHeroHeader("Dictionary", "", "ill_hero_cottage", dark,
                    onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                    onBack = { viewModel.navigateBack() })
            }
        }
        item(key = "word") {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                val w = maxWidth
                val enlarged = w < 280.dp || androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f
                val artHeight = (w * .47f).coerceIn(130.dp, 260.dp)
                val meadowRes = resolveCountrysideDrawable("ill_thumb_meadow", dark, "detail-meadow")
                val approvedMeadow = isApprovedCountrysidePainting(meadowRes)
                val meadow = rememberCountrysidePainter(meadowRes, w * .56f)
                Box(Modifier.fillMaxWidth().heightIn(min = artHeight)) {
                    if (!enlarged) {
                        CountrysidePainting(meadow, Modifier.align(Alignment.BottomEnd).fillMaxWidth(.56f).height(artHeight),
                            fadeLeft = if (approvedMeadow) 0f else .13f,
                            fadeBottom = if (approvedMeadow) 0f else .09f)
                    }
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(word.word.lowercase(), color = ink,
                            style = TextStyle(fontFamily = DisplayFontFamily,
                                fontSize = (w.value * .105f).coerceIn(30f, 54f).sp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(word.phonetic, color = ink, style = TextStyle(fontFamily = LongFontFamily, fontSize = 15.sp),
                                modifier = Modifier.weight(1f, fill = false))
                            Box(Modifier.size(44.dp).clip(CircleShape).background(card).clickable(role = Role.Button) { viewModel.pronounce(word.word) },
                                contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.VolumeUp, "Pronounce word", tint = accent) }
                            Box(Modifier.size(44.dp).clickable(role = Role.Button) { viewModel.toggleSave(word.id) }, contentAlignment = Alignment.Center) {
                                Icon(if (state.savedWordIds.contains(word.id)) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    "Bookmark word", tint = accent, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(word.partOfSpeech.label(), color = if (dark) DarkHighlight else Color.White,
                            style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 13.sp),
                            modifier = Modifier.clip(CircleShape).background(if (dark) DarkCard else BrandGreen)
                                .padding(horizontal = 14.dp, vertical = 5.dp))
                        Text(word.definitions.firstOrNull().orEmpty(), color = ink,
                            style = TextStyle(fontFamily = LongFontFamily, fontSize = 16.sp, lineHeight = 22.sp),
                            modifier = Modifier.fillMaxWidth(if (enlarged) 1f else .56f))
                        if (enlarged) Image(meadow, null, Modifier.fillMaxWidth().height(artHeight), contentScale = ContentScale.Fit)
                    }
                }
            }
            Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.weight(1f).height(1.dp).background(rule))
                LeafSprig(color = accent, modifier = Modifier.size(19.dp))
                Box(Modifier.weight(1f).height(1.dp).background(rule))
            }
        }
        val sections = listOf("Synonyms" to word.synonyms.joinToString(", "),
            "Antonyms" to word.antonyms.joinToString(", "), "Etymology" to word.etymology,
            "Meanings" to word.definitions.drop(1).joinToString("\n"))
        sections.filter { it.second.isNotBlank() }.forEach { (title, body) ->
            item(key = title) {
                Row(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(shape).background(card)
                    .border(1.dp, rule, shape).padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    LeafSprig(color = accent, modifier = Modifier.size(19.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(title, color = ink, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 17.sp))
                        Text(body, color = ink, style = TextStyle(fontFamily = LongFontFamily, fontSize = 13.sp, lineHeight = 18.sp))
                    }
                }
            }
        }
        if (word.examples.isNotEmpty()) item(key = "examples") {
            Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle("Usage Examples", dark, actionLabel = null)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val large = maxWidth < 280.dp || androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f
                    val art = rememberCountrysidePainter(resolveCountrysideDrawable("ill_books_daisies", dark, "study-books"), maxWidth * .40f)
                    val exampleArtHeight = (maxWidth * .34f).coerceIn(86.dp, 160.dp)
                    Box(Modifier.fillMaxWidth()) {
                        if (!large) {
                            Box(Modifier.matchParentSize()) {
                                CountrysidePainting(art, Modifier.align(Alignment.BottomEnd).fillMaxWidth(.42f).fillMaxHeight(),
                                    fadeLeft = .10f, fadeBottom = .08f)
                            }
                        }
                        Column(Modifier.fillMaxWidth(if (large) 1f else .58f).heightIn(min = if (large) 0.dp else if (word.examples.size > 1) exampleArtHeight else 0.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            word.examples.forEach { example ->
                                Row(Modifier.fillMaxWidth().clip(shape).background(card).border(1.dp, rule, shape).padding(11.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("“", color = accent, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 22.sp))
                                    Text(example, color = ink, modifier = Modifier.weight(1f),
                                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 13.sp, lineHeight = 18.sp))
                                }
                            }
                            if (large) Image(art, null, Modifier.fillMaxWidth().height(132.dp),
                                contentScale = ContentScale.Fit, alignment = Alignment.BottomEnd)
                        }
                    }
                }
            }
        }
        item(key = "closing") {
            CountrysideQuoteBanner("Sometimes, the best things in life are found by chance.", "Lexora", "ill_banner_church",
                dark, Modifier.padding(horizontal = 18.dp))
        }
        item(key = "explore") {
            Row(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(shape).border(1.dp, rule, shape)
                .clickable { viewModel.openWordExpanded(word) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Explore translations across 36 languages", color = ink, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = accent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IllustratedDailyWordScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val word = state.detailWord
    val saved = state.savedWordIds.contains(word.id)
    val shareContext = androidx.compose.ui.platform.LocalContext.current

    val bg = if (isDark) DarkBackground else Color(0xFFFFF1D9)
    val cardBg = if (isDark) DarkCard.copy(alpha = .45f) else Color.Transparent
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .27f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    val bridgeRes = resolveCountrysideDrawable("ill_hero_bridge", isDark, "daily-bridge")
    val approvedBridge = isApprovedCountrysidePainting(bridgeRes)
    val booksRes = resolveCountrysideDrawable("ill_books_daisies", isDark, "study-books")
    val bayRes = resolveCountrysideDrawable("ill_banner_bay", isDark, "daily-bay")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item(key = "detail_hero") {
            BoxWithConstraints(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
                val pageWidth = maxWidth
                val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
                val enlarged = fontScale >= 1.3f || pageWidth < 300.dp
                val artHeight = (pageWidth * (440f / 832f)).coerceIn(150.dp, 320.dp)
                Box(Modifier.fillMaxWidth().heightIn(min = artHeight)) {
                if (!enlarged) CountrysidePainting(
                    rememberCountrysidePainter(bridgeRes, pageWidth * .76f),
                    Modifier.align(Alignment.BottomEnd).fillMaxWidth(.76f).height(artHeight),
                    fadeLeft = if (approvedBridge) 0f else .14f,
                    fadeBottom = if (approvedBridge) 0f else .10f,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = fg,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { viewModel.navigateBack() },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (saved) accent else fg,
                                modifier = Modifier
                                    .size(21.dp)
                                    .clickable { viewModel.toggleSave(word.id) },
                            )
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = fg,
                                modifier = Modifier.size(24.dp).clickable {
                                    val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, word.word + " — " + word.definitions.firstOrNull().orEmpty())
                                    }
                                    shareContext.startActivity(android.content.Intent.createChooser(share, "Share word"))
                                },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, border, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        LeafSprig(color = accent, modifier = Modifier.size(11.dp))
                        Text("Word of the Day", color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.sp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = word.word,
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = (pageWidth.value * 62f / 832f).coerceIn(24f, 44f).sp),
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Pronounce word",
                            tint = muted,
                            modifier = Modifier.size(24.dp)
                                .clickable { viewModel.pronounce(word.word) },
                        )
                    }

                    Text(
                        text = word.phonetic,
                        color = muted,
                        style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 13.5.sp),
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.20f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(word.partOfSpeech.label(), color = fg, style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.5.sp))
                    }

                    Text(
                        text = word.definitions.firstOrNull().orEmpty(),
                        color = fg,
                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 14.sp, lineHeight = 19.sp),
                        modifier = Modifier.fillMaxWidth(if (enlarged) 1f else 0.54f),
                    )
                    if (enlarged) Image(rememberCountrysidePainter(bridgeRes, pageWidth), null,
                        modifier = Modifier.fillMaxWidth().height(artHeight), contentScale = ContentScale.Fit)
                }
                }
            }
        }

        // 4 Metadata Cards: Language | Origin | Frequency | Level
        item(key = "meta_row") {
            val metas = listOf(
                Triple(Icons.AutoMirrored.Outlined.MenuBook, "Language", word.language.substringBefore(" ")),
                Triple(Icons.Outlined.Language, "Origin", word.originPeriod),
                Triple(Icons.Outlined.StarOutline, "Frequency", word.frequency),
                Triple(Icons.Outlined.AutoStories, "Level", word.level),
            )
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = if (maxWidth < 310.dp || androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f) 2 else 4
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    metas.chunked(columns).forEach { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                group.forEach { (ic, label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(13.dp))
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(ic, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(16.dp))
                        Text(label, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.sp))
                        Text(value, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
                    }
                }
            }
        }

        // Meanings Card
        item(key = "meanings") {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, border, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                    Text("Meanings", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp))
                }
                word.definitions.drop(1).ifEmpty { word.definitions }.forEachIndexed { i, def ->
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(def, color = fg, style = TextStyle(fontFamily = LongFontFamily, fontSize = 12.5.sp, lineHeight = 17.sp))
                            word.examples.getOrNull(i)?.let { ex ->
                                Text("“$ex”", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp))
                            }
                        }
                    }
                }
            }
        }

        // Synonyms & Antonyms
        item(key = "syn_ant") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (word.synonyms.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(15.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LeafSprig(color = accent, modifier = Modifier.size(14.dp))
                            Text("Synonyms", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            word.synonyms.forEach { syn ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(bg.copy(alpha = 0.7f))
                                        .border(1.dp, border, CircleShape)
                                        .padding(horizontal = 11.dp, vertical = 4.5.dp),
                                ) {
                                    Text(syn, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                                }
                            }
                        }
                    }
                }
                if (word.antonyms.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(15.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LeafSprig(color = accent, modifier = Modifier.size(14.dp))
                            Text("Antonyms", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            word.antonyms.forEach { ant ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(bg.copy(alpha = 0.7f))
                                        .border(1.dp, border, CircleShape)
                                        .padding(horizontal = 11.dp, vertical = 4.5.dp),
                                ) {
                                    Text(ant, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Side-by-side Etymology & Usage Examples
        item(key = "etym_usage") {
            val shape = RoundedCornerShape(15.dp)
            val etymology: @Composable (Modifier) -> Unit = { layout ->
                val origin = word.etymology.ifBlank { "Origin information is not available." }
                val shortOrigin = origin.length <= 160
                Box(layout.clip(shape).background(cardBg).border(1.dp, border, shape)) {
                    Box(Modifier.matchParentSize()) {
                        CountrysidePainting(rememberCountrysidePainter(booksRes, 110.dp),
                            Modifier.align(Alignment.BottomEnd).fillMaxWidth(if (shortOrigin) .45f else .36f).height(76.dp),
                            fadeLeft = .15f, fadeBottom = 0f)
                    }
                    Column(Modifier.fillMaxWidth().padding(11.dp)
                        .heightIn(min = 104.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            LeafSprig(color = accent, modifier = Modifier.size(17.dp))
                            Text("Etymology", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                        }
                        Text(origin, color = fg, modifier = Modifier.fillMaxWidth(if (shortOrigin) .70f else 1f)
                            .padding(bottom = if (shortOrigin) 0.dp else 48.dp),
                            style = TextStyle(fontFamily = LongFontFamily, fontSize = 12.sp, lineHeight = 16.sp))
                    }
                }
            }
            val examples: @Composable (Modifier) -> Unit = { layout ->
                Column(layout.clip(shape).background(cardBg).border(1.dp, border, shape).padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                        Text("Usage Examples", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                    }
                    word.examples.distinct().forEachIndexed { index, example ->
                        if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                        Text("“$example”", color = fg, style = TextStyle(fontFamily = LongFontFamily,
                            fontStyle = FontStyle.Italic, fontSize = 12.sp, lineHeight = 16.sp))
                    }
                    if (word.examples.isEmpty()) Text("No usage examples available.", color = muted,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                if (maxWidth < 290.dp || androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        etymology(Modifier.fillMaxWidth())
                        examples(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        etymology(Modifier.weight(1.12f))
                        examples(Modifier.weight(.88f))
                    }
                }
            }
        }

        // Did You Know? panoramic bay card
        item(key = "did_you_know") {
            if (word.didYouKnow.isNotBlank()) {
                val shape = RoundedCornerShape(16.dp)
                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val referenceHeight = (maxWidth * .244f).coerceIn(72.dp, 160.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = referenceHeight)
                        .clip(shape)
                        .background(cardBg)
                        .border(1.dp, border, shape),
                ) {
                    Box(Modifier.matchParentSize()) {
                        CountrysidePainting(rememberCountrysidePainter(bayRes, maxWidth * .58f),
                            Modifier.align(Alignment.BottomEnd).fillMaxWidth(.60f).fillMaxHeight(),
                            fadeLeft = .25f, fadeBottom = 0f)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.57f)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                            Text("Did You Know?", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.5.sp))
                        }
                        Text(
                            text = word.didYouKnow,
                            color = fg.copy(alpha = 0.85f),
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp, lineHeight = 14.sp),
                        )
                    }
                }
                }
            }
        }

        // Progressive Link to Page 10 (36-Language Expanded Word Exploration)
        item(key = "expand_36_btn") {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(cardBg)
                    .border(1.dp, border, RoundedCornerShape(50))
                    .clickable(role = Role.Button) { viewModel.openWordExpanded(word) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Language, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(17.dp))
                    Text(
                        text = "Explore Translations Across 36 Languages",
                        color = fg,
                        style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                    )
                }
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
        }
    }
}

/**
 * Page 9: Search Results ("Dictionary Search") — Matched word summary card + matched entries list:
 *   - Hero: "Dictionary Search / Look up any word and explore its full meaning." + cottage art
 *   - Search bar with active query ("serendipity") + clear (X)
 *   - Main Matched Word Card: word + [noun] pill, IPA + speaker, botanical divider, definition,
 *     quote box, expandable rows (Synonyms, Antonyms, Etymology, Related Words pills),
 *     and full-width [Add to Favorites] button!
 *   - Additional matched words / idioms below + "Discover the beauty in every word." banner
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchResultsScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else Color(0xFFFFF1D9)
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF8EA).copy(alpha = .55f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .27f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    val matches = remember(
        state.searchQuery, state.filterLanguage, state.filterPos,
        state.filterDifficulty, state.filterSort, state.searchScopeTab,
    ) {
        viewModel.searchWords(state.searchQuery, limit = 8)
    }
    val primaryWord: WordEntry? = matches.firstOrNull()
    val fav = primaryWord?.let { state.favoriteWordIds.contains(it.id) } ?: false

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Dictionary\nSearch",
                subtitle = "Look up any word and explore its full meaning.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 148.dp,
            )
        }

        item(key = "search_bar") {
            CountrysideSearchPill(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search for a word...",
                isDark = isDark,
                onSubmit = { viewModel.submitSearch(state.searchQuery) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (state.searchScopeTab == "Idioms") {
            item(key = "idiom_search_results") {
                val matchingIdioms = LexiconData.idiomsAndPhrases.filter { phrase ->
                    state.searchQuery.isBlank() ||
                        phrase.phrase.contains(state.searchQuery, ignoreCase = true) ||
                        phrase.meaning.contains(state.searchQuery, ignoreCase = true) ||
                        phrase.example.contains(state.searchQuery, ignoreCase = true)
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)).background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(18.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Idioms & Phrases", color = fg,
                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 20.sp))
                    if (matchingIdioms.isEmpty()) {
                        Text("No matching idioms. Try another phrase.", color = muted,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp))
                    }
                    matchingIdioms.forEach { phrase ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(phrase.phrase, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp))
                            Text(phrase.meaning, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp))
                            Text(phrase.example, color = muted, style = TextStyle(fontFamily = LongFontFamily, fontSize = 13.sp, fontStyle = FontStyle.Italic))
                        }
                    }
                }
            }
        } else if (primaryWord == null) {
            // Empty state when no entry matches
            item(key = "empty_results") {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(18.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(if (state.searchQuery.isBlank()) "Search the dictionary" else "No matching entries", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 19.sp))
                    Text(
                        text = if (state.searchQuery.isBlank()) "Enter a word above to discover its meaning."
                        else "Try another spelling, language or filter. Example: “serendipity”.",
                        color = muted,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                    )
                }
            }
        } else {
            item(key = "primary_result_card") {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = primaryWord.word.lowercase(),
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 27.sp),
                            modifier = Modifier.weight(1f).clickable(role = Role.Button) { viewModel.openWord(primaryWord) },
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.22f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text(primaryWord.partOfSpeech.label(), color = fg, style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.5.sp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(primaryWord.phonetic, color = muted, style = TextStyle(fontFamily = LongFontFamily, fontSize = 13.sp),
                            modifier = Modifier.weight(1f, fill = false))
                        Box(Modifier.size(44.dp).clickable(role = Role.Button) { viewModel.pronounce(primaryWord.word) },
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.VolumeUp, "Pronounce word", tint = accent, modifier = Modifier.size(20.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f).height(1.dp).background(border))
                        LeafSprig(color = accent, modifier = Modifier.size(14.dp))
                        Box(Modifier.weight(1f).height(1.dp).background(border))
                    }

                    Text(
                        text = primaryWord.definitions.firstOrNull() ?: "No definition available yet.",
                        color = fg,
                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 15.sp, lineHeight = 21.sp),
                    )

                    if (primaryWord.examples.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent.copy(alpha = 0.14f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.FormatQuote, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(18.dp))
                            Text(
                                text = "“${primaryWord.examples.first()}”",
                                color = fg,
                                style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 12.5.sp, lineHeight = 17.5.sp),
                            )
                        }
                    }

                    // Expandable detail rows (tapping opens full Page 3 Word Detail)
                    ResultDetailRow(
                        label = "Synonyms",
                        value = primaryWord.synonyms.joinToString(", ").ifEmpty { "—" },
                        accent = accent,
                        fg = fg,
                        muted = muted,
                        onClick = { viewModel.openWord(primaryWord) },
                    )
                    ResultDetailRow(
                        label = "Antonyms",
                        value = primaryWord.antonyms.joinToString(", ").ifEmpty { "—" },
                        accent = accent,
                        fg = fg,
                        muted = muted,
                        onClick = { viewModel.openWord(primaryWord) },
                    )
                    ResultDetailRow(
                        label = "Etymology",
                        value = primaryWord.etymology,
                        accent = accent,
                        fg = fg,
                        muted = muted,
                        onClick = { viewModel.openWord(primaryWord) },
                    )

                    if (primaryWord.relatedWords.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openWordExpanded(primaryWord) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                                    Text("Related Words", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.5.sp))
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                primaryWord.relatedWords.forEach { rw ->
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background((if (isDark) accent else BrandGreen).copy(alpha = 0.18f))
                                            .clickable(role = Role.Button) { viewModel.submitSearch(rw) }
                                            .heightIn(min = 32.dp)
                                            .padding(horizontal = 11.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(rw, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp))
                                    }
                                }
                            }
                        }
                    }

                    // Add to Favorites full-width pill button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(if (fav) accent.copy(alpha = 0.22f) else bg.copy(alpha = 0.75f))
                            .border(1.dp, border, CircleShape)
                            .clickable(role = Role.Button) { viewModel.toggleFavorite(primaryWord.id) }
                            .heightIn(min = 44.dp)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (fav) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = if (fav) accent else fg,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (fav) "Added to Favorites" else "Add to Favorites",
                            color = fg,
                            style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        )
                    }
                }
            }
        }

        if (state.searchScopeTab != "Idioms" && matches.size > 1) {
            item(key = "more_matches") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    BotanicalSectionTitle(title = "More Matches", isDark = isDark, actionLabel = null)
                    matches.drop(1).forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp))
                                .clickable { viewModel.openWord(result) }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(result.word, color = fg,
                                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                Text(result.definitions.firstOrNull() ?: "No definition available.",
                                    color = muted,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp),
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open entry",
                                tint = muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "Discover the beauty in every word.",
                attribution = "Lexora",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun ResultDetailRow(
    label: String,
    value: String,
    accent: Color,
    fg: Color,
    muted: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LeafSprig(color = accent, modifier = Modifier.size(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.5.sp))
            Text(value, color = muted, style = TextStyle(fontFamily = LongFontFamily, fontSize = 12.sp, lineHeight = 16.sp))
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
    }
}

/**
 * Page 10: Expanded Word Exploration & Dictionary Lookup Hub (shares WordEntry with Page 3):
 *   - Hero: "Dictionary / Look up any word, anytime." + cottage vignette
 *   - Search bar with Clear (X) + Filter icon
 *   - Popular Searches pills + Clear All
 *   - "Words are doors to new worlds." stacked books + daisies banner
 *   - Recently Searched cards (serendipity, nostalgia, eloquence)
 *   - Quick Access 4-tile box (Word of the Day, Favourite Words, Bookmarks, 36 Languages)
 *   - Progressively Expandable 36-Language Translations & Contextual Explorer panel!
 *   - Panoramic English church meadow banner at bottom
 */
@Composable
fun ExpandedExplorationScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The approved Expanded Exploration reference is a complete word-detail
    // reading page, not Dictionary Home. Only the explicit translations route
    // opens the 36-language directory below.
    if (!state.detailExpanded36) {
        WordDetailProfileScreen(viewModel = viewModel)
        return
    }
    val isDark = state.isDark
    val word = state.detailWord
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var showAll36 by remember(state.detailExpanded36) { mutableStateOf(state.detailExpanded36) }
    val translations = remember(word.id) { LexiconData.translationsFor(word) }
    val churchRes = resolveCountrysideDrawable("ill_banner_church", isDark, "dictionary-panorama")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Dictionary",
                subtitle = "Look up any word, anytime.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 142.dp,
            )
        }

        item(key = "search") {
            CountrysideSearchPill(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search for a word...",
                isDark = isDark,
                showFilter = true,
                onFilterClick = { showAll36 = !showAll36 },
                onSubmit = { if (query.isNotBlank()) viewModel.submitSearch(query) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Popular Searches
        item(key = "popular_searches") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Popular Searches",
                    isDark = isDark,
                    actionLabel = if (state.popularSearches.isEmpty()) "Restore" else "Clear All",
                    isClearAction = state.popularSearches.isNotEmpty(),
                    onAction = {
                        if (state.popularSearches.isEmpty()) viewModel.restorePopularSearches()
                        else viewModel.clearPopularSearches()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                if (state.popularSearches.isNotEmpty()) {
                    val scroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scroll)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        state.popularSearches.forEach { term ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(cardBg)
                                    .border(1.dp, border, CircleShape)
                                    .clickable { viewModel.submitSearch(term) }
                                    .padding(horizontal = 13.dp, vertical = 6.dp),
                            ) {
                                Text(term, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.5.sp))
                            }
                        }
                    }
                }
            }
        }

        // Stacked Books & Daisies Quote Card
        item(key = "books_quote") {
            CountrysideQuoteBanner(
                quote = "Words are doors to new worlds.",
                attribution = "Lexora",
                illustrationBase = "ill_books_daisies",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Progressively Expandable 36-Language Word Exploration Panel
        item(key = "expanded_36_panel") {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, border, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAll36 = !showAll36 },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Language, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "${word.word} · ${translations.size} available languages",
                                color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.5.sp),
                            )
                            Text(
                                text = "${word.phonetic} · ${if (showAll36) "Hide translations" else "View available translations"}",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp),
                            )
                        }
                    }
                    Text(
                        text = if (showAll36) "Collapse" else "Expand →",
                        color = if (isDark) DarkAccent else BrandGreen,
                        style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
                    )
                }

                if (translations.isEmpty()) {
                    Text(
                        "This word has no offline translations yet.",
                        color = muted,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp),
                    )
                }
                val visibleTranslations = if (showAll36) translations else translations.take(4)
                // One column on compact phones: never truncate long translated words.
                val translationColumns = if (
                    androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 390
                ) 1 else 2
                visibleTranslations.chunked(translationColumns).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        pair.forEach { tr ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg.copy(alpha = 0.65f))
                                    .border(1.dp, border, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 9.dp, vertical = 6.dp),
                            ) {
                                Text("${tr.language} (${tr.code.uppercase()})", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.sp))
                                Text(tr.translated, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Recently Searched cards
        item(key = "recently_searched") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Recently Searched",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    listOf("serendipity", "nostalgia", "eloquence").forEach { id ->
                        val entry = LexiconData.getWordById(id) ?: return@forEach
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(14.dp))
                                .clickable { viewModel.openWord(entry) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.word.lowercase(), color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                Text("${entry.partOfSpeech.label()}  •  ${entry.phonetic}", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.sp))
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Access 4-tile box
        item(key = "quick_access") {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, border, RoundedCornerShape(16.dp))
                    .padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Quick Access", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                        Text("Jump to useful tools and features.", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp))
                    }
                }
                val qaItems = listOf(
                    Triple("Word of\nthe Day", Icons.AutoMirrored.Outlined.MenuBook, ScreenRoute.WORD_OF_THE_DAY),
                    Triple("Favourite\nWords", Icons.Outlined.StarOutline, ScreenRoute.COLLECTIONS),
                    Triple("Bookmarks", Icons.Outlined.BookmarkBorder, ScreenRoute.COLLECTIONS),
                    Triple("Study\nLists", Icons.Outlined.Description, ScreenRoute.STUDY_LISTS),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    qaItems.forEach { (label, ic, target) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg.copy(alpha = 0.65f))
                                .border(1.dp, border, RoundedCornerShape(12.dp))
                                .clickable { viewModel.navigate(target) }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(ic, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp, lineHeight = 11.5.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }

        // Panoramic English church meadow strip
        item(key = "church_panorama") {
            Image(
                painter = rememberCountrysidePainter(churchRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(1.dp, border, RoundedCornerShape(15.dp)),
            )
        }
    }
}
