package com.lexicon.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.remember
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
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val compact = config.screenWidthDp < 390
    val accessibilityText = config.fontScale >= 1.3f
    // On phone-sized windows, free space for the watercolor quotation below.
    // Retain increased height for larger system fonts and full artwork on tablets.
    val phoneHeight = config.screenWidthDp < 600 && config.screenHeightDp < 940
    val responsiveHeight = when {
        // The original painting is portrait-ish (~508x345). On an 800dp
        // tablet a 146dp hero leaves it a tiny corner vignette; give the
        // whole scene real page space without cropping a single brushstroke.
        config.screenWidthDp >= 600 ->
            maxOf(heroHeight + 64.dp, (config.screenWidthDp * 0.34f).dp)
                .coerceAtMost(300.dp)
        accessibilityText -> heroHeight + 62.dp
        phoneHeight -> heroHeight + 12.dp
        else -> heroHeight
    }
    val titleWidth = if (compact || accessibilityText) 0.76f else 0.58f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(responsiveHeight),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.90f)
                .height(responsiveHeight),
        )

        // A responsive painted-paper wash protects legibility
        // without removing the watercolor landscape.
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0.0f to (if (isDark) Color(0xFF0E131B).copy(alpha = 0.70f)
                        else Color(0xFFF3EBDD).copy(alpha = 0.65f)),
                    0.38f to (if (isDark) Color(0xFF0E131B).copy(alpha = 0.32f)
                        else Color(0xFFF3EBDD).copy(alpha = 0.27f)),
                    0.78f to Color.Transparent,
                )
            ),
        )
        if (isDark) {
            NocturneAtmosphere(Modifier.fillMaxSize())
            if (illustrationBase == "ill_hero_cottage") {
                CottageWindowGlow(Modifier.fillMaxSize())
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(titleWidth)
                .padding(start = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Keep the painted ornament 26dp, but let the tap target
                // breathe at 44dp without an opaque background.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(role = Role.Button, onClickLabel = "Toggle appearance") {
                            onLogoToggleTheme()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    LeafSprig(color = sprigColor, modifier = Modifier.size(26.dp))
                }
                Text(
                    text = title,
                    color = titleColor,
                    style = TextStyle(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (compact || title.length > 14) 24.sp else 28.sp,
                        lineHeight = if (accessibilityText) 35.sp else 30.sp,
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
                maxLines = if (accessibilityText) 4 else 2,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 16.dp)
                .size(44.dp)
                .clickable(role = Role.Button, onClickLabel = "Search history") {
                    onHistoryClick()
                },
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
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
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
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        }

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(role = Role.Button, onClickLabel = "Clear search") {
                        onValueChange("")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear",
                    tint = textColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DarkChipBorder else DeepForest.copy(alpha = 0.12f))
                        .padding(4.dp),
                )
            }
        }

        if (showCameraMic) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = "Scan word", tint = iconTint, modifier = Modifier.size(17.dp))
            Icon(Icons.Outlined.Mic, contentDescription = "Voice search", tint = iconTint, modifier = Modifier.size(17.dp))
        } else if (showMicOnly) {
            Icon(
                Icons.Outlined.Mic,
                contentDescription = "Focus search; keyboard dictation may be available",
                tint = iconTint,
                modifier = Modifier.size(44.dp).clickable(role = Role.Button) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                }.padding(10.dp),
            )
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
                contentDescription = "Change search filters",
                tint = iconTint,
                modifier = Modifier
                    .size(44.dp)
                    .clickable(role = Role.Button) { onFilterClick() }
                    .padding(12.dp),
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
 * with unified "— Lexora —" literary attribution.
 */
