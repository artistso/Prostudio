package io.github.artistso.prostudio.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.canvas.CanvasViewModel
import kotlin.math.roundToInt

/**
 * The perimeter frame board: InkFrame's frame stops running all four sides of the world —
 * in from the bottom-left along the bottom edge, up the right side, over the top right-to-left
 * and down the left side. One stop per stop of the active assist sequence (holds included — a
 * held frame takes the room it takes), and one last empty invitation, the "next" stop, that
 * taps in a fresh frame. A filled stop shows the frame's picture; the stop in play scales up on
 * the blush-to-accent gradient; held stops carry a luminous dot; a tap goes there. The stops
 * ride above the assist rail, which floats over the foot of the world — the rail is the glass
 * dock; the board is the world's frame.
 *
 * Shown only with an assist sequence (Animation Assist or Page Assist) in play. ProStudio's
 * engine has no frame cap, so the capacity badge reports the live count instead of a limit
 * that does not exist here.
 */
@Composable
fun PerimeterFrameBoard(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val slots = vm.frameSlots
    if (vm.animation == null || slots.isEmpty()) return
    val occurrences = remember(slots) { slots.groupingBy { it }.eachCount() }
    // the assist rail's own height, so the bottom side rides just above it; mirrors AnimationUi's Bar constants
    val railHeight = 25.dp + 96.dp + if (vm.timelines.isNotEmpty()) 42.dp else 0.dp
    BoxWithConstraints(modifier.fillMaxSize()) {
        val left = BoardSide
        val top = Ps.BarHeight + BoardTopGap
        val right = maxWidth - BoardSide
        val bottom = maxHeight - railHeight - BoardAboveRail
        val width = right - left
        val height = bottom - top
        val stops = slots.size
        // the board always shows at least a dozen stops — InkFrame's own floor — so the world is
        // framed by a full ring, mostly quiet until the frames need it
        val total = maxOf(12, ((stops + 1 + 11) / 12) * 12)
        // every stop gets the room the perimeter allows, evenly around the world
        val roomPerSlot = (width + height) * 2 / total
        val slotSize = (roomPerSlot - BoardGap).coerceIn(BoardSlotMin, BoardSlot)
        val positions = pathPositions(total, left, top, right, bottom, roomPerSlot, slotSize)
        for ((index, frame) in slots.withIndex()) {
            val (x, y) = positions[index]
            val playing = (vm.playingSlot ?: vm.currentSlot) == index
            val thumb = vm.animationFrames.getOrNull(frame)?.let { vm.frameThumbnails[it] }
            FrameBoardSlot(
                number = frame + 1,
                playing = playing,
                held = (occurrences[frame] ?: 1) > 1,
                thumbnail = thumb,
                size = slotSize,
                modifier = Modifier.offset { IntOffset(x.toPx().roundToInt(), y.toPx().roundToInt()) },
                onTap = { vm.tapFrame(index) },
            )
        }
        // the invitation: the next stop, an empty frame waiting to be born
        run {
            val (x, y) = positions[stops]
            NextFrameSlot(
                size = slotSize,
                modifier = Modifier.offset { IntOffset(x.toPx().roundToInt(), y.toPx().roundToInt()) },
                onTap = { vm.addFrame() },
            )
        }
        // the quiet ring: the stops not yet needed, dashed and dim, holding the world's frame
        for (index in stops + 1 until total) {
            val (x, y) = positions[index]
            EmptyFrameSlot(
                size = slotSize,
                modifier = Modifier.offset { IntOffset(x.toPx().roundToInt(), y.toPx().roundToInt()) },
            )
        }
        // the capacity badge: the live frame count (this engine has no frame cap)
        FrameCountBadge(
            count = vm.animationFrames.size,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = railHeight + slotSize / 2 + 14.dp),
        )
    }
}

