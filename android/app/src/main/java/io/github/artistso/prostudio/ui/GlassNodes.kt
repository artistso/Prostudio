package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.canvas.CanvasViewModel
import io.github.artistso.prostudio.canvas.Panel
import io.github.artistso.prostudio.core.GuideType
import io.github.artistso.prostudio.core.ToolKind
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

/**
 * InkFrame's optical command nodes: circular glass orbs floating around the stage — a bright rim
 * caught from the horizon, a dark optical centre behind each hand-drawn glyph, the label tracked
 * beneath. Tap and they answer (a panel, a tool, a fan of children blooming toward the drawing's
 * middle); drag one anywhere and it stays where it is put.
 */
@Composable
fun GlassNodes(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("interface", android.content.Context.MODE_PRIVATE) }
    var open by remember { mutableStateOf<GlassNode?>(null) }
    // each node's place, as a fraction of the stage, persisted as it is dragged
    val places = remember { mutableStateMapOf<GlassNode, Offset>() }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val w = maxWidth
        val h = maxHeight
        fun placeOf(node: GlassNode): Offset {
            places[node]?.let { return it }
            val fx = prefs.getFloat("glassnode_${node.name}_x", -1f)
            val fy = prefs.getFloat("glassnode_${node.name}_y", -1f)
            val p = if (fx >= 0f && fy >= 0f) Offset(fx, fy) else node.defaultPlace
            places[node] = p
            return p
        }
        fun persist(node: GlassNode, p: Offset) {
            places[node] = p
            prefs.edit().putFloat("glassnode_${node.name}_x", p.x).putFloat("glassnode_${node.name}_y", p.y).apply()
        }
        // with a node open, a tap out in the world closes it — and still reaches the canvas
        if (open != null) {
            Box(Modifier.fillMaxSize().pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    open = null
                }
            })
        }
        for (node in GlassNode.entries) {
            val place = placeOf(node)
            val centreX = (place.x * w.value).dp
            val centreY = (place.y * h.value).dp
            val children = if (open == node) childrenFor(node, vm) { open = null } else emptyList()
            Column(
                Modifier
                    .offset(x = centreX - WrapperHalf, y = centreY - WrapperHalf)
                    .width(WrapperSize),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlassOrb(
                    vm = vm,
                    node = node,
                    open = open == node,
                    onTap = {
                        when {
                            open == node -> open = null
                            node.opensFan(vm) -> open = node
                            else -> node.performDirect(vm)
                        }
                    },
                    onDrag = { deltaPx ->
                        val cur = placeOf(node)
                        val nx = ((cur.x * w.value + deltaPx.x / density.density) / w.value).coerceIn(0.025f, 0.905f)
                        val ny = ((cur.y * h.value + deltaPx.y / density.density) / h.value).coerceIn(0.045f, 0.90f)
                        places[node] = Offset(nx, ny)
                    },
                    onDragEnd = { persist(node, placeOf(node)) },
                )
                Text(
                    node.label.uppercase(),
                    color = Glass.Paper.copy(alpha = if (open == node) 1f else 0.92f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xA614000E))
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
            if (open == node && children.isNotEmpty()) {
                ChildrenFan(children, vm, centreX, centreY, w, h)
            }
        }
    }
}

/** The optical commands; ProStudio's own capabilities behind InkFrame's vocabulary. */
private enum class GlassNode(val label: String) {
    TOOLS("Tools"), LINE("Line"), BRUSHES("Brushes"), COLOR("Color"), LAYERS("Layers"),
    ACTIONS("Actions"), FRAMES("Frames"), STUDIO("Studio"), GALLERY("Gallery");

