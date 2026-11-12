package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
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
    // children torn off their fan stay pinned on the stage until re-linked, double-tapped, or collapsed
    val pinned = remember { mutableStateMapOf<String, PinnedChild>() }
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
        fun nodeCentreDp(node: GlassNode): Offset {
            val p = placeOf(node)
            return Offset(p.x * w.value, p.y * h.value)
        }
        fun nearAnyNode(atDp: Offset): Boolean = GlassNode.entries.any { node ->
            val c = nodeCentreDp(node)
            val dx = c.x - atDp.x
            val dy = c.y - atDp.y
            dx * dx + dy * dy <= REATTACH_DP * REATTACH_DP
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
                // the label rides bare under the orb, white ink on the plum with a soft halo — as the reference draws it
                Text(
                    node.label.uppercase(),
                    color = Color.White.copy(alpha = if (open == node) 1f else 0.92f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.35.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(
                        shadow = Shadow(Color(0xE62A001A), Offset(0f, 1.5f), blurRadius = 7f),
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (open == node && children.isNotEmpty()) {
                ChildrenFan(
                    parent = node,
                    children = children,
                    pinnedKeys = pinned.keys,
                    vm = vm,
                    cx = centreX,
                    cy = centreY,
                    w = w,
                    h = h,
                    onPin = { child, atDp ->
                        val key = "${node.name}:${child.id}"
                        pinned[key] = PinnedChild(node, child.id, atDp)
                        vm.showNotice("Pinned · drag near a circle to re-link · double-tap to release")
                    },
                )
            }
        }
        // the pin layer: torn-off child orbs that stay out on the stage when their parent closes
        for ((key, pin) in pinned.entries.toList()) {
            val live = childrenFor(pin.parent, vm) {}.firstOrNull { it.id == pin.childId } ?: continue
            PinnedChildOrb(
                child = live,
                vm = vm,
                at = pin.at,
                relinking = nearAnyNode(pin.at),
                onMove = { moved ->
                    val clamped = Offset(
                        moved.x.coerceIn(ChildHalf.value, w.value - ChildHalf.value),
                        moved.y.coerceIn(ChildHalf.value, h.value - ChildHalf.value * 1.6f),
                    )
                    pinned[key] = pin.copy(at = clamped)
                },
                onMoveEnd = {
                    val cur = pinned[key]?.at ?: pin.at
                    if (nearAnyNode(cur)) {
                        pinned.remove(key)
                        vm.showNotice("Re-linked")
                    }
                },
                onRelease = {
                    pinned.remove(key)
                    vm.showNotice("Pin released")
                },
            )
        }
        // #collapseBtn: the reference's bottom-right glass orb that folds every open fan and pinned child back
        CollapseAllOrb(
            visible = open != null || pinned.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 18.dp),
            onCollapse = {
                open = null
                pinned.clear()
                vm.showNotice("Collapsed · clean canvas")
            },
        )
    }
}

/** A child orb torn off its fan and pinned on the stage at [at] (in dp). */
private data class PinnedChild(val parent: GlassNode, val childId: String, val at: Offset)

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
                .background(Brush.radialGradient(listOf(Glass.Strong, Glass.Normal, Color(0x7714000E))))
                .border(1.dp, if (lit) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val glyph = nodeGlyph(node)
            when {
                node == GlassNode.COLOR -> Box(
                    Modifier.size(26.dp).clip(CircleShape)
                        .background(Color(0xFF000000.toInt() or vm.colorRgb()))
                        .border(1.2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                )
                glyph != null -> GlassGlyph(glyph, 26.dp)
            }
        }
    }
}

/** The fan of children an open node blooms toward the middle of the drawing. */
private const val DETACH_DP = 130f // how far a child must be dragged from its seat before it stays out of its seat
private const val REATTACH_DP = 62f // how near a parent orb a pinned child must come to snap home
private val CollapseOrbSize = 52.dp

/**
 * The fan of children an open node blooms, straight from the reference: every child orb sits at one shared
 * radius along an arc that opens toward the middle of the stage — a parent on the left rim sweeps its
 * children out to the right, and the mirror for the right rim — one orb per command, no connectors. The
 * spread is the reference's own: 78 dp for a small fan, 118 dp once the fan fills out.
 */
@Composable
private fun ChildrenFan(
    parent: GlassNode,
    children: List<NodeChild>,
    pinnedKeys: Set<String>,
    vm: CanvasViewModel,
    cx: Dp,
    cy: Dp,
    w: Dp,
    h: Dp,
    onPin: (NodeChild, Offset) -> Unit,
) {
    val unpinned = children.filter { "${parent.name}:${it.id}" !in pinnedKeys }
    if (unpinned.isEmpty()) return
    val centre = Offset(cx.value, cy.value)
    val toTheRight = centre.x <= w.value / 2f
    val spread = if (unpinned.size <= 4) 78f else 118f
    val start = if (toTheRight) -58f else 122f
    val end = if (toTheRight) 58f else 238f
    val marginX = 34f
    val marginTop = 30f
    val marginBottom = 58f // the label rides bare under the orb, so the foot of the stage needs room
    val seats = unpinned.indices.map { i ->
        val fraction = if (unpinned.size <= 1) 0.5f else i.toFloat() / (unpinned.size - 1).toFloat()
        val degrees = start + (end - start) * fraction
        val radians = degrees / 180f * PI.toFloat()
        Offset(
            (centre.x + cos(radians) * spread).coerceIn(marginX, w.value - marginX),
            (centre.y + sin(radians) * spread).coerceIn(marginTop, h.value - marginBottom),
        )
    }
    for ((i, child) in unpinned.withIndex()) {
        val seat = seats[i]
        ChildOrb(
            child = child,
            vm = vm,
            index = i,
            seat = seat,
            onPin = { atDp -> onPin(child, atDp) },
            modifier = Modifier.offset(x = (seat.x - ChildHalf.value).dp, y = (seat.y - ChildHalf.value).dp),
        )
    }
}

