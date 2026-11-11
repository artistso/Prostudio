package io.github.artistso.prostudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artistso.prostudio.canvas.CanvasViewModel

/**
 * Quick Help: the Glass Horizon's own manual, inked over the world — the six things
 * worth knowing at a glance, on a card of frosted plum. Tap the world outside to close.
 */
@Composable
fun QuickHelp(vm: CanvasViewModel) {
    BackHandler { vm.quickHelpOpen = false }
    Box(
        Modifier.fillMaxSize()
            .background(Color(0xA6000008))
            .pointerInput(Unit) { detectTapGestures { vm.quickHelpOpen = false } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(horizontal = 26.dp, vertical = 30.dp)
                .widthIn(max = 720.dp)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xE614000E))
                .border(1.dp, Glass.RimInner.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) { detectTapGestures { /* the card is the world: it doesn't close */ } }
                .padding(22.dp),
        ) {
            Text(
                "Quick Help",
                color = Glass.Paper,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                maxLines = 1,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                "GESTURES · CANVAS · TIMELINE · BRUSHES",
                color = Glass.RoseDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                maxLines = 1,
            )
            Spacer(Modifier.size(14.dp))
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpSectionCard(HelpSections[0], Modifier.weight(1f))
                    HelpSectionCard(HelpSections[1], Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpSectionCard(HelpSections[2], Modifier.weight(1f))
                    HelpSectionCard(HelpSections[3], Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpSectionCard(HelpSections[4], Modifier.weight(1f))
                    HelpSectionCard(HelpSections[5], Modifier.weight(1f))
                }
                // the gremlin writes his own section, uninvited, at the bottom
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1FBB0037))
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                ) {
                    Column {
                        Text(
                            "GREMLIN EDITION",
                            color = Glass.Paper,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            maxLines = 1,
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "If this app laughs at you, it was him. He means well. Probably. The half-circle tab on the right edge is his doorbell — ring it when the painting fights back. Beans in stock are legally binding. He has the belt. The eraser taped out in round 3. The enchantment is reversible. The buttons disagree.",
                            color = Glass.RoseDim,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.size(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Color(0x1FFFF0F3))
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.3f), CircleShape)
                        .clickable { vm.quickHelpOpen = false }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Text("Close", color = Glass.Paper, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                Spacer(Modifier.size(8.dp))
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Glass.AccentGradient)
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.6f), CircleShape)
                        .clickable { vm.quickHelpOpen = false; vm.gestureControlsOpen = true }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Text("Gesture controls", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

private data class HelpSection(val title: String, val items: List<String>)

private val HelpSections = listOf(
    HelpSection("Drawing", listOf(
        "A pen or a finger draws; the second finger is never a stroke.",
        "Hold a stroke still to straighten it — QuickShape.",
        "The StreamLine dial on the Line node smooths every stroke.",
    )),
    HelpSection("Stylus", listOf(
        "Hover shows the brush ring where the pen will land.",
        "Actions ▸ Prefs ▸ Advanced cursor settings sizes it.",
        "Every button and hold is remappable in Actions ▸ Gesture controls.",
    )),
    HelpSection("Timeline", listOf(
        "The perimeter board around the world jumps to any stop of the assist sequence.",
        "Tap the + stop to add a frame; held frames carry the rose dot.",
        "The rail at the foot plays and steps; tap a stop to be in it.",
    )),
    HelpSection("Layers", listOf(
        "Layers open from the bar; tap one to make it current.",
        "A stroke on a locked or hidden layer draws nothing — the app says why.",
    )),
    HelpSection("Gestures", listOf(
        "Two fingers pinch to scale, twist to turn the canvas.",
        "Two-finger tap undoes; three-finger tap redoes.",
        "Four-finger tap hides or shows the interface.",
        "Three-finger swipe down opens Copy & Paste.",
    )),
    HelpSection("This app", listOf(
        "Gesture controls turns every way of touching on or off.",
        "About BroBeans and the licences are under Actions ▸ Help.",
        "The light interface is under Actions ▸ Prefs ▸ Light interface.",
    )),
)

@Composable
private fun HelpSectionCard(section: HelpSection, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x14FFF0F3))
            .border(1.dp, Glass.RimInner.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Text(
            section.title.uppercase(),
            color = Glass.Paper,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            maxLines = 1,
        )
        Spacer(Modifier.size(6.dp))
        for (item in section.items) {
            Row(Modifier.padding(vertical = 2.dp)) {
                Text("·", color = Glass.RoseQuartz, fontSize = 13.sp, modifier = Modifier.padding(end = 6.dp))
                Text(item, color = Color(0xB8FFF0F3), fontSize = 12.5.sp, lineHeight = 15.sp)
            }
        }
    }
}
