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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
fun resolveCountrysideDrawable(baseName: String, isDark: Boolean, scene: String? = null): Int {
    val ctx = LocalContext.current
    if (scene != null) {
        // Interior scenes share geometry, but each appearance has its own
        // authored vector pigments. Dark Home retains its dedicated artwork.
        val appearance = if (isDark) "dark" else "light"
        val reference = ctx.resources.getIdentifier("ref_${appearance}_" + scene.replace('-', '_'), "drawable", ctx.packageName)
        if (reference != 0) return reference
    }
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
    onBack: (() -> Unit)? = null,
) {
    val titleColor = if (isDark) DarkForeground else Color(0xFF31462F)
    val subColor = if (isDark) DarkForeground.copy(alpha = .88f) else Color(0xFF31462F)
    val sprigColor = if (isDark) DarkAccent else OldWoodGold
    val scene = when (title) {
        "Notes" -> "notes-hero"
        "Study List" -> "study-hero"
        "Quick Templates" -> "templates-hero"
        "Search" -> "search-hero"
        "Dictionary\nSearch" -> "result-hero"
        "Dictionary" -> if (subtitle.isBlank()) "detail-hero" else "dictionary-hero"
        else -> null
    }
    val resId = resolveCountrysideDrawable(illustrationBase, isDark, scene)
    val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
    androidx.compose.foundation.layout.BoxWithConstraints(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
    ) {
        val w = maxWidth
        // All boards share the same 832-wide editorial grid, but retain
        // their own title, subtitle and painting.
        val inset = w * (42f / 832f)
        val stacked = fontScale >= 1.25f || w.value / fontScale.coerceAtLeast(1f) < 280f
        val height = (w * (if (onBack != null) 255f / 832f else 310f / 832f)).coerceIn(110.dp, 275.dp)
        val titleSize = (w.value * (if (title == "Quick Templates") 43f / 832f else 48f / 832f)).coerceIn(18f, 37f)
        val subtitleSize = (w.value * (23f / 832f)).coerceIn(11f, 18f)
        val sprigSize = (w * (50f / 832f)).coerceIn(22.dp, 38.dp)
        val copy: @Composable (Modifier) -> Unit = { copyModifier ->
            Column(copyModifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (onBack != null) {
                        Box(Modifier.size(44.dp).clickable(role = Role.Button) { onBack() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = titleColor)
                        }
                    } else {
                        LeafSprig(color = sprigColor, modifier = Modifier.size(sprigSize)
                            .clickable(role = Role.Button) { onLogoToggleTheme() })
                    }
                    Text(title, color = titleColor,
                        style = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal,
                            fontSize = titleSize.sp, lineHeight = (titleSize * 1.10f).sp, letterSpacing = (-.4).sp))
                }
                if (subtitle.isNotBlank()) Text(subtitle, color = subColor,
                    style = TextStyle(fontFamily = LongFontFamily, fontSize = subtitleSize.sp,
                        lineHeight = (subtitleSize * 1.34f).sp))
            }
        }
        val history: @Composable (Modifier) -> Unit = { position ->
            Box(position.size(44.dp).clickable(role = Role.Button) { onHistoryClick() },
                contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.History, contentDescription = "Search history",
                    tint = if (isDark) DarkAccent else Color(0xFF6C5738),
                    modifier = Modifier.size((w * (37f / 832f)).coerceIn(18.dp, 29.dp)))
            }
        }
        val painting: @Composable (Modifier) -> Unit = { position ->
            Image(painter = rememberCountrysidePainter(resId, w * .70f), contentDescription = null,
                contentScale = ContentScale.Fit, alignment = Alignment.BottomEnd, modifier = position)
        }
        if (stacked) {
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth()) {
                    copy(Modifier.fillMaxWidth().padding(start = inset, end = inset + (if (title == "Search") 0.dp else 44.dp), top = 12.dp))
                    if (title != "Search") history(Modifier.align(Alignment.TopEnd).padding(end = inset))
                }
                painting(Modifier.fillMaxWidth().height(height))
            }
        } else {
            Box(Modifier.fillMaxWidth().heightIn(min = height)) {
                painting(Modifier.align(Alignment.BottomEnd).fillMaxWidth(.72f).height(height))
                // A thin local edge wash; avoid a full-screen cream veil over
                // the cottage, church spire and foliage.
                Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(.56f).height(height)
                    .background(Brush.horizontalGradient(
                        0f to (if (isDark) Color(0xFF07121C) else Color(0xFFFFF1D9)).copy(alpha = .76f),
                        .44f to (if (isDark) Color(0xFF07121C) else Color(0xFFFFF1D9)).copy(alpha = .65f),
                        1f to Color.Transparent)))
                copy(Modifier.align(Alignment.CenterStart).fillMaxWidth(if (title == "Quick Templates") .60f else .53f)
                    .padding(start = inset, end = 2.dp, top = 12.dp, bottom = height * .06f))
                if (title != "Search") history(Modifier.align(Alignment.TopEnd).padding(end = inset, top = 4.dp))
            }
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
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = 0.22f)
    val iconTint = if (isDark) DarkAccent else DeepForest.copy(alpha = 0.65f)
    val textColor = if (isDark) DarkForeground else DeepForest
    val placeholderColor = if (isDark) DarkMutedForeground else Color(0xFF947650)

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
            .heightIn(min = 42.dp)
            .padding(horizontal = 15.dp, vertical = 8.dp),
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
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = 0.27f))
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
            Icon(
                Icons.Outlined.Mic,
                contentDescription = "Open keyboard for voice dictation",
                tint = iconTint,
                modifier = Modifier.size(26.dp).clickable(role = Role.Button) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                },
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
                else -> Color(0xFFFFF2DF).copy(alpha = .55f)
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
            modifier = Modifier.weight(1f),
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
                    fontSize = 18.sp,
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
    val cardBg = if (isDark) DarkCard else Color(0xFFF8EBD0)
    val border = if (isDark) DarkChipBorder else OldWoodGold.copy(alpha = .27f)
    val quoteColor = if (isDark) DarkForeground else Color(0xFF243B37)
    val attrColor = if (isDark) DarkAccent else Color(0xFF657D5A)
    val scene = when (quote) {
        "A well-written note is a small step towards wisdom." -> "notes-quote"
        "Knowledge is a journey, not a destination." -> "study-quote"
        "Better words. A brighter mind." -> "templates-quote"
        "A word is a doorway to a bigger world." -> "search-quote"
        "Words are doors to new worlds." -> "dictionary-books"
        "Sometimes, the best things in life are found by chance." -> "detail-quote"
        "Discover the beauty in every word." -> "result-quote"
        else -> null
    }
    val resId = resolveCountrysideDrawable(illustrationBase, isDark, scene)
    val shape = RoundedCornerShape(16.dp)
    val fontScale = androidx.compose.ui.platform.LocalConfiguration.current.fontScale
    androidx.compose.foundation.layout.BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = maxWidth
        val ratio = when (scene) {
            "search-quote" -> .30f
            "detail-quote" -> .203f
            "dictionary-books" -> .27f
            "result-quote" -> .25f
            else -> .25f
        }
        val bannerHeight = (width * ratio).coerceIn(72.dp, 180.dp)
        val stacked = fontScale >= 1.3f || width.value / fontScale.coerceAtLeast(1f) < 260f
        val painter = rememberCountrysidePainter(resId, width * if (stacked) 1f else .66f)
        val copy: @Composable (Modifier) -> Unit = { position ->
            Column(position, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)) {
                Text("“$quote”", color = quoteColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = TextStyle(fontFamily = LongFontFamily, fontStyle = FontStyle.Italic,
                        fontSize = (width.value * .037f).coerceIn(12f, 20f).sp,
                        lineHeight = (width.value * .047f).coerceIn(16f, 26f).sp))
                Text("— $attribution —", color = attrColor,
                    style = TextStyle(fontFamily = LongFontFamily, fontSize = 11.sp))
            }
        }
        val surface = Modifier.fillMaxWidth().clip(shape).background(cardBg).border(1.dp, border, shape)
        if (stacked) {
            Column(surface) {
                copy(Modifier.fillMaxWidth().padding(16.dp))
                Image(painter, null, Modifier.fillMaxWidth().height((width * .36f).coerceIn(90.dp, 180.dp)),
                    contentScale = ContentScale.Fit, alignment = Alignment.BottomEnd)
            }
        } else {
            Box(surface.heightIn(min = bannerHeight)) {
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                    Box(Modifier.fillMaxWidth(.67f).fillMaxHeight().clip(RoundedCornerShape(0.dp))) {
                        Image(painter, null, Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit, alignment = Alignment.BottomEnd)
                        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                            0f to cardBg, .18f to cardBg.copy(alpha = .70f),
                            .44f to Color.Transparent, 1f to Color.Transparent)))
                    }
                }
                copy(Modifier.align(Alignment.CenterStart).fillMaxWidth(.55f)
                    .padding(start = 15.dp, end = 5.dp, top = 10.dp, bottom = 10.dp))
            }
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
        val pigment = if (isDark) Color(0xFF8B99A0) else Color(0xFFB08A52)
        val wash = if (isDark) Color(0xFF334A5E) else Color(0xFFE0A95D)
        val side = size.minDimension
        if (side <= 0f) return@Canvas
        fun fraction(i: Int, salt: Int): Float {
            val n = ((i + 1) * 73856093L xor salt * 19349663L) and 0x7fffffff
            return ((n * 48271L) % 2147483647L).toFloat() / 2147483647f
        }
        // The unpainted paper is warmer along the edges and cooler in the
        // reading field, matching the interior reference rather than Home.
        if (!isDark) {
            drawRect(Brush.radialGradient(
                colors = listOf(Color(0xFFFFFAEF).copy(alpha = .72f), Color.Transparent),
                center = Offset(size.width * .51f, size.height * .46f), radius = size.height * .57f))
            repeat(46) { i ->
                val x = fraction(i, 4)
                val y = fraction(i, 7)
                val edge = (kotlin.math.abs(x - .5f) * 2f).coerceIn(0f, 1f)
                val center = Offset(size.width * x, size.height * y)
                val radius = side * (.035f + fraction(i, 19) * .17f)
                drawCircle(brush = Brush.radialGradient(
                    colors = listOf(wash.copy(alpha = .015f + edge * .08f), Color.Transparent),
                    center = center, radius = radius), radius = radius, center = center)
            }
        }
        repeat(if (isDark) 650 else 1800) { i ->
            val x = fraction(i, 11) * size.width
            val y = fraction(i, 31) * size.height
            val length = (.65f + fraction(i, 37) * 1.4f).dp.toPx()
            drawLine(pigment.copy(alpha = if (isDark) .035f else .065f),
                Offset(x, y), Offset(x + length, y + length * (fraction(i, 43) - .5f)),
                strokeWidth = .35.dp.toPx(), cap = StrokeCap.Round)
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
