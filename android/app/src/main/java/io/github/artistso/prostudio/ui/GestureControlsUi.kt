package io.github.artistso.prostudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.canvas.CanvasViewModel
import io.github.artistso.prostudio.core.GestureOption
import io.github.artistso.prostudio.canvas.GestureOptions
import io.github.artistso.prostudio.canvas.heading
import io.github.artistso.prostudio.canvas.title
import io.github.artistso.prostudio.core.GesturePurpose
import io.github.artistso.prostudio.canvas.GestureSettings

// the Glass Horizon materials: the page is the world at night, its cards the frosted plum glass
private val ScreenFill get() = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF220A1E), Color(0xFF150010)))
private val RailFill = Color(0x8C14000E)
private val CardFill = Color(0xA614000E)
private val Ink get() = Glass.Paper
private val Detail get() = Color(0xB8FFF0F3)
private val RailIdle get() = Color(0xD9F7CAC9)
private val Track get() = Color(0x3DF7CAC9)
// a delay whose way of touching is off: a faint line and a dim knob, no colour
private val TrackOff = Color(0x14FFF0F3)
private val KnobOff = Color(0x33FFF0F3)

/** Where a wide screen's page starts and ends, beside the card of what gestures can do. */
private val WideContentStart = 358.dp
private val WideContentEnd = 28.dp
private val CardEdge get() = Glass.RimInner.copy(alpha = 0.35f)
private val PageInk get() = Color(0xB8FFF0F3)

/** What gestures can do, by icon and name on a card headed "Gesture controls" (a wide screen's). */
@Composable
private fun GesturePagesCard(page: GesturePurpose, onPage: (GesturePurpose) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp).width(320.dp).fillMaxHeight().clip(shape).background(RailFill).border(1.dp, CardEdge, shape)) {
        Text("Gesture controls", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 24.dp, top = 19.dp))
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 8.dp, top = 17.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (p in GesturePurpose.entries) {
                val chosen = p == page
                Row(
                    Modifier.fillMaxWidth().height(53.dp).clip(CircleShape).background(if (chosen) Ps.Accent else Color.Transparent).clickable { onPage(p) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(54.dp), contentAlignment = Alignment.Center) {
                        Icon(icon(p), contentDescription = null, tint = if (chosen) Color.White else RailIdle, modifier = Modifier.requiredSize(37.dp))
                    }
                    Text(p.title, color = if (chosen) Color.White else PageInk, fontSize = 17.sp, maxLines = 1)
                }
            }
        }
    }
}

private fun icon(p: GesturePurpose): ImageVector = when (p) {
    GesturePurpose.SMUDGE -> PsIcons.Smudge
    GesturePurpose.ERASE -> PsIcons.Erase
    GesturePurpose.ASSISTED -> PsIcons.GestureAssisted
    GesturePurpose.EYEDROPPER -> PsIcons.GestureEyedropper
    GesturePurpose.QUICKSHAPE -> PsIcons.GestureQuickShape
    GesturePurpose.QUICKMENU -> PsIcons.GestureQuickMenu
    GesturePurpose.FULL_SCREEN -> PsIcons.GestureFullScreen
    GesturePurpose.CLEAR_LAYER -> PsIcons.GestureClearLayer
    GesturePurpose.COPY_PASTE -> PsIcons.GestureCopyPaste
    GesturePurpose.LAYER_SELECT -> PsIcons.GestureLayerSelect
    GesturePurpose.HOVER -> PsIcons.GestureHover
    GesturePurpose.GENERAL -> PsIcons.GestureGeneral
}

/**
 * Gesture controls, full screen: a rail of what gestures can do and, for the one chosen, a card of
 * switches for the ways of touching that do it (a delay under the held ones), and Done.
 */
