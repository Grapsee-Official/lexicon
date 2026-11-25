package com.lexicon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lexicon.ui.components.rememberCountrysidePainter
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
 * Compute columns from usable width rather than dropping to one column for
 * every enlarged-text device. A tablet at 800dp and fontScale=1.3 has room
 * for two illustrated bookplates; a 320dp phone does not.
 *
 * Aligns with the native SwiftUI adaptive grid minimums (165/260dp).
 */
internal fun countrysideAdaptiveColumns(
    screenWidthDp: Int,
    fontScale: Float,
    maxColumns: Int = 4,
    accessibleMinCardWidthDp: Int = 260,
): Int {
    val minCardWidthDp = if (fontScale >= 1.3f) accessibleMinCardWidthDp else 165
    val usableWidthDp = (screenWidthDp - 32).coerceAtLeast(0) // 16dp page gutters
    return ((usableWidthDp + 8) / (minCardWidthDp + 8)).coerceIn(1, maxColumns)
}

/**
 * Page 4: Collections — Organise favourites, bookmarks, notes and personal vocabulary collections.
 */
@Composable
fun CollectionsHubScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val bg = if (isDark) DarkBackground else WarmCream
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var filter by remember(state.collectionFilter) { mutableStateOf(state.collectionFilter) }
    var showAllRecent by remember { mutableStateOf(false) }

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
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
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
                onFilterClick = {
                    val options = listOf("All", "Favorites", "Bookmarks")
                    filter = options[(options.indexOf(filter) + 1) % options.size]
                },
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
                    actionLabel = null,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val cards = listOf(
                    QuadCollectionSpec("Favorites", "${state.favoriteWordIds.size} words", Icons.Outlined.FavoriteBorder, "ill_col_favorites") {
                        filter = "Favorites"
                    },
                    QuadCollectionSpec("Bookmarks", "${state.savedWordIds.size} words", Icons.Outlined.BookmarkBorder, "ill_col_bookmarks") {
                        filter = "Bookmarks"
                    },
                    QuadCollectionSpec("My Notes", "${state.notes.size} notes", Icons.Outlined.Description, "ill_col_notes") {
                        viewModel.navigate(ScreenRoute.MY_NOTES)
                    },
                    QuadCollectionSpec("Study List", "${state.studyLists.sumOf { it.totalWords }} words", Icons.Outlined.School, "ill_col_studylist") {
                        viewModel.navigate(ScreenRoute.STUDY_LISTS)
                    },
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val config = androidx.compose.ui.platform.LocalConfiguration.current
                    val collectionColumns = countrysideAdaptiveColumns(
                        config.screenWidthDp, config.fontScale, maxColumns = 2,
                    )
                    cards.chunked(collectionColumns).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowCards.forEach { spec ->
                                val artRes = resolveCountrysideDrawable(spec.art, isDark)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f) 146.dp else 112.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(16.dp))
                                        .clickable(role = Role.Button) { spec.onClick() },
                                ) {
                                    Image(
                                        painter = painterResource(artRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.BottomEnd,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.86f),
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
                                            .size(24.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        // Two strokes outline the arrow itself, not the artwork:
                                        // the full watercolor remains visible around the icon.
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = null,
                                            tint = if (isDark) DarkBackground else Color.White,
                                            modifier = Modifier.size(21.dp),
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = null,
                                            tint = fg,
                                            modifier = Modifier.size(17.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent saved entries; the Favorites and Bookmarks filters show real user state.
        item(key = "recently_added") {
            val ids = when (filter) {
                "Favorites" -> state.favoriteWordIds
                "Bookmarks" -> state.savedWordIds
                else -> (state.savedWordIds + state.favoriteWordIds).distinct()
            }
            val thumbnails = listOf("ill_thumb_lavender", "ill_thumb_cottage", "ill_thumb_bridge")
            val recentDefs = ids.mapIndexed { index, id ->
                id to thumbnails[index % thumbnails.size]
            }.filter { (id, _) ->
                query.isBlank() || id.contains(query.trim(), ignoreCase = true)
            }.let { if (showAllRecent) it else it.take(3) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Recently Added",
                    isDark = isDark,
                    actionLabel = if (showAllRecent) "Show less" else "View all",
                    onAction = { showAllRecent = !showAllRecent },
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
                                contentScale = ContentScale.Fit,
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
                attribution = "Lexora",
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
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var showAllNotes by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<com.lexicon.data.NoteItem?>(null) }
    var editorTitle by remember { mutableStateOf("") }
    var editorBody by remember { mutableStateOf("") }

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
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Notes",
                subtitle = "Capture thoughts, meanings and your language journey.",
                illustrationBase = "ill_hero_notes",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY) },
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
                onFilterClick = {
                    val options = listOf("All", "Personal", "Word Notes", "Study", "Ideas")
                    selectedCat = options[(options.indexOf(selectedCat) + 1) % options.size]
                },
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
                    actionLabel = if (showAllNotes) "Show less" else "View all",
                    onAction = { selectedCat = "All"; query = ""; showAllNotes = !showAllNotes },
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
                        val config = androidx.compose.ui.platform.LocalConfiguration.current
                        val noteColumns = countrysideAdaptiveColumns(
                            config.screenWidthDp, config.fontScale, maxColumns = 2,
                        )
                        (if (showAllNotes) filteredNotes else filteredNotes.take(4))
                            .chunked(noteColumns)
                            .forEach { rowNotes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowNotes.forEach { note ->
                                    val noteScene = when (note.category) {
                                        NoteCategory.WORD_NOTE -> "notes-flowers"
                                        NoteCategory.STUDY -> "notes-books"
                                        NoteCategory.PERSONAL -> "notes-quill"
                                        NoteCategory.IDEA -> "notes-lantern"
                                    }
                                    val artRes = resolveCountrysideDrawable(note.illustration ?: "ill_note_flowers", isDark, noteScene)
                                    var menuOpen by remember(note.id) { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .then(if (config.fontScale >= 1.3f) Modifier.heightIn(min = 218.dp) else Modifier.aspectRatio(368f / 249f))
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(cardBg)
                                            .border(1.dp, border, RoundedCornerShape(16.dp))
                                            .clickable {
                                                editorTitle = note.title
                                                editorBody = note.body
                                                editingNote = note
                                            },
                                    ) {
                                        Image(
                                            painter = rememberCountrysidePainter(artRes, 112.dp),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            alignment = Alignment.BottomEnd,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                 .fillMaxHeight(0.76f)
                                                // Preserve the complete original watercolor in
                                                // its own right-hand zone, without a cream mask.
                                                .fillMaxWidth(if (config.fontScale >= 1.3f) 0.32f else 0.42f),
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
                                                Box {
                                                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "Note options", tint = muted,
                                                        modifier = Modifier.size(24.dp).clickable(role = Role.Button) { menuOpen = true })
                                                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                                        DropdownMenuItem(text = { Text("Edit note") }, onClick = {
                                                            menuOpen = false
                                                            editorTitle = note.title
                                                            editorBody = note.body
                                                            editingNote = note
                                                        })
                                                        DropdownMenuItem(text = { Text(if (note.bookmarked) "Remove bookmark" else "Bookmark note") },
                                                            onClick = { menuOpen = false; viewModel.toggleNoteBookmark(note.id) })
                                                    }
                                                }
                                            }
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                            ) {
                                                Text(note.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 14.sp), maxLines = 2, overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.fillMaxWidth(.88f))
                                                Text(note.body, color = fg.copy(alpha = 0.88f), style = TextStyle(fontFamily = LongFontFamily, fontSize = 11.sp, lineHeight = 14.sp),
                                                    modifier = Modifier.fillMaxWidth(if (config.fontScale >= 1.3f) .68f else .62f),
                                                    maxLines = if (config.fontScale >= 1.3f) 4 else 3, overflow = TextOverflow.Ellipsis)
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
                                                    contentDescription = if (note.bookmarked) "Remove note bookmark" else "Bookmark note",
                                                    tint = if (note.bookmarked) accent else muted,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clickable(role = Role.Button) { viewModel.toggleNoteBookmark(note.id) },
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(noteColumns - rowNotes.size) {
                                    Spacer(Modifier.weight(1f))
                                }
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
                                .heightIn(min = 49.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(cardBg)
                                .border(1.dp, border, RoundedCornerShape(13.dp))
                                .clickable(role = Role.Button) {
                                    editorTitle = "New $label"
                                    editorBody = ""
                                    editingNote = com.lexicon.data.NoteItem(
                                        id = "__new_note__", category = cat,
                                        title = editorTitle, body = "",
                                        dateLabel = "",
                                    )
                                }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(ic, contentDescription = null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "A well-written note is a small step towards wisdom.",
                attribution = "Lexora",
                illustrationBase = "ill_banner_church",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    editingNote?.let { note ->
        AlertDialog(
            onDismissRequest = { editingNote = null },
            title = { Text(if (note.id == "__new_note__") "Create note" else "Edit note", color = fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editorTitle,
                        onValueChange = { editorTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editorBody,
                        onValueChange = { editorBody = it },
                        label = { Text("Your note") },
                        minLines = 4,
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = editorTitle.isNotBlank(), onClick = {
                    if (note.id == "__new_note__") {
                        viewModel.addNote(note.category, editorTitle, editorBody)
                    } else {
                        viewModel.updateNote(note.id, editorTitle, editorBody)
                    }
                    editingNote = null
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (note.id != "__new_note__") {
                        TextButton(onClick = {
                            viewModel.deleteNote(note.id)
                            editingNote = null
                        }) { Text("Delete") }
                    }
                    TextButton(onClick = { editingNote = null }) { Text("Cancel") }
                }
            },
            containerColor = cardBg,
        )
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
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var practicingListId by remember { mutableStateOf<String?>(null) }
    var answerRevealed by remember { mutableStateOf(false) }
    var showCreateList by remember { mutableStateOf(false) }
    var showStudyFilter by remember { mutableStateOf(false) }
    var studyFilter by remember { mutableStateOf("All lists") }
    var newListTitle by remember { mutableStateOf("") }
    var newListWords by remember { mutableStateOf("") }
    var listError by remember { mutableStateOf(false) }
    var expandedListMenu by remember { mutableStateOf<String?>(null) }
    val totalWords = state.studyLists.sumOf { it.totalWords }
    val overallPct = if (totalWords > 0) (state.studyLists.sumOf { it.completedWords } * 100) / totalWords else 0
    val booksRes = resolveCountrysideDrawable("ill_books_daisies", isDark, "study-books")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Study List",
                subtitle = "Learn, practice and grow one word at a time.",
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
                placeholder = "Search your study lists...",
                isDark = isDark,
                showFilter = true,
                onFilterClick = { showStudyFilter = true },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // 4 Stat Cards (Active Lists, Total Words, Overall Progress, Day Streak)
        item(key = "stats") {
            val stats = listOf(
                Triple(Icons.AutoMirrored.Outlined.MenuBook, "${state.studyLists.size}", "Active Lists"),
                Triple(Icons.Outlined.CheckCircleOutline, "$totalWords", "Total Words"),
                Triple(Icons.Outlined.BarChart, "$overallPct%", "Overall Progress"),
                Triple(Icons.Outlined.StarOutline, "${state.dayStreak}", "Day Streak"),
            )
            val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val columns = if (fontScale >= 1.4f || maxWidth < 270.dp) 2 else 4
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    stats.chunked(columns).forEach { group ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            group.forEach { (ic, num, label) ->
                                Column(Modifier.weight(1f).heightIn(min = 65.dp)
                                    .clip(RoundedCornerShape(14.dp)).background(cardBg)
                                    .border(1.dp, border, RoundedCornerShape(14.dp))
                                    .padding(vertical = 9.dp, horizontal = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)) {
                                    Icon(ic, null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(18.dp))
                                    Text(num, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp))
                                    Text(label, color = muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Study Card (6/10 circular progress ring + Keep going! + stacked books art)
        item(key = "todays_study") {
            val ringColor = if (isDark) DarkAccent else BrandGreen
            val enlarged = androidx.compose.ui.platform.LocalConfiguration.current.fontScale >= 1.3f
            val trackColor = border
            val progress = (state.todayStudiedWords.toFloat() / state.todayGoalWords.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

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
                    onAction = {
                        practicingListId = state.studyLists.firstOrNull {
                            it.wordIds.isNotEmpty() && it.completedWords < it.wordIds.size
                        }?.id
                        answerRevealed = false
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(if (enlarged) 100.dp else 76.dp), contentAlignment = Alignment.Center) {
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

                    Box(Modifier.width(1.dp).height(66.dp).background(border))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(if (progress >= 1f) "Goal reached!" else "Keep going!", color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 15.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text(
                            text = "“Small steps\nlead to big progress.”",
                            color = muted,
                            style = TextStyle(fontFamily = LongFontFamily, fontSize = 10.5.sp, lineHeight = 14.sp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }

                    if (!enlarged) Image(
                        painter = rememberCountrysidePainter(booksRes, 112.dp),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomEnd,
                        modifier = Modifier.width(112.dp).height(88.dp),
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
                    actionLabel = "New list",
                    onAction = { listError = false; showCreateList = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    val visibleStudyLists = state.studyLists.filter {
                        (query.isBlank() || it.title.contains(query, ignoreCase = true)) &&
                            when (studyFilter) {
                                "In progress" -> it.completedWords < it.totalWords
                                "Completed" -> it.totalWords > 0 && it.completedWords >= it.totalWords
                                else -> true
                            }
                    }
                    if (visibleStudyLists.isEmpty()) {
                        Text("No study lists match your search.", color = muted,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                            modifier = Modifier.padding(12.dp))
                    }
                    visibleStudyLists.forEach { sl ->
                            val scene = when (sl.thumbnail) {
                                "ill_thumb_lavender" -> "study-vocab"
                                "ill_thumb_cottage" -> "study-exam"
                                "ill_thumb_books" -> "study-advanced"
                                "ill_thumb_meadow" -> "study-travel"
                                else -> null
                            }
                            val thumbRes = resolveCountrysideDrawable(sl.thumbnail, isDark, scene)
                            BoxWithConstraints(Modifier.fillMaxWidth()) {
                            val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
                            val narrow = maxWidth < 330.dp || fontScale >= 1.2f
                            val barWidth = (maxWidth * (210f / 760f)).coerceIn(56.dp, 112.dp)
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
                                    .padding(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                Image(
                                    painter = rememberCountrysidePainter(thumbRes, 60.dp),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .width(if (narrow) 48.dp else 60.dp)
                                        .height(if (narrow) 48.dp else 44.dp)
                                        .clip(RoundedCornerShape(9.dp)),
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(sl.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.5.sp), maxLines = if (narrow) 3 else 2, overflow = TextOverflow.Ellipsis)
                                    Text("${sl.totalWords} words • ${sl.percentComplete}% complete", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                                    if (narrow) StudyListProgress(sl.percentComplete, border, ringTint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.fillMaxWidth())
                                }
                                if (!narrow) StudyListProgress(sl.percentComplete, border, ringTint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.width(barWidth))
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable(role = Role.Button) { practicingListId = sl.id; answerRevealed = false },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(Modifier.size(26.dp).clip(CircleShape).background(if (isDark) DarkAccent else BrandGreen), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = "Practice ${sl.title}", tint = if (isDark) DarkBackground else Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Box {
                                    Icon(
                                        Icons.Outlined.MoreVert, contentDescription = "Manage list",
                                        tint = muted,
                                        modifier = Modifier.size(24.dp).clickable { expandedListMenu = sl.id },
                                    )
                                    DropdownMenu(
                                        expanded = expandedListMenu == sl.id,
                                        onDismissRequest = { expandedListMenu = null },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Reset progress") },
                                            onClick = { viewModel.resetStudyList(sl.id); expandedListMenu = null },
                                        )
                                        if (sl.id.startsWith("sl_custom_")) {
                                            DropdownMenuItem(
                                                text = { Text("Delete list") },
                                                onClick = { viewModel.deleteStudyList(sl.id); expandedListMenu = null },
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

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "Knowledge is a journey, not a destination.",
                attribution = "Lexora",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    if (showStudyFilter) {
        AlertDialog(
            onDismissRequest = { showStudyFilter = false },
            title = { Text("Study lists") },
            text = {
                Column {
                    listOf("All lists", "In progress", "Completed").forEach { option ->
                        TextButton(onClick = {
                            studyFilter = option
                            showStudyFilter = false
                        }) { Text(if (studyFilter == option) "✓ $option" else option) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showStudyFilter = false }) { Text("Done") } },
        )
    }

    if (showCreateList) {
        AlertDialog(
            onDismissRequest = { showCreateList = false; listError = false },
            title = { Text("New study list", color = fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Gather words into your own countryside notebook.", color = muted,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                    )
                    OutlinedTextField(
                        value = newListTitle, onValueChange = { newListTitle = it },
                        label = { Text("List name") }, singleLine = true,
                    )
                    OutlinedTextField(
                        value = newListWords, onValueChange = { newListWords = it; listError = false },
                        label = { Text("Words, separated by commas") }, minLines = 2,
                    )
                    Text(
                        "Leave words blank to use bookmarks. Only known offline words are added.",
                        color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp),
                    )
                    if (listError) {
                        Text(
                            "No matching words found. Try Serendipity, Solitude or Wanderlust.",
                            color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 12.sp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newListTitle.isNotBlank(),
                    onClick = {
                        if (viewModel.createStudyList(newListTitle, newListWords)) {
                            showCreateList = false
                            newListTitle = ""
                            newListWords = ""
                            query = ""
                        } else listError = true
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateList = false; listError = false }) { Text("Cancel") }
            },
            containerColor = cardBg,
        )
    }

    // Study progress changes only after the answer is revealed and confirmed.
    practicingListId?.let { listId ->
        val list = state.studyLists.firstOrNull { it.id == listId }
        val entry = list?.wordIds?.getOrNull(list.completedWords)?.let { LexiconData.getWordById(it) }
        AlertDialog(
            onDismissRequest = { practicingListId = null; answerRevealed = false },
            title = { Text(list?.title ?: "Study session", color = fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (entry != null) {
                        Text(entry.word, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 24.sp))
                        Text(entry.phonetic, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp))
                        if (answerRevealed) {
                            Text(entry.definitions.firstOrNull().orEmpty(), color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 15.sp))
                            entry.examples.firstOrNull()?.let { example ->
                                Text(example, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp))
                            }
                        } else {
                            Text("Recall the meaning, then reveal the answer.", color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp))
                        }
                    } else {
                        Text("All available words reviewed. Beautiful work!", color = fg, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 14.sp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (entry == null) {
                        practicingListId = null
                    } else if (!answerRevealed) {
                        answerRevealed = true
                    } else {
                        viewModel.advanceStudyList(listId)
                        answerRevealed = false
                        if ((list?.completedWords ?: 0) + 1 >= (list?.wordIds?.size ?: 0)) practicingListId = null
                    }
                }) { Text(if (entry == null) "Done" else if (answerRevealed) "Mark learned" else "Reveal meaning") }
            },
            dismissButton = { TextButton(onClick = { practicingListId = null; answerRevealed = false }) { Text("Close") } },
            containerColor = cardBg,
        )
    }
}

@Composable
private fun StudyListProgress(percent: Int, track: Color, ringTint: Color, modifier: Modifier = Modifier) {
    val progress = percent.coerceIn(0, 100) / 100f
    Box(modifier.height(5.dp).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape).background(ringTint))
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
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val accent = if (isDark) DarkAccent else OldWoodGold

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    var showAllTemplates by remember { mutableStateOf(false) }
    var showCreateTemplate by remember { mutableStateOf(false) }
    var templateForNote by remember { mutableStateOf<com.lexicon.data.VocabTemplate?>(null) }
    var templateNoteTitle by remember { mutableStateOf("") }
    var templateNoteBody by remember { mutableStateOf("") }
    var newTemplateTitle by remember { mutableStateOf("") }
    var newTemplateDescription by remember { mutableStateOf("") }
    var newTemplateCategory by remember { mutableStateOf(TemplateCategory.LEARNING) }
    val filtered = state.templates.filter { t ->
        (filter == "All" || t.category.label == filter) &&
            (query.isBlank() || t.title.contains(query, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Quick Templates",
                subtitle = "Start with a template, or create your own.",
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
                placeholder = "Search templates...",
                isDark = isDark,
                showFilter = true,
                onFilterClick = {
                    val options = listOf("All", "Learning", "Writing", "Exam", "Daily Life")
                    filter = options[(options.indexOf(filter) + 1) % options.size]
                },
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
                    actionLabel = if (showAllTemplates) "Show less" else "View all",
                    onAction = { filter = "All"; query = ""; showAllTemplates = !showAllTemplates },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (filtered.isEmpty()) {
                        Text("No templates match your search or category.", color = muted,
                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                            modifier = Modifier.padding(12.dp))
                    }
                    val config = androidx.compose.ui.platform.LocalConfiguration.current
                    val templateColumns = countrysideAdaptiveColumns(
                        config.screenWidthDp,
                        config.fontScale,
                        maxColumns = 2,
                        accessibleMinCardWidthDp = 270,
                    )
                    (if (showAllTemplates) filtered else filtered.take(4))
                        .chunked(templateColumns)
                        .forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { tpl ->
                                val scene = when (tpl.illustration) {
                                    "ill_tpl_daily" -> "template-vocab"
                                    "ill_tpl_exam" -> "template-exam"
                                    "ill_tpl_write" -> "template-writing"
                                    "ill_tpl_travel" -> "template-travel"
                                    else -> null
                                }
                                val artRes = resolveCountrysideDrawable(tpl.illustration, isDark, scene)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(16.dp))
                                        .clickable {
                                            templateNoteTitle = tpl.title
                                            templateNoteBody = tpl.sampleWords.joinToString(", ").ifBlank { tpl.subtitle }
                                            templateForNote = tpl
                                        },
                                ) {
                                    // Preserve the original 370:152 painting at every width.
                                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(370f / 152f)) {
                                        Image(
                                            painter = rememberCountrysidePainter(artRes, 200.dp),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                // No badge/cream patch: accessible native tap target.
                                                .size(44.dp)
                                                .clickable(role = Role.Button) {
                                                    viewModel.toggleTemplateStar(tpl.id)
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(Modifier.size(22.dp).clip(CircleShape).background(cardBg),
                                                contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (tpl.starred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                    contentDescription = if (tpl.starred) "Unstar ${tpl.title}" else "Star ${tpl.title}",
                                                    tint = accent,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                    }
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 47.dp)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tpl.title, color = fg, style = TextStyle(fontFamily = DisplayFontFamily, fontSize = 13.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(tpl.subtitle, color = muted, style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp, lineHeight = 14.sp), maxLines = 2)
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
                            repeat(templateColumns - rowItems.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Create Your Own ("+ New Template")
        item(key = "create_own") {
            val floralsRes = resolveCountrysideDrawable("ill_note_flowers", isDark, "template-create")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotanicalSectionTitle(
                    title = "Create Your Own",
                    isDark = isDark,
                    actionLabel = "View all",
                    onAction = { showCreateTemplate = true },
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
                            showCreateTemplate = true
                        },
                ) {
                    Image(
                        painter = rememberCountrysidePainter(floralsRes, 132.dp),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(132.dp),
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
                attribution = "Lexora",
                illustrationBase = "ill_banner_church",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    if (showCreateTemplate) {
        AlertDialog(
            onDismissRequest = { showCreateTemplate = false },
            title = { Text("New template", color = fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedTextField(
                        value = newTemplateTitle,
                        onValueChange = { newTemplateTitle = it },
                        label = { Text("Template name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newTemplateDescription,
                        onValueChange = { newTemplateDescription = it },
                        label = { Text("Description") },
                        minLines = 2,
                    )
                    TemplateCategory.entries.chunked(2).forEach { categories ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { category ->
                                TextButton(onClick = { newTemplateCategory = category }) {
                                    Text(category.label,
                                        color = if (newTemplateCategory == category) fg else muted,
                                        style = TextStyle(fontSize = 11.sp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newTemplateTitle.isNotBlank(),
                    onClick = {
                        viewModel.createCustomTemplate(newTemplateTitle, newTemplateDescription, newTemplateCategory)
                        showCreateTemplate = false
                        newTemplateTitle = ""
                        newTemplateDescription = ""
                        filter = "All"
                        query = ""
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTemplate = false }) { Text("Cancel") }
            },
            containerColor = cardBg,
        )
    }

    templateForNote?.let { template ->
        AlertDialog(
            onDismissRequest = { templateForNote = null },
            title = { Text("Write from template", color = fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(template.subtitle, color = muted,
                        style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp))
                    OutlinedTextField(
                        value = templateNoteTitle,
                        onValueChange = { templateNoteTitle = it },
                        label = { Text("Note title") }, singleLine = true,
                    )
                    OutlinedTextField(
                        value = templateNoteBody,
                        onValueChange = { templateNoteBody = it },
                        label = { Text("Your thoughts") }, minLines = 4,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = templateNoteTitle.isNotBlank(),
                    onClick = {
                        val cat = when (template.category) {
                            TemplateCategory.LEARNING, TemplateCategory.EXAM -> NoteCategory.STUDY
                            TemplateCategory.WRITING -> NoteCategory.WORD_NOTE
                            TemplateCategory.DAILY_LIFE -> NoteCategory.PERSONAL
                        }
                        viewModel.addNote(cat, templateNoteTitle, templateNoteBody)
                        templateForNote = null
                        viewModel.navigate(ScreenRoute.MY_NOTES)
                    },
                ) { Text("Save note") }
            },
            dismissButton = {
                TextButton(onClick = { templateForNote = null }) { Text("Cancel") }
            },
            containerColor = cardBg,
        )
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
    val cardBg = if (isDark) DarkCard else Color(0xFFFFF2DF).copy(alpha = .72f)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .28f)
    val fg = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey

    var draft by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<QuadFilterSpec?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            CountrysideHeroHeader(
                title = "Search",
                subtitle = "Find any word, explore its meaning, and discover more.",
                illustrationBase = "ill_hero_cottage",
                isDark = isDark,
                onHistoryClick = { viewModel.navigate(ScreenRoute.DICTIONARY_HOME) },
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
                    if (draft.isNotBlank()) viewModel.submitSearch(draft)
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
            if (trimmed.isNotEmpty() && state.searchScopeTab != "Idioms") {
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
                                        .clickable {
                                            viewModel.addRecentSearch(w.word)
                                            viewModel.openWord(w)
                                        }
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
                        val idiomMatches = LexiconData.idiomsAndPhrases.filter { ip ->
                            trimmed.isEmpty() ||
                                ip.phrase.contains(trimmed, ignoreCase = true) ||
                                ip.meaning.contains(trimmed, ignoreCase = true) ||
                                ip.example.contains(trimmed, ignoreCase = true)
                        }
                        if (idiomMatches.isEmpty()) {
                            Text("No matching idioms. Try another expression.", color = muted,
                                style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                                modifier = Modifier.padding(14.dp))
                        }
                        idiomMatches.forEachIndexed { idx, ip ->
                            if (idx > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(border))
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(role = Role.Button) { viewModel.submitSearch(ip.phrase) }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
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
                                            text = entry?.let { "${it.partOfSpeech.label()}  •  ${it.phonetic}" } ?: "Search again for word details",
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
                val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val columns = if (fontScale >= 1.4f || maxWidth < 270.dp) 2 else 4
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        filters.chunked(columns).forEach { group ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                group.forEach { f ->
                                    Column(Modifier.weight(1f).heightIn(min = 65.dp)
                                        .clip(RoundedCornerShape(13.dp)).background(cardBg)
                                        .border(1.dp, border, RoundedCornerShape(13.dp))
                                        .clickable(role = Role.Button) { selectedFilter = f }
                                        .padding(vertical = 8.dp, horizontal = 3.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)) {
                                        Icon(f.icon, null, tint = if (isDark) DarkAccent else BrandGreen, modifier = Modifier.size(17.dp))
                                        Text(f.title, color = muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                                        Text("${f.value} ⌄", color = fg, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            style = TextStyle(fontFamily = BodyFontFamily, fontSize = 9.5.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "quote") {
            CountrysideQuoteBanner(
                quote = "A word is a doorway to a bigger world.",
                attribution = "Lexora",
                illustrationBase = "ill_banner_bridge",
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    selectedFilter?.let { filter ->
        AlertDialog(
            onDismissRequest = { selectedFilter = null },
            title = { Text(filter.title) },
            text = {
                Column {
                    viewModel.searchFilterOptions(filter.key).forEach { option ->
                        TextButton(onClick = {
                            viewModel.selectSearchFilter(filter.key, option)
                            selectedFilter = null
                        }) { Text(if (option == filter.value) "✓ $option" else option) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedFilter = null }) { Text("Done") } },
        )
    }
}

private data class QuadFilterSpec(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val value: String,
    val key: String,
)
