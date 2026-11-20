package com.lexicon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.ScreenRoute
import com.lexicon.data.WordEntry
import com.lexicon.ui.components.BotanicalSectionTitle
import com.lexicon.ui.components.CountrysideHeroHeader
import com.lexicon.ui.components.CountrysideQuoteBanner
import com.lexicon.ui.components.CountrysideSearchPill
import com.lexicon.ui.components.LeafSprig
import com.lexicon.ui.components.NocturneAtmosphere
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
 * Shared Word-Entry Architecture powering:
 *   - Page 2: [WordOfTheDayScreen] (Daily featured word, cottage-bridge vignette, About the Word, 6 Explore Further tiles)
 *   - Page 3: [WordDetailProfileScreen] (Full word profile, 4 metadata tiles, Meanings, Synonyms, Antonyms, Etymology + Usage, Did You Know?)
 *   - Page 9: [SearchResultsScreen] (Dictionary Search inline word card + Synonyms/Antonyms/Etymology/Related Words + Add to Favorites)
 *   - Page 10: [ExpandedExplorationScreen] (Dictionary Lookup Hub + progressively expandable 36-Language Translations Explorer)
 */

@Composable
fun WordOfTheDayScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    val featuredWords = listOf(
        LexiconData.serendipityEntry,
        LexiconData.solitudeEntry,
        LexiconData.wordOfTheDay,
        LexiconData.dictionary[3],
    )
    var dayIdx by remember { mutableIntStateOf(0) }
    val utcDay = remember { (System.currentTimeMillis() / 86_400_000L).toInt() }
    val word = LexiconData.featuredWord(dayIdx)

    val bridgeRes = resolveCountrysideDrawable("ill_hero_bridge", isDark)
    val openBookRes = resolveCountrysideDrawable("ill_open_book_meadow", isDark)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 162.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Every piece is part of the reading page, not overlapping strata
        // that force a tall banner around a smaller picture.
        item(key = "wotd_hero_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(role = Role.Button, onClickLabel = "Toggle appearance") {
                                viewModel.toggleTheme()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        LeafSprig(
                            color = if (isDark) DarkAccent else BrandGreen,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Word of the Day",
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 24.sp),
                        )
                        Text(
                            "A new word, a wider world.",
                            color = muted,
                            style = TextStyle(fontFamily = LongFontFamily, fontSize = 12.sp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, border, CircleShape)
                            .clickable(role = Role.Button, onClickLabel = "Next featured word") {
                                dayIdx = (dayIdx + 1) % featuredWords.size
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .background(cardBg, CircleShape)
                        .border(1.dp, border, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                    Text(
                        java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.ENGLISH)
                            .format(java.util.Date()),
                        color = fg,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                    )
                }
                // A flexible manuscript leaf; no truncated description and no
                // empty fixed-height region beneath its words.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(cardBg, RoundedCornerShape(18.dp))
                        .border(1.dp, border, RoundedCornerShape(18.dp))
                        .clickable(role = Role.Button, onClickLabel = "Read ${word.word}") {
                            viewModel.openWord(word)
                        }
                        .padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            word.word,
                            modifier = Modifier.weight(1f, fill = false),
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 31.sp),
                        )
                        // The audio control is not a dead icon.
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Pronounce ${word.word}",
                            tint = muted,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(role = Role.Button) { viewModel.pronounce(word.word) }
                                .padding(11.dp),
                        )
                    }
                    Text(
                        "${word.partOfSpeech.label()}  ·  ${word.phonetic}",
                        color = muted,
                        style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 14.sp),
                    )
                    Text(
                        word.definitions.firstOrNull().orEmpty(),
                        color = fg,
                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 15.sp, lineHeight = 22.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    word.examples.firstOrNull()?.takeIf { it.isNotBlank() }?.let { example ->
                        Text(
                            "“$example”",
                            color = fg.copy(alpha = 0.85f),
                            style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic, fontSize = 13.sp, lineHeight = 19.sp),
                        )
                    }
                }
                // The watercolor occupies exactly its own natural proportions,
                // unrestricted by the information card above.
                val watercolor = painterResource(bridgeRes)
                val dims = watercolor.intrinsicSize
                val ratio = if (dims.width > 1f && dims.height > 1f) dims.width / dims.height else 1.45f
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = watercolor,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().aspectRatio(ratio),
                    )
                    if (isDark) NocturneAtmosphere(Modifier.matchParentSize())
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    featuredWords.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(role = Role.Button, onClickLabel = "Featured word ${index + 1}") {
                                    dayIdx = index
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (index == dayIdx) 8.dp else 5.dp)
                                    .background(if (index == dayIdx) (if (isDark) DarkAccent else BrandGreen) else border, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // About the Word
        item(key = "about_word") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "About the Word",
                    isDark = isDark,
                    actionLabel = "View More",
                    onAction = { viewModel.openWord(word) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(16.dp))
                        .clickable { viewModel.openWord(word) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Image(
                        painter = painterResource(openBookRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(116.dp)
                            .height(86.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Text(
                        text = word.etymology,
                        color = fg.copy(alpha = 0.88f),
                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 11.5.sp, lineHeight = 16.5.sp),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Explore Further (2x3 Grid: Meaning, Synonyms, Antonyms, Etymology, Translations, Usage Examples)
        item(key = "explore_further") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Explore Further",
                    isDark = isDark,
                    actionLabel = null,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val links = listOf(
                    Triple("Meaning", Icons.AutoMirrored.Outlined.MenuBook, false),
                    Triple("Synonyms", Icons.Outlined.SwapHoriz, false),
                    Triple("Antonyms", Icons.Outlined.CompareArrows, false),
                    Triple("Etymology", Icons.Outlined.AutoStories, false),
                    Triple("Translations", Icons.Outlined.Language, true),
                    Triple("Usage Examples", Icons.Outlined.Description, false),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    links.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { (label, icon, to36) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(13.dp))
                                        .clickable(role = Role.Button) {
                                            if (to36) viewModel.openWordExpanded(word)
                                            else viewModel.openWord(word)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(17.dp))
                                    Text(
                                        text = label,
                                        color = fg,
                                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

/**
 * Page 3: Word Detail — Full word profile (shares WordEntry with Page 10):
 *   - Top bar: [←] ... [Bookmark] [Share]
 *   - "Word of the Day" pill + large Playfair title + speaker + IPA + [noun] pill + stone bridge vignette
 *   - 4 Metadata Cards: Language | Origin | Frequency | Level
 *   - Meanings (numbered 1, 2 with italic contextual quotes)
 *   - Synonyms & Antonyms capsule pills
 *   - Side-by-side Etymology (with stacked books art) & Usage Examples
 *   - "Explore in 36 Languages →" progressive expansion button into Page 10
 *   - Did You Know? panoramic coastal palace banner
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailProfileScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val word = state.detailWord
    val saved = state.savedWordIds.contains(word.id)

    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    val context = LocalContext.current
    val compactReading = androidx.compose.ui.platform.LocalConfiguration.current.fontScale < 1.25f
    val bridgeRes = resolveCountrysideDrawable("ill_hero_bridge", isDark)
    // The reference artwork is a tall transparent watercolor. Drawing it
    // on the manuscript canvas (rather than measuring it as a LazyColumn
    // child) lets the cottage and bridge run behind the tiles without
    // giving the hero an extra 400dp of empty layout height.
    val bridgeBitmap = ImageBitmap.imageResource(id = bridgeRes)
    val booksRes = resolveCountrysideDrawable("ill_books_daisies", isDark)
    val bayRes = resolveCountrysideDrawable("ill_banner_bay", isDark)

    // On larger screens a countryside dictionary remains a readable book
    // leaf, not a stretched dashboard. Center the same canonical phone
    // composition on the paper without changing its content hierarchy.
    Box(
        modifier = Modifier.fillMaxSize().background(bg),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(bg),
            contentPadding = PaddingValues(bottom = 162.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactReading) 8.dp else 12.dp),
        ) {
        item(key = "detail_hero") {
            // Preserve the owner's composition: watercolor occupies the whole
            // hero behind the parchment typography, not a separate picture.
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .heightIn(min = if (compactReading) 188.dp else 225.dp),
            ) {
                val sceneHeight = if (compactReading) {
                    (maxWidth * 0.46f).coerceIn(178.dp, 290.dp)
                } else {
                    (maxWidth * 0.55f).coerceIn(215.dp, 355.dp)
                }
                Canvas(Modifier.matchParentSize()) {
                    // Paint at intrinsic aspect, independently of the hero's
                    // measured text height; absolutely no Crop or thumbnail
                    // slot. The complete artwork may extend into the
                    // transparent tops of the four information cards.
                    val paintedWidth = size.width * 0.70f
                    val paintedHeight = paintedWidth *
                        bridgeBitmap.height.toFloat() / bridgeBitmap.width.toFloat()
                    drawImage(
                        image = bridgeBitmap,
                        dstOffset = IntOffset(
                            x = (size.width - paintedWidth).toInt(),
                            y = (-60.dp).toPx().toInt(),
                        ),
                        dstSize = IntSize(
                            width = paintedWidth.toInt().coerceAtLeast(1),
                            height = paintedHeight.toInt().coerceAtLeast(1),
                        ),
                    )
                }
                if (isDark) NocturneAtmosphere(Modifier.matchParentSize())
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = sceneHeight)
                        .padding(bottom = if (compactReading) 3.dp else 8.dp),
                    verticalArrangement = Arrangement.spacedBy(if (compactReading) 2.dp else 5.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = fg,
                            modifier = Modifier.size(44.dp)
                                .clickable(role = Role.Button, onClickLabel = "Back") { viewModel.navigateBack() }
                                .padding(10.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (saved) "Remove bookmark" else "Save word",
                            tint = if (saved) accent else fg,
                            modifier = Modifier.size(44.dp)
                                .clickable(role = Role.Button) { viewModel.toggleSave(word.id) }
                                .padding(10.dp),
                        )
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share word",
                            tint = fg,
                            modifier = Modifier.size(44.dp)
                                .clickable(role = Role.Button) {
                                    val text = "${word.word} — ${word.definitions.firstOrNull().orEmpty()}"
                                    val send = Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, text)
                                    context.startActivity(Intent.createChooser(send, "Share word"))
                                }
                                .padding(10.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 20.dp)
                            .background(if (isDark) DarkCard else Color(0xFFF0E5D1), CircleShape)
                            .border(1.dp, border, CircleShape)
                            .padding(horizontal = if (compactReading) 9.dp else 12.dp,
                                vertical = if (compactReading) 3.dp else 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LeafSprig(color = accent, modifier = Modifier.size(13.dp))
                        Text(
                            "Word of the Day", color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = if (compactReading) 12.sp else 14.sp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.74f).padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            word.word.replaceFirstChar { it.uppercase() },
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily,
                                fontSize = if (compactReading) 30.sp else 32.sp,
                                lineHeight = if (compactReading) 34.sp else 36.sp),
                            modifier = Modifier.weight(1f, fill = false),
                            softWrap = true,
                        )
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Pronounce word",
                            tint = fg,
                            modifier = Modifier.size(44.dp)
                                .clickable(role = Role.Button) { viewModel.pronounce(word.word) }
                                .padding(11.dp),
                        )
                    }
                    Text(
                        word.phonetic,
                        color = muted,
                        style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic,
                            fontSize = if (compactReading) 13.sp else 15.sp),
                        modifier = Modifier.padding(start = 20.dp),
                    )
                    Row(
                        modifier = Modifier.padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.background(accent.copy(alpha = .17f), CircleShape)
                                .border(1.dp, border, CircleShape)
                                .padding(horizontal = if (compactReading) 10.dp else 13.dp,
                                    vertical = if (compactReading) 3.dp else 5.dp),
                        ) {
                            Text(
                                word.partOfSpeech.label(), color = fg,
                                style = TextStyle(fontFamily = LongFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = if (compactReading) 11.sp else 13.sp),
                            )
                        }
                        LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                    }
                    Text(
                        word.definitions.firstOrNull().orEmpty(),
                        color = fg,
                        style = TextStyle(fontFamily = LongFontFamily,
                            fontSize = if (compactReading) 14.sp else 17.sp,
                            lineHeight = if (compactReading) 19.sp else 22.sp),
                        modifier = Modifier.fillMaxWidth(.62f).padding(start = 20.dp),
                    )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                metas.forEach { (ic, label, value) ->
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
                        Text(value, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                (if (word.id == "serendipity") word.definitions.drop(1) else word.definitions).take(2).forEachIndexed { i, def ->
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

        // Two quiet journal entries instead of two fixed-height storage boxes.
        // No truncated etymology, dead vertical space, or pinned 96dp artwork.
        // Height follows text and the watercolor's own transparent silhouette.
        item(key = "etym_usage") {
            // The approved reference has two manuscript cards side by side.
            // Botanicals fade into the right edge of Etymology, never sitting
            // in a detached full-width image strip below the card.
            val shape = RoundedCornerShape(17.dp)
            val inkStyle = TextStyle(fontFamily = LongFontFamily,
                fontSize = if (compactReading) 10.5.sp else 11.sp,
                lineHeight = if (compactReading) 13.5.sp else 14.5.sp)
            val examples = word.examples.filter { it.isNotBlank() }.distinct().let { all ->
                    (if (word.id == "serendipity") all.drop(2) else all).take(2)
                }
            val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                val stacked = maxWidth < 335.dp || fontScale >= 1.35f
                val itemHeight = if (stacked) 142.dp else 121.dp

                @Composable
                fun EtymologyCard(modifier: Modifier) {
                    Box(
                        modifier = modifier.heightIn(min = itemHeight)
                            .background(cardBg, shape).border(1.dp, border, shape),
                    ) {
                        Image(
                            painter = painterResource(booksRes),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.BottomEnd,
                            modifier = Modifier.align(Alignment.BottomEnd)
                                .fillMaxWidth(if (stacked) .35f else .56f)
                                .height(if (stacked) 100.dp else 95.dp),
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(if (stacked) .70f else .78f)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                                Text("Etymology", color = fg,
                                    style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp))
                            }
                            Text(word.etymology, color = fg.copy(alpha = .9f),
                                style = inkStyle)
                        }
                    }
                }

                @Composable
                fun UsageCard(modifier: Modifier) {
                    Column(
                        modifier = modifier.heightIn(min = itemHeight)
                            .background(cardBg, shape).border(1.dp, border, shape)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LeafSprig(color = accent, modifier = Modifier.size(15.dp))
                            Text("Usage Examples", color = fg,
                                style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp))
                        }
                        examples.forEachIndexed { index, example ->
                            if (index > 0) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                            }
                            Text("“$example”", color = fg.copy(alpha = .89f),
                                style = TextStyle(fontFamily = LongFontFamily,
                                    fontStyle = FontStyle.Italic, fontSize = 12.sp, lineHeight = 17.sp))
                        }
                    }
                }

                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EtymologyCard(Modifier.fillMaxWidth())
                        if (examples.isNotEmpty()) UsageCard(Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EtymologyCard(Modifier.weight(1f))
                        if (examples.isNotEmpty()) UsageCard(Modifier.weight(1f))
                    }
                }
            }
        }

        // The true painted bay forms the bookplate, not a shallow strip
        // The bookplate follows the *source image's* aspect ratio.
        // Text grows above the painting where normal overlay would collide.
        item(key = "did_you_know") {
            if (word.didYouKnow.isNotBlank()) {
                val shape = RoundedCornerShape(17.dp)
                val painter = painterResource(bayRes)
                val dimensions = painter.intrinsicSize
                val artRatio = if (dimensions.width > 1f && dimensions.height > 1f)
                    dimensions.width / dimensions.height else 1.94f
                val typeScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    // Reference banner: complete watercolor fills its right
                    // half; its natural *rendered* height sets the whole card.
                    // Never use the entire card width to determine PNG height.
                    val paintedHeight = (maxWidth * .228f).coerceIn(88.dp, 110.dp)
                    val paintedWidth = paintedHeight * artRatio
                    val stacked = maxWidth < 345.dp || typeScale >= 1.3f ||
                        word.didYouKnow.length > 135
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        if (stacked) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Outlined.Lightbulb, contentDescription = null,
                                        tint = accent, modifier = Modifier.size(18.dp))
                                    Text("Did You Know?", color = fg,
                                        style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 16.sp))
                                }
                                Text(word.didYouKnow, color = fg,
                                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp, lineHeight = 17.sp))
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(paintedHeight)
                                .clip(shape)
                                .background(cardBg)
                                .border(1.dp, border, shape),
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.BottomEnd,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(paintedWidth)
                                    .height(paintedHeight),
                            )
                            if (!stacked) {
                                Column(
                                    modifier = Modifier.align(Alignment.CenterStart)
                                        .fillMaxWidth(.61f)
                                        .padding(start = 12.dp, end = 3.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Outlined.Lightbulb, contentDescription = null,
                                            tint = accent, modifier = Modifier.size(17.dp))
                                        Text("Did You Know?", color = fg,
                                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                    }
                                    Text(word.didYouKnow, color = fg.copy(alpha = 0.90f),
                                        style = TextStyle(fontFamily = BodyFontFamily,
                                            fontSize = 11.sp, lineHeight = 15.sp))
                                }
                            }
                        }
                    }
                }
            }
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
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
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
            .background(bg),
        contentPadding = PaddingValues(bottom = 162.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Dictionary\nSearch",
                subtitle = "Look up any word and explore its full meaning.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                actionKind = "back",
                actionLabel = "Back to Search",
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = primaryWord.word.lowercase(),
                            color = fg,
                            style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 27.sp),
                            modifier = Modifier.clickable { viewModel.openWord(primaryWord) },
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
                        Text(primaryWord.phonetic, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.5.sp))
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Pronounce word",
                            tint = muted,
                            modifier = Modifier.size(24.dp)
                                .clickable { viewModel.pronounce(primaryWord.word) },
                        )
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
                        style = TextStyle(fontFamily = LongFontFamily, fontSize = 14.sp, lineHeight = 20.sp),
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
                                text = "“${primaryWord.examples.firstOrNull().orEmpty()}”",
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
                                            .background(accent.copy(alpha = 0.18f))
                                            .padding(horizontal = 11.dp, vertical = 4.5.dp),
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

        if (matches.size > 1) {
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
            Text(value, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.5.sp, lineHeight = 15.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
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
    // Expanded Exploration has its own search and translation experience.
    // Do not alias it to Word of the Day: doing so erased an entire route.
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
    val churchRes = resolveCountrysideDrawable("ill_banner_church", isDark)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 162.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Dictionary",
                subtitle = "Look up any word, anytime.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
                actionKind = "back",
                actionLabel = "Back to Search",
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
                                Text(tr.translated, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 12.sp, lineHeight = 16.sp))
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
                    state.recentSearches.take(6).forEach { term ->
                        val entry = LexiconData.getWordById(term) ?: return@forEach
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

        // Panoramic church watercolor retains its *own* source proportions.
        // A 74dp thumbnail well cropped the grounds, sky and wildflower edge.
        item(key = "church_panorama") {
            val panorama = painterResource(churchRes)
            val natural = panorama.intrinsicSize
            val ratio = if (natural.width > 1f && natural.height > 1f)
                natural.width / natural.height else 3f
            Image(
                painter = panorama,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .aspectRatio(ratio),
            )
        }
    }
}
