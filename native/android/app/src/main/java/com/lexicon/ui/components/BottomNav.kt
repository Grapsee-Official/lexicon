package com.lexicon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.data.ScreenRoute
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
    val compactLabels = LocalConfiguration.current.screenWidthDp < 360
    val activeTab = state.activeTab
    val isDark = state.isDark

    val homeNight = isDark && state.route == ScreenRoute.HOME
    val barBg = if (homeNight) Color(0xFF0C151C).copy(alpha = 0.95f)
        else if (isDark) DarkCard.copy(alpha = 0.96f) else CardSurface.copy(alpha = 0.96f)
    val barBorder = if (homeNight) Color(0xFFB6A486).copy(alpha = 0.16f)
        else if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.10f)
    val activeColor = if (isDark) DarkAccent else BrandGreen
    val inactiveColor = if (isDark) DarkAccent.copy(alpha = 0.78f) else DeepForest.copy(alpha = 0.88f)

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
            .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavItem(
                label = "Home",
                modifier = Modifier.weight(1f),
                outlinedIcon = Icons.Outlined.Home,
                filledIcon = Icons.Filled.Home,
                isActive = activeTab == TabKey.HOME,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
                compact = compactLabels,
            ) {
                viewModel.setActiveTab(TabKey.HOME)
            }

            BottomNavItem(
                label = "Dictionary",
                modifier = Modifier.weight(1f),
                outlinedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                filledIcon = Icons.AutoMirrored.Outlined.MenuBook,
                isActive = activeTab == TabKey.DICTIONARY,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
                compact = compactLabels,
            ) {
                viewModel.setActiveTab(TabKey.DICTIONARY)
            }

            // Reserve a symmetric slot: the actual search circle floats above
            // the capsule and must NOT be clipped by its rounded background.
            Box(Modifier.weight(1f).height(54.dp))

            BottomNavItem(
                label = "Collections",
                modifier = Modifier.weight(1f),
                outlinedIcon = Icons.Outlined.BookmarkBorder,
                filledIcon = Icons.Filled.Bookmark,
                isActive = activeTab == TabKey.COLLECTIONS,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
                compact = compactLabels,
            ) {
                viewModel.setActiveTab(TabKey.COLLECTIONS)
            }

            BottomNavItem(
                label = "More",
                modifier = Modifier.weight(1f),
                outlinedIcon = Icons.Outlined.MoreHoriz,
                filledIcon = Icons.Outlined.MoreHoriz,
                isActive = activeTab == TabKey.MORE,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                dotColor = if (isDark) DarkHighlight else BrandGreen,
                compact = compactLabels,
            ) {
                viewModel.setActiveTab(TabKey.MORE)
            }
        }

        // Floating search button, overlaid OUTSIDE the clipped capsule.
        // Matches the raised, bookish search seal in both original UI boards.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-7).dp)
                .size(54.dp)
                .then(
                    if (!isDark) Modifier.shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = DeepForest.copy(alpha = 0.16f),
                        spotColor = DeepForest.copy(alpha = 0.16f),
                    ) else Modifier
                )
                .clip(CircleShape)
                .background(if (homeNight) Color(0xFF0A131B) else if (isDark) Color(0xFF101620) else BrandGreen)
                .border(
                    width = if (isDark) 1.5.dp else 3.dp,
                    color = if (isDark) DarkAccent.copy(alpha = 0.70f) else CardSurface,
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
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    modifier: Modifier = Modifier,
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    dotColor: Color,
    compact: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .semantics { selected = isActive }
            .clickable(role = Role.Tab) { onClick() }
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (isActive) filledIcon else outlinedIcon,
            contentDescription = null,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(19.dp),
        )
        Text(
            text = label,
            color = if (isActive) activeColor else inactiveColor,
            style = if (compact) LexiconTextStyles.NavLabel.copy(fontSize = 8.sp) else LexiconTextStyles.NavLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .size(3.5.dp)
                .clip(CircleShape)
                .background(if (isActive) dotColor else Color.Transparent),
        )
    }
}