@Composable
fun CountrysideQuoteBanner(
    quote: String,
    attribution: String = "Lexora",
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
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    // The 94dp phone banner is too shallow for an 800dp tablet: the
    // complete watercolor becomes a tiny isolated thumbnail. Scale its
    // bookplate height with usable width, but never crop the original art.
    val scaledHeight = ((config.screenWidthDp - 32).coerceAtLeast(0) * 0.20f)
        .dp.coerceIn(94.dp, 164.dp)
    val bannerHeight = if (config.fontScale >= 1.3f) maxOf(scaledHeight, 120.dp) else scaledHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .clip(shape)
            .background(cardBg)
            .border(1.dp, border, shape),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterEnd,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to cardBg.copy(alpha = 0.22f),
                        0.33f to cardBg.copy(alpha = 0.14f),
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
                maxLines = if (config.fontScale >= 1.3f) 3 else 2,
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


/**
 * A responsive, fully editable native-vector layer for the moonlit English
 * countryside. Tiny fireflies and starlight are positioned as fractions of
 * the available canvas; nothing here is a flattened raster illustration.
 *
 * Keep this atmosphere subtle: the painted cottage and the written word
 * remain the protagonists. No animation, so reduced-motion is respected.
 */
@Composable
fun NocturneAtmosphere(modifier: Modifier = Modifier) {
    val sparks = listOf(
        floatArrayOf(.54f, .10f, .90f), floatArrayOf(.60f, .22f, .68f),
        floatArrayOf(.67f, .08f, .58f), floatArrayOf(.76f, .18f, 1.0f),
        floatArrayOf(.86f, .09f, .70f), floatArrayOf(.94f, .26f, .66f),
        floatArrayOf(.57f, .37f, .52f), floatArrayOf(.71f, .33f, .79f),
        floatArrayOf(.83f, .39f, .55f), floatArrayOf(.96f, .47f, .85f),
        floatArrayOf(.64f, .53f, .74f), floatArrayOf(.76f, .60f, .56f),
        floatArrayOf(.89f, .66f, .88f), floatArrayOf(.53f, .72f, .48f),
        floatArrayOf(.69f, .82f, .75f), floatArrayOf(.82f, .89f, .52f),
    )
    Canvas(modifier = modifier) {
        val gold = Color(0xFFF0D89A)
        val soft = Color(0xFFBBD0D0)
        sparks.forEachIndexed { index, point ->
            val center = Offset(size.width * point[0], size.height * point[1])
            val intensity = point[2]
            val radius = (if (index % 4 == 0) 1.5f else 0.85f).dp.toPx()
            drawCircle(
                color = gold.copy(alpha = 0.045f * intensity),
                radius = radius * 7f,
                center = center,
            )
            drawCircle(
                color = (if (index % 3 == 0) gold else soft).copy(alpha = 0.54f * intensity),
                radius = radius,
                center = center,
            )
            if (index % 4 == 0) {
                val arm = 5.dp.toPx() * intensity
                drawLine(
                    color = gold.copy(alpha = 0.36f),
                    start = Offset(center.x - arm, center.y),
                    end = Offset(center.x + arm, center.y),
                    strokeWidth = 0.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = gold.copy(alpha = 0.36f),
                    start = Offset(center.x, center.y - arm),
                    end = Offset(center.x, center.y + arm),
                    strokeWidth = 0.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        // Fine distant stars are separate editable vector points, never
        // a repeated bitmap or animation.
        repeat(64) { index ->
            val x = 0.48f + ((index * 37 % 97) / 96f) * 0.50f
            val y = 0.04f + ((index * 41 % 89) / 88f) * 0.78f
            val radius = (0.32f + (index % 4) * 0.11f).dp.toPx()
            drawCircle(
                color = (if (index % 5 == 0) gold else soft)
                    .copy(alpha = 0.14f + (index % 3) * 0.055f),
                radius = radius,
                center = Offset(size.width * x, size.height * y),
            )
        }
    }
}


/**
 * A native-vector paper wash shared across the existing countryside screens.
 * Deterministic normalised coordinates keep it responsive, non-animated and
 * visually consistent with the editable SVG and SwiftUI drawing.
 */
@Composable
fun CountrysideMaterialOverlay(isDark: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val pigment = if (isDark) Color(0xFFF0D89A) else Color(0xFF8A7152)
        val wash = if (isDark) Color(0xFF334A5E) else Color(0xFFC98267)
        val side = size.minDimension
        drawCircle(
            color = wash.copy(alpha = if (isDark) 0.025f else 0.022f),
            radius = side * 0.27f,
            center = Offset(size.width * 0.04f, size.height * 0.16f),
        )
        drawCircle(
            color = wash.copy(alpha = if (isDark) 0.025f else 0.018f),
            radius = side * 0.34f,
            center = Offset(size.width * 0.96f, size.height * 0.77f),
        )
        // Fine non-repeating page granulation. Same normalised editable
        // geometry as SwiftUI and the companion source SVGs.
        repeat(620) { i ->
            val x = ((i * 73 + 19) % 997).toFloat() / 997f
            val y = ((i * 101 + 37) % 991).toFloat() / 991f
            drawCircle(
                color = pigment.copy(alpha = if (isDark) 0.045f else 0.062f),
                radius = (0.36f + (i % 5) * 0.13f).dp.toPx(),
                center = Offset(size.width * x, size.height * y),
            )
        }
    }
}

/** Editable lantern-lit windows over the moonlit watercolor cottage. */
@Composable
fun CottageWindowGlow(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val glowPoints = listOf(Offset(.80f, .69f), Offset(.855f, .67f), Offset(.88f, .74f))
        val pale = Color(0xFFF0D89A)
        for (p in glowPoints) {
            val c = Offset(size.width * p.x, size.height * p.y)
            drawCircle(pale.copy(alpha = .085f), 15.dp.toPx(), c)
            drawCircle(Color(0xFFD4A45A).copy(alpha = .23f), 5.dp.toPx(), c)
            drawCircle(pale.copy(alpha = .72f), 1.25.dp.toPx(), c)
        }
    }
}