    /** Where it floats by default, as a fraction of the stage. */
    val defaultPlace: Offset
        get() = when (this) {
            // seeded by the golden ratio downward from the corner orbs, as the Fibonacci gaps open
            TOOLS -> Offset(0.088f, 0.161f)
            LINE -> Offset(0.088f, 0.271f)
            BRUSHES -> Offset(0.088f, 0.449f)
            COLOR -> Offset(0.088f, 0.749f)
            LAYERS -> Offset(0.912f, 0.161f)
            ACTIONS -> Offset(0.912f, 0.271f)
            FRAMES -> Offset(0.912f, 0.449f)
            STUDIO -> Offset(0.088f, 0.095f)
            GALLERY -> Offset(0.912f, 0.095f)
        }

    /** Whether a tap opens the fan of children, or does its one thing at once. */
    fun opensFan(vm: CanvasViewModel): Boolean = when (this) {
        TOOLS, LINE, ACTIONS, COLOR -> true
        FRAMES -> vm.animation != null
        else -> false
    }

    /** Its one thing, for the nodes that do one thing. */
    fun performDirect(vm: CanvasViewModel) {
        when (this) {
            BRUSHES -> vm.toggle(Panel.BRUSHES)
            LAYERS -> vm.toggle(Panel.LAYERS)
            STUDIO -> vm.toggleQuickMenu(vm.screenCentre)
            GALLERY -> vm.closeArtwork()
            FRAMES -> vm.changeAnimationAssist(true)
            else -> Unit
        }
    }
}

/** A child of an open node: a glyph orb, an accent-lit one when it is what is chosen, a swatch, or a dial. */
private data class NodeChild(
    val id: String,
    val label: String,
    val glyph: String? = null,
    val swatch: Int? = null,
    val accent: Boolean = false,
    val dial: Boolean = false,
    val onTap: () -> Unit = {},
)

/** The children an open node fans out. */
private fun childrenFor(node: GlassNode, vm: CanvasViewModel, close: () -> Unit): List<NodeChild> =
    when (node) {
        GlassNode.TOOLS -> listOf(
            NodeChild("paint", "Paint", "ink", accent = vm.tool == ToolKind.PAINT) { vm.toolPressed(ToolKind.PAINT); close() },
            NodeChild("smudge", "Smudge", "smudge", accent = vm.tool == ToolKind.SMUDGE) { vm.toolPressed(ToolKind.SMUDGE); close() },
            NodeChild("erase", "Erase", "eraser", accent = vm.tool == ToolKind.ERASE) { vm.toolPressed(ToolKind.ERASE); close() },
        )
        GlassNode.LINE -> listOf(
            NodeChild("streamline", "StreamLine", dial = true),
            NodeChild("qshape", "QuickShape", "qshape") { vm.quickShapeLast(); close() },
            NodeChild("sym", "Symmetry", "sym") {
                vm.openGuideEditor()
                vm.chooseGuideKind(GuideType.SYMMETRY)
                vm.symmetryOptionsOpen = true
                close()
            },
        )
        GlassNode.ACTIONS -> listOf(
            NodeChild("undo", "Undo", "undo") { vm.undo(); close() },
            NodeChild("redo", "Redo", "redo") { vm.redo(); close() },
            NodeChild("adjust", "Adjust", "tune", accent = vm.adjustment != null || vm.panel == Panel.ADJUSTMENTS) { vm.adjustmentsPressed(); close() },
            NodeChild("select", "Select", "select", accent = vm.selecting) { vm.toggleSelecting(); close() },
            NodeChild("transform", "Transform", "expand", accent = vm.transforming) { vm.toggleTransform(); close() },
        )
        GlassNode.COLOR -> {
            val recent = vm.history.take(5).mapIndexed { i, rgb ->
                NodeChild("c$i", "", swatch = rgb, accent = rgb == vm.colorRgb()) { vm.setRgb(rgb); close() }
            }
            recent + NodeChild("more", "More", "custom") { vm.toggle(Panel.COLOR); close() }
        }
        GlassNode.FRAMES -> {
            val playing = vm.playingSlot != null
            val current = vm.currentSlot ?: 0
            listOf(
                NodeChild("play", if (playing) "Pause" else "Play", if (playing) "pause" else "play", accent = playing) { vm.togglePlaying(); close() },
                NodeChild("prev", "Prev", "prev") { vm.tapFrame(current - 1); close() },
                NodeChild("next", "Next", "next") { vm.tapFrame(current + 1); close() },
                NodeChild("add", "Add Frame", "add") { vm.addFrame(); close() },
                NodeChild("dup", "Duplicate", "dup") { vm.duplicateFrame(); close() },
                NodeChild("del", "Delete", "del") { vm.deleteFrame(); close() },
                NodeChild("settings", "Settings", "tune", accent = vm.animationSettingsOpen) {
                    vm.animationSettingsOpen = !vm.animationSettingsOpen
                    vm.frameOptionsOpen = false
                    close()
                },
                NodeChild("assist", "End Assist", "loop") { vm.changeAnimationAssist(false); close() },
            )
        }
        else -> emptyList()
    }

