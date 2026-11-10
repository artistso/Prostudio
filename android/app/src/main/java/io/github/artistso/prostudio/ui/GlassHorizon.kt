package io.github.artistso.prostudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.random.Random

/**
 * The Glass Horizon's world, drawn over the engine's deep plum surround: the bright rose horizon at
 * the top of the sky, conic light rays from it, a vignette pulling the edges down into shadow, and
 * fine grain so the world never reads as a flat fill. The artwork underneath stays alive — every
 * layer fades out before the middle of the stage — and the layer never takes a touch.
 */
@Composable
fun HorizonAtmosphere(modifier: Modifier = Modifier) {
    val specks = remember { scatterGrain() }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val topCentre = Offset(w / 2f, -h * 0.12f)
        // 1. the horizon: a radial bloom from just above the glass, rose easing through magenta to
        //    the deep plum of the far edge, transparent well before the middle of the stage
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Glass.Highlight.copy(alpha = 0.95f),
                    0.07f to Glass.RoseQuartz.copy(alpha = 0.92f),
                    0.17f to Glass.RoseDim.copy(alpha = 0.78f),
                    0.30f to Glass.HorizonDeep.copy(alpha = 0.58f),
                    0.46f to Glass.ShadowPlum.copy(alpha = 0.30f),
                    0.62f to Glass.Deep.copy(alpha = 0.0f),
                ),
                center = topCentre,
                radius = maxOf(w, h) * 1.02f,
            ),
        )
        // 2. light rays: conic sectors fanning from the horizon, soft rose at low alpha, never a
        //    flat wash; the middle of the stage stays clear for the work
        drawRect(
            brush = Brush.sweepGradient(
                colorStops = RAYS,
                center = topCentre,
            ),
        )
        // 3. the vignette: transparent through the middle of the field, deep plum-black at the edges
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.55f to Color.Transparent,
                    0.82f to Glass.VignetteEdge.copy(alpha = 0.34f),
                    1.00f to Glass.VignetteEdge,
                ),
                center = Offset(w / 2f, h * 0.44f),
                radius = hypot(w, h) * 0.60f,
            ),
        )
        // 4. grain: fine rose-white and plum-black flecks, fixed per composition, a whisper at a time
        for (s in specks) {
            drawCircle(
                color = if (s.light) Glass.Paper.copy(alpha = s.alpha) else Glass.Deep.copy(alpha = s.alpha * 1.6f),
                radius = s.radius * minOf(w, h),
                center = Offset(s.x * w, s.y * h),
            )
        }
    }
}

/**
 * The frame glass the stage sits in: a rounded ring of frosted rose at the canvas's edge, its strong
 * edge upper-left where the horizon light comes from, a bright inner rim, and a deep exterior
 * shadow falling away into the world. Decorative only; the engine owns the drawing surface inside it.
 */
@Composable
fun FrameGlass(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val inset = 5.dp.toPx()
        val ring = 10.dp.toPx()
        val corner = CornerRadius(30.dp.toPx())
        val rect = Size(size.width - inset * 2f, size.height - inset * 2f)
        // the long exterior shadow, dropped down and away from the horizon light
        drawRoundRect(
            color = Glass.FrameShadow.copy(alpha = 0.55f),
            topLeft = Offset(inset, inset + 6.dp.toPx()),
            size = rect,
            cornerRadius = corner,
            style = Stroke(ring * 2f),
        )
        // the glass body: strong where the light lands, thinning as it falls away
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Glass.GradientStrong, Glass.Strong, Glass.GradientWeak, Glass.GradientWeak),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            topLeft = Offset(inset, inset),
            size = rect,
            cornerRadius = corner,
            style = Stroke(ring),
        )
        // the bright inner rim: most of what the horizon catches
        drawRoundRect(
            color = Glass.RimInner,
            topLeft = Offset(inset + ring / 2f, inset + ring / 2f),
            size = Size(rect.width - ring, rect.height - ring),
            cornerRadius = CornerRadius(30.dp.toPx() - ring / 2f),
            style = Stroke(1.2.dp.toPx()),
        )
    }
}

/** One grain speck: position and radius as fractions of the stage, plus an alpha. */
private data class Speck(val x: Float, val y: Float, val radius: Float, val alpha: Float, val light: Boolean)

