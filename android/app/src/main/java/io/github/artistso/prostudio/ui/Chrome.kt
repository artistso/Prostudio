package io.github.artistso.prostudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.foundation.layout.wrapContentHeight
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.canvas.CanvasViewModel

private val SliderLeft = 5.dp
private val SizeTrackTop = 6.dp
private val OpacityTrackTop = 248.dp

/**
 * The size and opacity capsule floating in the world (the right one, [right]-handed): brush
 * size, the modify button, opacity, its tracks [trackHeight] long. On the right everything is
 * mirrored but the glyphs.
 */
@Composable
fun SideBar(vm: CanvasViewModel, modifier: Modifier = Modifier, right: Boolean = false, trackHeight: Dp = Ps.SliderHeight, shown: Float = 1f) {
    // a control's left edge, mirrored on the right
    fun x(left: Dp, width: Dp) = if (right) Ps.SideBarWidth - left - width else left
    // shorter tracks bring what is under each up with them
    val shorter = Ps.SliderHeight - trackHeight
    val opacityTop = OpacityTrackTop - shorter
    // its glass slides out with it as the interface hides
    val aside = with(androidx.compose.ui.platform.LocalDensity.current) { Ps.SideBarWidth.toPx() } * (1f - shown) * if (right) 1f else -1f
    val moved = Offset(aside, 0f)
    val look = rememberGooglyDrift(4200)
    val body = BarGlass(shown, moved, pastLeft = if (right) 0.dp else Ps.SideBarCorner, pastRight = if (right) Ps.SideBarCorner else 0.dp)
    val track = BarGlass(shown, moved, window = true)
    Box(
        Modifier.then(rememberFrost(Ps.SideBarCorner, body)).then(modifier).size(Ps.SideBarWidth, opacityTop + trackHeight + 14.dp)
            .drawBehind {
                // translucent, with windows where the slider tracks are: they show what is behind
                val r = Ps.SideBarCorner.toPx()
                val track = Size(Ps.SliderWidth.toPx(), trackHeight.toPx())
                val trackR = CornerRadius(Ps.SliderCorner.toPx())
                val body = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r), CornerRadius(r), CornerRadius(r), CornerRadius(r)))
                    for (top in listOf(SizeTrackTop, opacityTop)) {
                        val o = Offset(x(SliderLeft, Ps.SliderWidth).toPx(), top.toPx())
                        addRoundRect(RoundRect(o.x, o.y, o.x + track.width, o.y + track.height, trackR))
                    }
                }
                drawPath(body, Glass.Gradient)
                // the glass's rim: the horizon catching the edge, tracks rimmed too
                drawPath(body, Glass.RimInner.copy(alpha = 0.4f), style = Stroke(1.dp.toPx()))
            },
    ) {
        BrushSlider(vm.size, vm::changeSize, Modifier.offset(x(SliderLeft, Ps.SliderWidth), SizeTrackTop).then(rememberFrost(Ps.SliderCorner, track)), panel(vm, CanvasViewModel.SliderPanel.SIZE), vm.sizeNotches, trackHeight, look)
        // the modify button: the next touch on the canvas picks a colour
        Box(
            Modifier.offset(x(5.dp, 40.dp), 191.dp - shorter).size(40.dp, 41.dp).pointerInput(Unit) {
                // held, it changes what a canvas touch does; tapped, it does what Tap □ is set to
                awaitEachGesture {
                    awaitFirstDown().consume()
                    vm.modifyPressed()
                    val up = waitForUpOrCancellation()
                    up?.consume()
                    vm.modifyReleased(cancelled = up == null)
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(Ps.ModifySize).drawBehind {
                    val w = Ps.ModifyStroke.toPx()
                    drawRoundRect(
                        if (vm.pickNext) Ps.Accent else Ps.SideGlyph,
                        topLeft = Offset(w / 2f, w / 2f),
                        size = Size(size.width - w, size.height - w),
                        cornerRadius = CornerRadius(Ps.ModifyCorner.toPx() - w / 2f),
                        style = Stroke(w),
                    )
                },
            )
        }
        BrushSlider(vm.opacity, vm::changeOpacity, Modifier.offset(x(SliderLeft, Ps.SliderWidth), opacityTop).then(rememberFrost(Ps.SliderCorner, track)), panel(vm, CanvasViewModel.SliderPanel.OPACITY), vm.opacityNotches, trackHeight, look)
        // beside the slider it belongs to, a little above it, outside the bar
        vm.sliderPanel?.let { which ->
            val top = if (which == CanvasViewModel.SliderPanel.SIZE) SizeTrackTop else opacityTop
            SliderPanel(vm, which, Modifier.offset(x(SliderLeft + Ps.SliderWidth + SliderPanelGap, SliderPanelSide), top - SliderPanelRise))
        }
    }
}

