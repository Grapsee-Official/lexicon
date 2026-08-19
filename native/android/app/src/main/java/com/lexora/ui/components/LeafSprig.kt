package com.lexora.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * 3-leaf botanical sprig — direct vector translation of the SVG that sits
 * next to the "Words" headline in the web Hero. Brand green #677554.
 *
 * The web SVG is:
 *   - main vertical stem
 *   - two side leaves (ellipse, rotated ±35°, opacity 0.85)
 *   - one top leaf (smaller ellipse)
 *   - thin veins through each leaf
 *
 * All strokes use a very thin line (0.8 in web units) — we approximate that
 * with a 0.8-density-pixel stroke.
 */
@Composable
fun LeafSprig(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF677554),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // The original SVG viewBox is 40x40 — scale factor:
        val s = minOf(w, h) / 40f
        val cx = w / 2f

        // Stroke widths (thin to match web 0.8 / 0.4)
        val stemStroke = 0.8f * s
        val veinStroke = 0.4f * s

        // === Main stem ===
        drawLine(
            color = color,
            start = Offset(cx, 36f * s),
            end   = Offset(cx, 4f * s),
            strokeWidth = stemStroke,
            cap = StrokeCap.Round,
        )

        // === Left leaf (rotated -35°) ===
        rotate(degrees = -35f, pivot = Offset(11f * s, 22f * s)) {
            drawLeaf(
                center = Offset(11f * s, 22f * s),
                rx = 8f * s,
                ry = 3.2f * s,
                color = color.copy(alpha = 0.85f),
            )
        }
        // left vein
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(5.5f * s, 24.5f * s),
            end   = Offset(16.5f * s, 19.5f * s),
            strokeWidth = veinStroke,
            cap = StrokeCap.Round,
        )

        // === Right leaf (rotated +35°) ===
        rotate(degrees = 35f, pivot = Offset(29f * s, 15f * s)) {
            drawLeaf(
                center = Offset(29f * s, 15f * s),
                rx = 8f * s,
                ry = 3.2f * s,
                color = color.copy(alpha = 0.85f),
            )
        }
        // right vein
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(23.5f * s, 12.5f * s),
            end   = Offset(34.5f * s, 17.5f * s),
            strokeWidth = veinStroke,
            cap = StrokeCap.Round,
        )

        // === Top leaf (smaller ellipse) ===
        drawLeaf(
            center = Offset(cx, 7f * s),
            rx = 4.5f * s,
            ry = 2.8f * s,
            color = color.copy(alpha = 0.9f),
        )
        // top vein
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(cx, 5f * s),
            end   = Offset(cx, 9f * s),
            strokeWidth = veinStroke,
            cap = StrokeCap.Round,
        )
    }
}

/** Draw a filled ellipse leaf (around a centre, with rx/ry half-extents). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    center: Offset,
    rx: Float,
    ry: Float,
    color: Color,
) {
    val left = center.x - rx
    val top = center.y - ry
    val right = center.x + rx
    val bottom = center.y + ry
    drawOval(
        color = color,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
    )
}
