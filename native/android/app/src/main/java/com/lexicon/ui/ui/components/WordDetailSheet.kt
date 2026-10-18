package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatQuote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Repeat
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.WordEntry
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.iconButton
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LongFontFamily
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.Terracotta

/**
 * WordDetailSheet — Material 3 ModalBottomSheet translating the web's
 * `src/components/lexicon/word-detail-sheet.tsx` 1:1.
 *
 * Opens whenever `vm.uiState.selectedWord != null`; dismissing the sheet
 * (scrim tap or drag-down) calls [LexiconViewModel.closeWord].
 *
 * Layout:
 *   1. Header  — "LEXORA ENTRY" badge, big serif title, phonetic · POS,
 *                three circular action buttons (pronounce / save / share)
 *   2. Definitions — numbered list, Lora 15sp, leading-relaxed
 *   3. Etymology — card with border, Lora 13sp
 *   4. Origin row — Languages icon + "Origin:" label + language text
 *   5. Synonyms  — pill tags, secondary tint
 *   6. Antonyms  — pill tags, destructive tint
 *   7. In Context — italic blockquotes with left highlight border
 *
 * Max height = 88% of the screen (matches web `max-h-[88dvh]`).
 */
@Composable
fun WordDetailSheet(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val word = state.selectedWord ?: return
    val isDark = state.isDark
    val saved = state.savedWordIds.contains(word.id)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val maxHeight = (configuration.screenHeightDp.dp * 0.88f)

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeWord() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
        ) {
            WordDetailHeader(
                word = word,
                isSaved = saved,
                isDark = isDark,
                onPronounce = { /* pronunciation playback hook */ },
                onToggleSave = { viewModel.toggleSave(word.id) },
                onShare = { /* share intent hook */ },
            )

            Spacer(Modifier.height(20.dp))

            // ====================== Definitions ======================
            DefinitionsSection(word = word, isDark = isDark)

            Spacer(Modifier.height(20.dp))

            // ====================== Etymology ========================
            if (word.etymology.isNotBlank()) {
                EtymologySection(word = word, isDark = isDark)
                Spacer(Modifier.height(20.dp))
            }

            // ====================== Language origin ==================
            if (word.language.isNotBlank()) {
                LanguageRow(word = word, isDark = isDark)
                Spacer(Modifier.height(20.dp))
            }

            // ====================== Synonyms =========================
            if (word.synonyms.isNotEmpty()) {
                SynonymsSection(word = word)
                Spacer(Modifier.height(20.dp))
            }

            // ====================== Antonyms =========================
            if (word.antonyms.isNotEmpty()) {
                AntonymsSection(word = word)
                Spacer(Modifier.height(20.dp))
            }

            // ====================== In Context (examples) ============
            if (word.examples.isNotEmpty()) {
                ExamplesSection(word = word, isDark = isDark)
            }
        }
    }
}

// =============================================================================
// Header
// =============================================================================

@Composable
private fun WordDetailHeader(
    word: WordEntry,
    isSaved: Boolean,
    isDark: Boolean,
    onPronounce: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ----- Left: label + title + phonetic/POS ---------------------
        Column(modifier = Modifier.weight(1f)) {
            // "LEXORA ENTRY" badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val accent = if (isDark) DarkAccent else OldWoodGold
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "LEXORA ENTRY",
                    color = accent,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.6.sp,
                    ),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Big serif word title — Fraunces, 3xl (~30sp), semibold
            Text(
                text = word.word,
                color = MaterialTheme.colorScheme.onBackground,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                ),
                // The word title is the sheet's h1 — screen-reader users can
                // jump straight to it after the sheet opens.
                modifier = Modifier.sectionHeading(1),
            )

            Spacer(Modifier.height(4.dp))

            // Phonetic (italic) · part of speech (lowercase)
            val annotation = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = BodyFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) { append(word.phonetic) }
                withStyle(
                    SpanStyle(
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    ),
                ) { append("  ·  ") }
                withStyle(
                    SpanStyle(
                        fontFamily = BodyFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    ),
                ) { append(word.partOfSpeech.label()) }
            }
            Text(text = annotation)
        }

        // ----- Right: 3 circular action buttons -----------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CircleActionButton(
                contentDescription = AccessibilityAudit.Descriptions.Volume,
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                iconTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                container = MaterialTheme.colorScheme.surface,
                border = MaterialTheme.colorScheme.outline,
                onClick = onPronounce,
            )

            CircleActionButton(
                contentDescription = if (isSaved) AccessibilityAudit.Descriptions.BookmarkOn else AccessibilityAudit.Descriptions.BookmarkOff,
                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                iconTint = if (isSaved) Terracotta
                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                container = if (isSaved) Terracotta.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface,
                border = if (isSaved) Terracotta.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline,
                onClick = onToggleSave,
            )

            CircleActionButton(
                contentDescription = AccessibilityAudit.Descriptions.Share,
                icon = Icons.Outlined.Share,
                iconTint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                container = MaterialTheme.colorScheme.surface,
                border = MaterialTheme.colorScheme.outline,
                onClick = onShare,
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    container: Color,
    border: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, border, CircleShape)
            .iconButton(contentDescription)
            .enforceTouchTarget()
            .clickable(role = Role.Button) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
    }
}

