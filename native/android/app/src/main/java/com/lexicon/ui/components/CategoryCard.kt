package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark
    val largeText = LocalConfiguration.current.fontScale > 1.2f
    val ctx = LocalContext.current
    // One original painting for both themes; remove transparent storage margins only.
    val resId = ctx.resources.getIdentifier(category.illustration, "drawable", ctx.packageName)
    // Let the underlying page ink show through the bookplate rather than
    // introducing a second opaque rectangle around the original art.
    val surface = if (isDark) Color(0xFF15212A).copy(alpha = 0.22f) else CardSurface
    val lowerSurface = if (isDark) Color(0xFF15212A).copy(alpha = 0.47f) else Color(0xFFF8F1E6)
    val ink = if (isDark) DarkForeground else DeepForest
    val muted = if (isDark) DarkMutedForeground else WarmGrey
    val border = if (isDark) Color(0xFFB6A486).copy(alpha = 0.12f) else DeepForest.copy(alpha = 0.10f)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .heightIn(min = if (largeText) 190.dp else if (isDark) 122.dp else 130.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(surface, lowerSurface)))
            .border(1.dp, border, shape)
            .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.DICTIONARY) }
            .padding(horizontal = if (isDark) 5.dp else 7.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (resId != 0) {
            Image(
                painter = rememberPreparedArtwork(resId, ArtworkTreatment.BOOKPLATE),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(if (largeText) 88.dp else if (isDark) 64.dp else 65.dp),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
            )
        } else {
            Spacer(Modifier.height(if (largeText) 88.dp else if (isDark) 64.dp else 65.dp))
        }
        Spacer(Modifier.height(if (isDark) 3.dp else 5.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = category.title,
                color = ink,
                style = LexiconTextStyles.CategoryTitle.copy(fontSize = 12.sp, lineHeight = 14.sp),
                textAlign = TextAlign.Center,
            )
            Text(
                text = category.subtitle,
                color = muted,
                style = LexiconTextStyles.CategorySubtitle.copy(fontSize = if (isDark) 9.sp else 9.5.sp, lineHeight = 12.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
