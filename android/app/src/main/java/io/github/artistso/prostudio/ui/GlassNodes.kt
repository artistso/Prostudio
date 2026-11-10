package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import io.github.artistso.prostudio.core.ToolKind
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * InkFrame's optical command nodes: circular glass orbs floating around the stage — a bright rim
 * caught from the horizon, a dark optical centre behind each glyph, the label tracked beneath.
 * Tap and they answer (a panel, a tool, a fan of children blooming toward the drawing's middle);
 * drag one anywhere and it stays where it is put.
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
                    color = Glass.RoseQuartz.copy(alpha = if (open == node) 1f else 0.82f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.7.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (open == node && children.isNotEmpty()) {
                ChildrenFan(children, centreX, centreY, w, h)
            }
        }
    }
}

/** The eight optical commands; ProStudio's own capabilities behind InkFrame's vocabulary. */
private enum class GlassNode(val label: String) {
    TOOLS("Tools"), BRUSHES("Brushes"), COLOR("Color"), LAYERS("Layers"),
    ACTIONS("Actions"), FRAMES("Frames"), STUDIO("Studio"), GALLERY("Gallery");

    /** Where it floats by default, as a fraction of the stage. */
    val defaultPlace: Offset
        get() = when (this) {
            TOOLS -> Offset(0.088f, 0.315f)
            BRUSHES -> Offset(0.088f, 0.465f)
            COLOR -> Offset(0.088f, 0.615f)
            LAYERS -> Offset(0.912f, 0.315f)
            ACTIONS -> Offset(0.912f, 0.465f)
            FRAMES -> Offset(0.912f, 0.615f)
            STUDIO -> Offset(0.088f, 0.095f)
            GALLERY -> Offset(0.912f, 0.095f)
        }

    /** Whether a tap opens the fan of children, or does its one thing at once. */
    fun opensFan(vm: CanvasViewModel): Boolean = when (this) {
        TOOLS, ACTIONS, COLOR -> true
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

/** A child of an open node: a glyph orb, an accent-lit one when it is what is chosen, or a swatch. */
private data class NodeChild(
    val id: String,
    val label: String,
    val glyph: ImageVector? = null,
    val swatch: Int? = null,
    val accent: Boolean = false,
    val onTap: () -> Unit,
)

/** The children an open node fans out. */
private fun childrenFor(node: GlassNode, vm: CanvasViewModel, close: () -> Unit): List<NodeChild> =
    when (node) {
        GlassNode.TOOLS -> listOf(
            NodeChild("paint", "Paint", PsIcons.Brush, accent = vm.tool == ToolKind.PAINT) { vm.toolPressed(ToolKind.PAINT); close() },
            NodeChild("smudge", "Smudge", PsIcons.Smudge, accent = vm.tool == ToolKind.SMUDGE) { vm.toolPressed(ToolKind.SMUDGE); close() },
            NodeChild("erase", "Erase", PsIcons.Erase, accent = vm.tool == ToolKind.ERASE) { vm.toolPressed(ToolKind.ERASE); close() },
        )
        GlassNode.ACTIONS -> listOf(
            NodeChild("undo", "Undo", PsIcons.Undo) { vm.undo(); close() },
            NodeChild("redo", "Redo", PsIcons.Redo) { vm.redo(); close() },
            NodeChild("adjust", "Adjust", PsIcons.Adjustments, accent = vm.adjustment != null || vm.panel == Panel.ADJUSTMENTS) { vm.adjustmentsPressed(); close() },
            NodeChild("select", "Select", PsIcons.Selection, accent = vm.selecting) { vm.toggleSelecting(); close() },
            NodeChild("transform", "Transform", PsIcons.Transform, accent = vm.transforming) { vm.toggleTransform(); close() },
        )
        GlassNode.COLOR -> {
            val recent = vm.history.take(5).mapIndexed { i, rgb ->
                NodeChild("c$i", "", swatch = rgb, accent = rgb == vm.colorRgb()) { vm.setRgb(rgb); close() }
            }
            recent + NodeChild("more", "More", PsIcons.More) { vm.toggle(Panel.COLOR); close() }
        }
        GlassNode.FRAMES -> listOf(
            NodeChild("add", "Add Frame", PsIcons.Add) { vm.addFrame(); close() },
            NodeChild("settings", "Settings", PsIcons.AdjustKnob, accent = vm.animationSettingsOpen) {
                vm.animationSettingsOpen = !vm.animationSettingsOpen
                vm.frameOptionsOpen = false
                close()
            },
            NodeChild("assist", "Assist", PsIcons.Tick, accent = true) { vm.changeAnimationAssist(false); close() },
        )
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
private fun nodeGlyph(node: GlassNode): ImageVector? = when (node) {
    GlassNode.TOOLS -> PsIcons.Brush
    GlassNode.BRUSHES -> PsIcons.BrushSet
    GlassNode.LAYERS -> PsIcons.Layers
    GlassNode.ACTIONS -> PsIcons.Actions
    GlassNode.FRAMES -> PsIcons.TabVideo
    GlassNode.STUDIO -> PsIcons.MenuGrid
    GlassNode.GALLERY -> PsIcons.CardStack
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
                    glyph != null -> Image(rememberVectorPainter(glyph), contentDescription = node.label, colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Glass.Paper), modifier = Modifier.requiredSize(23.dp))
                }
            }
        }
    }
}