/** The stops' top-left positions along the perimeter: bottom edge left to right, right edge up, top right to left, left edge down. */
private fun pathPositions(count: Int, left: Dp, top: Dp, right: Dp, bottom: Dp, pitch: Dp, slot: Dp): List<Pair<Dp, Dp>> {
    val width = (right - left).value
    val height = (bottom - top).value
    val edges = floatArrayOf(width, height, width, height)
    val positions = ArrayList<Pair<Dp, Dp>>(count)
    repeat(count) { i ->
        var along = (i + 0.5f) * pitch.value
        var e = 0
        while (along > edges[e]) {
            along -= edges[e]
            e = (e + 1) % edges.size
        }
        val half = slot.value / 2f
        val (x, y) = when (e) {
            0 -> (left.value + along - half) to (bottom.value - half)
            1 -> (right.value - half) to (bottom.value - along - half)
            2 -> (right.value - along - half) to (top.value - half)
            else -> (left.value - half) to (top.value + along - half)
        }
        positions += x.dp to y.dp
    }
    return positions
}

@Composable
private fun FrameBoardSlot(
    number: Int,
    playing: Boolean,
    held: Boolean,
    thumbnail: androidx.compose.ui.graphics.ImageBitmap?,
    size: Dp,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val shape = RoundedCornerShape(BoardSlotRadius)
    Box(
        modifier
            .graphicsLayer {
                if (playing) {
                    scaleX = 1.32f
                    scaleY = 1.32f
                }
            }
            .requiredSize(size)
            .clip(shape)
            .then(if (playing) Modifier.background(Glass.AccentGradient) else Modifier.background(Glass.Gradient))
            .border(
                width = if (playing) 1.6.dp else 1.dp,
                color = if (playing) Color.White.copy(alpha = 0.92f) else Glass.RimInner.copy(alpha = if (thumbnail != null) 0.55f else 0.30f),
                shape = shape,
            )
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnail != null) {
            Image(thumbnail, contentDescription = "Frame $number", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            // a scrim under the number so it stays legible over any picture
            Box(Modifier.fillMaxSize().background(Color(0x52000000)))
        }
        Text(
            "$number",
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (playing) Color.White else Glass.Paper.copy(alpha = 0.92f),
            maxLines = 1,
        )
        if (held) {
            // the hold: a small luminous rose indicator at the foot of the stop
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9FB8)),
            )
        }
    }
}

/** The invitation stop: dashed rose, a plus, and a tap gives the world one more frame. */
@Composable
private fun NextFrameSlot(
    size: Dp,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val shape = RoundedCornerShape(BoardSlotRadius)
    Box(
        modifier
            .requiredSize(size)
            .clip(shape)
            .background(Color(0x1AF7CAC9))
            .border(
                width = 1.dp,
                color = Glass.RimInner.copy(alpha = 0.55f),
                shape = shape,
            )
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "+",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Glass.Paper.copy(alpha = 0.85f),
            maxLines = 1,
        )
    }
}

/** A stop not yet needed: dashed, dim, not a button — the world's frame holding its place. */
@Composable
private fun EmptyFrameSlot(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .requiredSize(size)
            .clip(RoundedCornerShape(BoardSlotRadius))
            .drawBehind {
                val radius = CornerRadius(BoardSlotRadius.toPx())
                drawRoundRect(
                    color = Color(0x668C5A6B),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
                    ),
                    cornerRadius = radius,
                )
            },
    )
}

/** The live frame count, a floating plum pill at the foot of the world; InkFrame's capacity badge, honest about what this engine enforces. */
@Composable
private fun FrameCountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xD02B0A20))
            .border(1.dp, Glass.RimInner.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$count ${if (count == 1) "FRAME" else "FRAMES"}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            color = Glass.Paper,
            maxLines = 1,
        )
    }
}

private val BoardSide = 14.dp
private val BoardTopGap = 9.dp
private val BoardAboveRail = 10.dp
private val BoardSlot = 18.dp
private val BoardSlotMin = 12.dp
private val BoardGap = 5.dp
private val BoardSlotRadius = 5.dp
