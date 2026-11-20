package com.lexicon.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.LexiconData
import com.lexicon.data.ScreenRoute
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.Terracotta

/**
 * MoreScreen — 1:1 translation of the web more-screen.tsx.
 *
 * Layout:
 *   1. Profile header card — avatar (64dp circular), "Curious Soul" name,
 *      "Lexicon Companion · since 2026" subtitle, saved count + recent count.
 *   2. Settings groups (uppercase muted header + card):
 *        APPEARANCE    — Theme (toggle), Text size, Notifications
 *        DICTIONARY    — Language pairs, Storage, Download language packs
 *        PRIVACY & DATA— Clear recent searches, Clear cache
 *        ABOUT         — About Lexicon, Privacy policy, Open source licences
 *   3. Footer — "Crafted in the spirit of the Cotswolds" + tagline.
 *
 * Theme toggle reads vm.uiState.isDark and calls vm.toggleTheme().
 * "Clear recent searches" calls vm.clearRecentSearches() and shows the
 * live count from vm.uiState.recentSearches.size.
 */
@Composable
fun MoreScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsState()
    val isDark = state.isDark
    val context = LocalContext.current
    var infoDialog by remember { mutableStateOf<String?>(null) }
    var readingPage by remember { mutableStateOf<String?>(null) }
    if (readingPage != null) {
        if (readingPage == "journey") {
            MoreJourneyPage(viewModel = viewModel, onBack = { readingPage = null })
        } else {
            MoreReaderPage(
                page = readingPage!!,
                onBack = { readingPage = null },
            )
        }
        return
    }

    // Screen entrance animation: fadeIn + slideUp (y: 12dp → 0dp)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "screen-alpha",
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        animationSpec = tween(300),
        label = "screen-offset-y",
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .offset(y = offsetY),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 162.dp, // clear floating BottomNav and reveal final content
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // === Profile header ===
        item(key = "profile") {
            ProfileHeader(
                savedCount = state.savedWordIds.size,
                recentCount = state.recentSearches.size,
            )
        }

        // === Appearance ===
        item(key = "appearance") {
            SettingsGroup(title = "Appearance") {
                SettingsRow(
                    icon = if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    iconLabel = "Theme toggle",
                    label = "Theme",
                    value = if (isDark) "Dark" else "Light",
                    onClick = { viewModel.toggleTheme() },
                )
                SettingsRow(
                    icon = Icons.Outlined.Visibility,
                    iconLabel = "Text size",
                    label = "Text size",
                    value = "Device",
                    onClick = { infoDialog = "text" },
                )
                SettingsRow(
                    icon = Icons.Outlined.Notifications,
                    iconLabel = "Notifications",
                    label = "Notifications",
                    value = "Not scheduled",
                    onClick = { infoDialog = "notifications" },
                    isLast = true,
                )
            }
        }

        // === Dictionary ===
        item(key = "dictionary") {
            SettingsGroup(title = "Dictionary") {
                SettingsRow(
                    icon = Icons.Outlined.Language,
                    iconLabel = "Language pairs",
                    label = "Language pairs",
                    value = "${LexiconData.translationsFor(LexiconData.serendipityEntry).size} samples",
                    onClick = { viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION, word = LexiconData.serendipityEntry, expand36 = true) },
                )
                SettingsRow(
                    icon = Icons.Outlined.Storage,
                    iconLabel = "Storage",
                    label = "Storage",
                    value = "On device",
                    onClick = { infoDialog = "storage" },
                )
                SettingsRow(
                    icon = Icons.Outlined.Download,
                    iconLabel = "Download language packs",
                    label = "Download language packs",
                    value = "Unavailable",
                    onClick = { infoDialog = "packs" },
                    isLast = true,
                )
            }
        }

        // === Privacy & Data ===
        item(key = "privacy") {
            SettingsGroup(title = "Privacy & Data") {
                SettingsRow(
                    icon = Icons.Outlined.Description,
                    iconLabel = "Clear recent searches",
                    label = "Clear recent searches",
                    value = if (state.recentSearches.isNotEmpty())
                        "${state.recentSearches.size} items"
                    else "Empty",
                    onClick = { infoDialog = "recent" },
                )
                SettingsRow(
                    icon = Icons.Outlined.Storage,
                    iconLabel = "Clear cache",
                    label = "Clear cache",
                    value = "Temporary files",
                    onClick = { infoDialog = "cache" },
                    isLast = true,
                )
            }
        }

        // A real local reading-history destination, never a placeholder.
        item(key = "journey") {
            SettingsGroup(title = "Your Library") {
                SettingsRow(
                    icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    iconLabel = "Reading journey",
                    label = "Reading journey",
                    value = "${state.recentSearches.size} recent · ${state.savedWordIds.size} saved",
                    onClick = { readingPage = "journey" },
                    isLast = true,
                )
            }
        }

        // === About ===
        item(key = "about") {
            SettingsGroup(title = "About") {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    iconLabel = "About Lexicon",
                    label = "About Lexicon",
                    value = "Native edition",
                    onClick = { readingPage = "about" },
                )
                SettingsRow(
                    icon = Icons.Outlined.Description,
                    iconLabel = "Privacy policy",
                    label = "Privacy & storage",
                    value = "",
                    onClick = { readingPage = "privacy" },
                )
                SettingsRow(
                    icon = Icons.Outlined.Code,
                    iconLabel = "Open source licences",
                    label = "Open source licences",
                    value = "",
                    onClick = { readingPage = "licenses" },
                    isLast = true,
                )
            }
        }

        // === Footer ===
        item(key = "footer") {
            MoreFooter()
        }
    }

    if (infoDialog != null) {
        val title = when (infoDialog) {
            "text" -> "Comfortable reading"
            "notifications" -> "Daily-word reminders"
            "storage" -> "Your offline library"
            "packs" -> "Language packs"
            "recent" -> "Clear search history?"
            "cache" -> "Clear temporary cache?"
            "about" -> "About Lexicon"
            "privacy" -> "Privacy & local storage"
            else -> "Open-source libraries"
        }
        val description = when (infoDialog) {
            "text" -> "Lexicon follows Android font-size and accessibility settings. Open device Display settings to adjust reading size."
            "notifications" -> "Daily notifications are not currently scheduled. Your Word of the Day remains available offline."
            "storage" -> "Saved words, notes, study lists and templates are stored on this device. The bundled dictionary works offline."
            "packs" -> "The English dictionary is bundled. Additional downloadable language packs are not yet supported. The translation view shows only curated available translations."
            "recent" -> "Remove recent searches on this device? Your saved words and notes will be kept."
            "cache" -> "Remove temporary files? Saved words, notes and study progress will remain."
            "about" -> "Lexicon · Words. Wisdom. Worlds. A quiet offline-first language companion inspired by the English countryside."
            "privacy" -> "Word collections, notes, settings and study progress are kept in local app storage. Search history and temporary files can be cleared separately."
            else -> "Lexicon uses Kotlin, AndroidX, Jetpack Compose, SQLite and a shared C++17 dictionary engine. See the license notices of each dependency before redistribution."
        }
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(title) },
            text = { Text(description) },
            confirmButton = {
                TextButton(onClick = {
                    when (infoDialog) {
                        "text" -> context.startActivity(
                            Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        "recent" -> viewModel.clearRecentSearches()
                        "cache" -> context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                    }
                    infoDialog = null
                }) {
                    Text(when (infoDialog) {
                        "text" -> "Display settings"
                        "recent" -> "Clear history"
                        "cache" -> "Clear cache"
                        else -> "Done"
                    })
                }
            },
            dismissButton = {
                if (infoDialog == "text" || infoDialog == "recent" || infoDialog == "cache") {
                    TextButton(onClick = { infoDialog = null }) { Text("Cancel") }
                }
            },
        )
    }
}