/** What a slider's panel does: shown while it is dragged ([held] false), kept up when it is held still. */
class SliderPanelHooks(val show: (held: Boolean) -> Unit, val hide: () -> Unit)

private fun panel(vm: CanvasViewModel, which: CanvasViewModel.SliderPanel) = SliderPanelHooks(
    show = { held ->
        vm.sliderPanel = which
        vm.sliderPanelKept = held
    },
    hide = { if (!vm.sliderPanelKept) vm.sliderPanel = null },
)

private val SliderPanelSide = 190.dp
private val SliderPanelGap = 8.dp
private val SliderPanelRise = 10.dp
// dark glass over the canvas (what is behind it blurred), or light
private val SliderPanelFill get() = if (Ps.light) Color(0xFFE7E7E7) else Color(0xC93E3E3E)
private val SliderPanelInk get() = if (Ps.light) Color(0xFF332F3B) else Color(0xFFF0F0F0)
// the dot is drawn in the interface's ink, not the brush's colour
private val SliderPanelDot get() = if (Ps.light) Color.Black else Color.White

/**
 * A slider's panel: its name and value ("Size 8%"), a button saving the value as a notch on the
 * slider ("+"; "-" takes away the notch it is on), and in the middle the brush's stamp as it would
 * paint now (soft where it is soft), its size on the screen (and at its opacity).
 */
@Composable
private fun SliderPanel(vm: CanvasViewModel, which: CanvasViewModel.SliderPanel, modifier: Modifier) {
    val size = which == CanvasViewModel.SliderPanel.SIZE
    val value = if (size) vm.size else vm.opacity
    val diameter = androidx.compose.runtime.remember(vm.size) { vm.brushScreenDiameter() }
    // the brush's stamp as it paints (soft where it is soft), as many pixels across as it shows
    val density = androidx.compose.ui.platform.LocalDensity.current
    val radius = with(density) { (diameter / 2f).coerceIn(1.5f, SliderPanelSide.toPx() / 2f - 8.dp.toPx()) }
    val side = (radius * 2f).roundToInt().coerceAtLeast(2)
    val stamp = androidx.compose.runtime.remember(vm.size, side) { vm.brushStamp(side) }
    // its own size, past the bar's edge (the bar's width would otherwise squeeze it)
    Box(
        modifier.wrapContentSize(Alignment.TopStart, unbounded = true).size(SliderPanelSide)
            .then(if (Ps.light) Modifier else rememberFrost(22.dp))
            .clip(RoundedCornerShape(22.dp)).background(SliderPanelFill)
            // a hairline rim, as measured (about 25 over the glass)
            .border(1.dp, Ps.GlassMenuEdge, RoundedCornerShape(22.dp)),
    ) {
        Text(
            androidx.compose.ui.text.buildAnnotatedString {
                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                append(if (size) "Size" else "Opacity")
                pop()
                append(" ${(value * 100f).roundToInt()}%")
            },
            color = SliderPanelInk,
            fontSize = 19.sp,
            maxLines = 1,
            modifier = Modifier.offset(16.dp, 6.dp).height(32.dp).wrapContentHeight(Alignment.CenterVertically),
        )
        // read again as the notches change, so a value saved or taken away shows at once
        val notches = if (size) vm.sizeNotches else vm.opacityNotches
        val onNotch = androidx.compose.runtime.remember(notches, value) { vm.notchAt(size, value) != null }
        val canSave = androidx.compose.runtime.remember(notches, value) { vm.canSaveNotch(size) }
        if (onNotch || canSave) {
            Box(Modifier.offset(150.dp, 6.dp).size(32.dp).tap { vm.toggleNotch(size) }, contentAlignment = Alignment.Center) {
                Icon(
                    if (onNotch) PsIcons.NotchRemove else PsIcons.NotchAdd,
                    contentDescription = if (onNotch) "Remove" else "Save",
                    tint = SliderPanelInk,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val ink = SliderPanelDot.copy(alpha = if (size) 1f else value.coerceIn(0.02f, 1f))
            if (stamp == null) {
                drawCircle(ink, radius, center)
            } else {
                drawImage(
                    stamp,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - side / 2f, center.y - side / 2f),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ink),
                )
            }
        }
    }
}

