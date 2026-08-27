package com.lexicon.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexicon.R
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.WordEntry
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LongFontFamily
import com.lexicon.ui.theme.OldWoodGold
import kotlinx.coroutines.delay

/**
 * CollectionsScreen — 1:1 translation of the web collections-screen.tsx.
 *
 * Two states:
 *   1. EMPTY (no saved words): watercolour empty state — cat-literature
 *      illustration with faded edges, bookmark badge, "Your collection
 *      awaits" headline, description, and a "Saved words sync offline" pill.
 *   2. NON-EMPTY: "Saved Words" header with count, then a card containing
 *      saved word rows. Each row animates in with fadeIn + slideInHorizontally
 *      (staggered by index × 40ms).
 *
 * Row interactions:
 *   - Tap row → vm.openWord(entry)
 *   - Tap trash → vm.toggleSave(word.id) (removes from saved)
 *
 * Observes vm.uiState.savedWordIds and filters LexiconData.dictionary by IDs.
 */
@Composable
fun CollectionsScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsState()

    val savedWords = remember(state.savedWordIds) {
        // Preserve insertion order (savedWordIds is append-on-save).
        state.savedWordIds.mapNotNull { id -> LexiconData.getWordById(id) }
    }

    if (savedWords.isEmpty()) {
        EmptyCollectionState()
    } else {
        SavedWordsList(
            savedWords = savedWords,
            onOpen = { entry -> viewModel.openWord(entry) },
            onRemove = { id -> viewModel.toggleSave(id) },
        )
    }
}

// =======================================================================
// Empty state — watercolour illustration + headline + pill
// =======================================================================

@Composable
private fun EmptyCollectionState() {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else OldWoodGold

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 32.dp,
            vertical = 64.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // === Illustration box (160dp with fade-edges mask) ===
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Blurred halo behind the illustration (primary/10)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                (if (isDark) DarkAccent else BrandGreen)
                                    .copy(alpha = 0.10f),
                            ),
                    )

                    // Illustration with radial fade-edges mask + 0.8 alpha
                    Image(
                        painter = painterResource(R.drawable.cat_literature),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .graphicsLayer {
                                this.alpha = if (isDark) 0.6f else 0.8f
                                this.compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Black,
                                            0.55f to Color.Black,
                                            1.0f to Color.Transparent,
                                        ),
                                        center = Offset(
                                            size.width / 2f,
                                            size.height / 2f,
                                        ),
                                        radius = size.maxDimension / 2f,
                                    ),
                                    blendMode = BlendMode.DstIn,
                                )
                            },
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                    )

                    // Bookmark badge — bottom-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(48.dp)
                            .shadow(2.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Headline
                Text(
                    text = "Your collection awaits",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = DisplayFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(8.dp))

                // Description
                Text(
                    text = "Tap the bookmark icon on any word to save it here. Build your own private herbarium of language.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                // "Saved words sync offline" pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Saved words sync offline across all your devices",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

// =======================================================================
// Saved words list — header + card of animated rows
// =======================================================================

@Composable
private fun SavedWordsList(
    savedWords: List<WordEntry>,
    onOpen: (WordEntry) -> Unit,
    onRemove: (String) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 96.dp, // clear floating BottomNav
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header — "Saved Words" + count
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Saved Words",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = DisplayFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                val count = savedWords.size
                Text(
                    text = "$count ${if (count == 1) "word" else "words"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }

        // Card containing all saved word rows
        item(key = "saved-card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp),
                    ),
            ) {
                savedWords.forEachIndexed { idx, entry ->
                    SavedWordRow(
                        entry = entry,
                        index = idx,
                        isFirst = idx == 0,
                        isDark = isDark,
                        onOpen = { onOpen(entry) },
                        onRemove = { onRemove(entry.id) },
                    )
                }
            }
        }
    }
}

// =======================================================================
// Single saved-word row with staggered entrance animation
// =======================================================================

@Composable
private fun SavedWordRow(
    entry: WordEntry,
    index: Int,
    isFirst: Boolean,
    isDark: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    // Staggered fadeIn + slideInHorizontally (x: -8dp → 0dp)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 40L)
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "row-alpha",
    )
    val offsetX by animateDpAsState(
        targetValue = if (visible) 0.dp else (-8).dp,
        animationSpec = tween(durationMillis = 250),
        label = "row-offset-x",
    )

    val accent = if (isDark) DarkAccent else OldWoodGold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(x = offsetX),
    ) {
        // Top hairline divider (skipped on first row)
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Filled bookmark icon (accent colour)
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )

            // Word + part of speech + definition
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = entry.word,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = DisplayFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = entry.partOfSpeech.label(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.definitions.first(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = LongFontFamily,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Volume icon
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = "Listen to ${entry.word}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )

            // Remove (trash) button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove ${entry.word}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
