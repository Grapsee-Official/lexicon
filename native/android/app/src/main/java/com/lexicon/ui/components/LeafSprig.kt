package com.lexicon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.lexicon.ui.theme.OldWoodGold

/**
 * Diagonal 5-leaf botanical olive/laurel sprig matching the reference PNGs
 * (used next to "Words" in the Hero and next to "WORD OF THE DAY").
 */
@Composable
fun LeafSprig(
    modifier: Modifier = Modifier,
    color: Color = OldWoodGold,
) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height) / 32f

        // Curved diagonal stem from bottom-left (5, 27) to top-right (25, 6)
        val stem = Path().apply {
            moveTo(5f * s, 27f * s)
            quadraticBezierTo(14f * s, 17f * s, 24f * s, 7f * s)
        }
        drawPath(
            path = stem,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.35f * s,
                cap = StrokeCap.Round,
            ),
        )

        // Upper-left pair of pointed botanical leaves
        drawPointedLeaf(cx = 11.5f * s, cy = 16.5f * s, len = 8.5f * s, wid = 3.6f * s, angle = -72f, color = color)
        drawPointedLeaf(cx = 17.5f * s, cy = 10.5f * s, len = 8.0f * s, wid = 3.4f * s, angle = -66f, color = color)

        // Lower-right pair of pointed botanical leaves
        drawPointedLeaf(cx = 15.5f * s, cy = 20.5f * s, len = 8.5f * s, wid = 3.6f * s, angle = 12f, color = color)
        drawPointedLeaf(cx = 21.0f * s, cy = 14.8f * s, len = 7.8f * s, wid = 3.3f * s, angle = 8f, color = color)

        // Crown tip leaf
        drawPointedLeaf(cx = 25.0f * s, cy = 6.2f * s, len = 7.2f * s, wid = 3.1f * s, angle = -38f, color = color)
    }
}

private fun DrawScope.drawPointedLeaf(
    cx: Float,
    cy: Float,
    len: Float,
    wid: Float,
    angle: Float,
    color: Color,
) {
    rotate(degrees = angle, pivot = Offset(cx, cy)) {
        val p = Path().apply {
            moveTo(cx - len * 0.5f, cy)
            quadraticBezierTo(cx, cy - wid, cx + len * 0.5f, cy)
            quadraticBezierTo(cx, cy + wid, cx - len * 0.5f, cy)
            close()
        }
        drawPath(path = p, color = color.copy(alpha = 0.92f))
    }
}
