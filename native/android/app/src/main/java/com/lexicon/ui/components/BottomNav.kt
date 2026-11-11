package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.AccessibilityAudit
import com.lexicon.ui.enforceTouchTarget
import com.lexicon.ui.iconButton
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkHighlight
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.PrimaryForeground
import com.lexicon.ui.theme.WarmGrey

/**
 * BottomNav — Floating capsule navigation bar matching Light & Dark PNGs 1:1:
 *   - Light Mode: elevated warm cream capsule, BrandGreen active state + solid
 *     BrandGreen center Search circle with white magnifier.
 *   - Dark Mode: sleek nocturnal capsule (#141B25) with 1dp border, antique gold
 *     (#D4A45A) icons/labels, and a refined dark circular Search button outlined
 *     in antique gold with a golden magnifying glass (exact match to Dark PNG!).
 */
@Composable
fun BottomNav(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTab = state.activeTab
    val isDark = state.isDark

    val barBg = if (isDark) DarkCard.copy(alpha = 0.96f) else CardSurface.copy(alpha = 0.96f)
    val barBorder = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.10f)
    val activeColor = if (isDark) DarkAccent else BrandGreen
    val inactiveColor = if (isDark) DarkAccent.copy(alpha = 0.78f) else WarmGrey

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Bottom)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = DeepForest.copy(alpha = 0.14f),
                        spotColor = DeepForest.copy(alpha = 0.14f),
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(50))
            .background(barBg)
            .border(1.dp, barBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavItem(
                label = "Home",
                outlinedIcon = Icons.Outlined.Home,
                filledIcon = Icons.Filled.Home,
                isActive = activeTab == TabKey.HOME,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
            ) {
                viewModel.setActiveTab(TabKey.HOME)
            }

            BottomNavItem(
                label = "Dictionary",
                outlinedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                filledIcon = Icons.AutoMirrored.Outlined.MenuBook,
                isActive = activeTab == TabKey.DICTIONARY,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
            ) {
                viewModel.setActiveTab(TabKey.DICTIONARY)
            }

            // Center Search Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF101620) else BrandGreen)
                    .border(
                        width = if (isDark) 1.dp else 0.dp,
                        color = if (isDark) DarkAccent.copy(alpha = 0.55f) else Color.Transparent,
                        shape = CircleShape,
                    )
                    .iconButton(AccessibilityAudit.Descriptions.SearchFab)
                    .clickable(role = Role.Button) { viewModel.openSearch() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = if (isDark) DarkHighlight else PrimaryForeground,
                    modifier = Modifier.size(19.dp),
                )
            }

            BottomNavItem(
                label = "Collections",
                outlinedIcon = Icons.Outlined.BookmarkBorder,
                filledIcon = Icons.Filled.Bookmark,
                isActive = activeTab == TabKey.COLLECTIONS,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
            ) {
                viewModel.setActiveTab(TabKey.COLLECTIONS)
            }

            BottomNavItem(
                label = "More",
                outlinedIcon = Icons.Outlined.MoreHoriz,
                filledIcon = Icons.Outlined.MoreHoriz,
                isActive = activeTab == TabKey.MORE,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
            ) {
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
    activeColor: Color,
    inactiveColor: Color,
    dotColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .semantics { selected = isActive }
            .clickable(role = Role.Tab) { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (isActive) filledIcon else outlinedIcon,
            contentDescription = null,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            color = if (isActive) activeColor else inactiveColor,
            style = LexiconTextStyles.NavLabel,
        )
        Box(
            modifier = Modifier
                .size(3.5.dp)
                .clip(CircleShape)
                .background(if (isActive) dotColor else Color.Transparent),
        )
    }
}
