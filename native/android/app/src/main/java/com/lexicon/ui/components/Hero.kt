package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.iconButton
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WarmCream

/**
 * Hero — Independent hero section (1:1 translation of web Hero).
 *
 * SIMPLE 2-COLUMN LAYOUT:
 *   LEFT  (weight 1f ≈ 55%): headline + paragraph
 *   RIGHT (weight ≈ 45%)   : contained image, no bleed, no scale
 *
 * Headline colour: BrandGreen (#677554).
 * The 3-leaf botanical sprig sits inline after "Words".
 */
@Composable
fun Hero(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WarmCream)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // === LEFT: text column ===
        Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Headline — 2 lines, sprig inline after "Words".
            // The headline is the page's h1 — screen-reader users land
            // here first when the home feed loads.
            Column(modifier = Modifier.sectionHeading(1)) {
                Text(
                    text = "Discover the",
                    color = BrandGreen,
                    style = LexiconTextStyles.HeroHeadline,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "World of ",
                        color = BrandGreen,
                        style = LexiconTextStyles.HeroHeadline,
                    )
                    Text(
                        text = "Words",
                        color = BrandGreen,
                        style = LexiconTextStyles.HeroHeadline,
                    )
                    Spacer(Modifier.width(4.dp))
                    LeafSprig(
                        color = BrandGreen,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Text(
                text = "A timeless dictionary crafted for curious minds.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = LexiconTextStyles.HeroParagraph,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // === RIGHT: image column ===
        Box(
            modifier = Modifier
                .weight(0.45f)
                .height(200.dp)
                .iconButton(AccessibilityAudit.Descriptions.Search)
                .clickable(role = Role.Button) { viewModel.openSearch() },
            contentAlignment = Alignment.CenterEnd,
        ) {
            Image(
                painter = painterResource(R.drawable.lexicon_countryside_day),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
