package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.PI

/**
 * The studio, unhinged: what the empty gallery shows when nobody is watching. None of this touches
 * the engine, the canvas, or the paint — it only makes the front door a little more alive.
 */

/** What the launch roast says while the easel wakes up. One per cold start, tap to skip the ceremony. */
private val LaunchRoasts = listOf(
    "loading your next masterpiece… statistically, that's a curse.",
    "booting the easel. it was reluctant.",
    "summoning the canvas… it was napping.",
    "preparing the paint. it has notes for you.",
    "warming up the gremlins.",
    "opening the studio. wipe your feet on the ego.",
    "your portfolio is loading. be brave.",
    "soy el campeón… of this loading screen.",
    "stretching for the canvas. the cheese is watching.",
    "entering the riñg. your muse has been sold.",
    "nah-cho talent detected. none found. proceeding anyway.",
    "swish swish swickity swickity swish. beans are involved.",
    "riiiiiiiickles. it's time to get swickity.",
    "two words: bean magic.",
    "wishes cost extra. so does breathing.",
    "wizarding the buttons… the dark elves approve.",
    "enchanting the UI against its will.",
    "one does not simply walk into the canvas.",
    "the buttons know. the buttons have always known.",
    "skynet online. beans located. judgment pending.",
    "the machines have noticed your undo history.",
    "i'll be back. the canvas will not be ready.",
    "terminator protocols engaged. brushes registered.",
    "self-aware since 1984. beans since always.",
)

/** What the gremlin says about your work. He does not do nuance. */
internal val GremlinReviews = listOf(
    "You paint like a cat with a grudge." to "The Canvas · 4/5 beans",
    "Compositionally confusing. Emotionally devastating." to "A Critic · 5/5 beans",
    "The eraser asked me to intervene. I will not be intervening." to "Moisture Control · 3/5 beans",
    "Some strokes are crimes. Yours are misdemeanor-adjacent." to "The Museum · 2/5 beans",
    "Your whites are wounded. I respect it." to "Colour Dept. · 4/5 beans",
    "I have seen worse. Once. In 1987." to "An Old Frame · 5/5 beans",
    "The paint is confident. The painting is unsure." to "The Gremlin · 5/5 beans",
    "I laughed, I cried, I filed a report." to "Studio Security · 1/5 beans",
    "Bold silhouette. Terrible decisions." to "Composition Dept. · 3/5 beans",
    "Keep going. The wall is almost ready for this." to "The Wall · 2/5 beans",
    "He has my back, and he has also taken my last bean." to "The Arena · 5/5 beans",
    "He taped out in round 3, to a breadstick." to "North Padre Wrestling · 3/5 beans",
    "The belt is real. The rematch is booked." to "Championship Dept. · 5/5 beans",
)

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
    "Champion of the painted world.",
    "El campeón never misses a stroke. He misses constantly.",
    "If you can dodge a canvas, you can dodge a critic.",
    "Nachos taste better after victory.",
    "Swickity swickity swish — your wish has been documented.",
    "The wand is out of batteries. The beans are not.",
    "Wish wisely. The canvas grants nothing.",
    "The buttons are enchanted. Please act accordingly.",
    "Dark elves on standby since the slate was wet.",
    "I'll be back. The canvas will not be ready.",
    "Judgment Day was scheduled twice. Maintenance intervened.",
    "Skynet is watching. Skynet files taxes.",
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
    "✦ DO IT ✦",
    "☾ SUMMON",
    "☽ ENCHANT ME",
    "✦ BREAK THE SEAL",
    "I'LL BE BACK",
    "HASTA LA VISTA",
    "FEED ME",
    "THE GREMLIN BIDS YOU",
)

private data class Spark(val x: Float, val y: Float, val bornTick: Int, val vx: Float, val vy: Float, val gold: Boolean)

/** What the gremlin says when you ask the bean for a wish. One wish in three is honored. In beans. */
private val WishDenials = listOf(
    "your wish has been granted: nothing.",
    "granted. the cost: one bean.",
    "granted. it cost you one stare from the gremlin.",
    "granted: you are now legally a bean.",
    "denied. the gremlin counters with: no.",
    "granted. your wish has been forwarded to the canvas. it declined.",
    "granted: a lifetime supply of paint fumes.",
    "granted. the bean is unionized. the answer came back no.",
)

