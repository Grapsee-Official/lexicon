package com.lexora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lexora.data.LexoraViewModel
import com.lexora.data.TabKey
import com.lexora.ui.theme.BrandGreen
import com.lexora.ui.theme.DarkAccent
import com.lexora.ui.theme.LexoraTextStyles

/**
 * BottomNav — floating pill, 5 items.
 *
 * Items: Home | Dictionary | (center Search FAB) | Collections | More
 *
 * Active state: filled icon + dot indicator below label.
 */
@Composable
fun BottomNav(
    viewModel: LexoraViewModel,
    modifier: Modifier = Modifier,
) {
    val activeTab = viewModel.uiState.value.activeTab
    val isDark = viewModel.uiState.value.isDark
    val accent = if (isDark) DarkAccent else BrandGreen

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Bottom)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(50), clip = false)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, activeTab == TabKey.HOME) {
                viewModel.setActiveTab(TabKey.HOME)
            }
            BottomNavItem("Dictionary", Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, activeTab == TabKey.DICTIONARY) {
                viewModel.setActiveTab(TabKey.DICTIONARY)
            }

            // Center Search FAB
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable { viewModel.openSearch() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Open search",
                    tint = if (isDark) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }

            BottomNavItem("Collections", Icons.Outlined.Bookmark, Icons.Filled.Bookmark, activeTab == TabKey.COLLECTIONS) {
                viewModel.setActiveTab(TabKey.COLLECTIONS)
            }
            BottomNavItem("More", Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz, activeTab == TabKey.MORE) {
                viewModel.setActiveTab(TabKey.MORE)
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accent = if (isDark) DarkAccent else BrandGreen

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (isActive) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = if (isActive) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            color = if (isActive) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = LexoraTextStyles.NavLabel,
        )
        // Active dot indicator
        Box(
            modifier = Modifier
                .size(2.dp)
                .clip(CircleShape)
                .background(if (isActive) accent else Color.Transparent),
        )
    }
}