/**
 * Local, working reading-journal page. Recent queries and saved word IDs are
 * sourced from the same persisted state as Dictionary and Collections.
 * Each row opens its real lexical destination, not a simulated detail card.
 */
@Composable
private fun MoreJourneyPage(viewModel: LexiconViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    BackHandler(onBack = onBack)
    val ink = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface
    val line = ink.copy(alpha = 0.14f)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 170.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "journey_header") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to More",
                    tint = ink,
                    modifier = Modifier.size(44.dp)
                        .clickable { onBack() }.padding(10.dp),
                )
                Column(Modifier.padding(start = 7.dp)) {
                    Text("Reading Journey", color = ink, fontFamily = DisplayFontFamily, fontSize = 28.sp)
                    Text("Words gathered along the way", color = ink.copy(alpha = 0.75f), fontSize = 13.sp)
                }
            }
        }
        item(key = "journey_recent_title") {
            Text("Recently explored", color = ink, fontFamily = DisplayFontFamily, fontSize = 22.sp)
        }
        if (state.recentSearches.isEmpty()) {
            item(key = "journey_empty_history") {
                Text("Your discoveries will appear here after you search for a word.", color = ink.copy(alpha = 0.8f))
            }
        }
        state.recentSearches.forEachIndexed { index, query ->
            item(key = "journey_recent_${index}_${query}") {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(surface, RoundedCornerShape(14.dp))
                        .border(1.dp, line, RoundedCornerShape(14.dp))
                        .clickable {
                            onBack()
                            val word = LexiconData.getWordById(query.lowercase())
                            if (word == null) viewModel.submitSearch(query) else viewModel.openWord(word)
                        }
                        .padding(horizontal = 15.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.History, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                    Text(query, color = ink, fontFamily = DisplayFontFamily, fontSize = 18.sp,
                        modifier = Modifier.weight(1f).padding(start = 12.dp))
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = ink, modifier = Modifier.size(20.dp))
                }
            }
        }
        item(key = "journey_saved_title") {
            Text("Words kept close", color = ink, fontFamily = DisplayFontFamily, fontSize = 22.sp)
        }
        if (state.savedWordIds.isEmpty()) {
            item(key = "journey_empty_saved") {
                Text("Bookmark a word to gather it in your personal library.", color = ink.copy(alpha = 0.8f))
            }
        }
        state.savedWordIds.forEachIndexed { index, id ->
            val entry = LexiconData.getWordById(id)
            if (entry != null) {
                item(key = "journey_saved_${index}_${id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(surface, RoundedCornerShape(14.dp))
                            .border(1.dp, line, RoundedCornerShape(14.dp))
                            .clickable { onBack(); viewModel.openWord(entry) }
                            .padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(19.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(entry.word, color = ink, fontFamily = DisplayFontFamily, fontSize = 18.sp)
                            Text(entry.phonetic, color = ink.copy(alpha = 0.68f), fontSize = 11.sp)
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = ink, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


/**
 * Real native reading pages behind About / Privacy / Licences.
 * Unlike a one-line alert they are scrollable, accessible, and return with
 * system Back without accidentally changing the selected bottom tab.
 */
@Composable
private fun MoreReaderPage(page: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val title = when (page) {
        "about" -> "About Lexicon"
        "privacy" -> "Privacy & storage"
        else -> "Open-source notices"
    }
    val paragraphs = when (page) {
        "about" -> listOf(
            "Words. Wisdom. Worlds.",
            "Lexicon is a quiet, native reading companion inspired by the old English countryside. Its illustrations, journal notes and dictionary belong to one thoughtful world.",
            "Built for Android with Jetpack Compose and a shared C++17 dictionary engine. The English starter library and your saved words can be used offline."
        )
        "privacy" -> listOf(
            "Your words are yours.",
            "Saved words, study lists, templates, notes and recent searches are stored in the application's local storage. You can clear recent searches separately without deleting your notes or saved words.",
            "Temporary cache files can be cleared in More. Removing the app may remove locally stored material. Keep your own copies of anything precious."
        )
        else -> listOf(
            "A little library is built by many hands.",
            "Lexicon uses AndroidX, Jetpack Compose, Kotlin and a shared C++17 engine. These technologies carry their own licences and acknowledgements.",
            "For the complete upstream licence texts and the project's source code, consult the dependency distributions and the Grapsee-Official/lexicon repository. This page does not substitute for the upstream legal notices."
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = 166.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to More",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onBack() }
                        .padding(10.dp),
                )
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = DisplayFontFamily,
                    fontSize = 27.sp,
                )
            }
        }
        paragraphs.forEachIndexed { index, text ->
            item(key = "paragraph_${index}") {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = if (index == 0) DisplayFontFamily else com.lexicon.ui.theme.LongFontFamily,
                    fontSize = if (index == 0) 23.sp else 16.sp,
                    lineHeight = if (index == 0) 30.sp else 25.sp,
                )
            }
        }
    }
}

