package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lexicon.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.Category
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WarmGrey

/**
 * A small illustrated bookplate, not a square thumbnail crop.
 * Keep the entire transparent watercolor and its ragged edges visible.
 */
@Composable
fun CategoryCard(
    category: Category,
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    shelfArtworkHeight: Dp? = null,
    referenceShelfWidth: Dp? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val fontScale = LocalConfiguration.current.fontScale
    val largeText = fontScale >= 1.5f
    val artHeight = shelfArtworkHeight ?: if (largeText) 100.dp else 74.dp
    val referenceTitle = referenceShelfWidth?.value?.let { (it * 14.5f / 117f).coerceIn(9.5f, 14.5f) }
    val referenceSubtitle = referenceShelfWidth?.value?.let { (it * 10.5f / 117f).coerceIn(8f, 11f) }
    val ctx = LocalContext.current
    // All ten known categories have separate light and dark native vectors.
    // Keep the original artwork only as a fallback for future categories.
    val boardRes = if (!isDark) ctx.resources.getIdentifier("ref_light_home_" + category.id, "drawable", ctx.packageName) else 0
    val lightRes = if (!isDark) ctx.resources.getIdentifier(
        "${category.illustration}_light", "drawable", ctx.packageName
    ) else 0
    val resId = if (isDark) when (category.id) {
        "vocabulary" -> R.drawable.ref_home_vocabulary
        "geography" -> R.drawable.ref_home_geography
        "literature" -> R.drawable.ref_home_literature
        "science" -> R.drawable.ref_home_science
        "history" -> R.drawable.ref_home_history
        "mythology" -> R.drawable.ref_home_mythology
        "philosophy" -> R.drawable.ref_home_philosophy
        "art_architecture" -> R.drawable.ref_home_art_architecture
        "nature" -> R.drawable.ref_home_nature
        "culture_traditions" -> R.drawable.ref_home_culture_traditions
        else -> 0
    } else if (boardRes != 0) boardRes else if (lightRes != 0) lightRes else
        ctx.resources.getIdentifier(category.illustration, "drawable", ctx.packageName)
    // Let the underlying page ink show through the bookplate rather than
    // introducing a second opaque rectangle around the original art.
    val surface = if (isDark) Color(0xFF101B23).copy(alpha = 0.32f) else CardSurface
    val lowerSurface = if (isDark) Color(0xFF101B23).copy(alpha = 0.12f) else Color(0xFFF8F1E6)
    val ink = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) Color(0xFFB4AD9E) else WarmGrey
    val border = if (isDark) Color(0xFFB6A486).copy(alpha = 0.10f) else Color(0xFFA58A61).copy(alpha = 0.15f)
    val shape = RoundedCornerShape(if (isDark) 10.dp else 12.dp)

    Column(
        modifier = modifier
            .heightIn(min = if (referenceShelfWidth != null && !largeText) maxOf(referenceShelfWidth * (147f / 117f), artHeight + 28.dp * fontScale + 8.dp)
                else if (shelfArtworkHeight != null && !largeText) artHeight + 28.dp * fontScale + 8.dp
                else artHeight + (if (largeText) 46.dp else 33.dp) * fontScale.coerceAtMost(2.5f) + 10.dp)
            .clip(shape)
            .background(
                if (isDark) Brush.verticalGradient(
                    0f to surface, 0.66f to surface.copy(alpha = 0.22f), 1f to lowerSurface,
                ) else Brush.verticalGradient(listOf(surface, lowerSurface))
            )
            .border(1.dp, border, shape)
            .clickable(role = Role.Button) { viewModel.openCategory(category.id) }
            .padding(horizontal = if (largeText) 7.dp else 3.dp, vertical = if (isDark && !largeText) 4.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (resId != 0) {
            // Reference vectors already contain their edge and color treatment.
            val painting = if (boardRes != 0) rememberCountrysidePainter(resId, referenceShelfWidth ?: artHeight)
                else if (isDark) rememberCountrysidePainter(resId, referenceShelfWidth ?: artHeight)
                else rememberPreparedArtwork(resId, ArtworkTreatment.BOOKPLATE)
            Image(
                painter = painting,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(artHeight),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
            )
        } else {
            Spacer(Modifier.height(artHeight))
        }
        Spacer(Modifier.height(if (isDark && !largeText) 0.dp else 5.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (shelfArtworkHeight != null && !largeText) 1.dp else 2.dp),
        ) {
            Text(
                text = category.title,
                color = ink,
                style = LexiconTextStyles.CategoryTitle.copy(fontSize = if (referenceTitle != null && !largeText) referenceTitle.sp else if (shelfArtworkHeight != null && !largeText) 10.5.sp else 12.sp,
                    lineHeight = if (referenceTitle != null && !largeText) (referenceTitle * 1.2f).sp else if (shelfArtworkHeight != null && !largeText) 12.5.sp else 14.sp),
                textAlign = TextAlign.Center,
            )
            Text(
                text = category.subtitle,
                color = muted,
                style = LexiconTextStyles.CategorySubtitle.copy(
                    fontSize = if (referenceSubtitle != null && !largeText) referenceSubtitle.sp else if (shelfArtworkHeight != null) 8.5.sp else 9.5.sp,
                    lineHeight = if (referenceSubtitle != null && !largeText) (referenceSubtitle * 1.3f).sp else if (shelfArtworkHeight != null) 11.sp else 12.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