@Composable
private fun ChildOrb(
    child: NodeChild,
    vm: CanvasViewModel,
    index: Int,
    seat: Offset,
    onPin: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    // the drag of this orb away from its seat; past DETACH_DP it pins onto the stage
    val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
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
                scaleX = pop.value
                scaleY = pop.value
                alpha = pop.value.coerceIn(0f, 1f)
            }
            .pointerInput(tight) {
                if (tight) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        val d = drag.value
                        if (d.x * d.x + d.y * d.y > DETACH_DP * DETACH_DP) {
                            onPin(seat + d)
                        } else {
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
        ChildOrbBody(child, vm, relinking = false, onTap = child.onTap, onDoubleTap = null)
    }
}

/** A child orb that has been torn off its fan and lives on the stage until re-linked or released. */
@Composable
private fun PinnedChildOrb(
    child: NodeChild,
    vm: CanvasViewModel,
    at: Offset,
    relinking: Boolean,
    onMove: (Offset) -> Unit,
    onMoveEnd: () -> Unit,
    onRelease: () -> Unit,
) {
    val tight = child.dial
    val density = LocalDensity.current
    Column(
        Modifier
            .offset(x = (at.x - 36f).dp, y = (at.y - ChildHalf.value).dp)
            .width(72.dp)
            .pointerInput(tight, at) {
                if (tight) return@pointerInput
                var cur = at
                detectDragGestures(
                    onDragEnd = { onMoveEnd() },
                    onDrag = { change, delta ->
                        change.consume()
                        cur += Offset(delta.x / density.density, delta.y / density.density)
                        onMove(cur)
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChildOrbBody(child, vm, relinking = relinking, onTap = child.onTap, onDoubleTap = onRelease)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChildOrbBody(
    child: NodeChild,
    vm: CanvasViewModel,
    relinking: Boolean,
    onTap: () -> Unit,
    onDoubleTap: (() -> Unit)?,
) {
    val clickMod = if (onDoubleTap != null) {
        Modifier.combinedClickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onTap,
            onDoubleClick = onDoubleTap,
        )
    } else {
        Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onTap,
        )
    }
    when {
        child.dial -> StreamLineDial(vm, Modifier.size(ChildSize))
        child.swatch != null -> {
            val swatchColor = Color(0xFF000000.toInt() or child.swatch)
            Box(
                Modifier.size(ChildSize)
                    .then(clickMod)
                    .drawBehind {
                        if (relinking) drawCircle(Glass.Accent.copy(alpha = 0.42f), radius = size.minDimension * 0.76f, center = center)
                        drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 2.dp.toPx()))
                    }
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(swatchColor.copy(alpha = 0.98f), swatchColor.copy(alpha = 0.58f))))
                    .border(1.dp, if (child.accent || relinking) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {}
        }
        else -> Box(
            Modifier.size(ChildSize)
                .then(clickMod)
                .drawBehind {
                    if (relinking) drawCircle(Glass.Accent.copy(alpha = 0.42f), radius = size.minDimension * 0.76f, center = center)
                    drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 2.dp.toPx()))
                }
                .clip(CircleShape)
                .background(
                    if (child.accent) Brush.radialGradient(listOf(Color(0xFFF7CAC9), Glass.Accent))
                    else Brush.radialGradient(listOf(Glass.Strong, Color(0x9414000E))),
                )
                .border(1.dp, if (child.accent || relinking) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            GlassGlyph(child.glyph, 20.dp)
        }
    }
    if (child.label.isNotEmpty()) {
        // bare white ink under the orb, as the reference draws it
        Text(
            child.label.uppercase(),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.45.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(Color(0xE62A001A), Offset(0f, 1f), blurRadius = 5f),
            ),
        )
    }
}

/**
 * InkFrame's `#collapseBtn`: the 52 dp glass orb at the bottom-right that folds every open fan and
 * pinned child back to a clean canvas.
 */
@Composable
private fun CollapseAllOrb(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onCollapse: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "collapseScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.92f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
        label = "collapseAlpha",
    )
    if (alpha <= 0.01f) return
    Box(
        modifier
            .size(CollapseOrbSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .drawBehind {
                drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.5.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 3.dp.toPx()))
            }
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Glass.Strong, Glass.Normal, Color(0x7714000E))))
            .border(1.dp, Glass.RimInner, CircleShape)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onCollapse,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GlassGlyph("collapse", 24.dp)
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
