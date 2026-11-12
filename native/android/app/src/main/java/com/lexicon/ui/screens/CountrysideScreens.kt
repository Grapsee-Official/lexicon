package com.lexicon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.lexicon.data.NoteCategory
import com.lexicon.data.ScreenRoute
import com.lexicon.data.TemplateCategory
import com.lexicon.ui.components.BotanicalSectionTitle
import com.lexicon.ui.components.CountrysideFilterPills
import com.lexicon.ui.components.CountrysideHeroHeader
import com.lexicon.ui.components.CountrysideQuoteBanner
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
 * Page 4: Collections — Organise favourites, bookmarks, notes and personal vocabulary collections.
 */
@Composable
fun CollectionsHubScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }

    val filterItems = listOf(
        "All" to null,
        "Favorites" to Icons.Outlined.FavoriteBorder,
        "Bookmarks" to Icons.Outlined.BookmarkBorder,
        "Notes" to Icons.Outlined.Description,
        "Study Lists" to Icons.Outlined.School,
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Collections",
                subtitle = "Save, organize and revisit your favorite words.",
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
                placeholder = "Search in your collections...",
                isDark = isDark,
                showFilter = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "pills") {
            CountrysideFilterPills(
                items = filterItems,
                selected = filter,
                isDark = isDark,
                onSelect = { selected ->
                    filter = selected
                    when (selected) {
                        "Notes" -> viewModel.navigate(ScreenRoute.MY_NOTES)
                        "Study Lists" -> viewModel.navigate(ScreenRoute.STUDY_LISTS)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // My Collections 2x2 Grid (Favorites, Bookmarks, My Notes, Study List)
        item(key = "grid") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "My Collections",
                    isDark = isDark,
                    sprigAfterTitle = true,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.STUDY_LISTS) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val cards = listOf(
                    QuadCollectionSpec("Favorites", "${20 + state.favoriteWordIds.size} words", Icons.Outlined.FavoriteBorder, "ill_col_favorites") {
                        filter = "Favorites"
                    },
                    QuadCollectionSpec("Bookmarks", "${15 + state.savedWordIds.size} words", Icons.Outlined.BookmarkBorder, "ill_col_bookmarks") {
                        filter = "Bookmarks"
                    },
                    QuadCollectionSpec("My Notes", "${8 + state.notes.size} words", Icons.Outlined.Description, "ill_col_notes") {
                        viewModel.navigate(ScreenRoute.MY_NOTES)
                    },
                    QuadCollectionSpec("Study List", "30 words", Icons.Outlined.School, "ill_col_studylist") {
                        viewModel.navigate(ScreenRoute.STUDY_LISTS)
                    },
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cards.chunked(2).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowCards.forEach { spec ->
                                val artRes = resolveCountrysideDrawable(spec.art, isDark)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(106.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(16.dp))
                                        .clickable(role = Role.Button) { spec.onClick() },
                                ) {
                                    Image(
                                        painter = painterResource(artRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.CenterEnd,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.72f),
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(11.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(bg.copy(alpha = 0.85f))
                                                .border(1.dp, border, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(spec.icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
                                        }
                                        Column {
                                            Text(spec.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.5.sp))
                                            Text(spec.countLabel, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.sp))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(9.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(cardBg.copy(alpha = 0.92f))
                                            .border(1.dp, border, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recently Added (Serendipity, Ethereal, Tranquility with botanical thumbnails)
        item(key = "recently_added") {
            val recentDefs = listOf(
                "serendipity" to "ill_thumb_lavender",
                "ethereal" to "ill_thumb_cottage",
                "tranquility" to "ill_thumb_bridge",
            ).filter { (id, _) ->
                query.isBlank() || id.contains(query.trim(), ignoreCase = true)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Recently Added",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    recentDefs.forEach { (id, thumb) ->
                        val entry = LexiconData.getWordById(id) ?: return@forEach
                        val thumbRes = resolveCountrysideDrawable(thumb, isDark)
                        val fav = state.favoriteWordIds.contains(entry.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(14.dp))
                                .clickable { viewModel.openWord(entry) }
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Image(
                                painter = painterResource(thumbRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(9.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.word, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.5.sp))
                                Text("${entry.partOfSpeech.label()}  •  ${entry.phonetic}", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontStyle = FontStyle.Italic, fontSize = 10.5.sp))
                            }
                            Icon(
                                imageVector = if (fav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (fav) accent else muted,
                                modifier = Modifier
                                    .size(17.dp)
                                    .clickable { viewModel.toggleFavorite(entry.id) },
                            )
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "A collection of words is a collection of worlds.",
                attribution = "Lexicon",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

private data class QuadCollectionSpec(
    val title: String,
    val countLabel: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val art: String,
    val onClick: () -> Unit,
)

/**
 * Page 5: My Notes — Create, edit, search and organise personal notes and vocabulary observations.
 */
@Composable
fun MyNotesScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }

    val filteredNotes = state.notes.filter { n ->
        val catOk = selectedCat == "All" ||
            (selectedCat == "Word Notes" && n.category == NoteCategory.WORD_NOTE) ||
            (selectedCat == "Personal" && n.category == NoteCategory.PERSONAL) ||
            (selectedCat == "Study" && n.category == NoteCategory.STUDY) ||
            (selectedCat == "Ideas" && n.category == NoteCategory.IDEA)
        val qOk = query.isBlank() || n.title.contains(query, ignoreCase = true) || n.body.contains(query, ignoreCase = true)
        catOk && qOk
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Notes",
                subtitle = "Capture thoughts, meanings and your language journey.",
                illustrationBase = "ill_hero_notes",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.COLLECTIONS) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 144.dp,
            )
        }

        item(key = "search") {
            CountrysideSearchPill(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search your notes...",
                isDark = isDark,
                showFilter = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "pills") {
            CountrysideFilterPills(
                items = listOf(
                    "All" to null,
                    "Personal" to Icons.Outlined.PersonOutline,
                    "Word Notes" to Icons.AutoMirrored.Outlined.MenuBook,
                    "Study" to Icons.Outlined.School,
                    "Ideas" to Icons.Outlined.Lightbulb,
                ),
                selected = selectedCat,
                isDark = isDark,
                onSelect = { selectedCat = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // My Notes 2x2 Grid
        item(key = "notes_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "My Notes",
                    isDark = isDark,
                    sprigAfterTitle = true,
                    actionLabel = "View all",
                    onAction = { selectedCat = "All"; query = "" },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No notes match your search yet. Tap a Quick Template below to create one!", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp))
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        filteredNotes.take(4).chunked(2).forEach { rowNotes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowNotes.forEach { note ->
                                    val artRes = resolveCountrysideDrawable(note.illustration ?: "ill_note_flowers", isDark)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(132.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(cardBg)
                                            .border(1.dp, border, RoundedCornerShape(16.dp))
                                            .clickable {
                                                LexiconData.getWordById(note.title)?.let { viewModel.openWord(it) }
                                                    ?: viewModel.toggleNoteBookmark(note.id)
                                            },
                                    ) {
                                        Image(
                                            painter = painterResource(artRes),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.BottomEnd,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .fillMaxHeight(0.86f)
                                                .fillMaxWidth(0.50f),
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(11.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(accent.copy(alpha = 0.22f))
                                                        .padding(horizontal = 8.dp, vertical = 2.5.dp),
                                                ) {
                                                    Text(note.category.label, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.5.sp))
                                                }
                                                Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
                                            }
                                            Column(
                                                modifier = Modifier.fillMaxWidth(0.76f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                            ) {
                                                Text(note.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(note.body, color = fg.copy(alpha = 0.8f), style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp, lineHeight = 13.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = muted, modifier = Modifier.size(10.dp))
                                                    Text(note.dateLabel, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.5.sp))
                                                }
                                                Icon(
                                                    imageVector = if (note.bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = null,
                                                    tint = if (note.bookmarked) accent else muted,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { viewModel.toggleNoteBookmark(note.id) },
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowNotes.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Quick Templates strip (creates a live note or opens Page 7!)
        item(key = "quick_templates_strip") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BotanicalSectionTitle(
                    title = "Quick Templates",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.QUICK_TEMPLATES) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = "Use a template to start a new note.",
                    color = muted,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val starters = listOf(
                    Triple("Word Note", Icons.AutoMirrored.Outlined.MenuBook, NoteCategory.WORD_NOTE),
                    Triple("Study Note", Icons.Outlined.School, NoteCategory.STUDY),
                    Triple("Idea", Icons.Outlined.Lightbulb, NoteCategory.IDEA),
                    Triple("Favourite", Icons.Outlined.StarOutline, NoteCategory.PERSONAL),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    starters.forEach { (label, ic, cat) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp))
                                .clickable(role = Role.Button) {
                                    viewModel.addNote(cat, "New $label", "Added from countryside Quick Templates.")
                                }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(ic, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp), maxLines = 1)
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "A well-written note is a small step towards wisdom.",
                attribution = "Lexicon",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * Page 6: Study Lists — Custom vocabulary lists, learning progress, revision and practice sessions.
 */
@Composable
fun StudyListsScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    val totalWords = state.studyLists.sumOf { it.totalWords }
    val overallPct = if (totalWords > 0) (state.studyLists.sumOf { it.completedWords } * 100) / totalWords else 78
    val booksRes = resolveCountrysideDrawable("ill_books_daisies", isDark)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Study List",
                subtitle = "Learn, practice and grow one word at a time.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.QUICK_TEMPLATES) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 142.dp,
            )
        }

        item(key = "search") {
            CountrysideSearchPill(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search your study lists...",
                isDark = isDark,
                showFilter = true,
                onFilterClick = { viewModel.navigate(ScreenRoute.QUICK_TEMPLATES) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // 4 Stat Cards (Active Lists, Total Words, Overall Progress, Day Streak)
        item(key = "stats") {
            val stats = listOf(
                Triple(Icons.AutoMirrored.Outlined.MenuBook, "${state.studyLists.size + 1}", "Active Lists"),
                Triple(Icons.Outlined.CheckCircleOutline, "${totalWords + 32}", "Total Words"),
                Triple(Icons.Outlined.BarChart, "$overallPct%", "Overall Progress"),
                Triple(Icons.Outlined.StarOutline, "${state.dayStreak}", "Day Streak"),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                stats.forEach { (ic, num, label) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(ic, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(17.dp))
                        Text(num, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                        Text(label, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.5.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Today's Study Card (6/10 circular progress ring + Keep going! + stacked books art)
        item(key = "todays_study") {
            val ringColor = if (isDark) DarkAccent else BrandGreen
            val trackColor = border
            val progress = (state.todayStudiedWords.toFloat() / state.todayGoalWords.toFloat()).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, border, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BotanicalSectionTitle(
                    title = "Today’s Study",
                    isDark = isDark,
                    actionLabel = "View details",
                    onAction = { viewModel.advanceStudyList("sl1") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                            )
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.todayStudiedWords}/${state.todayGoalWords}",
                                color = fg,
                                style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                            )
                            Text("words", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                        }
                    }

                    Box(Modifier.width(1.dp).height(62.dp).background(border))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Keep going!", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                        Text(
                            text = "“Small steps\nlead to big progress.”",
                            color = muted,
                            style = TextStyle(fontFamily = LongFontFamily, fontSize = 10.5.sp, lineHeight = 14.sp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }

                    Image(
                        painter = painterResource(booksRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(88.dp)
                            .height(74.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                }
            }
        }

        // Your Study Lists (4 rows with progress bar + Play session button)
        item(key = "lists") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Your Study Lists",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { viewModel.navigate(ScreenRoute.QUICK_TEMPLATES) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    state.studyLists
                        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
                        .forEach { sl ->
                            val thumbRes = resolveCountrysideDrawable(sl.thumbnail, isDark)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, border, RoundedCornerShape(14.dp))
                                    .clickable {
                                        sl.wordIds.firstOrNull()?.let { id ->
                                            LexiconData.getWordById(id)?.let { viewModel.openWord(it) }
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Image(
                                    painter = painterResource(thumbRes),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(56.dp)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(9.dp)),
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(sl.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.5.sp), maxLines = 1)
                                    Text("${sl.totalWords} words • ${sl.percentComplete}% complete", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(border),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(sl.percentComplete / 100f)
                                            .clip(CircleShape)
                                            .background(if (isDark) DarkAccent else BrandGreen),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) DarkAccent else BrandGreen)
                                        .clickable(role = Role.Button) { viewModel.advanceStudyList(sl.id) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Practice", tint = if (isDark) DarkBackground else Color.White, modifier = Modifier.size(16.dp))
                                }
                                Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
                            }
                        }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "Knowledge is a journey, not a destination.",
                attribution = "Lexicon",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * Page 7: Quick Templates — Ready-made learning and writing templates, plus custom template creation.
 */
@Composable
fun QuickTemplatesScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val filtered = state.templates.filter { t ->
        (filter == "All" || t.category.label == filter) &&
            (query.isBlank() || t.title.contains(query, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Quick Templates",
                subtitle = "Start with a template, or create your own.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.MY_NOTES) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 142.dp,
            )
        }

        item(key = "search") {
            CountrysideSearchPill(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search templates...",
                isDark = isDark,
                showFilter = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "pills") {
            CountrysideFilterPills(
                items = listOf(
                    "All" to null,
                    "Learning" to Icons.AutoMirrored.Outlined.MenuBook,
                    "Writing" to Icons.Outlined.Edit,
                    "Exam" to Icons.Outlined.School,
                    "Daily Life" to Icons.Outlined.Lightbulb,
                ),
                selected = filter,
                isDark = isDark,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Popular Templates 2x2 Grid
        item(key = "popular_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Popular Templates",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { filter = "All"; query = "" },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filtered.take(4).chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { tpl ->
                                val artRes = resolveCountrysideDrawable(tpl.illustration, isDark)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.addNote(NoteCategory.STUDY, tpl.title, tpl.subtitle)
                                            viewModel.navigate(ScreenRoute.MY_NOTES)
                                        },
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().height(76.dp)) {
                                        Image(
                                            painter = painterResource(artRes),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(cardBg.copy(alpha = 0.88f))
                                                .clickable { viewModel.toggleTemplateStar(tpl.id) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (tpl.starred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(13.dp),
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tpl.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(tpl.subtitle, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp, lineHeight = 12.sp), maxLines = 2)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, border, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Create Your Own ("+ New Template")
        item(key = "create_own") {
            val floralsRes = resolveCountrysideDrawable("ill_note_flowers", isDark)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Create Your Own",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { viewModel.createCustomTemplate("Countryside Reading", "Collect poetic nature words.", TemplateCategory.WRITING) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, border, RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) {
                            viewModel.createCustomTemplate("Botanical Lexicon", "Custom village field-notes template.", TemplateCategory.LEARNING)
                        },
                ) {
                    Image(
                        painter = painterResource(floralsRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(104.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .border(1.dp, accent, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("New Template", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                            Text("Create a custom template for your needs.", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 10.5.sp))
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "Better words. A brighter mind.",
                attribution = "Lexicon",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * Page 8: Search & Discovery — Search suggestions, recent searches, filters, language selection & search history.
 */
@Composable
fun SearchDiscoveryScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey

    var draft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Search",
                subtitle = "Find any word, explore its meaning, and discover more.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION) },
                onLogoToggleTheme = { viewModel.toggleTheme() },
                heroHeight = 142.dp,
            )
        }

        item(key = "search_bar") {
            CountrysideSearchPill(
                value = draft,
                onValueChange = { draft = it },
                placeholder = "Search for a word...",
                isDark = isDark,
                showMicOnly = true,
                onSubmit = {
                    val q = draft.ifBlank { "serendipity" }
                    viewModel.submitSearch(q)
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item(key = "scope_tabs") {
            CountrysideFilterPills(
                items = listOf(
                    "All" to null,
                    "Words" to Icons.AutoMirrored.Outlined.MenuBook,
                    "Meanings" to Icons.AutoMirrored.Outlined.List,
                    "Examples" to Icons.Outlined.FormatQuote,
                    "Idioms" to Icons.Outlined.StarOutline,
                ),
                selected = state.searchScopeTab,
                isDark = isDark,
                onSelect = { viewModel.setSearchScopeTab(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Live Results OR Recent Searches / Idioms list
        item(key = "recent_or_live") {
            val trimmed = draft.trim()
            if (trimmed.isNotEmpty()) {
                val matches = viewModel.searchWords(trimmed, limit = 6)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotanicalSectionTitle(
                        title = "Suggestions (${matches.size})",
                        isDark = isDark,
                        actionLabel = "Full Results",
                        onAction = { viewModel.submitSearch(trimmed) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(16.dp)),
                    ) {
                        if (matches.isEmpty()) {
                            Text(
                                text = "No offline match for “$trimmed” with current filters.",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                                modifier = Modifier.padding(16.dp),
                            )
                        } else {
                            matches.forEachIndexed { i, w ->
                                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.submitSearch(w.word) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(w.word.lowercase(), color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                        Text("${w.partOfSpeech.label()}  •  ${w.phonetic}", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontStyle = FontStyle.Italic, fontSize = 11.sp))
                                    }
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else if (state.searchScopeTab == "Idioms") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotanicalSectionTitle(
                        title = "Countryside Idioms & Phrases",
                        isDark = isDark,
                        actionLabel = "All Words",
                        onAction = { viewModel.setSearchScopeTab("All") },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(16.dp)),
                    ) {
                        LexiconData.idiomsAndPhrases.forEachIndexed { idx, ip ->
                            if (idx > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("${ip.phrase} (${ip.kind})", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.sp))
                                Text(ip.meaning, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp))
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotanicalSectionTitle(
                        title = "Recent Searches",
                        isDark = isDark,
                        actionLabel = if (state.recentSearches.isEmpty()) null else "Clear All",
                        isClearAction = true,
                        onAction = { viewModel.clearRecentSearches() },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(16.dp)),
                    ) {
                        if (state.recentSearches.isEmpty()) {
                            Text(
                                text = "Your search history is clear. Tap any word or filter below to explore.",
                                color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                                modifier = Modifier.padding(16.dp),
                            )
                        } else {
                            state.recentSearches.take(5).forEachIndexed { idx, term ->
                                val entry = LexiconData.getWordById(term)
                                if (idx > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.submitSearch(term) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(term.lowercase(), color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                        Text(
                                            text = "${entry?.partOfSpeech?.label() ?: "noun"}  •  ${entry?.phonetic ?: "/ˈlɛksɪkɒn/"}",
                                            color = muted,
                                            style = TextStyle(fontFamily = BodyFontFamily, fontStyle = FontStyle.Italic, fontSize = 10.5.sp),
                                        )
                                    }
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Filters (4 interactive tiles: Language, Part of Speech, Difficulty, Sort By)
        item(key = "search_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Search Filters",
                    isDark = isDark,
                    actionLabel = null,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val filters = listOf(
                    QuadFilterSpec(Icons.Outlined.Language, "Language", state.filterLanguage, "language"),
                    QuadFilterSpec(Icons.AutoMirrored.Outlined.MenuBook, "Part of Speech", state.filterPos, "pos"),
                    QuadFilterSpec(Icons.Outlined.BarChart, "Difficulty", state.filterDifficulty, "difficulty"),
                    QuadFilterSpec(Icons.Outlined.StarOutline, "Sort By", state.filterSort, "sort"),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    filters.forEach { f ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp))
                                .clickable(role = Role.Button) { viewModel.cycleSearchFilter(f.key) }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(f.icon, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(16.dp))
                            Text(f.title, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 8.5.sp), maxLines = 1)
                            Text("${f.value} ⌄", color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 9.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "A word is a doorway to a bigger world.",
                attribution = "Lexicon",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

private data class QuadFilterSpec(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val value: String,
    val key: String,
)