/**
 * An upright slider moved by dragging anywhere on it: the thumb follows the finger (relative,
 * not jump-to-touch), 1 at the top. Moved, its [panel] shows while it moves; held still, it stays up.
 * Its [notches] (saved values) show as marks on the track; a value dragged near one is pulled onto
 * it, and a tap on one goes there. On a notch, the thumb shows a blue bar.
 */
@Composable
fun BrushSlider(value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier, panel: SliderPanelHooks? = null, notches: List<Float> = emptyList(), height: Dp = Ps.SliderHeight, look: Float = 0f) {
    val current by rememberUpdatedState(value)
    val change by rememberUpdatedState(onChange)
    val hooks by rememberUpdatedState(panel)
    val marks by rememberUpdatedState(notches)
    fun near(v: Float, reach: Float = CanvasViewModel.NOTCH_NEAR) =
        marks.filter { kotlin.math.abs(it - v) < reach }.minByOrNull { kotlin.math.abs(it - v) }
    Box(
        modifier.size(Ps.SliderWidth, height)
            .pointerInput(height) {
                val travel = (height - Ps.ThumbHeight - Ps.ThumbInset * 2).toPx()
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    var last = down.position.y
                    var moved = false
                    var lifted = false
                    // the system took the touch back (the pen coming near takes the screen from a
                    // finger): it wasn't lifted, so the panel stays up for the pen to tap its +
                    var taken = false
                    // where the finger has taken it, before a notch pulls it in
                    var raw = current
                    // follows the finger; false once it is lifted. Another pointer's event (the
                    // pen hovering in) isn't this touch's.
                    suspend fun AwaitPointerEventScope.follow(): Boolean {
                        val c = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return true
                        if (!c.pressed) {
                            // a cancelled touch ends already consumed; a lift doesn't
                            taken = c.isConsumed
                            return false
                        }
                        val dy = c.position.y - last
                        if (dy != 0f) {
                            if (!moved) hooks?.show(false)
                            moved = true
                            last = c.position.y
                            raw = (raw - dy / travel).coerceIn(0f, 1f)
                            change(near(raw) ?: raw)
                        }
                        c.consume()
                        return true
                    }
                    withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (!lifted && !moved) lifted = !follow()
                    }
                    if (!lifted && !moved) hooks?.show(true)
                    // a tap on a notch's mark goes to it
                    if (lifted && !moved && !taken) {
                        val inset = Ps.ThumbInset.toPx()
                        val at = 1f - (down.position.y - inset - Ps.ThumbHeight.toPx() / 2f) / travel
                        near(at, Ps.ThumbHeight.toPx() / 2f / travel)?.let { change(it) }
                    }
                    while (!lifted) lifted = !follow()
                    if (taken) hooks?.show(true) else if (moved) hooks?.hide()
                }
            }
            .drawBehind {
                val inset = Ps.ThumbInset.toPx()
                val thumb = Size(Ps.ThumbWidth.toPx(), Ps.ThumbHeight.toPx())
                val at = Offset((size.width - thumb.width) / 2f, inset + (size.height - thumb.height - inset * 2) * (1f - value))
                val knob = RoundRect(at.x, at.y, at.x + thumb.width, at.y + thumb.height, CornerRadius(thumb.height / 2f))
                // the track has a window where the thumb is, so the thumb shows over the bar alone;
                // a lighter rim runs round it
                val corner = Ps.SliderCorner.toPx()
                val rim = Ps.SliderRimWidth.toPx()
                val inside = RoundRect(rim, rim, size.width - rim, size.height - rim, CornerRadius(corner - rim))
                val track = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRoundRect(inside)
                    addRoundRect(knob)
                }
                drawPath(track, Ps.SliderTrack)
                val ring = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(corner)))
                    addRoundRect(inside)
                }
                drawPath(ring, Ps.SliderRim)
                // the notches: short bars across the track; on one, the thumb shows a blue bar
                val bar = Size(NotchWidth.toPx(), NotchHeight.toPx())
                val travel = size.height - thumb.height - inset * 2
                for (n in notches) {
                    val y = inset + thumb.height / 2f + travel * (1f - n)
                    drawRoundRect(NotchMark, Offset((size.width - bar.width) / 2f, y - bar.height / 2f), bar, CornerRadius(bar.height / 2f))
                }
                drawPath(Path().apply { addRoundRect(knob) }, Ps.SliderThumb)
                if (notches.any { kotlin.math.abs(it - value) < 1e-4f }) {
                    drawRoundRect(Ps.Accent, Offset((size.width - bar.width) / 2f, at.y + (thumb.height - bar.height) / 2f), bar, CornerRadius(bar.height / 2f))
                }
                // the thumb has eyes; it watches its own track
                val eyeY = at.y + thumb.height / 2f
                for (side in listOf(-1f, 1f)) {
                    val ex = at.x + thumb.width / 2f + side * 7.dp.toPx()
                    drawCircle(Color.White, 4.1.dp.toPx(), Offset(ex, eyeY))
                    val px = ex + kotlin.math.sin(look + side * 1.7f) * 1.25.dp.toPx()
                    val py = eyeY + kotlin.math.cos(look * 0.9f + side) * 1.25.dp.toPx()
                    drawCircle(Color(0xFF1B0A14), 1.9.dp.toPx(), Offset(px, py))
                }
            },
    )
}