@Composable
fun GestureControls(vm: CanvasViewModel) {
    // it opens on the page it was last on
    var page by vm::gesturePage
    var resetting by remember { mutableStateOf(false) }
    val settings = vm.gestures
    BackHandler { vm.gestureControlsOpen = false }
    // on a wide screen what gestures can do is named on a card, the page and its title to its right
    val screen = androidx.compose.ui.platform.LocalConfiguration.current
    val wide = screen.screenWidthDp > screen.screenHeightDp
    Box(Modifier.fillMaxSize().background(ScreenFill).pointerInput(Unit) { detectTapGestures { } }) {
        if (wide) {
            GesturePagesCard(page) { page = it }
        } else {
            Column(Modifier.width(81.dp).fillMaxHeight().background(RailFill).verticalScroll(rememberScrollState()).padding(start = 8.dp, top = 46.dp, bottom = 20.dp)) {
                for (p in GesturePurpose.entries) {
                    val chosen = p == page
                    Box(
                        Modifier.size(65.dp, 53.dp).clip(CircleShape).background(if (chosen) Ps.Accent else Color.Transparent).clickable { page = p },
                        contentAlignment = Alignment.Center,
                    ) {
                        // each drawn in a padded frame, fitted 37 across
                        Icon(icon(p), contentDescription = p.title, tint = if (chosen) Color.White else RailIdle, modifier = Modifier.requiredSize(37.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                }
            }
        }
        if (wide) {
            Text(
                page.heading, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(start = WideContentStart, end = WideContentEnd).offset(y = 28.dp).fillMaxWidth(),
            )
        } else {
            Text(page.heading, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.offset(x = 101.dp, y = 28.dp))
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 20.dp).size(44.dp, 45.dp).clip(CircleShape).background(Glass.AccentGradient).border(1.dp, Glass.RimInner.copy(alpha = 0.6f), CircleShape).clickable { vm.gestureControlsOpen = false },
            contentAlignment = Alignment.Center,
        ) {
            Icon(PsIcons.DoneTick, contentDescription = "Done", tint = Color.White, modifier = Modifier.requiredSize(40.dp))
        }
        Column(Modifier.padding(start = if (wide) WideContentStart else 105.dp, end = if (wide) WideContentEnd else 24.dp, top = 100.dp).verticalScroll(rememberScrollState())) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(CardFill)) {
                val options = GestureOptions[page].orEmpty()
                options.forEachIndexed { i, o ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(ScreenFill))
                    // 72 apart with the line; a held one closer up, its delay under it
                    OptionRow(o, settings.isOn(page, o.key), if (o.delayed) 60.dp else 71.dp) { settings.set(page, o.key, it) }
                    if (o.delayed) DelayRow(settings.delay(page), enabled = settings.isOn(page, o.key)) { settings.setDelay(page, it) }
                }
            }
            // under General's settings, everything back as it ships
            if (page == GesturePurpose.GENERAL) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)).background(CardFill).clickable { resetting = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Reset to defaults", color = Ink, fontSize = 15.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
    Alert(
        if (resetting) Unit else null,
        title = { "Reset gesture controls?" },
        message = { "Every gesture goes back to how the app starts out." },
        action = "Reset",
        onCancel = { resetting = false },
        onAction = {
            settings.resetAll()
            resetting = false
        },
    )
}

/** A way of touching: its name and what it does, and its switch. */
@Composable
private fun OptionRow(o: GestureOption, on: Boolean, height: Dp, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(height).clickable { onChange(!on) }.padding(start = 28.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(o.label, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(o.detail, color = Detail, fontSize = 13.sp, maxLines = 1)
        }
        Switch(on, enabled = true)
    }
}

/** How long a hold waits, 0.1 to 1.5 seconds, dragged or tapped along the bar; greyed while its way of touching is off. */
@Composable
private fun DelayRow(seconds: Float, enabled: Boolean, onChange: (Float) -> Unit) {
    val change by rememberUpdatedState(onChange)
    val span = GestureSettings.MAX_DELAY_S - GestureSettings.MIN_DELAY_S
    val fraction = ((seconds - GestureSettings.MIN_DELAY_S) / span).coerceIn(0f, 1f)
    Row(Modifier.fillMaxWidth().height(50.dp).padding(start = 28.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Delay", color = Ink, fontSize = 15.sp, modifier = Modifier.width(74.dp))
        Box(
            Modifier.weight(1f).fillMaxHeight()
                .then(
                    if (!enabled) {
                        Modifier
                    } else {
                        Modifier.pointerInput(Unit) {
                            fun at(x: Float) = GestureSettings.MIN_DELAY_S + (x / size.width).coerceIn(0f, 1f) * span
                            detectTapGestures { change(at(it.x)) }
                        }.pointerInput(Unit) {
                            fun at(x: Float) = GestureSettings.MIN_DELAY_S + (x / size.width).coerceIn(0f, 1f) * span
                            detectHorizontalDragGestures(onDragStart = { change(at(it.x)) }) { c, _ ->
                                c.consume()
                                change(at(c.position.x))
                            }
                        }
                    },
                )
                .drawBehind {
                    val y = size.height / 2f
                    val x = fraction * size.width
                    if (enabled) {
                        val h = 3.dp.toPx()
                        drawRect(Track, Offset(0f, y - h / 2f), Size(size.width, h))
                        drawRect(Ps.Accent, Offset(0f, y - h / 2f), Size(x, h))
                        val knob = Size(16.dp.toPx(), 10.dp.toPx())
                        drawRoundRect(Color.White, Offset(x - knob.width / 2f, y - knob.height / 2f), knob, CornerRadius(knob.height / 2f))
                    } else {
                        val h = 1.5.dp.toPx()
                        drawRect(TrackOff, Offset(0f, y - h / 2f), Size(size.width, h))
                        val knob = Size(16.dp.toPx(), 10.dp.toPx())
                        drawRoundRect(KnobOff, Offset(x - knob.width / 2f, y - knob.height / 2f), knob, CornerRadius(knob.height / 2f))
                    }
                },
        )
        Text("%.2fs".format(seconds), color = Ink, fontSize = 15.sp, modifier = Modifier.padding(start = 16.dp))
    }
}