/** Whether the node's panel or mode is what is open or chosen. */
private fun nodeActive(node: GlassNode, vm: CanvasViewModel): Boolean = when (node) {
    GlassNode.BRUSHES -> vm.panel == Panel.BRUSHES
    GlassNode.COLOR -> vm.panel == Panel.COLOR
    GlassNode.LAYERS -> vm.panel == Panel.LAYERS
    GlassNode.ACTIONS -> vm.panel == Panel.ACTIONS
    GlassNode.FRAMES -> vm.animation != null
    GlassNode.STUDIO -> vm.quickMenuAt != null
    else -> false
}

/** The glyph an orb carries; the colour node carries its swatch instead. */
private fun nodeGlyph(node: GlassNode): String? = when (node) {
    GlassNode.TOOLS -> "pencil"
    GlassNode.LINE -> "line"
    GlassNode.BRUSHES -> "tools"
    GlassNode.LAYERS -> "layers"
    GlassNode.ACTIONS -> "actions"
    GlassNode.FRAMES -> "frames"
    GlassNode.STUDIO -> "studio"
    GlassNode.GALLERY -> "gallery"
    GlassNode.COLOR -> null
}

private val OrbSize = 58.dp
private val WrapperSize = 74.dp
private val WrapperHalf = 37.dp
private val ChildSize = 48.dp
private val ChildHalf = 24.dp

@Composable
private fun GlassOrb(
    vm: CanvasViewModel,
    node: GlassNode,
    open: Boolean,
    onTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    val lit = open || nodeActive(node, vm)
    // the long exterior shadow and the open node's accent burn, behind a little extra room
    Box(
        Modifier.size(WrapperSize).drawBehind {
            val radius = OrbSize.toPx() / 2f
            if (lit) drawCircle(Glass.Accent.copy(alpha = 0.28f), radius * 1.38f, center)
            drawCircle(Color(0x73000000), radius + 2.dp.toPx(), Offset(center.x + 1.dp.toPx(), center.y + 3.5.dp.toPx()))
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(OrbSize)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDragEnd() },
                        onDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
                    )
                }
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onTap,
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Glass.Strong, Glass.Normal)))
                .border(1.dp, if (lit) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Glass.OpticalCenter), contentAlignment = Alignment.Center) {
                val glyph = nodeGlyph(node)
                when {
                    node == GlassNode.COLOR -> Box(
                        Modifier.size(22.dp).clip(CircleShape)
                            .background(Color(0xFF000000.toInt() or vm.colorRgb()))
                            .border(1.2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    )
                    glyph != null -> GlassGlyph(glyph, 24.dp)
                }
            }
        }
    }
}

/** The fan of children an open node blooms toward the middle of the drawing. */
private const val GOLDEN_ANGLE = 2.3999632f // 137.5078 degrees — the angle between successive sunflower seeds
private const val DETACH_DP = 130f // how far a child must be dragged from its seat before its connector lets go

