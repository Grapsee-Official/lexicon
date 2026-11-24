package com.lexicon.ui.components

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.R
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.sectionHeading
import com.lexicon.ui.theme.BrandGreen
import com.lexicon.ui.theme.DarkAccent
import com.lexicon.ui.theme.WarmCream
import com.lexicon.ui.theme.DarkForeground
import com.lexicon.ui.theme.DarkSage
import com.lexicon.ui.theme.DeepForest
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.OldWoodGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.math.exp

@Composable
fun Hero(
    viewModel: LexiconViewModel,
    modifier: Modifier = Modifier,
    sharedNightScene: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.isDark

    val headlineMain = if (isDark) DarkForeground else DeepForest
    val headlineWords = if (isDark) DarkSage else BrandGreen
    val sprigColor = if (isDark) DarkAccent else OldWoodGold
    val subtextColor = if (isDark) Color(0xFFD3C6B2) else DeepForest.copy(alpha = 0.90f)
    val configuration = LocalConfiguration.current
    val largeText = configuration.fontScale > 1.2f

    // Use the actual content width, including split-screen and tablet windows.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        val expanded = maxWidth >= 600.dp
        val heroHeight = when {
            expanded && largeText -> 425.dp
            expanded -> 365.dp
            largeText -> 330.dp
            compact -> if (isDark) 250.dp else 290.dp
            else -> if (isDark) 242.dp else 278.dp
        }
        val textWidth = when {
            isDark && largeText -> 0.70f
            isDark -> 0.51f
            compact || largeText -> 0.60f
            else -> 0.52f
        }
        val headlineStyle = when {
            expanded -> LexiconTextStyles.HeroHeadline.copy(fontSize = 36.sp, lineHeight = 42.sp)
            isDark && !largeText -> LexiconTextStyles.HeroHeadline.copy(fontSize = if (compact) 24.sp else 25.sp, lineHeight = 29.sp)
            else -> LexiconTextStyles.HeroHeadline
        }
        val paragraphStyle = if (isDark && !largeText) {
            LexiconTextStyles.HeroParagraph.copy(fontSize = 10.5.sp, lineHeight = 14.5.sp)
        } else LexiconTextStyles.HeroParagraph

        Box(
            modifier = Modifier.fillMaxWidth()
                .then(if (isDark) Modifier.heightIn(min = heroHeight) else Modifier.height(heroHeight)),
        ) {
            if (isDark) {
                if (!sharedNightScene) {
                    HomeNightScene(Modifier.matchParentSize())
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.hero_light),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth(if (compact) 0.82f else 0.88f)
                        .height(heroHeight),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth(0.60f)
                        .height(heroHeight)
                        .background(
                            Brush.horizontalGradient(
                                0.00f to WarmCream.copy(alpha = 0.94f),
                                0.42f to WarmCream.copy(alpha = 0.72f),
                                1.00f to Color.Transparent,
                            ),
                        ),
                )
            }

            val heroCopy: @Composable (Modifier) -> Unit = { copyModifier ->
                Column(
                    modifier = copyModifier,
                    verticalArrangement = Arrangement.spacedBy(if (isDark) 6.dp else 8.dp),
                ) {
                    Column(modifier = Modifier.sectionHeading(1)) {
                        Text(
                            text = "Discover the",
                            color = headlineMain,
                            style = headlineStyle,
                        )
                        Text(
                            text = "World of",
                            color = headlineMain,
                            style = headlineStyle,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Words",
                                color = headlineWords,
                                style = headlineStyle,
                            )
                            Spacer(Modifier.width(8.dp))
                            LeafSprig(
                                color = sprigColor,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }

                    Text(
                        text = "A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word.",
                        color = subtextColor,
                        style = paragraphStyle,
                        modifier = Modifier.fillMaxWidth(if (isDark && largeText) 1f else 0.88f),
                        maxLines = if (isDark) Int.MAX_VALUE else if (largeText) 8 else 7,
                    )
                }
            }

            if (isDark) {
                // Native vertical flow reserves real space for the full paragraph.
                // Enlarged text increases the hero instead of hiding behind Search.
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = heroHeight).padding(bottom = 4.dp),
                    // Exactly two children: extra space goes between copy and search,
                    // while the copy's bottom padding guarantees the minimum gap.
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    heroCopy(
                        Modifier.fillMaxWidth(textWidth)
                            .padding(start = 20.dp, top = if (largeText) 8.dp else 15.dp, bottom = 10.dp),
                    )
                    FloatingSearchBar(
                        viewModel = viewModel,
                        sceneIntegrated = true,
                        modifier = Modifier.padding(start = 20.dp)
                            .fillMaxWidth(if (largeText) 0.85f else 0.73f),
                    )
                }
            } else {
                heroCopy(
                    Modifier.align(Alignment.TopStart).fillMaxWidth(textWidth)
                        .padding(start = 18.dp, top = 4.dp),
                )
                FloatingSearchBar(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 2.dp)
                        .fillMaxWidth(if (compact) 0.90f else 0.75f),
                )
            }
        }
    }
}

