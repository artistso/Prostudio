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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * The perimeter frame board: InkFrame's frame stops, one per stop of the active assist sequence
 * (holds included — a held frame takes the room it takes), running around the world in a ∩ from
 * the bottom-left, up and over the top and down the far side, clear of the bar, the assist rail
 * and the rail's lanes. A filled stop shows the frame's picture; the stop in play scales up on the
 * blush-to-accent gradient; held stops carry a luminous dot; a tap goes there.
 *
 * Shown only with an assist sequence (Animation Assist or Page Assist) in play.
 */
@Composable
fun PerimeterFrameBoard(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val slots = vm.frameSlots
    if (vm.animation == null || slots.isEmpty()) return
    val occurrences = remember(slots) { slots.groupingBy { it }.eachCount() }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val left = BoardSide
        val top = Ps.BarHeight + BoardTopGap
        val right = maxWidth - BoardSide
        val bottom = maxHeight - BoardBottomInset
        val width = right - left
        val height = bottom - top
        val total = slots.size
        // every stop gets the room the ∩ allows, evenly along the whole path
        val roomPerSlot = (width + height * 2) / total
        val slotSize = (roomPerSlot - BoardGap).coerceIn(BoardSlotMin, BoardSlot)
        val positions = arcPositions(total, left, top, right, bottom, roomPerSlot, slotSize)
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
    }
}

/** The stops' top-left positions along the ∩ path: bottom-left corner up the left edge, over the top, down the right. */
private fun arcPositions(count: Int, left: Dp, top: Dp, right: Dp, bottom: Dp, pitch: Dp, slot: Dp): List<Pair<Dp, Dp>> {
    val edges = floatArrayOf((bottom - top).value, (right - left).value, (bottom - top).value)
    val positions = ArrayList<Pair<Dp, Dp>>(count)
    var edge = 0
    var along = 0f
    repeat(count) {
        while (along > edges[edge]) {
            along -= edges[edge]
            edge = (edge + 1) % edges.size
        }
        val (x, y) = when (edge) {
            0 -> left to bottom - along.dp - slot
            1 -> left + along.dp to top
            else -> right - slot to top + along.dp
        }
        positions += x to y
        along += pitch.value
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

private val BoardSide = 11.dp
private val BoardTopGap = 9.dp
private val BoardBottomInset = 14.dp
private val BoardSlot = 18.dp
private val BoardSlotMin = 12.dp
private val BoardGap = 5.dp
private val BoardSlotRadius = 5.dp
