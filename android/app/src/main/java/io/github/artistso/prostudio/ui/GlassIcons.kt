package io.github.artistso.prostudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.PathParser

/**
 * InkFrame's line-art glyph set, stroke for stroke. Every glyph lives on a 24 × 24 field and is
 * drawn the way the reference draws them: a white 1.7-unit stroke with round caps and joins over a
 * soft dark halo so the line reads on any paper, `soft` primitives at half strength, `fill`
 * primitives solid. Hand-copied from the original SVG set; a glyph that exists there exists here,
 * byte for byte in its path data.
 */
object GlassIcons {
    sealed interface Prim {
        val soft: Boolean
        val fill: Boolean
        /** Multiplier on the 1.7-unit stroke, where the original sets its own width. */
        val w: Float
        /** Dash pattern in 24-field units, where the original dashes (the marching ants). */
        val dash: FloatArray?
        /** A stroke colour the original sets itself (the chroma circles). */
        val tint: Color?
    }

    data class P(val d: String, override val soft: Boolean = false, override val fill: Boolean = false, override val w: Float = 1f, override val dash: FloatArray? = null, override val tint: Color? = null) : Prim
    data class C(val cx: Float, val cy: Float, val r: Float, override val soft: Boolean = false, override val fill: Boolean = false, override val w: Float = 1f, override val dash: FloatArray? = null, override val tint: Color? = null) : Prim
    data class L(val x1: Float, val y1: Float, val x2: Float, val y2: Float, override val soft: Boolean = false, override val w: Float = 1f, override val dash: FloatArray? = null, override val tint: Color? = null) : Prim { override val fill = false }
    data class R(val x: Float, val y: Float, val width: Float, val h: Float, val rx: Float, override val soft: Boolean = false, override val fill: Boolean = false, override val w: Float = 1f, override val dash: FloatArray? = null, override val tint: Color? = null) : Prim

