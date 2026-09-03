package com.lexora.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexora.data.LexoraData
import com.lexora.data.LexoraViewModel
import com.lexora.data.WordEntry
import com.lexora.ui.AccessibilityAudit
import com.lexora.ui.enforceTouchTarget
import com.lexora.ui.iconButton
import com.lexora.ui.liveAnnouncement
import com.lexora.ui.sectionHeading
import com.lexora.ui.theme.BodyFontFamily
import com.lexora.ui.theme.DisplayFontFamily
import com.lexora.ui.theme.LongFontFamily

/**
 * SearchModal — full-screen search overlay translating the web's
 * `src/components/lexora/search-modal.tsx` 1:1.
 *
 * Animations:
 *   - outer layer:  fade-in / fade-out (200ms)
 *   - inner layer:  slide-in-vertically + fade-in (spring)
 *
 * Visibility is driven by `vm.uiState.searchOpen`. The system back button
 * and the close (X) button both call [LexoraViewModel.closeSearch].
 *
 * Layout:
 *   ┌───────────────────────────────────────────────┐
 *   │ [✕]  [🔍 Search for a word…   🎤  📷]          │ ← top bar
 *   ├───────────────────────────────────────────────┤
 *   │                                               │
 *   │ Empty state:                                  │
 *   │   Recent searches list   + Clear button       │
 *   │   Suggested pills (Petrichor, Saudade, …)     │
 *   │                                               │
 *   │ OR                                            │
 *   │                                               │
 *   │ Results: word · POS · first definition        │
 *   │                                               │
 *   │ OR                                            │
 *   │                                               │
 *   │ No-results watercolour empty state            │
 *   └───────────────────────────────────────────────┘
 *
 * Tapping a result:
 *   vm.addRecentSearch(word.word) → vm.closeSearch() → 200ms delay → vm.openWord(word)
 */
@Composable
fun SearchModal(viewModel: LexoraViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val open = state.searchOpen
    val recent = state.recentSearches

    var query by remember { mutableStateOf("") }
    // Holds the word the user just tapped — after the modal slides out
    // (200ms) we forward it to viewModel.openWord.
    var pendingWord by remember { mutableStateOf<WordEntry?>(null) }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus input on open (250ms delay to let the slide-in finish)
    LaunchedEffect(open) {
        if (open) {
            kotlinx.coroutines.delay(250)
            runCatching { focusRequester.requestFocus() }
        } else if (pendingWord == null) {
            // Plain close (X / back) → just clear the query
            query = ""
        }
    }

    // When the modal closes because a result was tapped, briefly delay
    // before opening the word-detail sheet so the two don't overlap.
    LaunchedEffect(open, pendingWord) {
        if (!open && pendingWord != null) {
            kotlinx.coroutines.delay(200)
            val w = pendingWord
            pendingWord = null
            query = ""
            if (w != null) viewModel.openWord(w)
        }
    }

    // System back button closes the modal when it is open
    BackHandler(enabled = open) {
        viewModel.closeSearch()
    }

    // Single AnimatedVisibility combining the outer fade + inner slide that
    // the web achieves via two stacked motion.div layers. Doing it in one
    // layer guarantees the slide-out exit finishes before the content is
    // removed from composition (which would otherwise clip the inner exit
    // when the outer fade-out completed first).
    AnimatedVisibility(
        visible = open,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)) +
            slideInVertically(
                initialOffsetY = { it / 5 },
                animationSpec = tween(durationMillis = 280),
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 220)) +
            slideOutVertically(
                targetOffsetY = { it / 5 },
                animationSpec = tween(durationMillis = 220),
            ),
    ) {
        SearchModalContent(
            query = query,
            onQueryChange = { query = it },
            recentSearches = recent,
            focusRequester = focusRequester,
            onClose = {
                query = ""
                viewModel.closeSearch()
            },
            onSubmit = {
                val trimmed = query.trim()
                if (trimmed.isNotEmpty()) {
                    viewModel.addRecentSearch(trimmed)
                }
            },
            onClearRecent = { viewModel.clearRecentSearches() },
            onRecentClick = { q -> query = q },
            onSuggestedClick = { s -> query = s },
            onSelectWord = { w ->
                viewModel.addRecentSearch(w.word)
                pendingWord = w
                viewModel.closeSearch()
            },
        )
    }
}

