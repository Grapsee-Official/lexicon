package com.lexicon.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexicon.data.AlphabetSection
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.WordEntry
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.LongFontFamily
import kotlinx.coroutines.launch

/**
 * DictionaryScreen — 1:1 translation of the web dictionary-screen.tsx.
 *
 * Features:
 *   - Alphabetical browse with a sticky A–Z quick-index (horizontal scroll row
 *     of 24dp letter circles).
 *   - Active letter is tracked from scroll position (derivedStateOf reading
 *     the LazyColumn's firstVisibleItemIndex).
 *   - Tapping a letter smooth-scrolls to that section.
 *   - Each section has a large serif letter header + word count + a horizontal
 *     divider line, followed by a grouped card of word rows.
 *   - Each word row: word title (serif), part of speech (italic, muted),
 *     definition (1 line, ellipsised), volume icon on the right.
 *   - Tap row → vm.openWord(entry).
 *
 * Uses getAlphabetSections() from LexiconData.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DictionaryScreen(viewModel: LexiconViewModel) {
    val sections = remember { LexiconData.getAlphabetSections() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Precompute the LazyColumn item index for each section's header.
    // Layout: [0] = stickyHeader (A–Z bar), then for each section two items:
    //         header + card.
    val sectionHeaderIndices = remember(sections) {
        var idx = 1 // skip sticky A–Z bar at index 0
        sections.map { _ ->
            val headerIdx = idx
            idx += 2 // header item + card item
            headerIdx
        }
    }

    // Active letter = the section whose header is at or above the first
    // visible item. This mirrors the web IntersectionObserver pattern.
    val activeSection by remember {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            if (first <= 0) 0
            else {
                var active = 0
                sectionHeaderIndices.forEachIndexed { i, headerIdx ->
                    if (headerIdx <= first) active = i
                }
                active
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 96.dp, // clear floating BottomNav
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // === Sticky A–Z quick index ===
        stickyHeader {
            AlphabetQuickIndex(
                sections = sections,
                activeIndex = activeSection,
                onLetterTap = { i ->
                    scope.launch {
                        listState.animateScrollToItem(sectionHeaderIndices[i])
                    }
                },
            )
        }

        // === Sections ===
        sections.forEach { section ->
            // Section header: large serif letter + divider + count
            item(key = "header-${section.letter}", contentType = "sectionHeader") {
                LetterSectionHeader(section = section)
            }

            // Section card containing all word rows
            item(key = "card-${section.letter}", contentType = "sectionCard") {
                LetterSectionCard(
                    section = section,
                    onWordClick = { entry -> viewModel.openWord(entry) },
                )
            }
        }
    }
}

// =======================================================================
// Sticky A–Z quick-index bar
// =======================================================================

/**
 * Horizontal scroll row of 24dp letter circles.
 *
 * Active letter → filled circle (primary green / dark accent) with cream text.
 * Inactive letter → transparent circle, muted text.
 *
 * Background uses an 85%-opacity page colour (mirrors the web `paper-surface`
 * class) with a bottom hairline border.
 */
@Composable
private fun AlphabetQuickIndex(
    sections: List<AlphabetSection>,
    activeIndex: Int,
    onLetterTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else BrandGreen

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
            .padding(vertical = 6.dp),
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            sections.forEachIndexed { i, section ->
                val isActive = i == activeIndex
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isActive) accent else Color.Transparent)
                        .clickable { onLetterTap(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = section.letter,
                        color = if (isActive)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = DisplayFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isActive)
                            FontWeight.SemiBold
                        else
                            FontWeight.Normal,
                    )
                }
            }
        }
    }
}

// =======================================================================
// Letter section header — large serif letter + divider + count
// =======================================================================

@Composable
private fun LetterSectionHeader(section: AlphabetSection) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else BrandGreen
    val count = section.entries.size
    val wordLabel = if (count == 1) "word" else "words"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Large serif letter
        Text(
            text = section.letter,
            color = accent,
            fontFamily = DisplayFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        // Horizontal divider line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
        // Word count
        Text(
            text = "$count $wordLabel",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = LexiconTextStyles.ArticleMeta,
        )
    }
}

// =======================================================================
// Letter section card — grouped word rows
// =======================================================================

@Composable
private fun LetterSectionCard(
    section: AlphabetSection,
    onWordClick: (WordEntry) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
    ) {
        section.entries.forEachIndexed { idx, entry ->
            if (idx > 0) {
                // Top hairline divider between rows (mirrors `border-t border-border`)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )
            }
            DictionaryRow(
                entry = entry,
                onClick = { onWordClick(entry) },
            )
        }
    }
}

// =======================================================================
// Single dictionary word row
// =======================================================================

@Composable
private fun DictionaryRow(
    entry: WordEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Word + part of speech + definition column
        Column(
            modifier = Modifier.weight(1f),
        ) {
            // Word title + part of speech (baseline-aligned)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.word,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = DisplayFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = entry.partOfSpeech.label(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Default,
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
            Spacer(Modifier.height(2.dp))
            // Definition — 1 line
            Text(
                text = entry.definitions.first(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontFamily = LongFontFamily,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Volume icon
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
            contentDescription = "Listen to ${entry.word}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
    }
}