/** Liquify's ring: its reach as a thin dark circle with a light edge, a fine cross in its middle. */
@Composable
private fun LiquifyRing(vm: CanvasViewModel) {
    val at = vm.liquifyRingAt ?: return
    val r = vm.liquifyRingRadius
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        for ((width, color) in listOf(3.dp.toPx() to Color.White, 1.dp.toPx() to Color.Black)) {
            drawCircle(color, r, at, style = Stroke(width))
            val arm = 5.dp.toPx()
            drawLine(color, at - Offset(arm, 0f), at + Offset(arm, 0f), width)
            drawLine(color, at - Offset(0f, arm), at + Offset(0f, arm), width)
        }
    }
}

private val NotchWidth = 20.dp
private val NotchHeight = 4.dp
private val NotchMark = Color(0x1AFFFFFF)

/**
 * The brush's outline where the pen paints or hovers, in the brush's own shape (the round tip's a
 * circle): a thin dark line with a light edge, or in the colour painted with.
 */
@Composable
fun BrushCursor(vm: CanvasViewModel) {
    LiquifyRing(vm)
    val at = vm.cursorAt ?: return
    val r = vm.cursorRadius
    if (r < 2f) return
    val style = vm.cursorStyle
    val paint = CanvasSpace.color(vm.colorRgb())
    val shape = vm.cursorShape
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        // its loops across the brush's diameter, about where it is
        val paths = shape.map { ring ->
            Path().apply {
                for (i in 0 until ring.size / 2) {
                    val x = at.x + ring[i * 2] * r * 2f
                    val y = at.y + ring[i * 2 + 1] * r * 2f
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
        }
        fun outline(color: Color, width: Float) {
            if (paths.isEmpty()) drawCircle(color, r, at, style = Stroke(width))
            for (p in paths) drawPath(p, color, style = Stroke(width))
        }
        val contrast = { outline(Color.White, 3.dp.toPx()); outline(Color.Black, 1.dp.toPx()) }
        val colour = { outline(Color(0x99FFFFFF), 3.dp.toPx()); outline(paint, 1.5.dp.toPx()) }
        when (style) {
            CanvasViewModel.CursorStyle.CONTRAST -> contrast()
            CanvasViewModel.CursorStyle.COLOR -> colour()
            // as the brush says: none, contrasting lines, or the colour
            CanvasViewModel.CursorStyle.BRUSH -> when (vm.cursorOutline) {
                0 -> Unit
                2 -> colour()
                else -> contrast()
            }
        }
    }
}