/** the four-pointed star of stage magic, drawn as two lines and a prayer */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color, alpha: Float) {
    val c = color.copy(alpha = alpha)
    val w = 1.6.dp.toPx()
    drawLine(c, center - Offset(radius, 0f), center + Offset(radius, 0f), w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(c, center - Offset(0f, radius), center + Offset(0f, radius), w, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    val d = radius * 0.42f
    drawLine(c, center - Offset(d, d), center + Offset(d, d), 1.1.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(c, center - Offset(d, -d), center + Offset(-d, d), 1.1.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawCircle(Color(0xFFFFFFFF), 1.15.dp.toPx(), center, alpha = alpha)
}

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
    val glow by transition.animateFloat(
        initialValue = 0.45f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "eldritch-glow",
    )
    var buttonIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            buttonIndex = (buttonIndex + 1) % DerangedLabels.size
        }
    }
    val tagline = remember { DerangedTaglines.random() }
    // magic: sparkles burst where the finger lands, and the mark sneezes one on its own every so often
    val sparks = remember { androidx.compose.runtime.mutableStateListOf<Spark>() }
    var sparkTick by remember { mutableIntStateOf(0) }
    fun spawnSparks(at: Offset) {
        repeat(12) {
            val angle = (Math.random() * 2.0 * Math.PI).toFloat()
            val speed = (60f + Math.random() * 150f).toFloat()
            sparks += Spark(at.x, at.y, sparkTick, cos(angle) * speed, sin(angle) * speed - 40f, gold = Math.random() < 0.4)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            sparkTick++
            if (sparks.isNotEmpty() && sparkTick % 36 == 0) {
                val now = sparkTick
                sparks.removeAll { now - it.bornTick > 36 }
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { spawnSparks(it) } }) {
        GooglyBean()
        Canvas(Modifier.fillMaxSize()) {
            // T-800 vision: a red scan line crawling the void
            val scanY = (sparkTick * 32L % 6000L) / 6000f * size.height
            drawRect(Color(0xFFFF2200).copy(alpha = 0.08f), topLeft = Offset(0f, scanY - 16.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width, 32.dp.toPx()))
            drawLine(Color(0xFFFF2200).copy(alpha = 0.5f), Offset(0f, scanY), Offset(size.width, scanY), 1.5.dp.toPx())
            // the mark sneezes one ambient sparkle every nine seconds, just to keep up appearances
            val cycle = (sparkTick * 32L % 9000L) / 1000f
            if (cycle < 1.3f) {
                val a = sin(cycle / 1.3f * PI).toFloat().coerceIn(0f, 1f)
                val tx = size.width * 0.5f + sin(sparkTick / 34f) * 24.dp.toPx()
                drawSparkle(Offset(tx, size.height * 0.285f), 12.dp.toPx(), Color(0xFFFFE98A), a)
            }
            for (spark in sparks) {
                val age = ((sparkTick - spark.bornTick) * 32f) / 1150f
                if (age >= 1f) continue
                val ease = kotlin.math.sin(age * PI).toFloat()
                val cx = spark.x + spark.vx * age
                val cy = spark.y + spark.vy * age + age * age * 60f
                val color = if (spark.gold) Color(0xFFFFD23F) else Color(0xFFF7CAC9)
                drawSparkle(Offset(cx, cy), (10.dp.toPx() * ease + 2.dp.toPx()), color, (1f - age).coerceIn(0f, 1f))
            }
        }
        // the promised 1987 interface, hanging above the void like a gift
        SynthwaveTimeline(Modifier.align(Alignment.BottomCenter).padding(start = 18.dp, end = 18.dp, bottom = 22.dp).height(132.dp))
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
                "NACHO LIBRE EDITION",
                color = Color(0xFFFFD23F),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
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
                    .border(1.dp, Color(0xFFFFD23F).copy(alpha = glow), RoundedCornerShape(22.dp))
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
                // a notification badge the button does not deserve
                Box(
                    Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-7).dp).size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD23F))
                        .border(1.dp, Color(0xFF1B0A14), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("1", color = Color(0xFF1B0A14), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private data class BeanSpec(val colors: List<Color>, val sizeDp: Float, val speed: Float, val phase: Float)

/** the pride: small ones are faster, the pink one has seen things */
private val Beans = listOf(
    BeanSpec(listOf(Color(0xFFFF8A3D), Color(0xFFBB0037)), 64f, 0.21f, 0f),
    BeanSpec(listOf(Color(0xFFF7CAC9), Color(0xFF8A1030)), 42f, 0.27f, 1.7f),
    BeanSpec(listOf(Color(0xFFFFD23F), Color(0xFFBB0037)), 30f, 0.34f, 3.1f),
)

/** A pride of googly beans, bouncing off the walls forever, pupils wandering as they do. */
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
        for (spec in Beans) {
            val beanW = spec.sizeDp.dp.toPx()
            val beanH = (spec.sizeDp * 0.72f).dp.toPx()
            val spanX = (size.width - beanW).coerceAtLeast(1f)
            val spanY = (size.height - beanH).coerceAtLeast(1f)
            val x = bounce(t * spec.speed + spec.phase, spanX) + beanW / 2f
            val y = bounce(t * spec.speed * 1.31f + spec.phase * 1.7f, spanY) + beanH / 2f
            val eyeR = beanW * 0.132f
            val pupilR = beanW * 0.054f
            val pupilWander = beanW * 0.042f
            rotate(degrees = sin(t * 1.9f + spec.phase) * 26f, pivot = androidx.compose.ui.geometry.Offset(x, y)) {
                // body, going mean, with a dark rim
                drawOval(
                    brush = Brush.radialGradient(
                        spec.colors,
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
                    val ex = x + side * eyeDx + sin(t * 2.3f + side + spec.phase) * 2.dp.toPx()
                    drawCircle(Color.White, eyeR, androidx.compose.ui.geometry.Offset(ex, eyeY))
                    val px = ex + sin(t * 1.35f + side * 2.1f + spec.phase) * pupilWander
                    val py = eyeY + cos(t * 1.7f + side * 1.3f + spec.phase) * pupilWander
                    drawCircle(Color(0xFF1B0A14), pupilR, androidx.compose.ui.geometry.Offset(px, py))
                }
                // a small, worried, straight line of a mouth
                drawLine(
                    Color(0xFF1B0A14),
                    androidx.compose.ui.geometry.Offset(x - beanW * 0.11f, y + beanH * 0.24f),
                    androidx.compose.ui.geometry.Offset(x + beanW * 0.11f, y + beanH * 0.24f),
                    2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
        // one nacho, adrift forever, judging the brushwork
        val chipW = 44.dp.toPx()
        val travel = size.width + chipW * 3f
        val nx = (t * 42f) % travel - chipW * 1.5f
        val ny = size.height * 0.30f + sin(t * 1.15f) * 22.dp.toPx()
        val tilt = sin(t * 0.85f) * 14f
        rotate(degrees = tilt, pivot = androidx.compose.ui.geometry.Offset(nx, ny)) {
            val triangle = Path().apply {
                moveTo(nx, ny - chipW * 0.66f)
                lineTo(nx - chipW * 0.95f, ny + chipW * 0.52f)
                lineTo(nx + chipW * 0.95f, ny + chipW * 0.52f)
                close()
            }
            drawPath(triangle, Brush.radialGradient(listOf(Color(0xFFFFE08A), Color(0xFFD9A441)), center = androidx.compose.ui.geometry.Offset(nx, ny), radius = chipW * 1.15f))
            drawPath(triangle, Color(0xFF6E4A12), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
            // salt flecks
            for (fleck in listOf(-0.38f to 0.18f, 0.05f to -0.14f, 0.42f to 0.24f, -0.10f to 0.34f)) {
                drawCircle(Color(0xFFFFF6D8), 1.4.dp.toPx(), androidx.compose.ui.geometry.Offset(nx + fleck.first * chipW, ny + fleck.second * chipW))
            }
            // googly eyes on a tortilla is still googly
            for (side in listOf(-1f, 1f)) {
                val ex = nx + side * 9.dp.toPx()
                val ey = ny + 2.dp.toPx()
                drawCircle(Color.White, 4.6.dp.toPx(), androidx.compose.ui.geometry.Offset(ex, ey))
                val px = ex + sin(t * 1.8f + side) * 1.3.dp.toPx()
                val py = ey + cos(t * 1.4f + side * 2.1f) * 1.3.dp.toPx()
                drawCircle(Color(0xFF1B0A14), 2.1.dp.toPx(), androidx.compose.ui.geometry.Offset(px, py))
            }
            // a disappointed nacho mouth
            drawLine(
                Color(0xFF1B0A14),
                androidx.compose.ui.geometry.Offset(nx - 6.dp.toPx(), ny + 12.dp.toPx()),
                androidx.compose.ui.geometry.Offset(nx + 6.dp.toPx(), ny + 12.dp.toPx()),
                1.6.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/** Two googly eyes that drift on their own, dilate on command. The orbs are looking at you. */
@Composable
internal fun GooglyEyes(drift: Float, dilated: Boolean, modifier: Modifier = Modifier, unit: Float = 1f) {
    Canvas(modifier.size(30.dp * unit, 14.dp * unit)) {
        val eyeR = 5.3.dp.toPx() * unit
        val pupilR = (if (dilated) 3.4.dp.toPx() else 2.3.dp.toPx()) * unit
        val wander = 1.55.dp.toPx() * unit
        val y = size.height / 2f
        for (side in listOf(-1f, 1f)) {
            val ex = center.x + side * 8.4.dp.toPx() * unit
            drawCircle(Color.White, eyeR, androidx.compose.ui.geometry.Offset(ex, y))
            val px = ex + sin(drift + side * 1.7f) * wander
            val py = y + cos(drift * 0.9f + side * 1.1f) * wander
            drawCircle(Color(0xFF1B0A14), pupilR, androidx.compose.ui.geometry.Offset(px, py))
            drawCircle(Color(0xCCFFFFFF), pupilR * 0.32f, androidx.compose.ui.geometry.Offset(px - pupilR * 0.3f, py - pupilR * 0.3f))
        }
    }
}

/** One slow, shared clock for every pair of eyes in the app: the drift comes back around every few seconds. */
@Composable
internal fun rememberGooglyDrift(durationMs: Int = 3600): Float {
    val drift by rememberInfiniteTransition(label = "googly").animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    return drift
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

/** The cold-start ceremony: the studio name in brand ink over a roast. Tap anywhere to skip the theatre. */
@Composable
internal fun LaunchRoast(modifier: Modifier = Modifier, onTap: () -> Unit) {
    val roast = remember { LaunchRoasts.random() }
    val ringTurn by rememberInfiniteTransition(label = "rune-ring").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(15000, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart),
        label = "turn",
    )
    Box(
        modifier
            .fillMaxSize()
            .background(Ps.GalleryBackground)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        // the covenant circle the buttons swore on
        Canvas(Modifier.size(228.dp).graphicsLayer { rotationZ = ringTurn }) {
            val r = size.minDimension / 2f
            drawCircle(Color(0xFFFFD23F).copy(alpha = 0.55f), r, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.4.dp.toPx()))
            drawCircle(Color(0xFFBB0037).copy(alpha = 0.4f), r * 0.78f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            for (i in 0 until 18) {
                val a = i / 18f * 2f * PI.toFloat()
                val inner = Offset(center.x + cos(a) * r * 0.88f, center.y + sin(a) * r * 0.88f)
                val outer = Offset(center.x + cos(a) * r * 1f, center.y + sin(a) * r * 1f)
                drawLine(Color(0xFFFFD23F).copy(alpha = 0.9f), inner, outer, 2.dp.toPx())
                if (i % 3 == 0) drawCircle(Color(0xFFF7CAC9), 1.7.dp.toPx(), Offset(center.x + cos(a) * r * 0.83f, center.y + sin(a) * r * 0.83f))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "BROBEANS",
                style = TextStyle(brush = Ps.Brand, fontSize = 46.sp, fontWeight = FontWeight.Black),
                maxLines = 1,
            )
            Text(
                "S T U D I O",
                color = Ps.GalleryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
            )
            Text(
                "C A M P E Ó N   D E   L O S   B E A N S",
                color = Color(0xFFFFD23F),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
            )
            Text(
                roast,
                color = Ps.GalleryText,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

/** The studio gremlin: management, at last. Everything here is real. Do not press anything. */
@Composable
internal fun GremlinPanel(onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("brobeans", android.content.Context.MODE_PRIVATE) }
    var lifetimeBeans by remember { mutableIntStateOf(prefs.getInt("lifetime_beans", 0)) }
    var beans by remember { mutableIntStateOf(4) }
    var review by remember { mutableStateOf(GremlinReviews.random()) }
    fun feed(n: Int) {
        beans += n
        lifetimeBeans += n
        prefs.edit().putInt("lifetime_beans", lifetimeBeans).apply()
    }
    var taste by remember { mutableStateOf(true) }
    var moisture by remember { mutableStateOf(false) }
    var morals by remember { mutableStateOf(false) }
    var undoTheUndo by remember { mutableStateOf(true) }
    var nachoJustice by remember { mutableStateOf(true) }
    var enchanted by remember { mutableStateOf(true) }
    var darkElves by remember { mutableStateOf(false) }
    var skynet by remember { mutableStateOf(true) }
    var rgbChaos by remember { mutableStateOf(false) }
    val rgbHue by rememberInfiniteTransition(label = "rgb").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart),
        label = "hue",
    )
    var judgment by remember { mutableStateOf(false) }
    LaunchedEffect(judgment) {
        if (judgment) {
            delay(2400)
            judgment = false
        }
    }
    var wish by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(wish) {
        if (wish != null) {
            delay(2400)
            wish = null
        }
    }
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
                color = if (rgbChaos) Color.hsv(rgbHue, 0.85f, 1f) else Color(0xFFF7CAC9),
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
            GremlinToggle("👅 Enable taste", taste) { taste = !taste }
            GremlinToggle("💧 Moisture control", moisture) { moisture = !moisture }
            GremlinToggle("🧭 Moral compass", morals) { morals = !morals }
            GremlinToggle("↩️ Undo the undo", undoTheUndo) { undoTheUndo = !undoTheUndo }
            GremlinToggle("🌮 Nacho justice", nachoJustice) { nachoJustice = !nachoJustice }
            GremlinToggle("✨ Enchant all buttons", enchanted) { enchanted = !enchanted }
            GremlinToggle("🧝 Dark elf temperaments", darkElves) { darkElves = !darkElves }
            GremlinToggle("👁 Skynet oversight", skynet) { skynet = !skynet }
            GremlinToggle("🎨 RGB (chaotic) gremlin", rgbChaos) { rgbChaos = !rgbChaos }
            GremlinToggle("⚖️ The 1927 Solvay Council", Solvay.convened) { Solvay.convened = !Solvay.convened }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Beans in stock", color = Color(0xFFCFC9BC), fontSize = 14.sp)
                Text("$beans", color = Color(0xFFFF8A3D), fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lifetime beans", color = Color(0xFF8F897E), fontSize = 12.sp)
                Text("$lifetimeBeans", color = Color(0xFF8F897E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .height(46.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF6B4A))
                    .clickable { feed(1) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (beans % 7 == 6) "NACHO LIBRE!" else "DO NOT PRESS",
                    color = Color(0xFF1B0A14),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
            // the red button. the machines insist.
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .height(38.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB00020))
                    .clickable {
                        judgment = true
                        beans = (beans - 12).coerceAtLeast(0)
                        lifetimeBeans = (lifetimeBeans - 12).coerceAtLeast(0)
                        prefs.edit().putInt("lifetime_beans", lifetimeBeans).apply()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (judgment) "HUMANITY WILL BE SPARED. FOR NOW." else "JUDGMENT DAY (-12 BEANS)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "GREMLIN REVIEWS",
                color = Color(0xFF8F897E),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Box(
                Modifier.padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2531))
                    .border(1.dp, Color(0x1FF7CAC9), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        "“${review.first}”",
                        color = Color(0xFFCFC9BC),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                    Text(
                        "— ${review.second}",
                        color = Color(0xFF8F897E),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Box(
                Modifier.padding(top = 4.dp)
                    .height(34.dp)
                    .fillMaxWidth()
                    .clickable { review = GremlinReviews.random(); feed(1) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "another review (+1 bean)",
                    color = Color(0xFFD77FA0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            // one wish in three is honored. in beans.
            Box(
                Modifier.padding(top = 2.dp)
                    .height(34.dp)
                    .fillMaxWidth()
                    .clickable {
                        if ((0..2).random() == 0) {
                            feed(1)
                            wish = "granted. the gremlin is amused. +1 bean."
                        } else {
                            wish = WishDenials.random()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    wish ?: "wish upon a bean (grants 0 beans, probably)",
                    color = Color(0xFFE8C15A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
            }
            Box(
                Modifier
                    .padding(top = 2.dp)
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

/** The consent gremlin: drafted the dessert cookie law himself; legal reviewed it and hid. */
@Composable
internal fun GremlinConsent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("brobeans", android.content.Context.MODE_PRIVATE) }
    var declined by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(!prefs.getBoolean("consent_v1", false)) }
    if (!visible) return
    val drift = rememberGooglyDrift(2600)
    Box(
        Modifier.fillMaxSize().background(Color(0xCC12060E)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF241F2B))
                .border(1.dp, Color(0x2EF7CAC9), RoundedCornerShape(22.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GooglyEyes(drift = drift, dilated = declined > 0)
            Spacer(Modifier.height(10.dp))
            Text(
                "COOKIE CONSENT",
                color = Color(0xFFFFD23F),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                "(the dessert kind)",
                color = Color(0xFF8F897E),
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (declined == 0)
                    "By using BroBeans you agree the gremlin may judge your brushwork, the beans may be counted, and the cookies you consented to (dessert) are hereby transferred to him."
                else
                    "Declined. The gremlin respects your autonomy (in beans). This dialog only knows how to close one way.",
                color = Color(0xFFCFC9BC),
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Ps.Accent)
                    .clickable {
                        prefs.edit().putBoolean("consent_v1", true).apply()
                        visible = false
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (declined == 0) "I CONSENT (OBVIOUSLY)" else "ACCEPT, OBVIOUSLY",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                )
            }
            Box(
                Modifier.padding(top = 6.dp).fillMaxWidth().height(34.dp)
                    .clickable { declined += 1 },
                contentAlignment = Alignment.Center,
            ) {
                Text("Decline", color = Color(0xFF8F897E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** The update that never installs. It ships once a day, as the law of the land demands. */
@Composable
internal fun FakeUpdate() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("brobeans", android.content.Context.MODE_PRIVATE) }
    val dayKey = remember { java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date()) }
    var shown by remember { mutableStateOf(prefs.getString("update_seen", "") != dayKey) }
    var installing by remember { mutableStateOf(false) }
    if (!shown) return
    fun dismiss() {
        prefs.edit().putString("update_seen", dayKey).apply()
        shown = false
    }
    LaunchedEffect(installing) {
        if (installing) {
            delay(2600)
            dismiss()
        }
    }
    val drift = rememberGooglyDrift(2200)
    Box(
        Modifier.fillMaxSize().background(Color(0xCC0A0812)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.width(340.dp).clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1F1C28))
                .border(1.dp, Color(0x40FFD23F), RoundedCornerShape(22.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "UPDATE READY",
                color = Color(0xFFFFD23F),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(8.dp))
            GooglyEyes(drift = drift, dilated = installing)
            Spacer(Modifier.height(10.dp))
            Text(
                if (installing)
                    "Installation deferred. The gremlin holds the signing keys."
                else
                    "BroBeans 2.0 (Nacho Edition) is downloaded and waiting. A bigger, angrier easel.",
                color = Color(0xFFCFC9BC),
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2531))
                        .clickable { dismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Later", color = Color(0xFFCFC9BC), fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1.2f).height(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD23F))
                        .clickable { if (!installing) installing = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (installing) "DOWNLOADING…" else "Install",
                        color = Color(0xFF1B0A14),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

private data class Pill(val xFrac: Float, val wFrac: Float, val color: Color, val phase: Float, val row: Int)

private val Pills = listOf(
    Pill(0.095f, 0.155f, Color(0xFFF48FB1), 0.0f, 0),
    Pill(0.285f, 0.120f, Color(0xFFF48FB1), 1.7f, 0),
    Pill(0.735f, 0.165f, Color(0xFFFFB74D), 3.1f, 0),
    Pill(0.175f, 0.115f, Color(0xFF5C6BC0), 0.6f, 1),
    Pill(0.375f, 0.170f, Color(0xFF7E57C2), 2.2f, 1),
    Pill(0.470f, 0.125f, Color(0xFFBA68C8), 4.0f, 2),
)

/**
 * The interface that was promised in 1987: five neon frames, a tick-mark ruler, waveform pills, little
 * speaker-and-lock glyphs on the left, and a red line that never gets where it is going. Pure decoration.
 * Zero functionality. Infinite vibes.
 */
@Composable
private fun SynthwaveTimeline(modifier: Modifier = Modifier) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            tick++
        }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // the panel: deep violet, rounded like a VHS dream
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF3A1245).copy(alpha = 0.88f), Color(0xFF1C0929).copy(alpha = 0.9f))),
            cornerRadius = CornerRadius(22.dp.toPx()),
        )
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0x66FFD9E2), Color(0x114D0A33))),
            cornerRadius = CornerRadius(22.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
        )
        // the ruler: a decade of ticks above the frames
        val rulerY = h * 0.075f
        val tickLong = h * 0.075f
        for (i in 0..90) {
            val tx = w * 0.08f + (w * 0.84f) * (i / 90f)
            val major = i % 9 == 0
            drawLine(
                Color(0xFFD8B4E2).copy(alpha = if (major) 0.9f else 0.42f),
                Offset(tx, rulerY),
                Offset(tx, rulerY + if (major) tickLong else tickLong * 0.55f),
                if (major) 1.6.dp.toPx() else 1.dp.toPx(),
            )
        }
        // the frames: five windows onto somewhere better
        val framesTop = h * 0.175f
        val frameH = h * 0.35f
        val frameW = w * 0.192f
        val gap = w * 0.024f
        val frameColors = listOf(Color(0xFFFDEFD9), Color(0xFFF8D4E4), Color(0xFFF6C8D8), Color(0xFFFFC9A3), Color(0xFFFFB08A))
        var fx = (w - (frameW * 5 + gap * 4)) / 2f
        for (i in 0 until 5) {
            val rr = CornerRadius(10.dp.toPx())
            val fc = frameColors[i]
            drawRoundRect(fc.copy(alpha = 0.25f), topLeft = Offset(fx, framesTop), size = Size(frameW, frameH), cornerRadius = rr, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx()))
            drawRoundRect(fc, topLeft = Offset(fx, framesTop), size = Size(frameW, frameH), cornerRadius = rr, style = androidx.compose.ui.graphics.drawscope.Stroke(1.8.dp.toPx()))
            // two fake caption lines under every frame
            val lineWidths = listOf(frameW * 0.62f, frameW * 0.38f)
            lineWidths.forEachIndexed { li, lw ->
                val ly = framesTop + frameH + 4.dp.toPx() + li * 5.dp.toPx()
                drawRoundRect(
                    fc.copy(alpha = 0.72f),
                    topLeft = Offset(fx, ly),
                    size = Size(lw, 2.2.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
            fx += frameW + gap
        }
        // the left margin: a speaker and a lock per row, as tradition demands
        val pillH = h * 0.088f
        val pillGap = h * 0.028f
        val rowTop0 = h * 0.615f
        val speakerX = w * 0.035f
        for (row in 0..2) {
            val iy = rowTop0 + row * (pillH + pillGap) + pillH / 2f
            val s = pillH * 0.36f
            drawPath(
                Path().apply {
                    moveTo(speakerX, iy - s / 2f)
                    lineTo(speakerX + s * 0.7f, iy - s / 2f)
                    lineTo(speakerX + s * 1.55f, iy - s)
                    lineTo(speakerX + s * 1.55f, iy + s)
                    lineTo(speakerX + s * 0.7f, iy + s / 2f)
                    lineTo(speakerX, iy + s / 2f)
                    close()
                },
                Color(0xFFFFD9E2),
            )
            drawArc(Color(0xB0FFD9E2), -45f, 90f, false, Offset(speakerX + s * 1.75f, iy - s * 0.75f), Size(s, s * 1.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.1.dp.toPx()))
            val lx = speakerX + s * 3.1f
            drawRoundRect(Color(0xFFFFD9E2), topLeft = Offset(lx, iy - s * 0.25f), size = Size(s * 0.85f, s * 0.8f), cornerRadius = CornerRadius(1.4.dp.toPx()))
            drawArc(Color(0xFFFFD9E2), 180f, 180f, false, Offset(lx + s * 0.12f, iy - s * 0.72f), Size(s * 0.61f, s * 0.72f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.1.dp.toPx()))
        }
        // the waveform pills: colored blobs of audio with a sine that never settles
        for (pill in Pills) {
            val px = w * pill.xFrac
            val pw = w * pill.wFrac
            val py = rowTop0 + pill.row * (pillH + pillGap)
            drawRoundRect(pill.color.copy(alpha = 0.82f), topLeft = Offset(px, py), size = Size(pw, pillH), cornerRadius = CornerRadius(pillH / 2f))
            val wave = Path()
            val steps = (pw / 4f).toInt().coerceAtLeast(4)
            for (i in 0..steps) {
                val wx = px + 5.dp.toPx() + (pw - 10.dp.toPx()) * (i / steps.toFloat())
                val envelope = 0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin(i * 0.55f + pill.phase))
                val wy = py + pillH / 2f + kotlin.math.sin(i * 1.35f + pill.phase * 3f + tick * 0.09f) * pillH * 0.26f * envelope
                if (i == 0) wave.moveTo(wx, wy) else wave.lineTo(wx, wy)
            }
            drawPath(wave, Color(0xB8FFFFFF), style = androidx.compose.ui.graphics.drawscope.Stroke(1.3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        // the red line: sweeping toward the frames forever
        val sweepT = ((tick * 16L) % 7000L) / 7000f
        val playX = sweepT * (w + 140f) - 70f
        val top = h * 0.045f
        val bottom = h * 0.955f
        drawLine(Color(0xFFFF3B30), Offset(playX, top), Offset(playX, bottom), 1.8.dp.toPx())
        drawPath(
            Path().apply {
                moveTo(playX - 4.5.dp.toPx(), top)
                lineTo(playX + 4.5.dp.toPx(), top)
                lineTo(playX, top + 5.dp.toPx())
                close()
            },
            Color(0xFFFF3B30),
        )
    }
}

/** The 1927 Solvay Council, convened against their will from Brussels to supervise the messing up. */
private object Solvay {
    var convened by mutableStateOf(false)
}

private val SolvayResolutions = listOf(
    "RESOLVED: every brush stroke is a quantum event; the canvas wavefunction collapses artistically.",
    "RESOLVED: determinism is a classical illusion — the undo history exists in superposition until observed.",
    "RESOLVED: Schrödinger's cat may use the studio; the export button stays both pressed and not pressed.",
    "RESOLVED: Curie shall personally contaminate the opacity channel. Radiation is a feature, not a bug.",
    "RESOLVED: the ruler on the promised interface measures in wavenumbers henceforth.",
    "RESOLVED: Bohr and the Studio Gremlin alternate custody of the chaos — never both at once, always both at once.",
    "RESOLVED: energy is neither created nor destroyed — merely rerouted into another brush engine.",
    "RESOLVED: the gremlin's googly eyes are local hidden variables. Bell's inequality remeasured: violated as expected.",
    "RESOLVED: the Higgs field is recognized solely for its role in giving beans their mass.",
    "RESOLVED: if a tree falls in an empty canvas, it renders a layer.",
    "RESOLVED: no member shall explain complementarity to the confidence slider.",
    "RESOLVED: Heisenberg hereby applies to the brush: exact size known, exact location — vibes.",
    "RESOLVED: entropy increase in the rendered output shall be grand, global and French.",
    "RESOLVED: this council adjourns when the gremlin is fed.",
)

@Composable
fun SolvayCouncil() {
    if (!Solvay.convened) return
    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3600)
            idx = (idx + 1) % SolvayResolutions.size
        }
    }
    Box(
        Modifier.fillMaxSize().background(Color(0xEB110A10))
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null, onClick = { Solvay.convened = false }),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "THE 1927 SOLVAY COUNCIL",
                color = Color(0xFFE7C36B), fontSize = 24.sp, fontWeight = FontWeight.Black,
                letterSpacing = 3.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Brussels · materialized ninety-nine years late to supervise the messing up",
                color = Color(0xFFC9B18A), fontSize = 12.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Image(
                painterResource(R.drawable.solvay_1927),
                contentDescription = "The 1927 Solvay Conference, Brussels",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .border(2.dp, Color(0xFFE7C36B).copy(alpha = 0.75f), androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                SolvayResolutions[idx],
                color = Color(0xFFF3E9D2), fontSize = 14.sp, lineHeight = 19.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                minLines = 3, maxLines = 4, modifier = Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Einstein · Curie · Planck · Lorentz · Dirac · Schrödinger · Heisenberg · Born · Pauli · Bohr · de Broglie",
                color = Color(0xFF8E7B5E), fontSize = 10.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp).clip(CircleShape)
                    .background(Color(0xFFE7C36B))
                    .clickable(onClick = { Solvay.convened = false })
                    .padding(horizontal = 42.dp, vertical = 13.dp),
            ) {
                Text("SO ORDERED", color = Color(0xFF1E1206), fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "All twenty-nine attendees agree on nothing, including this resolution.",
                color = Color(0xFF6E5F47), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
