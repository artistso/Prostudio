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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
        "About ProStudio and the licences are under Actions ▸ Help.",
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

/**
 * InkFrame's `#studio` About overlay (`GH-029`): a frosted-glass panel over the stage with
 * ProStudio's identity, 2×2 metadata grid, expandable privacy & license disclosures, and credits.
 */
@Composable
fun StudioAbout(vm: CanvasViewModel) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "${info.versionName ?: "0.1.0"} (${androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(info)})"
        }.getOrDefault("0.1.0")
    }
    StudioAboutCard(
        versionText = version,
        onQuickHelp = {
            vm.studioAboutOpen = false
            vm.quickHelpOpen = true
        },
        onRecover = null,
        onClose = { vm.studioAboutOpen = false },
    )
}

@Composable
fun StudioAboutCard(
    versionText: String,
    onQuickHelp: (() -> Unit)? = null,
    onRecover: (() -> Unit)? = null,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    var privacyOpen by remember { mutableStateOf(false) }
    var licenseOpen by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxSize()
            .background(Color(0xA6000008))
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(horizontal = 26.dp, vertical = 28.dp)
                .widthIn(max = 560.dp)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Glass.Deep.copy(alpha = 0.92f))
                .background(Glass.Gradient)
                .border(1.dp, Glass.RimInner.copy(alpha = 0.42f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) { detectTapGestures { /* absorb inside card */ } }
                .padding(24.dp),
        ) {
            Text(
                "PROSTUDIO",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = 5.sp,
                style = TextStyle(brush = Glass.TitleGradient),
                maxLines = 1,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                "THE GLASS HORIZON · A CALM STUDIO",
                color = Glass.RoseDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                maxLines = 1,
            )
            Spacer(Modifier.size(14.dp))
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "ProStudio is a professional drawing & animation studio.",
                    color = Glass.Paper,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Free & ad-free. No account. No data collected. No AI in your artwork.",
                    color = Glass.Paper.copy(alpha = 0.80f),
                    fontSize = 12.5.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudioMetaCell("Version", "$versionText · Rust + Vulkan", Modifier.weight(1f))
                    StudioMetaCell("Runtime", "Offline-first native Rust + Compose", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudioMetaCell("Android", "io.github.artistso.prostudio · Stylus", Modifier.weight(1f))
                    StudioMetaCell("License", "MIT · free to use, modify, and share", Modifier.weight(1f))
                }
                StudioDisclosure(
                    title = "Privacy",
                    open = privacyOpen,
                    onToggle = { privacyOpen = !privacyOpen },
                    body = "ProStudio stores your artwork, brushes, palettes, and recovery snapshots locally on this device. There is no account system, no telemetry, and no network access during studio work.",
                )
                StudioDisclosure(
                    title = "License",
                    open = licenseOpen,
                    onToggle = { licenseOpen = !licenseOpen },
                    body = "Released under the MIT License. You may use, copy, modify, merge, publish, distribute, sublicense, and sell copies, with the copyright and license notice included.",
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(Glass.RimInner.copy(alpha = 0.22f)))
                Text(
                    "Made by Steven Owens · @soquarky\nBuilt with Arena.ai Agent Mode · arena.ai",
                    color = Glass.Paper.copy(alpha = 0.74f),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                )
            }
            Spacer(Modifier.size(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (onRecover != null) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0x1FFFF0F3))
                            .border(1.dp, Glass.RimInner.copy(alpha = 0.35f), CircleShape)
                            .clickable(onClick = onRecover)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("Gallery Recovery", color = Glass.Paper, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                    Spacer(Modifier.size(8.dp))
                }
                if (onQuickHelp != null) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(Color(0x1FFFF0F3))
                            .border(1.dp, Glass.RimInner.copy(alpha = 0.35f), CircleShape)
                            .clickable(onClick = onQuickHelp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("Quick Help", color = Glass.Paper, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                    Spacer(Modifier.size(8.dp))
                }
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Glass.AccentGradient)
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.6f), CircleShape)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text("Close", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StudioMetaCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x14FFF0F3))
            .border(1.dp, Glass.RimInner.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            label.uppercase(),
            color = Glass.RoseDim,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
            maxLines = 1,
        )
        Spacer(Modifier.size(3.dp))
        Text(value, color = Glass.Paper, fontSize = 12.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun StudioDisclosure(title: String, open: Boolean, onToggle: () -> Unit, body: String) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x12FFF0F3))
            .border(1.dp, Glass.RimInner.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Glass.Paper, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(if (open) "▾" else "▸", color = Glass.RoseQuartz, fontSize = 12.sp)
        }
        if (open) {
            Spacer(Modifier.size(6.dp))
            Text(body, color = Glass.Paper.copy(alpha = 0.80f), fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

/** A frosted Glass Horizon text sheet (used for Licences and long-form notices instead of stock Material3 AlertDialog). */
@Composable
fun GlassTextDialog(title: String, body: String, onClose: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(
            Modifier.widthIn(max = 540.dp)
                .heightIn(max = 560.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Glass.Deep.copy(alpha = 0.94f))
                .background(Glass.Gradient)
                .border(1.dp, Glass.RimInner.copy(alpha = 0.42f), RoundedCornerShape(22.dp))
                .padding(22.dp),
        ) {
            Text(
                title.uppercase(),
                color = Glass.Paper,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                maxLines = 1,
            )
            Spacer(Modifier.size(12.dp))
            Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Text(body, color = Glass.Paper.copy(alpha = 0.84f), fontSize = 12.5.sp, lineHeight = 17.sp)
            }
            Spacer(Modifier.size(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(Glass.AccentGradient)
                        .border(1.dp, Glass.RimInner.copy(alpha = 0.6f), CircleShape)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text("Done", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}
