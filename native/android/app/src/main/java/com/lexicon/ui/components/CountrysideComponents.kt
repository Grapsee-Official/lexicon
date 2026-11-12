package com.lexicon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexicon.R
import com.lexicon.ui.theme.BodyFontFamily
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.CardSurface
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.DarkCard
import com.lexicon.ui.theme.DarkChipBorder
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkMutedForeground
import com.lexicon.ui.theme.DarkSage
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.DisplayFontFamily
import com.lexicon.ui.theme.LongFontFamily
import com.lexicon.ui.theme.OldWoodGold
import com.lexicon.ui.theme.SearchPillLight
import com.lexicon.ui.theme.WarmGrey

@Composable
fun resolveCountrysideDrawable(baseName: String, isDark: Boolean): Int {
    val ctx = LocalContext.current
    val target = if (isDark) "${baseName}_dark" else baseName
    return ctx.resources.getIdentifier(target, "drawable", ctx.packageName)
        .takeIf { it != 0 }
        ?: ctx.resources.getIdentifier(baseName, "drawable", ctx.packageName)
            .takeIf { it != 0 }
        ?: R.drawable.hero_light
}

/**
 * CountrysideHeroHeader — Shared top watercolour banner across Pages 1, 2, 4, 5, 6, 7, 8, 9, 10:
 *   - Left: Botanical olive sprig + large Playfair serif title + gentle subtitle
 *   - Right: Seamless watercolour cottage / antique books / floral jar vignette + History clock button
 */
@Composable
fun CountrysideHeroHeader(
    title: String,
    subtitle: String,
    illustrationBase: String,
    isDark: Boolean,
    onHistoryClick: () -> Unit,
    onLogoToggleTheme: () -> Unit = {},
    heroHeight: Dp = 146.dp,
) {
    val titleColor = if (isDark) DarkForeground else DeepForest
    val subColor = if (isDark) DarkMutedForeground else DeepForest.copy(alpha = 0.78f)
    val sprigColor = if (isDark) DarkAccent else BrandGreen
    val resId = resolveCountrysideDrawable(illustrationBase, isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(heroHeight),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.66f)
                .height(heroHeight)
                .offset(x = 6.dp, y = 4.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.58f)
                .padding(start = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LeafSprig(
                    color = sprigColor,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable(role = Role.Button) { onLogoToggleTheme() },
                )
                Text(
                    text = title,
                    color = titleColor,
                    style = TextStyle(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (title.length > 14) 24.sp else 28.sp,
                        lineHeight = 30.sp,
                        letterSpacing = (-0.3).sp,
                    ),
                )
            }
            Text(
                text = subtitle,
                color = subColor,
                style = TextStyle(
                    fontFamily = LongFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 15.5.sp,
                ),
                maxLines = 2,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 16.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isDark) DarkCard.copy(alpha = 0.72f) else SearchPillLight.copy(alpha = 0.72f))
                .border(1.dp, if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f), CircleShape)
                .clickable(role = Role.Button) { onHistoryClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "Search history",
                tint = if (isDark) DarkAccent else DeepForest.copy(alpha = 0.78f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * CountrysideSearchPill — Elevated warm parchment capsule search bar supporting live typing,
 * clear button, camera/mic, or filter sliders icon.
 */
@Composable
fun CountrysideSearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    onSubmit: () -> Unit = {},
    showCameraMic: Boolean = false,
    showFilter: Boolean = false,
    showMicOnly: Boolean = false,
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val bg = if (isDark) DarkCard.copy(alpha = 0.96f) else SearchPillLight
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val iconTint = if (isDark) DarkAccent else DeepForest.copy(alpha = 0.65f)
    val textColor = if (isDark) DarkForeground else DeepForest
    val placeholderColor = if (isDark) DarkMutedForeground else WarmGrey.copy(alpha = 0.78f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = DeepForest.copy(alpha = 0.12f),
                        spotColor = DeepForest.copy(alpha = 0.12f),
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = placeholderColor,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 13.sp),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontSize = 13.5.sp,
                    color = textColor,
                ),
                cursorBrush = SolidColor(if (isDark) DarkAccent else BrandGreen),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f))
                    .clickable(role = Role.Button) { onValueChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = textColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        if (showCameraMic) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = "Scan word", tint = iconTint, modifier = Modifier.size(17.dp))
            Icon(Icons.Outlined.Mic, contentDescription = "Voice search", tint = iconTint, modifier = Modifier.size(17.dp))
        } else if (showMicOnly) {
            Icon(Icons.Outlined.Mic, contentDescription = "Voice search", tint = iconTint, modifier = Modifier.size(18.dp))
        }

        if (showFilter) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(border),
            )
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Filter",
                tint = iconTint,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(role = Role.Button) { onFilterClick() },
            )
        }
    }
}