/** The fan of children an open node blooms toward the middle of the drawing. */
@Composable
private fun ChildrenFan(
    children: List<NodeChild>,
    cx: Dp,
    cy: Dp,
    w: Dp,
    h: Dp,
) {
    // the orbs bloom out from the parent with a spring, fanned toward the stage's middle
    val pop = remember { Animatable(0.35f) }
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fade.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 600f))
        pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f))
    }
    val centre = Offset(cx.value, cy.value)
    val toCentre = Offset(w.value / 2f - centre.x, h.value / 2f - centre.y)
    val base = atan2(toCentre.y, toCentre.x)
    val spread = min(children.size * 0.62f, 2.5f)
    val step = if (children.size > 1) spread / (children.size - 1) else 0f
    val radius = 96f
    for ((i, child) in children.withIndex()) {
        val a = base - spread / 2f + step * i
        val chx = (centre.x + cos(a) * radius).coerceIn(ChildHalf.value + 4f, w.value - ChildHalf.value - 4f)
        val chy = (centre.y + sin(a) * radius).coerceIn(ChildHalf.value + 4f, h.value - ChildHalf.value - 4f)
        ChildOrb(
            child,
            Modifier
                .offset(x = (chx - ChildHalf.value).dp, y = (chy - ChildHalf.value).dp)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                    alpha = fade.value
                },
        )
    }
}

@Composable
private fun ChildOrb(child: NodeChild, modifier: Modifier = Modifier) {
    Column(modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(ChildSize)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = child.onTap,
                )
                .drawBehind {
                    drawCircle(Color(0x73000000), radius = size.minDimension / 2f + 1.dp.toPx(), center = Offset(center.x + 1.dp.toPx(), center.y + 2.dp.toPx()))
                }
                .clip(CircleShape)
                .then(
                    if (child.accent) Modifier.background(Glass.AccentGradient)
                    else Modifier.background(Brush.linearGradient(listOf(Glass.Strong, Glass.Normal))),
                )
                .border(1.dp, if (child.accent) Glass.RimInner else Glass.RimInner.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                child.swatch != null -> Box(
                    Modifier.size(25.dp).clip(CircleShape)
                        .background(Color(0xFF000000.toInt() or child.swatch))
                        .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                )
                child.glyph != null -> Image(rememberVectorPainter(child.glyph), contentDescription = child.label, colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Glass.Paper), modifier = Modifier.requiredSize(19.dp))
            }
        }
        if (child.label.isNotEmpty()) {
            Text(
                child.label.uppercase(),
                color = Glass.RoseQuartz.copy(alpha = 0.85f),
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