    private fun stroke(d: String, soft: Boolean = false, w: Float = 1f, dash: FloatArray? = null, tint: Color? = null) = P(d, soft = soft, fill = false, w = w, dash = dash, tint = tint)
    private fun fill(d: String) = P(d, fill = true)
    private fun circle(cx: Float, cy: Float, r: Float, soft: Boolean = false, fill: Boolean = false, w: Float = 1f, tint: Color? = null) = C(cx, cy, r, soft = soft, fill = fill, w = w, tint = tint)
    private fun line(x1: Float, y1: Float, x2: Float, y2: Float, soft: Boolean = false, w: Float = 1f) = L(x1, y1, x2, y2, soft = soft, w = w)
    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float, soft: Boolean = false, fill: Boolean = false, dash: FloatArray? = null) = R(x, y, w, h, rx, soft = soft, fill = fill, dash = dash)

    /** The reference glyphs, keyed by their names in the original set, plus the three the app adds. */
    val GLYPHS: Map<String, List<Prim>> = buildMap {
        put("tools", listOf(stroke("M4 19l7-7"), stroke("M9 6.5l5 5"), stroke("M14.5 4.5c1.6-.4 3.6.2 4.4 1.6.8 1.5.3 3.4-1 4.4l-3-3z", soft = true)))
        put("color", listOf(stroke("M12 3a9 9 0 100 18c1.4 0 2-.9 2-1.8 0-1.3-1.1-1.5-1.1-2.6 0-.8.7-1.4 1.6-1.4H16a5 5 0 005-5C21 6 17 3 12 3z"), circle(7.5f, 11.5f, 1.2f, fill = true), circle(11f, 7.8f, 1.2f, fill = true), circle(15.5f, 9.5f, 1.2f, fill = true)))
        put("actions", listOf(stroke("M12 3v3"), stroke("M12 18v3"), stroke("M3 12h3"), stroke("M18 12h3"), circle(12f, 12f, 3.2f)))
        put("frames", listOf(rect(3.5f, 6f, 17f, 12f, 2f), line(8f, 6f, 8f, 18f), line(16f, 6f, 16f, 18f), line(3.5f, 12f, 20.5f, 12f, soft = true)))
        put("themes", listOf(circle(12f, 12f, 8.5f), fill("M12 3.5a8.5 8.5 0 010 17z"), circle(12f, 12f, 2.4f)))
        put("studio", listOf(circle(12f, 12f, 8.5f), circle(12f, 12f, 3f, soft = true)))
        put("gallery", listOf(rect(3.5f, 5.5f, 11f, 9f, 1.6f), stroke("M9.5 9.5h11v9h-11z", soft = true)))
        put("help", listOf(circle(12f, 12f, 8.5f), stroke("M9.5 9.2a2.8 2.8 0 115 1.8c-1.5 1-2.4 1.7-2.4 3"), circle(12f, 17.2f, 1f, fill = true)))
        put("pencil", listOf(stroke("M4 20l1-4L16 5l3 3L8 19z"), stroke("M14 7l3 3")))
        put("ink", listOf(stroke("M6 18c0-4 4-7 6-13 2 6 6 9 6 13a6 6 0 01-12 0z"), stroke("M9.5 17.5a2.5 2.5 0 005 0", soft = true)))
        put("marker", listOf(stroke("M5 19l2-5 8-8 3 3-8 8z"), stroke("M13 6l3 3"), line(5f, 19f, 9f, 19f, soft = true)))
        put("glow", listOf(circle(12f, 12f, 3.4f, fill = true), stroke("M12 3v3M12 18v3M3 12h3M18 12h3M6 6l2 2M16 16l2 2M18 6l-2 2", soft = true)))
        put("frost", listOf(circle(12f, 12f, 7.5f, soft = true), stroke("M12 4v16M5.5 8l13 8M18.5 8l-13 8"), stroke("M9 6.5L12 9l3-2.5M9 17.5L12 15l3 2.5", soft = true)))
        put("smudge", listOf(stroke("M5 15c2.5 3 8.5 3 11 0 2-2.4.8-5.6-2.3-6.1-3-.5-5.7 1.5-5.2 3.6.4 1.7 2.9 2 4.2.8"), stroke("M7 18c2.5 1.8 7.5 2 10.5-.3", soft = true), circle(7f, 14f, 1.2f, fill = true)))
        put("neon", listOf(circle(12f, 12f, 7f, soft = true, w = 3.2f / 1.7f), circle(12f, 12f, 2.2f, fill = true)))
        put("star", listOf(stroke("M12 3v7M12 14v7M3 12h7M14 12h7"), circle(12f, 12f, 1.8f, fill = true)))
        put("conste", listOf(circle(5f, 18f, 1.6f, fill = true), circle(18f, 16f, 1.6f, fill = true), circle(12f, 5f, 1.6f, fill = true), stroke("M5 18l7-13 6 11z", soft = true)))
        put("eraser", listOf(stroke("M4 15l7-7 6 6-4 4H7z"), line(8f, 19f, 19f, 19f)))
        put("undo", listOf(stroke("M9 7L5 11l4 4"), stroke("M5 11h8a5 5 0 015 5v1", soft = true), stroke("M5 11h8a5 5 0 015 5")))
        put("redo", listOf(stroke("M15 7l4 4-4 4"), stroke("M19 11h-8a5 5 0 00-5 5")))
        put("clear", listOf(stroke("M7 7l10 10"), stroke("M17 7L7 17")))
        put("onion", listOf(circle(10f, 12f, 6f), stroke("M14 6.5a6 6 0 010 11", soft = true)))
        put("export", listOf(stroke("M12 4v10"), stroke("M8 10l4 4 4-4"), stroke("M5 18h14", soft = true)))
        put("gif", listOf(rect(3.5f, 5.5f, 17f, 13f, 1.6f), stroke("M3.5 8.5h17M3.5 15.5h17", soft = true), stroke("M6 7v-1M9 7v-1M12 7v-1M15 7v-1M18 7v-1M6 18v-1M9 18v-1M12 18v-1M15 18v-1M18 18v-1", soft = true), stroke("M9 10.5v3h1.5v-1H10M13 10.5v3M15.5 10.5v3M15.5 12h1.5", w = 1.4f / 1.7f)))
        put("video", listOf(rect(3f, 7f, 13f, 10f, 1.6f), stroke("M16 10l5-2v8l-5-2z"), fill("M7 11l4 2-4 2z")))
        put("layers", listOf(stroke("M12 4l8 4-8 4-8-4z"), stroke("M4 12l8 4 8-4", soft = true), stroke("M4 16l8 4 8-4", soft = true)))
        put("layerAdd", listOf(stroke("M4 8l6-3 6 3-6 3z"), stroke("M4 14l6 3 6-3", soft = true), stroke("M18 14v6M15 17h6", w = 1.6f / 1.7f)))
        put("paper", listOf(stroke("M6 4h9l3 3v13H6z"), stroke("M15 4v3h3", soft = true), circle(12f, 13f, 2.4f, fill = true)))
        put("fx", listOf(fill("M12 4l1.7 4 4.3.6-3.2 3 .8 4.4L12 14l-3.6 2 .8-4.4-3.2-3 4.3-.6z")))
        put("bloom", listOf(circle(12f, 12f, 4f, fill = true), circle(12f, 12f, 8f, soft = true, w = 1.4f / 1.7f), circle(12f, 12f, 11f, soft = true, w = 0.8f / 1.7f)))
        put("sparkle", listOf(fill("M6 6l1 3 3 1-3 1-1 3-1-3-3-1 3-1z"), fill("M17 12l1 2 2 1-2 1-1 2-1-2-2-1 2-1z"), fill("M12 18l.7 1.4 1.4.6-1.4.6-.7 1.4-.7-1.4-1.4-.6 1.4-.6z")))
        put("chroma", listOf(circle(10f, 12f, 5f, tint = Color(0xFFFF6666)), circle(12f, 12f, 5f, tint = Color(0xFF66FF66)), circle(14f, 12f, 5f, tint = Color(0xFF6666FF))))
        put("sym", listOf(circle(12f, 12f, 1.6f, fill = true), stroke("M12 3v6M12 21v-6M3 12h6M21 12h-6M6 6l3 3M18 18l-3-3M18 6l-3 3M6 18l3-3", soft = true)))
        put("select", listOf(rect(4f, 5f, 16f, 14f, 1.6f, dash = floatArrayOf(3f, 2f)), circle(4f, 5f, 1.2f, fill = true), circle(20f, 5f, 1.2f, fill = true), circle(4f, 19f, 1.2f, fill = true), circle(20f, 19f, 1.2f, fill = true)))
        put("lasso", listOf(stroke("M6 12c0-4 3-6 6-6s6 2 6 6-3 6-6 6-6-2-6-6z"), stroke("M12 18c-1 2-3 3-4 3", soft = true), circle(8f, 21f, 1.4f, fill = true)))
        put("fill", listOf(stroke("M4 13l7-7 7 7-7 7z"), fill("M18 15c1 1.5 2 3 2 4a2 2 0 11-4 0c0-1 1-2.5 2-4z"), stroke("M4 13h14", soft = true)))
        put("dropper", listOf(stroke("M5 19l3-3 6-6 3 3-6 6-3 3z"), fill("M15 4l3 3-2 2-3-3z"), circle(5f, 20f, 1f, fill = true)))
        put("compare", listOf(rect(4f, 5f, 16f, 14f, 1.6f), stroke("M12 3v18", soft = true), stroke("M7 12h2M15 12h2", w = 1.4f / 1.7f)))
        put("imp", listOf(rect(3.5f, 6.5f, 17f, 12f, 1.8f), stroke("M5.5 15.5l4-4 3 3 2-2 4 4", soft = true), circle(15.5f, 10f, 1.5f, fill = true), stroke("M12 10v-3M9 4l3 3 3-3", w = 1.6f / 1.7f)))
        put("layerDup", listOf(stroke("M4 7l6-3 6 3-6 3z"), stroke("M8 14l6-3 6 3-6 3z", soft = true)))
        put("layerDel", listOf(stroke("M4 8l6-3 6 3-6 3z"), stroke("M4 14l6 3 6-3", soft = true), stroke("M14 16l6 6M20 16l-6 6", w = 1.6f / 1.7f)))
        put("layerUp", listOf(stroke("M4 12l6-3 6 3-6 3z", soft = true), stroke("M12 18V6M8 10l4-4 4 4")))
        put("layerDn", listOf(stroke("M4 12l6-3 6 3-6 3z", soft = true), stroke("M12 6v12M8 14l4 4 4-4")))
        put("eyeOn", listOf(stroke("M2.5 12S6 5 12 5s9.5 7 9.5 7-3.5 7-9.5 7S2.5 12 2.5 12z"), circle(12f, 12f, 2.6f, fill = true)))
        put("eyeOff", listOf(stroke("M2.5 12S6 5 12 5s9.5 7 9.5 7-3.5 7-9.5 7S2.5 12 2.5 12z"), stroke("M4 4l16 16", w = 1.8f / 1.7f)))
        put("blend", listOf(circle(9f, 10f, 5f), circle(15f, 14f, 5f, soft = true)))
        put("stylus", listOf(stroke("M5 19l1.5-4.5L16 5l3 3-9.5 9.5z"), stroke("M14 7l3 3"), stroke("M5 19l3.5-1", soft = true)))
        put("palm", listOf(stroke("M8 13V6.5a1.3 1.3 0 012.6 0V11m0-.5V5.2a1.3 1.3 0 012.6 0V11m0-.3V6.4a1.3 1.3 0 012.6 0V13c0 4-2 6-5 6s-4.4-1.6-5.6-3.6L6 13.2a1.3 1.3 0 012.2-1.3z")))
        put("line", listOf(stroke("M4 16C8 16 9 7 13 7s4 6 7 6"), circle(4f, 16f, 1.4f, fill = true), circle(20f, 13f, 1.4f, fill = true)))
        put("stream", listOf(stroke("M4 9c3 0 4 2 8 2s5-2 8-2"), stroke("M4 15c3 0 4 2 8 2s5-2 8-2", soft = true)))
        put("qshape", listOf(circle(9f, 9f, 4.5f), stroke("M13 15l5 5M14 20h6v-6", soft = true)))
        put("full", listOf(stroke("M4 9V4h5"), stroke("M20 9V4h-5"), stroke("M4 15v5h5"), stroke("M20 15v5h-5")))
        put("expand", listOf(stroke("M9 5H5v4"), stroke("M15 5h4v4"), stroke("M9 19H5v-4"), stroke("M15 19h4v-4"), circle(12f, 12f, 2f, soft = true)))
        put("play", listOf(fill("M8 5l11 7-11 7z")))
        put("pause", listOf(rect(7f, 5f, 3.4f, 14f, 1f, fill = true), rect(13.6f, 5f, 3.4f, 14f, 1f, fill = true)))
        put("add", listOf(stroke("M12 5v14"), stroke("M5 12h14")))
        put("dup", listOf(rect(4.5f, 4.5f, 10f, 10f, 2f), rect(9.5f, 9.5f, 10f, 10f, 2f, soft = true)))
        put("del", listOf(stroke("M6 8h12"), stroke("M9 8V6h6v2"), stroke("M7 8l1 11h8l1-11")))
        put("dissolve", listOf(circle(9f, 12f, 6f), circle(15f, 12f, 6f, soft = true)))
        put("loop", listOf(stroke("M7 8h7a4 4 0 010 8H6"), stroke("M9 5L6 8l3 3")))
        put("mblur", listOf(circle(15f, 12f, 4f), stroke("M3 12h4M5 9h3M5 15h3", soft = true)))
        put("hold", listOf(stroke("M9 6v12M15 6v12"), stroke("M5 12h2M17 12h2", soft = true)))
        put("list", listOf(circle(6f, 12f, 1.6f, fill = true), circle(12f, 12f, 1.6f, fill = true), circle(18f, 12f, 1.6f, fill = true)))
        put("custom", listOf(circle(12f, 12f, 8f), stroke("M12 8v8M8 12h8")))
        // the app's own, drawn in the same hand: a lane slider (adjustments), step chevrons
        put("tune", listOf(stroke("M5 8h14M5 16h14"), circle(9f, 8f, 1.8f, fill = true), circle(15f, 16f, 1.8f, fill = true)))
        put("prev", listOf(stroke("M15 6l-6 6 6 6")))
        put("next", listOf(stroke("M9 6l6 6-6 6")))
    }
}