/**
 * CountrysideFilterPills — Horizontal filter chip row ("All" solid forest green + outlined cream pills).
 */
@Composable
fun CountrysideFilterPills(
    items: List<Pair<String, ImageVector?>>,
    selected: String,
    isDark: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (label, icon) ->
            val active = label == selected
            val bg = when {
                active && isDark -> DarkAccent
                active -> BrandGreen
                isDark -> DarkCard
                else -> CardSurface
            }
            val fg = when {
                active && isDark -> Color(0xFF0E131B)
                active -> Color.White
                isDark -> DarkForeground.copy(alpha = 0.88f)
                else -> DeepForest.copy(alpha = 0.86f)
            }
            val border = if (active) Color.Transparent else if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f)

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, border, CircleShape)
                    .clickable(role = Role.Tab) { onSelect(label) }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
                }
                Text(
                    text = label,
                    color = fg,
                    style = TextStyle(
                        fontFamily = BodyFontFamily,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 11.5.sp,
                    ),
                )
            }
        }
    }
}

/**
 * BotanicalSectionTitle — Playfair Display title with botanical olive sprig + optional right action ("View all →" or "Clear All").
 */
@Composable
fun BotanicalSectionTitle(
    title: String,
    isDark: Boolean,
    sprigAfterTitle: Boolean = false,
    actionLabel: String? = "View all",
    isClearAction: Boolean = false,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val titleColor = if (isDark) DarkForeground else DeepForest
    val sprigColor = if (isDark) DarkAccent else OldWoodGold
    val actionColor = if (isDark) DarkAccent else DeepForest.copy(alpha = 0.76f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!sprigAfterTitle) {
                LeafSprig(color = sprigColor, modifier = Modifier.size(16.dp))
            }
            Text(
                text = title,
                color = titleColor,
                style = TextStyle(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp,
                ),
            )
            if (sprigAfterTitle) {
                LeafSprig(color = sprigColor, modifier = Modifier.size(16.dp))
            }
        }

        if (actionLabel != null) {
            Row(
                modifier = Modifier
                    .clickable(role = Role.Button) { onAction() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = actionLabel,
                    color = actionColor,
                    style = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp),
                )
                Icon(
                    imageVector = if (isClearAction) Icons.Outlined.DeleteOutline else Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = actionColor,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

/**
 * CountrysideQuoteBanner — Panoramic English village / stone bridge / stacked books quote banner
 * with unified "— Lexicon —" literary attribution.
 */
@Composable
fun CountrysideQuoteBanner(
    quote: String,
    attribution: String = "Lexicon",
    illustrationBase: String = "ill_banner_bridge",
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBg = if (isDark) DarkCard else CardSurface
    val border = if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.11f)
    val quoteColor = if (isDark) DarkForeground else DeepForest
    val attrColor = if (isDark) DarkAccent else OldWoodGold
    val resId = resolveCountrysideDrawable(illustrationBase, isDark)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(shape)
            .background(cardBg)
            .border(1.dp, border, shape),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.58f),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to cardBg,
                        0.46f to cardBg.copy(alpha = 0.94f),
                        0.72f to Color.Transparent,
                    )
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.58f)
                .padding(start = 16.dp, end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "“$quote”",
                color = quoteColor,
                style = TextStyle(
                    fontFamily = DisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "— $attribution —",
                color = attrColor,
                style = TextStyle(
                    fontFamily = BodyFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                ),
            )
        }
    }
}
