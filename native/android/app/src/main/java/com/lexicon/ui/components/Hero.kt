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
import androidx.compose.ui.unit.Dp
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
            isDark -> (maxWidth * 0.60f).coerceIn(210.dp, 290.dp)
            compact -> 290.dp
            else -> 278.dp
        }
        val textWidth = when {
            isDark && largeText -> 0.70f
            isDark -> 0.51f
            compact || largeText -> 0.60f
            else -> 0.52f
        }
        val headlineStyle = when {
            expanded -> LexiconTextStyles.HeroHeadline.copy(fontSize = 36.sp, lineHeight = 42.sp)
            isDark && !largeText -> LexiconTextStyles.HeroHeadline.copy(
                fontSize = (maxWidth.value * 0.061f).coerceIn(22f, 27f).sp,
                lineHeight = (maxWidth.value * 0.069f).coerceIn(25f, 30.5f).sp,
            )
            else -> LexiconTextStyles.HeroHeadline
        }
        val paragraphStyle = if (isDark && !largeText) {
            LexiconTextStyles.HeroParagraph.copy(fontSize = 10.sp, lineHeight = 13.sp)
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
                        modifier = Modifier.fillMaxWidth(if (isDark && largeText) 1f else if (isDark) 0.82f else 0.88f),
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
                            .padding(start = 20.dp, top = if (largeText) 8.dp else 21.dp, bottom = 10.dp),
                    )
                    FloatingSearchBar(
                        viewModel = viewModel,
                        sceneIntegrated = true,
                        modifier = Modifier.padding(start = 20.dp)
                            .fillMaxWidth(if (largeText) 0.85f else 0.76f),
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
fun HomeNightScene(
    modifier: Modifier = Modifier,
    extendsBehindTags: Boolean = false,
    headerInset: Dp = 0.dp,
) {
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
        val header = headerInset.toPx().coerceIn(0f, size.height)
        val contentHeight = (size.height - header).coerceAtLeast(1f)
        val stretch = if (largeText) 1f else 1.04f
        val artWidth = minOf(size.width * (if (largeText) 0.96f else 0.99f),
            contentHeight * aspect / stretch * 1.08f)
        val artHeight = artWidth / aspect * stretch
        val artX = maxOf(size.width * 0.63f - artWidth * 0.54f,
            size.width - artWidth * 0.95f)
        // Source landmarks: moon ~54%,17%; book center ~62%,69%.
        // Reserve the extra tag area for the fading foreground, not extra sky.
        val moonY = minOf(contentHeight * (if (extendsBehindTags) 0.09f else 0.10f), size.width * 0.055f)
        val artY = header + if (largeText) contentHeight * 0.90f - artHeight
            else moonY - artHeight * 0.17f
        clipRect {
            translate(left = artX, top = artY) {
                with(painting) { draw(size = Size(artWidth, artHeight)) }
            }
        }

        // The source-space grade already dissolves the painted edges.
        // Use only a reading-column shadow and a short bottom fade here.
        val edge = if (largeText) 0.67f else 0.47f
        drawRect(
            brush = Brush.linearGradient(
                0f to Color.Transparent,
                (edge - 0.18f) to Color.Transparent,
                (edge - 0.08f) to Color.Black.copy(alpha = 0.40f),
                (edge + 0.07f) to Color.Black,
                1f to Color.Black,
                start = Offset.Zero,
                end = Offset(size.width, contentHeight * 0.24f),
            ),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Black,
                0.83f to Color.Black,
                0.96f to Color.Black.copy(alpha = 0.12f),
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
internal enum class ArtworkTreatment { MOONLIT, DAILY_LANDSCAPE, STORY, BOOKPLATE }

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
        var mass = 0.0
        var weightedX = 0.0
        var weightedY = 0.0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val alpha = pixels[y * w + x] ushr 24
                if (alpha < 12) transparent++
                if (alpha >= 4) {
                    val weight = alpha / 255.0
                    mass += weight
                    weightedX += (x + 0.5) * weight
                    weightedY += (y + 0.5) * weight
                    left = minOf(left, x)
                    top = minOf(top, y)
                    right = maxOf(right, x)
                    bottom = maxOf(bottom, y)
                }
            }
        }
        if (right >= left && bottom >= top && mass > 0 && transparent > w.toLong() * h / 8) {
            val paintedArea = (right - left + 1).toDouble() * (bottom - top + 1)
            val coverage = (mass / paintedArea).coerceIn(0.0, 1.0)
            val pad = (maxOf(right - left + 1, bottom - top + 1) * 0.035f).roundToInt()
            left = (left - pad).coerceAtLeast(0)
            top = (top - pad).coerceAtLeast(0)
            right = (right + pad).coerceAtMost(w - 1)
            bottom = (bottom + pad).coerceAtMost(h - 1)
            bitmap = Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
            return opticalBookplate(
                bitmap, coverage,
                weightedX / mass - left, weightedY / mass - top,
            ).asImageBitmap()
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
    if (moon) {
        val sky = OriginalSkyPatch(pixels, w, h)
        refinePaintedStars(pixels, w, h)
        gradeMoonlitPainting(pixels, w, h)
        val composed = reframeMoonlitSubjects(pixels, w, h)
        sky.extendBehind(composed, w, h)
        return Bitmap.createBitmap(composed, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    val dailyLandscape = treatment == ArtworkTreatment.DAILY_LANDSCAPE
    val lightField = ArtworkLightField(w, h)
    for (i in pixels.indices) {
        val p = pixels[i]
        val a = p ushr 24
        if (a == 0) continue
        val r = ((p ushr 16) and 255) / 255f
        val g = ((p ushr 8) and 255) / 255f
        val b = (p and 255) / 255f
        val l = r * 0.2126f + g * 0.7152f + b * 0.0722f
        // The Word of the Day is a separate landscape, not a cottage.
        // Do not apply hero-specific spatial corrections to this painting.
        val highlight = smoothArtworkStep(0.63f, 0.95f, l)
        val warm = smoothArtworkStep(0.06f, 0.24f, r - b)
        // The daily landscape is a small full-bleed painting, not an
        // emissive thumbnail. Preserve midtone foliage and let dark
        // source colors converge to its actual reading-surface ink.
        val gain = if (dailyLandscape) 0.88f + 0.10f * highlight
            else (0.85f + 0.13f * highlight - 0.02f * warm * (1f - highlight)).coerceIn(0f, 1f)
        val saturation = if (dailyLandscape) 0.90f + 0.08f * highlight
            else 0.80f + 0.18f * highlight
        val shadow = 1f - smoothArtworkStep(0.09f, 0.35f, l)
        if (!dailyLandscape) {
            val emission = (a / 255f) * smoothArtworkStep(0.45f, 0.83f, l) *
                smoothArtworkStep(0.05f, 0.22f, r - b) * 0.42f
            lightField.add(i % w, i / w, emission)
        }
        val inkMix = if (dailyLandscape) 0.72f * (1f - smoothArtworkStep(0.03f, 0.24f, l)) else 0f
        val alpha = a * (if (dailyLandscape) 0.78f else 0.58f) +
            a * (if (dailyLandscape) 0.22f else 0.42f) * smoothArtworkStep(0.035f, 0.24f, l)
        val rr = (l + (r - l) * saturation) * gain * (1f - 0.10f * shadow) *
            (1f - inkMix) + 8f / 255f * inkMix
        val gg = (l + (g - l) * saturation) * gain * (1f - inkMix) + 18f / 255f * inkMix
        val bb = ((l + (b - l) * saturation) * gain + 0.012f * shadow) *
            (1f - inkMix) + 26f / 255f * inkMix
        pixels[i] = (alpha.roundToInt().coerceIn(0, 255) shl 24) or
            ((rr * 255f).roundToInt().coerceIn(0, 255) shl 16) or
            ((gg * 255f).roundToInt().coerceIn(0, 255) shl 8) or
            (bb * 255f).roundToInt().coerceIn(0, 255)
    }
    if (!dailyLandscape) lightField.compositeBehind(pixels)
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
    // Discover the full connected region before changing a single pixel.
    // A nearly uniform painting or an artwork touching most of the edges
    // is ambiguous: keep it rather than risk erasing actual brushwork.
    while (head < tail) {
        val i = queue[head++]
        val x = i % w
        val y = i / w
        if (x > 0) offer(i - 1)
        if (x + 1 < w) offer(i + 1)
        if (y > 0) offer(i - w)
        if (y + 1 < h) offer(i + w)
    }
    if (tail < pixels.size * 0.08f || tail > pixels.size * 0.94f) return false
    var clearCount = 0
    for (j in 0 until tail) {
        if (distance(pixels[queue[j]]) <= 8) clearCount++
    }
    // Most of the candidate region must be a truly flat matte rather than
    // a gradual dark wash. Preserve uncertain art in its original form.
    if (clearCount < tail * 0.82f) return false

    for (j in 0 until tail) {
        val i = queue[j]
        val p = pixels[i]
        val alpha = smoothArtworkStep(8f, 26f, distance(p).toFloat())
        if (alpha <= 0.001f) {
            pixels[i] = 0
        } else {
            fun unmix(c: Int, m: Int): Int =
                ((c - m * (1f - alpha)) / alpha).roundToInt().coerceIn(0, 255)
            pixels[i] = ((alpha * 255).roundToInt() shl 24) or
                (unmix((p ushr 16) and 255, mr) shl 16) or
                (unmix((p ushr 8) and 255, mg) shl 8) or unmix(p and 255, mb)
        }
    }
    return true
}


/**
 * A small, image-derived light field. All emission originates in the uploaded
 * pixels. Two separable box passes approximate a soft optical bloom at a
 * fraction of the original image memory. Composite UNDER the graded painting,
 * including its transparent fringe, never over the native text or controls.
 */
private class ArtworkLightField(private val width: Int, private val height: Int) {
    private val columns = minOf(96, width)
    private val rows = minOf(height, maxOf(1, (96f * height / width).roundToInt()))
    private val values = FloatArray(columns * rows)
    private val counts = IntArray(columns * rows)

    fun add(x: Int, y: Int, value: Float) {
        val i = (y * rows / height) * columns + x * columns / width
        values[i] += value
        counts[i]++
    }

    fun compositeBehind(pixels: IntArray) {
        // counts excludes alpha-zero source pixels; use full cell area so
        // transparent borders cannot amplify a tiny light into a large halo.
        for (i in values.indices) {
            val x = i % columns
            val y = i / columns
            val cellW = ((x + 1) * width + columns - 1) / columns -
                (x * width + columns - 1) / columns
            val cellH = ((y + 1) * height + rows - 1) / rows -
                (y * height + rows - 1) / rows
            values[i] /= maxOf(1, cellW * cellH, counts[i]).toFloat()
        }
        var blurred = values
        repeat(2) { blurred = blur(blur(blurred, true), false) }
        for (y in 0 until height) {
            val fy = ((y + 0.5f) * rows / height - 0.5f).coerceIn(0f, (rows - 1).toFloat())
            val y0 = fy.toInt()
            val y1 = minOf(rows - 1, y0 + 1)
            val dy = fy - y0
            for (x in 0 until width) {
                val fx = ((x + 0.5f) * columns / width - 0.5f).coerceIn(0f, (columns - 1).toFloat())
                val x0 = fx.toInt()
                val x1 = minOf(columns - 1, x0 + 1)
                val dx = fx - x0
                val top = blurred[y0 * columns + x0] * (1f - dx) + blurred[y0 * columns + x1] * dx
                val bottom = blurred[y1 * columns + x0] * (1f - dx) + blurred[y1 * columns + x1] * dx
                val field = (top * (1f - dy) + bottom * dy) * 1.3f
                if (field < 0.002f) continue
                val i = y * width + x
                val p = pixels[i]
                val a = (p ushr 24) / 255f
                val halo = field.coerceAtMost(0.18f) * (1f - a)
                val out = a + halo
                if (out < 0.004f) continue
                fun channel(c: Int, tint: Float) =
                    ((c * a + 255f * tint * halo) / out).roundToInt().coerceIn(0, 255)
                pixels[i] = ((out * 255f).roundToInt().coerceIn(0, 255) shl 24) or
                    (channel((p ushr 16) and 255, 0.63f) shl 16) or
                    (channel((p ushr 8) and 255, 0.43f) shl 8) or channel(p and 255, 0.20f)
            }
        }
    }

    private fun blur(input: FloatArray, horizontal: Boolean): FloatArray {
        val result = FloatArray(input.size)
        for (y in 0 until rows) for (x in 0 until columns) {
            var sum = 0f
            for (d in -2..2) {
                val sx = if (horizontal) (x + d).coerceIn(0, columns - 1) else x
                val sy = if (horizontal) y else (y + d).coerceIn(0, rows - 1)
                sum += input[sy * columns + sx]
            }
            result[y * columns + x] = sum / 5f
        }
        return result
    }
}


// Source-specific art direction measured on the original 1536x1024 vignette.
// Work in straight RGB; retain the original artwork and never alter the light
// theme. Darken the decorative peripheral subjects, not the reading page.
private fun gradeMoonlitPainting(pixels: IntArray, w: Int, h: Int) {
    val distance = IntArray(pixels.size) { if ((pixels[it] ushr 24) <= 25) 0 else 32767 }
    // Chamfer distance to the actual painted edge, not the PNG rectangle.
    // Orthogonal cost 3, diagonal cost 4 approximates Euclidean distance.
    for (y in 0 until h) for (x in 0 until w) {
        val i = y * w + x
        var d = distance[i]
        if (x > 0) d = minOf(d, distance[i - 1] + 3)
        if (y > 0) {
            d = minOf(d, distance[i - w] + 3)
            if (x > 0) d = minOf(d, distance[i - w - 1] + 4)
            if (x + 1 < w) d = minOf(d, distance[i - w + 1] + 4)
        }
        distance[i] = d
    }
    for (y in h - 1 downTo 0) for (x in w - 1 downTo 0) {
        val i = y * w + x
        var d = distance[i]
        if (x + 1 < w) d = minOf(d, distance[i + 1] + 3)
        if (y + 1 < h) {
            d = minOf(d, distance[i + w] + 3)
            if (x > 0) d = minOf(d, distance[i + w - 1] + 4)
            if (x + 1 < w) d = minOf(d, distance[i + w + 1] + 4)
        }
        distance[i] = d
    }
    val scale = 3f * minOf(w, h)
    val atmosphere = NightPigmentField(w, h)
    fun s(lo: Float, hi: Float, v: Float) = smoothArtworkStep(lo, hi, v)
    fun g(x: Float, y: Float, cx: Float, cy: Float, rx: Float, ry: Float): Float {
        val dx = (x - cx) / rx
        val dy = (y - cy) / ry
        return exp(-1.2f * (dx * dx + dy * dy))
    }
    fun channel(v: Float) = (v * 255f).roundToInt().coerceIn(0, 255)
    for (i in pixels.indices) {
        val p = pixels[i]
        val a = (p ushr 24) / 255f
        if (a == 0f) continue
        val r = ((p ushr 16) and 255) / 255f
        val green = ((p ushr 8) and 255) / 255f
        val b = (p and 255) / 255f
        val l = 0.2126f * r + 0.7152f * green + 0.0722f * b
        val x = (i % w).toFloat() / maxOf(1, w - 1)
        val y = (i / w).toFloat() / maxOf(1, h - 1)
        val paper = s(0.37f, 0.48f, x) * (1f - s(0.80f, 0.88f, x)) *
            s(0.50f, 0.57f, y) * (1f - s(0.82f, 0.90f, y))
        val crescent = g(x, y, 0.539f, 0.17f, 0.055f, 0.075f)
        val roof = g(x, y, 0.70f, 0.31f, 0.20f, 0.14f)
        val windows = s(0.51f, 0.57f, x) * (1f - s(0.86f, 0.93f, x)) *
            s(0.29f, 0.37f, y) * (1f - s(0.50f, 0.55f, y))
        val stack = g(x, y, 0.34f, 0.72f, 0.17f, 0.18f)
        val lantern = g(x, y, 0.35f, 0.43f, 0.045f, 0.067f)
        val emission = s(0.52f, 0.86f, l) * s(0.10f, 0.32f, r - b) *
            maxOf(crescent, windows, lantern)
        val gain = 0.51f + 0.25f * paper + 0.10f * stack + 0.06f * roof +
            0.06f * windows + 0.27f * crescent + 0.28f * emission
        val sat = 0.72f - 0.29f * paper + 0.24f * emission
        val outer = s(0.13f, 0.28f, x) * (1f - 0.78f * s(0.88f, 1f, x)) *
            s(0.02f, 0.10f, y) * (1f - s(0.88f, 1f, y))
        val edge = 0.45f + 0.55f * s(0f, 0.035f, distance[i] / scale)
        val subject = maxOf(paper * 0.96f, crescent * 0.95f, windows * 0.92f, stack * 0.85f)
        val shadowAlpha = (0.35f + 0.65f * s(0.035f, 0.26f, l)) * edge
        val pen = g(x, y, 0.73f, 0.85f, 0.13f, 0.038f)
        val car = g(x, y, 0.48f, 0.50f, 0.065f, 0.035f)
        val alpha = a * maxOf(shadowAlpha, subject) * outer *
            (1f - 0.65f * pen) * (1f - 0.70f * car)
        // Near-black perimeter pigment should converge to the actual page ink,
        // not leave an opaque black silhouette. Keep interior subject contrast.
        val protect = maxOf(paper, crescent, windows, stack * 0.65f)
        val inkMix = 0.58f * (1f - s(0.04f, 0.23f, l)) * (1f - 0.75f * protect)
        val paperLift = 0.025f * paper * s(0.18f, 0.60f, l)
        val rr = ((l + (r - l) * sat) * gain * (1f - 0.05f * (1f - paper))) *
            (1f - inkMix) + (5f / 255f) * inkMix + paperLift
        val gg = ((l + (green - l) * sat) * gain) *
            (1f - inkMix) + (15f / 255f) * inkMix + paperLift
        val bb = ((l + (b - l) * sat) * gain + 0.009f * (1f - s(0.08f, 0.40f, l))) *
            (1f - inkMix) + (24f / 255f) * inkMix + paperLift
        // Build atmospheric color from the painting's own shadow pigment.
        // Exclude pages, windows and moon to avoid a broad amber halo.
        val weight = a * (1f - 0.85f * protect) * (1f - s(0.28f, 0.72f, l))
        atmosphere.add(i % w, i / w, weight, rr, gg, bb)
        pixels[i] = (channel(alpha) shl 24) or (channel(rr) shl 16) or
            (channel(gg) shl 8) or channel(bb)
    }
    atmosphere.compositeBehind(pixels)
}


/** Center the painted mass, not the arbitrary PNG rectangle. Sparse motifs
 * get a little more space than dense globes or books. Never crop the subject;
 * the optical shift is bounded by a transparent safety margin. */
private fun opticalBookplate(
    source: Bitmap, coverage: Double, centerX: Double, centerY: Double,
): Bitmap {
    val side = 640
    val fill = (0.98 - 0.10 * coverage).coerceIn(0.88, 0.98)
    val scale = side * fill / maxOf(source.width, source.height)
    val w = source.width * scale
    val h = source.height * scale
    val minMargin = side * 0.01
    val opticalX = (w / 2 - centerX * scale).coerceIn(-side * 0.035, side * 0.035)
    val opticalY = (h / 2 - centerY * scale).coerceIn(-side * 0.035, side * 0.035)
    val x = ((side - w) / 2 + opticalX).coerceIn(minMargin, side - w - minMargin)
    val y = ((side - h) / 2 + opticalY).coerceIn(minMargin, side - h - minMargin)
    val result = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG or android.graphics.Paint.ANTI_ALIAS_FLAG)
    canvas.drawBitmap(source, null,
        android.graphics.RectF(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat()), paint)
    return result
}


/** A diffuse field of actual source pigment, behind the painted silhouette.
 * Accumulate premultiplied color and coverage in a bounded grid, blur each
 * channel identically, then reconstruct straight RGB only at composition. */
private class NightPigmentField(private val width: Int, private val height: Int) {
    private val cols = minOf(64, width)
    private val rows = minOf(height, maxOf(1, (64f * height / width).roundToInt()))
    private val field = Array(4) { FloatArray(cols * rows) }

    fun add(x: Int, y: Int, weight: Float, r: Float, g: Float, b: Float) {
        val i = (y * rows / height) * cols + x * cols / width
        field[0][i] += weight
        field[1][i] += weight * r.coerceIn(0f, 1f)
        field[2][i] += weight * g.coerceIn(0f, 1f)
        field[3][i] += weight * b.coerceIn(0f, 1f)
    }

    fun compositeBehind(pixels: IntArray) {
        val blurred = Array(4) { c ->
            val input = field[c]
            for (i in input.indices) {
                val x = i % cols
                val y = i / cols
                val cw = ((x + 1) * width + cols - 1) / cols - (x * width + cols - 1) / cols
                val ch = ((y + 1) * height + rows - 1) / rows - (y * height + rows - 1) / rows
                input[i] /= maxOf(1, cw * ch).toFloat()
            }
            blur(blur(input, true), false)
        }
        fun byte(v: Float) = (v * 255f).roundToInt().coerceIn(0, 255)
        for (y in 0 until height) {
            val fy = ((y + 0.5f) * rows / height - 0.5f).coerceIn(0f, (rows - 1).toFloat())
            val y0 = fy.toInt()
            val y1 = minOf(rows - 1, y0 + 1)
            val dy = fy - y0
            for (x in 0 until width) {
                val fx = ((x + 0.5f) * cols / width - 0.5f).coerceIn(0f, (cols - 1).toFloat())
                val x0 = fx.toInt()
                val x1 = minOf(cols - 1, x0 + 1)
                val dx = fx - x0
                fun sample(c: Int): Float {
                    val v = blurred[c]
                    val t = v[y0 * cols + x0] * (1f - dx) + v[y0 * cols + x1] * dx
                    val b = v[y1 * cols + x0] * (1f - dx) + v[y1 * cols + x1] * dx
                    return t * (1f - dy) + b * dy
                }
                val coverage = sample(0)
                if (coverage < 0.003f) continue
                val i = y * width + x
                val p = pixels[i]
                val a = (p ushr 24) / 255f
                val haze = minOf(0.21f, coverage * 0.42f) * (1f - a)
                val out = a + haze
                if (out < 0.004f) continue
                fun channel(shift: Int, c: Int): Int {
                    val original = ((p ushr shift) and 255) / 255f
                    return byte((original * a + sample(c) / coverage * haze) / out)
                }
                pixels[i] = (byte(out) shl 24) or (channel(16, 1) shl 16) or
                    (channel(8, 2) shl 8) or channel(0, 3)
            }
        }
    }
    private fun blur(input: FloatArray, horizontal: Boolean): FloatArray {
        val output = FloatArray(input.size)
        val weights = intArrayOf(1, 4, 6, 4, 1)
        for (y in 0 until rows) for (x in 0 until cols) {
            var sum = 0f
            for (d in -2..2) {
                val sx = if (horizontal) x + d else x
                val sy = if (horizontal) y else y + d
                if (sx in 0 until cols && sy in 0 until rows) sum += input[sy * cols + sx] * weights[d + 2]
            }
            output[y * cols + x] = sum / 16f
        }
        return output
    }
}


/** A restrained local inverse warp of the original painting. No replacement
 * subjects: the moon occupies a smaller part of the sky, the house recedes
 * slightly, and the book moves toward the reference's lower-right focal area.
 * Smooth quartic falloff avoids seams. Sample premultiplied RGBA so transparent
 * margins never introduce dark or light fringes. */
private fun reframeMoonlitSubjects(source: IntArray, w: Int, h: Int): IntArray {
    val result = IntArray(source.size)
    val lastX = maxOf(1, w - 1)
    val lastY = maxOf(1, h - 1)
    // center x/y, falloff radii, subject scale, displacement x/y
    val regions = arrayOf(
        floatArrayOf(0.539f, 0.17f, 0.085f, 0.095f, 0.76f, 0f, 0f),
        floatArrayOf(0.72f, 0.34f, 0.23f, 0.20f, 0.94f, 0.010f, 0.008f),
        floatArrayOf(0.62f, 0.70f, 0.26f, 0.19f, 0.98f, 0.025f, 0.010f),
    )
    for (y in 0 until h) for (x in 0 until w) {
        val u = x.toFloat() / lastX
        val v = y.toFloat() / lastY
        var du = 0f
        var dv = 0f
        for (r in regions) {
            val nx = (u - r[0]) / r[2]
            val ny = (v - r[1]) / r[3]
            val xx = nx * nx
            val yy = ny * ny
            val influence = exp(-1.8f * (xx * xx + yy * yy))
            val inverse = 1f / r[4] - 1f
            du += influence * ((u - r[0]) * inverse - r[5] / r[4])
            dv += influence * ((v - r[1]) * inverse - r[6] / r[4])
        }
        val sx = (u + du) * lastX
        val sy = (v + dv) * lastY
        if (sx < 0f || sy < 0f || sx > w - 1 || sy > h - 1) continue
        val x0 = sx.toInt()
        val y0 = sy.toInt()
        val x1 = minOf(w - 1, x0 + 1)
        val y1 = minOf(h - 1, y0 + 1)
        val fx = sx - x0
        val fy = sy - y0
        val p0 = source[y0 * w + x0]
        val p1 = source[y0 * w + x1]
        val p2 = source[y1 * w + x0]
        val p3 = source[y1 * w + x1]
        val a0 = (p0 ushr 24) * (1f - fx) * (1f - fy)
        val a1 = (p1 ushr 24) * fx * (1f - fy)
        val a2 = (p2 ushr 24) * (1f - fx) * fy
        val a3 = (p3 ushr 24) * fx * fy
        val alpha = a0 + a1 + a2 + a3
        if (alpha < 0.5f) continue
        fun color(shift: Int): Int {
            val value = ((p0 ushr shift) and 255) * a0 + ((p1 ushr shift) and 255) * a1 +
                ((p2 ushr shift) and 255) * a2 + ((p3 ushr shift) and 255) * a3
            return (value / alpha).roundToInt().coerceIn(0, 255)
        }
        result[y * w + x] = (alpha.roundToInt().coerceIn(0, 255) shl 24) or
            (color(16) shl 16) or (color(8) shl 8) or color(0)
    }
    return result
}


/** The isolated decorative starbursts in the uploaded painting are much
 * larger than the reference's distant stars. Reduce only separate alpha
 * components in the upper sky, never the connected moon/cottage/foliage.
 * The resampled pigment is the original star, not a generated replacement. */
private fun refinePaintedStars(pixels: IntArray, w: Int, h: Int) {
    val topH = (h * 0.32f).toInt().coerceIn(1, h)
    val seen = BooleanArray(w * topH)
    val queue = IntArray(w * topH)
    for (start in seen.indices) {
        if (seen[start] || (pixels[start] ushr 24) <= 8) continue
        var head = 0
        var tail = 1
        queue[0] = start
        seen[start] = true
        var left = start % w
        var right = left
        var top = start / w
        var bottom = top
        while (head < tail) {
            val i = queue[head++]
            val x = i % w
            val y = i / w
            left = minOf(left, x); right = maxOf(right, x)
            top = minOf(top, y); bottom = maxOf(bottom, y)
            for (dy in -1..1) for (dx in -1..1) {
                val nx = x + dx; val ny = y + dy
                if (nx !in 0 until w || ny !in 0 until topH) continue
                val n = ny * w + nx
                if (!seen[n] && (pixels[n] ushr 24) > 8) {
                    seen[n] = true
                    queue[tail++] = n
                }
            }
        }
        val bw = right - left + 1
        val bh = bottom - top + 1
        if (bottom >= topH - 1 || left < w * 0.30f || bw > w * 0.055f ||
            bh > h * 0.085f || tail < 20 || maxOf(bw.toFloat() / w, bh.toFloat() / h) < 0.016f) continue
        // Protect the crescent even if a later source export disconnects it.
        if (left < w * 0.58f && right > w * 0.50f && top < h * 0.22f && bottom > h * 0.10f) continue
        val scale = if (maxOf(bw.toFloat() / w, bh.toFloat() / h) > 0.03f) 0.43f else 0.68f
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val sum = Array(4) { FloatArray(bw * bh) }
        for (j in 0 until tail) {
            val i = queue[j]
            val p = pixels[i]
            val nx = (cx + (i % w - cx) * scale).roundToInt().coerceIn(left, right)
            val ny = (cy + (i / w - cy) * scale).roundToInt().coerceIn(top, bottom)
            val n = (ny - top) * bw + nx - left
            val a = (p ushr 24) / 255f * scale * scale
            sum[0][n] += a
            sum[1][n] += a * ((p ushr 16) and 255)
            sum[2][n] += a * ((p ushr 8) and 255)
            sum[3][n] += a * (p and 255)
            pixels[i] = 0
        }
        for (n in sum[0].indices) {
            val a = sum[0][n]
            if (a < 0.003f) continue
            fun color(c: Int) = (sum[c][n] / a).roundToInt().coerceIn(0, 255)
            val i = (top + n / bw) * w + left + n % bw
            if ((pixels[i] ushr 24) > 8) continue
            seen[i] = true
            pixels[i] = ((a * 255f).roundToInt().coerceIn(0, 255) shl 24) or
                (color(1) shl 16) or (color(2) shl 8) or color(3)
        }
    }
}

/** Extend the genuine dark-blue sky pigment into the empty upper margins.
 * A small patch is sampled from the original sky between tree and cottage.
 * Mirror sampling avoids a rectangular seam; reject bright/non-sky pixels.
 * This lives behind the existing RGBA painting, not over the reading text. */
private class OriginalSkyPatch(source: IntArray, w: Int, h: Int) {
    private val side = 48
    private val texture = FloatArray(side * side) { i ->
        val x = ((0.49f + 0.065f * (i % side) / (side - 1)) * (w - 1)).roundToInt()
        val y = ((0.25f + 0.060f * (i / side) / (side - 1)) * (h - 1)).roundToInt()
        val p = source[y * w + x]
        val r = ((p ushr 16) and 255) / 255f
        val g = ((p ushr 8) and 255) / 255f
        val b = (p and 255) / 255f
        if ((p ushr 24) >= 200 && b > r && g > r) {
            (0.2126f * r + 0.7152f * g + 0.0722f * b - 0.12f).coerceIn(-0.045f, 0.065f)
        } else 0f
    }

    private fun mirror(value: Float): Float {
        val v = value % 2f
        return if (v <= 1f) v else 2f - v
    }

    fun extendBehind(pixels: IntArray, w: Int, h: Int) {
        fun smooth(lo: Float, hi: Float, v: Float) = smoothArtworkStep(lo, hi, v)
        fun byte(v: Float) = (v * 255f).roundToInt().coerceIn(0, 255)
        for (y in 0 until (h * 0.57f).toInt()) {
            val v = y.toFloat() / maxOf(1, h - 1)
            for (x in 0 until w) {
                val u = x.toFloat() / maxOf(1, w - 1)
                val shape = smooth(0.23f, 0.47f, u) * (1f - smooth(0.91f, 1f, u)) *
                    smooth(0f, 0.075f, v) * (1f - smooth(0.35f, 0.57f, v))
                if (shape < 0.002f) continue
                val tx = (mirror(u * 3.1f + v * 0.55f) * (side - 1)).roundToInt()
                val ty = (mirror(v * 3.7f + u * 0.35f) * (side - 1)).roundToInt()
                val grain = texture[ty * side + tx] * 0.30f
                val i = y * w + x
                val p = pixels[i]
                val a = (p ushr 24) / 255f
                val behind = 0.68f * shape * (1f - a)
                val out = a + behind
                if (out < 0.004f) continue
                fun channel(shift: Int, base: Float) = byte(
                    ((((p ushr shift) and 255) / 255f) * a + (base + grain).coerceIn(0f, 1f) * behind) / out
                )
                pixels[i] = (byte(out) shl 24) or (channel(16, 0.029f) shl 16) or
                    (channel(8, 0.064f) shl 8) or channel(0, 0.111f)
            }
        }
    }
}
