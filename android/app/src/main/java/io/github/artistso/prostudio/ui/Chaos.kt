package io.github.artistso.prostudio.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
            GremlinToggle("Nacho justice", nachoJustice) { nachoJustice = !nachoJustice }
            GremlinToggle("Enchant all buttons", enchanted) { enchanted = !enchanted }
            GremlinToggle("Dark elf temperaments", darkElves) { darkElves = !darkElves }
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