@Composable
private fun ChildrenFan(
    children: List<NodeChild>,
    vm: CanvasViewModel,
    cx: Dp,
    cy: Dp,
    w: Dp,
    h: Dp,
) {
    val bloom = remember { Animatable(0f) }
    LaunchedEffect(Unit) { bloom.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 230f)) }
    val centre = Offset(cx.value, cy.value)
    val toCentre = Offset(w.value / 2f - centre.x, h.value / 2f - centre.y)
    val base = atan2(toCentre.y, toCentre.x)
    // the seeds of a sunflower: the golden angle apart, the radius growing with the square root of the index —
    // an orb is a big seed, not a grain of wheat: the first child must clear the parent's rim entirely
    val k = (170f / kotlin.math.sqrt(children.size + 1f)).coerceIn(56f, 88f)
    val seats = children.indices.map { i ->
        val a = base + i * GOLDEN_ANGLE
        val r = k * kotlin.math.sqrt(i + 1f)
        Offset(
            (centre.x + cos(a) * r).coerceIn(40f, w.value - 40f),
            (centre.y + sin(a) * r).coerceIn(34f, h.value - 34f),
        )
    }
    // where each orb actually is (its seat, plus any drag), and which have let go of their connector
    // remembered for the fan's lifetime — the fan leaves the tree together, and the maps go with it
    val positions = remember { mutableStateMapOf<Int, Offset>().apply { seats.forEachIndexed { i, seat -> put(i, seat) } } }
    val detached = remember { mutableStateMapOf<Int, Boolean>() }
    // the connectors, behind the orbs, grown out of the parent with the bloom
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val u = size.width / w.value
        children.indices.forEach { i ->
            if (detached[i] == true) return@forEach
            val trim = ((bloom.value - i * 0.09f) / 0.42f).coerceIn(0f, 1f)
            if (trim <= 0f) return@forEach
            val seat = positions[i] ?: seats[i]
            val toward = seat - centre
            val len = toward.getDistance()
            val start = centre + if (len < 0.001f) Offset.Zero else toward * (30f / len) // from the parent's rim, underneath it
            val s = start * u
            val e = seat * u
            val dist = (e - s).getDistance()
            val dir = if (dist < 0.001f) Offset(1f, 0f) else (e - s) / dist
            val perp = Offset(-dir.y, dir.x)
            val droop = dist * 0.16f
            val bow = sin(i * 2.3f + 0.8f) * dist * 0.14f
            val control = (s + e) / 2f + perp * bow + Offset(0f, droop)
            // the connector grows out of the parent as the bloom reaches this orb — a curve of straight ink
            val steps = 20
            val upto = (steps * trim).roundToInt().coerceAtLeast(2)
            val points = (0..upto).map { segment -> quadratic(s, control, e, (segment.toFloat() / steps).coerceAtMost(1f)) }
            val halo = if (children[i].accent) Color(0x59FB8FB3) else Glass.RimInner.copy(alpha = 0.16f)
            val core = if (children[i].accent) Color(0xCCFFB7CD) else Glass.Paper.copy(alpha = 0.75f)
            for (segment in 1 until points.size) {
                drawLine(halo, points[segment - 1], points[segment], 4.6.dp.toPx(), cap = StrokeCap.Round)
            }
            for (segment in 1 until points.size) {
                drawLine(core, points[segment - 1], points[segment], 1.4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
    for ((i, child) in children.withIndex()) {
        ChildOrb(
            child,
            vm,
            i,
            seats[i],
            onPosition = { index, at -> positions[index] = at },
            onDetach = { index -> detached[index] = true },
            Modifier.offset(x = (seats[i].x - ChildHalf.value).dp, y = (seats[i].y - ChildHalf.value).dp),
        )
    }
}

/** A point on a quadratic bezier at [t]. */
private fun quadratic(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val a = (1f - t) * (1f - t)
    val b = 2f * (1f - t) * t
    val c = t * t
    return Offset(a * p0.x + b * p1.x + c * p2.x, a * p0.y + b * p1.y + c * p2.y)
}

@Composable
private fun ChildOrb(
    child: NodeChild,
    vm: CanvasViewModel,
    index: Int,
    seat: Offset,
    onPosition: (Int, Offset) -> Unit,
    onDetach: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // the drag of this orb away from its seat (dp), and whether its connector has let go
    val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var detached by remember { mutableStateOf(false) }
    // report the drag (and the spring back home) so the connector can follow
    LaunchedEffect(Unit) { snapshotFlow { drag.value }.collect { onPosition(index, seat + it) } }
    val pop = remember { Animatable(0.35f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 45L)
        pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 420f))
    }
    val tight = child.dial // the dial keeps its own vertical drag
    val density = LocalDensity.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(
        modifier
            .width(72.dp)
            .offset(drag.value.x.dp, drag.value.y.dp)
            .graphicsLayer {
                scaleX = pop.value * if (detached) 1.08f else 1f
                scaleY = pop.value * if (detached) 1.08f else 1f
                alpha = pop.value.coerceIn(0f, 1f)
            }
            .pointerInput(tight) {
                if (tight) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        val d = drag.value
                        if (!detached && d.x * d.x + d.y * d.y > DETACH_DP * DETACH_DP) {
                            detached = true
                            onDetach(index)
                        } else if (!detached) {
                            scope.launch { drag.animateTo(Offset.Zero, spring(dampingRatio = 0.32f, stiffness = 720f)) }
                        }
                    },
                    onDrag = { change, delta ->
                        change.consume()
                        val moved = Offset(delta.x / density.density, delta.y / density.density)
                        scope.launch { drag.snapTo(drag.value + moved) }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            child.dial -> StreamLineDial(vm, Modifier.size(ChildSize))
            child.swatch != null -> Box(
                Modifier.size(ChildSize)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = child.onTap,
                    )
                    .drawBehind {
                        drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 2.dp.toPx()))
                    }
                    .clip(CircleShape)
                    .then(if (child.accent) Modifier.background(Glass.AccentGradient) else Modifier.background(Brush.linearGradient(listOf(Glass.Strong, Glass.Normal))))
                    .border(2.dp, Glass.Paper.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(25.dp).clip(CircleShape).background(Color(0xFF000000.toInt() or child.swatch)))
            }
            else -> Box(
                Modifier.size(ChildSize)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = child.onTap,
                    )
                    .drawBehind {
                        drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 2.dp.toPx()))
                    }
                    .clip(CircleShape)
                    .then(if (child.accent) Modifier.background(Glass.AccentGradient) else Modifier.background(Brush.linearGradient(listOf(Glass.Strong, Glass.Normal))))
                    .border(1.dp, if (child.accent) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                GlassGlyph(child.glyph, 20.dp)
            }
        }
        if (child.label.isNotEmpty()) {
            Text(
                child.label.uppercase(),
                color = Glass.Paper.copy(alpha = 0.92f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xA614000E))
                    .border(1.dp, Glass.RimInner.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
    }
}

/** StreamLine, Procreate-style: a dial that smooths the stroke it draws; drag up and down to set it. */
@Composable
private fun StreamLineDial(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val value = vm.inputStabilization
    Box(
        modifier
            .clip(CircleShape)
            .background(Glass.OpticalCenter)
            .border(1.dp, Glass.RimInner.copy(alpha = 0.45f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    vm.changeInput(stabilization = (vm.inputStabilization - dragAmount.y / 160f).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.4.dp.toPx()
            val inset = stroke / 2f + 1.dp.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Color(0x29F7CAC9),
                startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (value > 0.001f) {
                drawArc(
                    brush = Glass.AccentGradient,
                    startAngle = 135f, sweepAngle = 270f * value, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text("${(value * 100f).roundToInt()}", color = Glass.Paper, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
