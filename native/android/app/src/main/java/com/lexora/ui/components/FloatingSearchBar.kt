package com.lexora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lexora.data.LexoraViewModel
import com.lexora.ui.theme.LexoraTextStyles

/**
 * FloatingSearchBar — pill-shaped search affordance.
 *
 * Layout: [search icon] [placeholder text] [camera icon] [mic icon]
 *
 * Stroke-equivalent: thin — using Icons.Outlined.* which gives a delicate
 * line weight similar to the web's strokeWidth 0.8.
 */
@Composable
fun FloatingSearchBar(
    viewModel: LexoraViewModel,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for a word…",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(50),
                clip = false,
            )
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { viewModel.openSearch() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp),
        )

        Text(
            text = placeholder,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = LexoraTextStyles.SearchPlaceholder,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.size(0.dp))

        Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp),
        )
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp),
        )
    }
}