// =======================================================================
// Profile header card
// =======================================================================

@Composable
private fun ProfileHeader(
    savedCount: Int,
    recentCount: Int,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else BrandGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, accent.copy(alpha = 0.3f), CircleShape),
        ) {
            Image(
                painter = painterResource(if (isDark) R.drawable.avatar_dark else R.drawable.avatar_default),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Name + subtitle + stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Curious Soul",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = DisplayFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Lexicon Companion · since 2026",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Saved count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = savedCount.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "saved",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
                // Recent count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = recentCount.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "recent",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

// =======================================================================
// Settings group — uppercase header + card of rows
// =======================================================================

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Uppercase muted header
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        // Card containing rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        ) {
            content()
        }
    }
}

// =======================================================================
// Single settings row — icon square + label + value + chevron
// =======================================================================

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconLabel: String,
    label: String,
    value: String,
    onClick: () -> Unit,
    isLast: Boolean = false,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else BrandGreen

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon in rounded square (primary/10 bg)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconLabel,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }

            // Label
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )

            // Value (if non-empty)
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }

            // Chevron right
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }

        // Hairline divider (skipped on last row)
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline),
            )
        }
    }
}

// =======================================================================
// Footer — heart + tagline
// =======================================================================

@Composable
private fun MoreFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Heart + craft message
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Terracotta,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Crafted in the spirit of the Cotswolds",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }

        // Tagline
        Text(
            text = "Lexicon · Words. Wisdom. Worlds.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 10.sp,
        )
    }
}
