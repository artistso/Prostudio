package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.R
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos

/**
 * The studio, unhinged: what the empty gallery shows when nobody is watching. None of this touches
 * the engine, the canvas, or the paint — it only makes the front door a little more alive.
 */

private val DerangedTaglines = listOf(
    "The canvas knows what you did.",
    "Paint faster. It's judging you.",
    "A quiet studio, apparently.",
    "Every stroke is a war crime.",
    "The muse has left the chat.",
    "Your art is legally art now.",
    "Wet paint. Dry tears.",
    "It's not broken, it's avant-garde.",
    "The eraser filed a complaint.",
    "Screaming is not a brush.",
    "Somewhere, a critic just fainted.",
    "The lights are on. Nobody is home.",
)

private val DerangedLabels = listOf(
    "New canvas",
    "Absolutely not",
    "One more?",
    "It's how you end up here",
    "Fine. One.",
    "New canvas, allegedly",
    "DO IT",
    "Don't blame me",
)

/** The gallery's empty state, deranged: the cursed mark wobbles, a googly bean bounces behind it, the button has opinions. */
@Composable
internal fun ChaosEmptyState(onNew: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "chaos")
    val wobble by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "wobble",
    )
    val beat by transition.animateFloat(
        initialValue = 1f, targetValue = 1.13f,
        animationSpec = infiniteRepeatable(tween(270), RepeatMode.Reverse),
        label = "beat",
    )
    var buttonIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            buttonIndex = (buttonIndex + 1) % DerangedLabels.size
        }
    }
    val tagline = remember { DerangedTaglines.random() }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        GooglyBean()
        Column(
            Modifier.align(Alignment.Center)
                .graphicsLayer { rotationZ = -1.6f },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.prostudio_mark),
                contentDescription = null,
                alpha = 0.95f,
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationZ = wobble * 6f
                        scaleX = beat
                        scaleY = beat
                    },
            )
            Text(
                "BroBeans",
                style = TextStyle(brush = Ps.Brand, fontSize = 36.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "S T U D I O",
                color = Ps.GalleryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                tagline,
                color = Ps.GalleryText,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
            Box(
                Modifier
                    .padding(top = 18.dp)
                    .height(44.dp)
                    .graphicsLayer { rotationZ = wobble * -1.6f }
                    .clip(RoundedCornerShape(22.dp))
                    .background(Ps.Accent)
                    .clickable { onNew() }
                    .padding(horizontal = 30.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    DerangedLabels[buttonIndex],
                    color = Color(0xFF26221F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A googly bean, bouncing off the walls forever, pupils wandering as they do. */
@Composable
private fun GooglyBean() {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            tick++
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val t = tick * 0.016f
        val beanW = 64.dp.toPx()
        val beanH = 46.dp.toPx()
        val margin = beanW
        val spanX = (size.width - margin).coerceAtLeast(1f)
        val spanY = (size.height - beanH).coerceAtLeast(1f)
        val x = bounce(t * 0.21f, spanX) + margin / 2f
        val y = bounce(t * 0.27f + 0.37f, spanY) + beanH / 2f
        val eyeR = 8.5.dp.toPx()
        val pupilR = 3.4.dp.toPx()
        val pupilWander = 2.6.dp.toPx()
        rotate(degrees = sin(t * 1.9f) * 26f, pivot = androidx.compose.ui.geometry.Offset(x, y)) {
            // body: the orange going mean, with a dark rim
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFF8A3D), Color(0xFFBB0037)),
                    center = androidx.compose.ui.geometry.Offset(x, y - beanH * 0.15f),
                    radius = beanW * 0.62f,
                ),
                topLeft = androidx.compose.ui.geometry.Offset(x - beanW / 2f, y - beanH / 2f),
                size = androidx.compose.ui.geometry.Size(beanW, beanH),
            )
            drawOval(
                Color(0xFF2A001A),
                topLeft = androidx.compose.ui.geometry.Offset(x - beanW / 2f, y - beanH / 2f),
                size = androidx.compose.ui.geometry.Size(beanW, beanH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()),
            )
            val eyeDx = beanW * 0.19f
            val eyeY = y - beanH * 0.08f
            for (side in listOf(-1f, 1f)) {
                val ex = x + side * eyeDx + sin(t * 2.3f + side) * 2.dp.toPx()
                drawCircle(Color.White, eyeR, androidx.compose.ui.geometry.Offset(ex, eyeY))
                val px = ex + sin(t * 1.35f + side * 2.1f) * pupilWander
                val py = eyeY + cos(t * 1.7f + side * 1.3f) * pupilWander
                drawCircle(Color(0xFF1B0A14), pupilR, androidx.compose.ui.geometry.Offset(px, py))
            }
            // a small, worried, straight line of a mouth
            drawLine(
                Color(0xFF1B0A14),
                androidx.compose.ui.geometry.Offset(x - 7.dp.toPx(), y + beanH * 0.24f),
                androidx.compose.ui.geometry.Offset(x + 7.dp.toPx(), y + beanH * 0.24f),
                2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/** triangle-wave bounce: 0..spanNorm..0, forever */
private fun bounce(u: Float, span: Float): Float {
    val period = 2f
    val m = ((u % period) + period) % period
    val f = if (m <= 1f) m else period - m
    return f * span
}

/** A half-circle pull-tab hugging the right edge. Tapping it summons the studio gremlin, who lives inside the app and was never asked. */
@Composable
internal fun GremlinTab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "gremlin-tab")
    val bob by transition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "bob",
    )
    Box(
        modifier
            .graphicsLayer { translationX = bob }
            .size(width = 30.dp, height = 96.dp)
            .clip(RoundedCornerShape(topStart = 48.dp, bottomStart = 48.dp))
            .background(Brush.radialGradient(listOf(Color(0xFFF7CAC9), Color(0xFFBB0037))))
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(topStart = 48.dp, bottomStart = 48.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(17.dp)) {
            drawCircle(Color.White, radius = size.minDimension / 2f, center = center)
            drawCircle(
                Color(0xFF1B0A14),
                radius = size.minDimension / 4.6f,
                center = center + androidx.compose.ui.geometry.Offset(2f, 1f),
            )
        }
    }
}

/** The studio gremlin: management, at last. Everything here is real. Do not press anything. */
@Composable
internal fun GremlinPanel(onClose: () -> Unit) {
    var beans by remember { mutableIntStateOf(4) }
    var taste by remember { mutableStateOf(true) }
    var moisture by remember { mutableStateOf(false) }
    var morals by remember { mutableStateOf(false) }
    var undoTheUndo by remember { mutableStateOf(true) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66100A12))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            Modifier
                .padding(end = 42.dp)
                .width(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF241F2B))
                .border(1.dp, Color(0x2EF7CAC9), RoundedCornerShape(20.dp))
                // a no-op click swallow: an empty tap on the panel goes nowhere (no ripple, no close)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(18.dp),
        ) {
            Text(
                "STUDIO GREMLIN",
                color = Color(0xFFF7CAC9),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                "management, at last",
                color = Color(0xFF8F897E),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(12.dp))
            GremlinToggle("Enable taste", taste) { taste = !taste }
            GremlinToggle("Moisture control", moisture) { moisture = !moisture }
            GremlinToggle("Moral compass", morals) { morals = !morals }
            GremlinToggle("Undo the undo", undoTheUndo) { undoTheUndo = !undoTheUndo }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Beans in stock", color = Color(0xFFCFC9BC), fontSize = 14.sp)
                Text("$beans", color = Color(0xFFFF8A3D), fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .height(46.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF6B4A))
                    .clickable { beans += 1 },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "DO NOT PRESS",
                    color = Color(0xFF1B0A14),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .height(40.dp)
                    .fillMaxWidth()
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "fine, go back to work",
                    color = Color(0xFFABA59A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GremlinToggle(label: String, on: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = Color(0xFFCFC9BC),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .size(42.dp, 24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (on) Color(0xFFBB0037) else Color(0xFF3A3542)),
            contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .padding(3.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7CAC9)),
            )
        }
    }
}
