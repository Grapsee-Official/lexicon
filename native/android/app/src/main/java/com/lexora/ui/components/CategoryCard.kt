package com.lexora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexora.data.Category
import com.lexora.data.LexoraViewModel
import com.lexora.data.TabKey
import com.lexora.ui.theme.LexoraTextStyles

/**
 * CategoryCard — borderless, seamless, transparent bg.
 *
 * Square image (object-cover) with title + subtitle centred below.
 * Width ≈ 96dp.
 */
@Composable
fun CategoryCard(
    category: Category,
    viewModel: LexoraViewModel,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val resId = ctx.resources.getIdentifier(category.illustration, "drawable", ctx.packageName)

    Column(
        modifier = modifier
            .width(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(role = Role.Button) { viewModel.setActiveTab(TabKey.DICTIONARY) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Square image — no border, no shadow, bg matches image
        Image(
            painter = painterResource(resId),
            contentDescription = "${category.title} illustration",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )

        // Title + subtitle — perfectly centred below image
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = category.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = LexoraTextStyles.CategoryTitle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = category.subtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = LexoraTextStyles.CategorySubtitle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