/** Parsed path cache — path data never changes, so it parses once per process. */
private val pathCache = mutableMapOf<String, Path>()
private fun cachedPath(d: String): Path = pathCache.getOrPut(d) {
    PathParser().parsePathString(d).toPath()
}

/**
 * InkFrame's glyph, drawn on the spot: a soft dark halo pass under a white pass, exactly as the
 * reference's drop-shadowed strokes read on paper. [name] is a key in [GlassIcons.GLYPHS].
 */
@Composable
fun GlassGlyph(name: String?, size: Dp, modifier: Modifier = Modifier, color: Color = Color.White, alpha: Float = 1f) {
    val primitives = remember(name) { name?.let { GlassIcons.GLYPHS[it] } } ?: return
    Canvas(modifier.requiredSize(size)) {
        val u = size.toPx() / 24f
        withTransform({ translate(left = 0f, top = 0.6f * u) }) { drawGlyphPass(primitives, u, color, alpha, halo = true) }
        drawGlyphPass(primitives, u, color, alpha, halo = false)
    }
}

private fun DrawScope.drawGlyphPass(primitives: List<GlassIcons.Prim>, u: Float, color: Color, alpha: Float, halo: Boolean) {
    val strokeBase = 1.7f * u
    for (prim in primitives) {
        if (halo && prim.tint != null) continue
        val a = alpha * (if (prim.soft) 0.5f else 1f)
        val tint = prim.tint
        val ink = when {
            halo && prim.fill -> Color.Black.copy(alpha = 0.45f * a)
            halo -> Color.Black.copy(alpha = 0.55f * a)
            tint != null -> tint
            else -> color.copy(alpha = a)
        }
        val w = strokeBase * prim.w + if (halo) 0.9f * u else 0f
        val stroke = Stroke(
            width = w,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathEffect = prim.dash?.let { PathEffect.dashPathEffect(FloatArray(it.size) { i -> it[i] * u }) },
        )
        when (prim) {
            is GlassIcons.P -> {
                val path = cachedPath(prim.d)
                if (prim.fill) drawPath(path, ink, alpha = 1f) else drawPath(path, ink, style = stroke)
            }
            is GlassIcons.C -> {
                val c = Offset(prim.cx * u, prim.cy * u)
                if (prim.fill) drawCircle(ink, prim.r * u, c) else drawCircle(ink, prim.r * u, c, style = stroke)
            }
            is GlassIcons.L -> {
                drawLine(ink, Offset(prim.x1 * u, prim.y1 * u), Offset(prim.x2 * u, prim.y2 * u), strokeWidth = w, cap = androidx.compose.ui.graphics.StrokeCap.Round, pathEffect = stroke.pathEffect)
            }
            is GlassIcons.R -> {
                val tl = Offset(prim.x * u, prim.y * u)
                val sz = Size(prim.width * u, prim.h * u)
                val corner = CornerRadius(prim.rx * u)
                if (prim.fill) {
                    drawRoundRect(ink, tl, sz, corner)
                } else {
                    drawRoundRect(ink, tl, sz, corner, style = stroke)
                }
            }
        }
    }
}