// The painting belongs to the masthead, not the text or search layout.
// Home supplies a layer spanning the hero and the trending controls.
@Composable
fun HomeNightScene(modifier: Modifier = Modifier, extendsBehindTags: Boolean = false) {
    val largeText = LocalConfiguration.current.fontScale > 1.2f
    val painting = rememberPreparedArtwork(R.drawable.hero_dark, ArtworkTreatment.MOONLIT)
    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val intrinsic = painting.intrinsicSize
        val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f)
            intrinsic.width / intrinsic.height else 1.5f
        // Source/reference landmark comparison: reduce the horizontal scale
        // and gently extend the vertical composition. This brings the book
        // into the lower search area without enlarging the roof and crescent.
        val stretch = if (largeText) 1f else 1.11f
        val artWidth = minOf(size.width * (if (largeText) 0.96f else 0.98f),
            size.height * aspect / stretch * 1.08f)
        val artHeight = artWidth / aspect * stretch
        val artX = maxOf(size.width * 0.63f - artWidth * 0.54f,
            size.width - artWidth * 0.95f)
        // Source landmarks: moon ~54%,17%; book center ~62%,69%.
        // Reserve the extra tag area for the fading foreground, not extra sky.
        val moonY = minOf(size.height * (if (extendsBehindTags) 0.09f else 0.10f), size.width * 0.055f)
        val artY = if (largeText) size.height * 0.90f - artHeight
            else moonY - artHeight * 0.17f
        clipRect {
            translate(left = artX, top = artY) {
                with(painting) { draw(size = Size(artWidth, artHeight)) }
            }
        }

        // Diagonal reading shadow: protect the text at the upper left,
        // allow the stacked books to emerge farther left at the bottom.
        val edge = if (largeText) 0.69f else if (size.width < 360.dp.toPx()) 0.52f else 0.49f
        drawRect(
            brush = Brush.linearGradient(
                0f to Color.Transparent,
                (edge - 0.19f) to Color.Transparent,
                (edge - 0.10f) to Color.Black.copy(alpha = 0.24f),
                (edge - 0.02f) to Color.Black.copy(alpha = 0.64f),
                (edge + 0.09f) to Color.Black,
                1f to Color.Black,
                start = Offset.Zero,
                end = Offset(size.width, size.height * 0.23f),
            ),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Black,
                0.89f to Color.Black,
                0.96f to Color.Black.copy(alpha = 0.82f),
                1f to Color.Black.copy(alpha = 0.66f),
            ),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.025f to Color.Black.copy(alpha = 0.48f),
                0.055f to Color.Black,
                0.80f to Color.Black,
                0.91f to Color.Black.copy(alpha = 0.43f),
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }
}


/**
 * Non-destructive presentation of original Home PNGs. Cached, downsampled
 * bookplates use exactly the same recipe in light and dark. Night scenes
 * retain the original alpha, with a luminance-dependent shadow dissolve and
 * a highlight-preserving tone curve. No generated/replacement illustration.
 */
internal enum class ArtworkTreatment { MOONLIT, STORY, BOOKPLATE }

private val preparedArtworkCache = object : LruCache<String, ImageBitmap>(24 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap): Int =
        value.width * value.height * 4
}

@Composable
internal fun rememberPreparedArtwork(resId: Int, treatment: ArtworkTreatment): Painter {
    val resources = LocalContext.current.resources
    val key = "$resId:$treatment"
    val prepared by produceState(
        initialValue = PreparedArtworkState(preparedArtworkCache.get(key), false),
        key,
    ) {
        val bitmap = withContext(Dispatchers.Default) {
            preparedArtworkCache.get(key) ?: prepareArtwork(resources, resId, treatment)
                ?.also { preparedArtworkCache.put(key, it) }
        }
        value = PreparedArtworkState(bitmap, true)
    }
    val image = prepared.bitmap
    return when {
        image != null -> remember(image) { BitmapPainter(image) }
        prepared.finished -> painterResource(resId)
        // A transparent placeholder exposes the intended ink while decoding.
        // Never flash the ungraded, full-resolution image on a cold start.
        else -> remember { ColorPainter(Color.Transparent) }
    }
}

private data class PreparedArtworkState(val bitmap: ImageBitmap?, val finished: Boolean)