/** A fixed constellation of flecks, so the grain does not shimmer between recompositions. */
private fun scatterGrain(): List<Speck> {
    val random = Random(GRAIN_SEED)
    return List(760) {
        Speck(
            x = random.nextFloat(),
            y = random.nextFloat(),
            radius = 0.0009f + random.nextFloat() * 0.0011f,
            alpha = 0.025f + random.nextFloat() * 0.045f,
            light = random.nextInt(4) != 0,
        )
    }
}

private const val GRAIN_SEED = 0x2545F491

/** The conic rays' colour stops: soft rose spokes alternating with open sky, alpha kept low. */
private val RAYS: Array<Pair<Float, Color>> = run {
    val rose = Color(0xFFF7CAC9)
    val stops = mutableListOf<Pair<Float, Color>>()
    // spoke positions across the full sweep, and how lit each one is (0: open sky)
    val angles = floatArrayOf(0.00f, 0.045f, 0.075f, 0.120f, 0.150f, 0.210f, 0.235f, 0.290f, 0.330f, 0.375f, 0.405f, 0.470f, 0.505f, 0.560f, 0.600f, 0.660f, 0.700f, 0.760f, 0.800f, 0.870f, 0.910f, 0.970f, 1.000f)
    val strength = floatArrayOf(0.00f, 0.10f, 0.00f, 0.00f, 0.13f, 0.00f, 0.00f, 0.09f, 0.00f, 0.00f, 0.11f, 0.00f, 0.00f, 0.12f, 0.00f, 0.00f, 0.08f, 0.00f, 0.00f, 0.10f, 0.00f, 0.09f, 0.00f)
    for (i in angles.indices) stops += angles[i] to rose.copy(alpha = strength[i])
    stops.toTypedArray()
}

/**
 * The wordmark, out in the world rather than behind a bar: the inkFrame title spec exactly —
 * display serif bold uppercase, .22em tracking, white through the rose 55% down to the accent,
 * a faint luminous shadow; THE GLASS HORIZON tracked .28em beneath, .86 opacity rose-dim.
 */
@androidx.compose.runtime.Composable
fun GlassWordmark(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        androidx.compose.material3.Text(
            "PROSTUDIO",
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 4.4.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                brush = Glass.TitleGradient,
                shadow = androidx.compose.ui.graphics.Shadow(Color(0x40FFFFFF), Offset(0f, 2f), 16f),
            ),
            maxLines = 1,
            softWrap = false,
        )
        androidx.compose.material3.Text(
            "THE GLASS HORIZON",
            fontSize = 10.sp,
            letterSpacing = 2.8.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Glass.RoseDim.copy(alpha = 0.86f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/**
 * The live glass lens: the pen's cursor, as the reference draws it — a soft field of glass at the
 * pen tip with a bright rim, a fine white nib mark at its heart and a plum shadow beneath. It is
 * drawn over the engine's paper, never into the artwork, and never takes a touch. Shown whenever
 * the pen is over the canvas (hover or contact) while the brush cursor is on.
 */
@androidx.compose.runtime.Composable
fun GlassLens(vm: io.github.artistso.prostudio.canvas.CanvasViewModel, modifier: Modifier = Modifier) {
    val at = vm.cursorAt
    if (at == null || !vm.brushCursorOn) return
    Canvas(modifier.fillMaxSize()) {
        val radius = 50.dp.toPx()
        val tip = at
        // the plum drop shadow beneath the ring
        drawCircle(Color(0x802A001A), radius, Offset(tip.x, tip.y + 2.dp.toPx()))
        // the glass field and its bright rim
        drawCircle(Color(0x0FF7CAC9), radius, tip)
        drawCircle(Glass.RimInner.copy(alpha = 0.9f), radius, tip, style = Stroke(1.2.dp.toPx()))
        // the nib: a fine white mark at the pen's heart
        drawLine(
            Color(0xD9FFF0F3),
            Offset(tip.x - 3.5.dp.toPx(), tip.y + 3.5.dp.toPx()),
            Offset(tip.x + 3.5.dp.toPx(), tip.y - 3.5.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(Color(0xD9FFF0F3), 1.1.dp.toPx(), tip)
    }
}