@Composable
private fun SearchModalContent(
    query: String,
    onQueryChange: (String) -> Unit,
    recentSearches: List<String>,
    focusRequester: FocusRequester,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    onClearRecent: () -> Unit,
    onRecentClick: (String) -> Unit,
    onSuggestedClick: (String) -> Unit,
    onSelectWord: (WordEntry) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ===================== Top search bar =====================
            SearchTopBar(
                query = query,
                onQueryChange = onQueryChange,
                onClose = onClose,
                onSubmit = onSubmit,
                focusRequester = focusRequester,
            )

            // ===================== Content ============================
            val trimmed = query.trim()
            val results = remember(query) {
                if (trimmed.isEmpty()) emptyList()
                else LexoraData.searchWords(trimmed, 8)
            }

            when {
                trimmed.isEmpty() -> EmptyStateContent(
                    recentSearches = recentSearches,
                    onClearRecent = onClearRecent,
                    onRecentClick = onRecentClick,
                    onSuggestedClick = onSuggestedClick,
                )

                results.isNotEmpty() -> ResultsList(
                    results = results,
                    query = trimmed,
                    onSelectWord = onSelectWord,
                )

                else -> NoResultsState(query = trimmed)
            }
        }
    }
}

// =============================================================================
// Top bar
// =============================================================================

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Close (X) button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .iconButton(AccessibilityAudit.Descriptions.Close)
                .enforceTouchTarget()
                .clickable(role = Role.Button) { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }

        // Pill-shaped search field with mic + camera inside
        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search for a word...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 13.sp,
                        ),
                        // The placeholder is a visual hint only — TalkBack
                        // reads the BasicTextField's own label below. Suppress
                        // to avoid a duplicate "Search for a word..." stop.
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { onSubmit() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .iconButton(AccessibilityAudit.Descriptions.Mic)
                    .enforceTouchTarget()
                    .clickable(role = Role.Button) { /* TODO: voice search */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .iconButton(AccessibilityAudit.Descriptions.Camera)
                    .enforceTouchTarget()
                    .clickable(role = Role.Button) { /* TODO: image search */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// =============================================================================
// Empty state — recent + suggested
// =============================================================================

@Composable
private fun EmptyStateContent(
    recentSearches: List<String>,
    onClearRecent: () -> Unit,
    onRecentClick: (String) -> Unit,
    onSuggestedClick: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // ----- Recent searches ----------------------------------------
        if (recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "RECENT",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                    ),
                    modifier = Modifier.sectionHeading(2),
                )
                Row(
                    modifier = Modifier
                        .iconButton(AccessibilityAudit.Descriptions.Clear)
                        .enforceTouchTarget()
                        .clickable(role = Role.Button) { onClearRecent() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "Clear",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                recentSearches.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .enforceTouchTarget()
                            .clickable(role = Role.Button) { onRecentClick(r) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = r,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            style = TextStyle(
                                fontFamily = BodyFontFamily,
                                fontSize = 14.sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ----- Suggested pills ----------------------------------------
        Text(
            text = "SUGGESTED",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
            ),
            modifier = Modifier.sectionHeading(2),
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                "Petrichor",
                "Saudade",
                "Komorebi",
                "Mellifluous",
                "Halcyon",
                "Numinous",
            ).forEach { s ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .enforceTouchTarget()
                        .clickable(role = Role.Button) { onSuggestedClick(s) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = s,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        style = TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                        ),
                    )
                }
            }
        }
    }
}

// =============================================================================
// Results list
// =============================================================================

@Composable
private fun ResultsList(
    results: List<WordEntry>,
    query: String,
    onSelectWord: (WordEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Live-region header: announces the result count to TalkBack as the
        // user types, without them having to swipe back up to the count.
        item {
            Text(
                text = "${results.size} ${if (results.size == 1) "result" else "results"} for \"$query\"",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontSize = 12.sp,
                ),
                modifier = Modifier.liveAnnouncement(LiveRegionMode.Polite),
            )
            Spacer(Modifier.height(4.dp))
        }
        items(results, key = { it.id }) { w ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .enforceTouchTarget()
                    .clickable(role = Role.Button) { onSelectWord(w) }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Baseline,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = w.word,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = TextStyle(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                )
                Text(
                    text = w.partOfSpeech.label(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                    ),
                )
                Text(
                    text = w.definitions.first(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = TextStyle(
                        fontFamily = LongFontFamily,
                        fontSize = 13.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// =============================================================================
// No-results state
// =============================================================================

@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No words found",
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
            ),
            // Heading so screen-reader users can jump to the no-results
            // message; live region so TalkBack announces it automatically
            // when the result set transitions from non-empty to empty.
            modifier = Modifier
                .sectionHeading(2)
                .liveAnnouncement(LiveRegionMode.Polite),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "We couldn't find \"$query\" in this offline pack. " +
                "Try another spelling or download more language pairs.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