// =============================================================================
// Sections
// =============================================================================

@Composable
private fun SectionLabel(
    text: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        // Section labels are h2 headings — TalkBack announces "Heading"
        // before each, letting screen-reader users jump between sections.
        modifier = Modifier.sectionHeading(2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = BodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
            ),
        )
    }
}

@Composable
private fun DefinitionsSection(word: WordEntry, isDark: Boolean) {
    val accent = if (isDark) DarkAccent else OldWoodGold
    Column {
        SectionLabel(
            text = if (word.definitions.size > 1) "Definitions" else "Definition",
            leadingIcon = Icons.AutoMirrored.Outlined.MenuBook,
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            word.definitions.forEachIndexed { i, def ->
                Row {
                    Text(
                        text = "${i + 1}.",
                        color = accent,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = BodyFontFamily,
                            fontSize = 12.sp,
                        ),
                        modifier = Modifier
                            .width(20.dp)
                            .padding(top = 4.dp),
                    )
                    Text(
                        text = def,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = LongFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EtymologySection(word: WordEntry, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Column {
            SectionLabel(text = "Etymology", leadingIcon = Icons.Outlined.AutoAwesome)
            Spacer(Modifier.height(8.dp))
            Text(
                text = word.etymology,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = LongFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
            )
        }
    }
}

@Composable
private fun LanguageRow(word: WordEntry, isDark: Boolean) {
    val accent = if (isDark) DarkAccent else OldWoodGold
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Origin:",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
            ),
        )
        Text(
            text = word.language,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = BodyFontFamily,
                fontSize = 13.sp,
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
private fun SynonymsSection(word: WordEntry) {
    Column {
        SectionLabel(text = "Synonyms", leadingIcon = Icons.AutoMirrored.Outlined.Repeat)
        Spacer(Modifier.height(8.dp))
        PillRow(
            words = word.synonyms,
            bg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            border = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
            text = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun AntonymsSection(word: WordEntry) {
    Column {
        SectionLabel(text = "Antonyms", leadingIcon = Icons.Outlined.Block)
        Spacer(Modifier.height(8.dp))
        PillRow(
            words = word.antonyms,
            bg = DestructiveColor.copy(alpha = 0.10f),
            border = DestructiveColor.copy(alpha = 0.20f),
            text = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun PillRow(
    words: List<String>,
    bg: Color,
    border: Color,
    text: Color,
) {
    // Flow layout — wraps to next line when out of horizontal space.
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        words.forEach { s ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, border, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = s,
                    color = text,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = BodyFontFamily,
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ExamplesSection(word: WordEntry, isDark: Boolean) {
    val accent = if (isDark) DarkAccent else OldWoodGold
    Column {
        SectionLabel(text = "In Context", leadingIcon = Icons.AutoMirrored.Outlined.FormatQuote)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            word.examples.forEach { ex ->
                // Blockquote — left border drawn behind the text, matching its height.
                Text(
                    text = ex,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = LongFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val stripeWidth = 2.dp.toPx()
                            drawRect(
                                color = accent.copy(alpha = 0.6f),
                                topLeft = Offset.Zero,
                                size = Size(
                                    width = stripeWidth,
                                    height = size.height,
                                ),
                            )
                        }
                        .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                )
            }
        }
    }
}

/** Muted red used for "antonym" destructive tints — matches the web `destructive`. */
private val DestructiveColor: Color = Color(0xFFB23A48)