private fun smoothArtworkStep(lo: Float, hi: Float, value: Float): Float {
    val t = ((value - lo) / (hi - lo)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun prepareArtwork(
    resources: Resources, resId: Int, treatment: ArtworkTreatment,
): ImageBitmap? = runCatching {
    val source = BitmapFactory.decodeResource(resources, resId, BitmapFactory.Options().apply {
        inScaled = false
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }) ?: return null
    var bitmap = source
    if (treatment == ArtworkTreatment.BOOKPLATE) {
        // Bound working memory before examining the transparent perimeter.
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest > 1280) {
            val scale = 1280f / longest
            bitmap = Bitmap.createScaledBitmap(bitmap,
                (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                (bitmap.height * scale).roundToInt().coerceAtLeast(1), true)
        }
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        // Already-transparent PNGs are left alone. An opaque, uniform neutral
        // matte is removed only from regions connected to the perimeter.
        if (removeNeutralBookplateMatte(pixels, w, h)) {
            bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        }
        var left = w
        var top = h
        var right = -1
        var bottom = -1
        var transparent = 0L
        for (y in 0 until h) {
            for (x in 0 until w) {
                val alpha = pixels[y * w + x] ushr 24
                if (alpha < 12) transparent++
                if (alpha >= 16) {
                    left = minOf(left, x)
                    top = minOf(top, y)
                    right = maxOf(right, x)
                    bottom = maxOf(bottom, y)
                }
            }
        }
        if (right >= left && bottom >= top && transparent > w.toLong() * h / 8) {
            val pad = (maxOf(right - left + 1, bottom - top + 1) * 0.055f).roundToInt()
            left = (left - pad).coerceAtLeast(0)
            top = (top - pad).coerceAtLeast(0)
            right = (right + pad).coerceAtMost(w - 1)
            bottom = (bottom + pad).coerceAtMost(h - 1)
            bitmap = Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
        }
    }
    val limit = if (treatment == ArtworkTreatment.BOOKPLATE) 640 else 1536
    val longest = maxOf(bitmap.width, bitmap.height)
    if (longest > limit) {
        val scale = limit.toFloat() / longest
        bitmap = Bitmap.createScaledBitmap(bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1), true)
    }
    if (treatment == ArtworkTreatment.BOOKPLATE) return bitmap.asImageBitmap()

    val w = bitmap.width
    val h = bitmap.height
    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
    val moon = treatment == ArtworkTreatment.MOONLIT
    for (i in pixels.indices) {
        val p = pixels[i]
        val a = p ushr 24
        if (a == 0) continue
        val r = ((p ushr 16) and 255) / 255f
        val g = ((p ushr 8) and 255) / 255f
        val b = (p and 255) / 255f
        val l = r * 0.2126f + g * 0.7152f + b * 0.0722f
        val highlight = smoothArtworkStep(0.63f, 0.95f, l)
        val warm = smoothArtworkStep(0.06f, 0.24f, r - b)
        // Source-space exposure: the open pages catch more light than the
        // surrounding canopy. The distant moon and window highlights stay
        // luminous; outer foliage recedes into the shared night ink.
        val sx = (i % w).toFloat() / maxOf(1, w - 1)
        val sy = (i / w).toFloat() / maxOf(1, h - 1)
        val paper = smoothArtworkStep(0.48f, 0.60f, sy) *
            (1f - smoothArtworkStep(0.81f, 0.95f, sy)) *
            smoothArtworkStep(0.35f, 0.48f, sx) *
            (1f - smoothArtworkStep(0.83f, 0.96f, sx))
        val canopy = (1f - smoothArtworkStep(0.44f, 0.60f, sy)) *
            (1f - smoothArtworkStep(0.48f, 0.61f, sx))
        val outerFoliage = smoothArtworkStep(0.80f, 0.97f, sx)
        val localExposure = if (moon) (0.14f * paper - 0.10f * canopy -
            0.06f * outerFoliage) * (1f - highlight) else 0f
        val baseGain = ((if (moon) 0.76f else 0.85f) +
            (if (moon) 0.22f else 0.13f) * highlight -
            (if (moon) 0.045f else 0.02f) * warm * (1f - highlight) +
            localExposure).coerceIn(0f, 1f)
        val saturation = 0.80f + 0.18f * highlight
        val shadow = 1f - smoothArtworkStep(0.09f, 0.35f, l)
        // Keep the actual crescent, windows, and open pages luminous.
        // The isolated outer foliage and bright petals are less important
        // than the reading foreground; dissolve them into the night field.
        val moonDx = (sx - 0.54f) / 0.105f
        val moonDy = (sy - 0.17f) / 0.14f
        val moonRegion = exp(-(moonDx * moonDx + moonDy * moonDy))
        val windowRegion = smoothArtworkStep(0.51f, 0.58f, sx) *
            (1f - smoothArtworkStep(0.87f, 0.94f, sx)) *
            smoothArtworkStep(0.23f, 0.35f, sy) *
            (1f - smoothArtworkStep(0.51f, 0.60f, sy))
        val foreground = smoothArtworkStep(0.49f, 0.61f, sy) *
            (1f - smoothArtworkStep(0.85f, 0.96f, sy)) *
            smoothArtworkStep(0.32f, 0.43f, sx) *
            (1f - smoothArtworkStep(0.85f, 0.95f, sx))
        val keep = (moonRegion + windowRegion + foreground).coerceIn(0f, 1f)
        val gain = if (moon) baseGain * (0.72f + 0.28f * keep) else baseGain
        val baseAlpha = a * (0.58f + 0.42f * smoothArtworkStep(0.035f, 0.24f, l))
        val alpha = if (moon) maxOf(
            baseAlpha * (0.69f + 0.31f * smoothArtworkStep(0.16f, 0.55f, l)) *
                (0.68f + 0.32f * keep),
            baseAlpha * keep * 0.94f,
        ) else baseAlpha
        val rr = (l + (r - l) * saturation) * gain * (1f - 0.10f * shadow)
        val gg = (l + (g - l) * saturation) * gain
        val bb = (l + (b - l) * saturation) * gain + 0.012f * shadow
        pixels[i] = (alpha.roundToInt().coerceIn(0, 255) shl 24) or
            ((rr * 255f).roundToInt().coerceIn(0, 255) shl 16) or
            ((gg * 255f).roundToInt().coerceIn(0, 255) shl 8) or
            (bb * 255f).roundToInt().coerceIn(0, 255)
    }
    Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
}.getOrNull()


// Conservative matte recognition, not global chroma-keying. Require all
// four borders AND an inset ring to match a nearly neutral opaque color.
// Flood only edge-connected pixels, so enclosed light/dark details survive.
private fun removeNeutralBookplateMatte(pixels: IntArray, w: Int, h: Int): Boolean {
    if (w < 32 || h < 32 || pixels.any { (it ushr 24) < 250 }) return false
    val matte = pixels[0]
    val mr = (matte ushr 16) and 255
    val mg = (matte ushr 8) and 255
    val mb = matte and 255
    val high = maxOf(mr, mg, mb)
    val low = minOf(mr, mg, mb)
    if (high - low > 16 || (high > 38 && low < 218)) return false
    fun distance(p: Int): Int = maxOf(
        kotlin.math.abs(((p ushr 16) and 255) - mr),
        kotlin.math.abs(((p ushr 8) and 255) - mg),
        kotlin.math.abs((p and 255) - mb),
    )
    val inset = (minOf(w, h) * 0.018f).roundToInt().coerceAtLeast(1)
    for (ring in intArrayOf(0, inset)) {
        for (sample in 0..24) {
            val x = ring + (w - 1 - 2 * ring) * sample / 24
            val y = ring + (h - 1 - 2 * ring) * sample / 24
            if (distance(pixels[ring * w + x]) > 9 ||
                distance(pixels[(h - 1 - ring) * w + x]) > 9 ||
                distance(pixels[y * w + ring]) > 9 ||
                distance(pixels[y * w + w - 1 - ring]) > 9) return false
        }
    }
    val visited = ByteArray(pixels.size)
    val queue = IntArray(pixels.size)
    var head = 0
    var tail = 0
    fun offer(i: Int) {
        if (visited[i].toInt() != 0) return
        visited[i] = 1
        if (distance(pixels[i]) < 26) queue[tail++] = i
    }
    for (x in 0 until w) { offer(x); offer((h - 1) * w + x) }
    for (y in 0 until h) { offer(y * w); offer(y * w + w - 1) }
    if (tail == 0) return false
    while (head < tail) {
        val i = queue[head++]
        val p = pixels[i]
        val alpha = smoothArtworkStep(8f, 26f, distance(p).toFloat())
        if (alpha <= 0.001f) {
            pixels[i] = 0
        } else {
            // Unmix the matte from the antialiased fringe before compositing.
            fun unmix(c: Int, m: Int): Int =
                ((c - m * (1f - alpha)) / alpha).roundToInt().coerceIn(0, 255)
            pixels[i] = ((alpha * 255).roundToInt() shl 24) or
                (unmix((p ushr 16) and 255, mr) shl 16) or
                (unmix((p ushr 8) and 255, mg) shl 8) or unmix(p and 255, mb)
        }
        val x = i % w
        val y = i / w
        if (x > 0) offer(i - 1)
        if (x + 1 < w) offer(i + 1)
        if (y > 0) offer(i - w)
        if (y + 1 < h) offer(i + w)
    }
    return true
}
