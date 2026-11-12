package io.github.artistso.prostudio.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ProStudio's interface colours and sizes. Sizes in dp equal the measured points.
 *
 * The identity: a darkroom at night. Warm ink panels lifted on a violet-charcoal black, paper-warm
 * words, and one accent doing all the talking — ember, the brand's warm end, reached for whenever
 * something is chosen or possible. The light interface (Prefs) is the same room with the lamps on:
 * warm paper and deep ink, the same ember a shade deeper so it still reads.
 */
object Ps {
    /** The light interface (Prefs) is on: the same room with the lamps on, not a grey theme. */
    var light by mutableStateOf(false)

    /** A colour for the interface as it is now: [dark], or [light] when the light interface is on. */
    private fun pick(dark: Long, light: Long) = Color(if (this.light) light else dark)

    val Panel: Color get() = pick(0xF226222E, 0xF7ECE9E2)
    val Row: Color get() = pick(0xFF332F3B, 0xFFDDDAD1)
    val RowSelected: Color get() = if (light) Color(0xFFA00030) else Glass.Accent
    val AccentRose: Color get() = Glass.Accent
    val DeepPlum: Color get() = Glass.Deep
    val Accent: Color get() = if (light) Color(0xFFA00030) else Glass.Accent
    /** The brand blend: blush into accent rose into deep plum. For the rare stroke that says where you are. */
    val Brand: Brush get() = Brush.linearGradient(listOf(Glass.Highlight, Glass.Accent, Glass.ShadowPlum))
    val Text: Color get() = pick(0xFFF5F1E8, 0xFF26221F)
    val TextDim: Color get() = pick(0x99F5F1E8, 0x9926221F)
    val Disabled: Color get() = pick(0x4DF5F1E8, 0x4D26221F)
    val Divider: Color get() = pick(0x26F5F1E8, 0x1F26221F)
    val Surround: Color get() = Glass.Deep

    // the bar across the top of the canvas
    /** The bar's ink, translucent, so the canvas shows through. */
    val Bar: Color get() = pick(0x29F7CAC9, 0xCFF5E4DE)
    val BarText: Color get() = pick(0xFFA8A294, 0xFF4C463D)
    val BarGlyph: Color get() = pick(0xFFCFC9BC, 0xFF3E3931)
    val BarGlyphSelected: Color get() = pick(0xFFFFD9E2, 0xFFA00030)
    val ActionDisc: Color get() = pick(0xFF1F1C26, 0xFFF3F0E9)
    val ActionGlyph: Color get() = pick(0xFF98928A, 0xFF5B564D)
    val ColorRing: Color get() = pick(0xFF8F897E, 0xFF6E6A61)
    val BarHeight = 44.dp
    val BarMargin = 40.dp
    val BarButtonWidth = 44.dp
    val BarButtonHeight = 38.dp
    val GalleryWidth = 58.dp
    val ActionDiscSize = 32.dp
    val ColorDot = 31.dp

    // the panels hanging from the top bar
    val PopoverFill: Color get() = pick(0xF21F0816, 0xFFECE9E2)
    /** A panel's own material over the canvas: what is behind it shows through, blurred. */
    val PopoverFrost: Color get() = pick(0xE614000E, 0xFFECE9E2)
    /**
     * The gallery's popovers: their glass as it shows over the gallery's dark (nothing blurs what
     * is behind them there, and unblurred it shows through too sharply).
     */
    val GalleryPopover: Color get() = pick(0xFF201D26, 0xFFECE9E2)
    val PopoverShadow: Color get() = pick(0xB30A000A, 0x24000000)
    val PopoverShadowRadius = 51.dp
    val PopoverCorner = 22.dp
    val PopoverGap = 12.dp
    val ArrowHeight = 12.dp
    val ArrowShift = 2.dp
    val PanelTitle: Color get() = pick(0xFFCFC9BC, 0xFF3E3931)
    val PanelGlyph: Color get() = pick(0xFFD2CCBF, 0xFF3E3931)
    val ChevronDisc: Color get() = pick(0xFF453F4C, 0xFFD0CCC2)
    val ChevronGlyph: Color get() = pick(0xFF242028, 0xFFEFEDE6)
    // a little white over the panel, so they follow its frosting
    val LayerRow: Color get() = pick(0x0DF5F1E8, 0xFFDDDAD1)
    val BackgroundRow: Color get() = pick(0x0DF5F1E8, 0xFFDDDAD1)
    val CheckFill: Color get() = pick(0xFFCFC9BC, 0xFF57534B)
    val ThumbBackground: Color get() = pick(0xFF48444F, 0xFFCBC7BC)
    /** A text layer's glyph on its thumbnail. */
    val TextLayerGlyph: Color get() = pick(0xFFC0BBB0, 0xFF1F1C19)
    // the brush library is glass like the other panels: the sets on the panel's own frost, the
    // brushes a shade darker, the set cells a little lighter (measured over white and over the
    // dark surround: 58 / 31, 52 / 26, 64 / 42)
    val SetColumn: Color get() = pick(0x00000000, 0xFFECE9E2)
    val SetCell: Color get() = pick(0x2E3A3644, 0xFFD9D5CC)
    val BrushColumn: Color get() = pick(0x26000000, 0xFFE3DFD5)
    val Grabber: Color get() = pick(0x7AC9C4B8, 0xFFB4B0A4)
    val SwatchEdge: Color get() = pick(0xFF4E4A55, 0xFFC2BDB1)
    val SaturationGrey = Color(0xFF7F7F7F)
    val ClearText: Color get() = pick(0xFFB7B2A6, 0xFF4E4941)
    val EmptySwatch: Color get() = pick(0xFF272430, 0xFFDAD6CD)
    val ValueGroup: Color get() = pick(0xFF38343F, 0xFFD8D4CB)
    val HexRow: Color get() = pick(0xFF33303B, 0xFFDDDAD1)
    val HexField: Color get() = pick(0xFF3F3B47, 0xFFCFCBC1)
    val MenuFill: Color get() = pick(0xFF4E4A56, 0xFFF1EEE7)
    /** A menu's words: paper on ink, ink on paper. */
    /** The rim of a glass panel, caught like the world's own. */
    val PopoverRim: Color get() = pick(0x4DFFF0F3, 0x1426221F)
    val MenuInk: Color get() = pick(0xFFF5F1E8, 0xFF26221F)
    /** The small frosted menus: over the canvas what is behind shows through, blurred. */
    val GlassMenu: Color get() = pick(0xCF302C39, 0xFAF1EEE7)
    /** A small menu over a panel: the panel can't show through blurred, so it is nearly solid, grey. */
    val GlassMenuOverPanel: Color get() = pick(0xFA221F29, 0xFAF1EEE7)
    val GlassMenuEdge: Color get() = pick(0x24F5F1E8, 0x1426221F)
    val RadioRing: Color get() = pick(0xFF54505E, 0xFFB2AEA2)
    val MenuDanger: Color get() = pick(0xFFEC5B4C, 0xFFC8482E)
    val MenuLine: Color get() = pick(0x33F5F1E8, 0x2626221F)
    val MenuDisabled: Color get() = pick(0x66F5F1E8, 0x6626221F)
    val SliderRest: Color get() = pick(0xFF4C4756, 0xFFC2BDB1)
    val EmptySlot: Color get() = pick(0xFF4A4654, 0xFFC9C5BB)
    // a dark wash on the panel's glass (26 over the dark surround, 44 over white)
    val ActionsGroup: Color get() = pick(0x421D1A24, 0xFFE9E5DC)
    // a faint lightening of whatever the glass shows (+5 over the dark surround, +8 over white)
    val ActionsSeparator: Color get() = pick(0x06F5F1E8, 0xFFE3DFD6)
    /** An Actions row's text: dimmer than the panel's title. */
    val ActionsRow: Color get() = pick(0xFFABA59A, 0xFF3E3931)
    val ActionsDisabled: Color get() = pick(0xFF57544F, 0xFFABA69B)
    val TabIdle: Color get() = pick(0xFFA49F94, 0xFF46413A)
    val SwitchOff: Color get() = pick(0xFF6E6A78, 0xFFA9A69E)
    val MessageBar: Color get() = pick(0xFF57535F, 0xFFD2CEC4)
    val MessageText: Color get() = pick(0xFFB7B2A6, 0xFF3E3931)
    val LoupeEdge = Color(0xFF7A766E)
    val SlideLabel: Color get() = pick(0xFFC4BFB2, 0xFF3E3931)
    val SlideValue: Color get() = pick(0xFFF0EDE4, 0xFF3E3931)
    val SlideTrack: Color get() = pick(0xFF332F3B, 0xFFC2BDB1)
    val SwitchFixed: Color get() = pick(0xFF35313E, 0xFFD5D1C8)
    val SwitchFixedKnob: Color get() = pick(0xFF464252, 0xFFEDEAE3)

    /** The centre of tool button [index] (0 is the colour button, counting leftwards), from the bar's right edge. */
    fun toolCentreFromRight(index: Int, barWidth: Dp): Dp {
        val fit = barFit(barWidth)
        return fit.margin + fit.button / 2 + (fit.button + fit.toolGap) * index
    }

    /** The centre of action button [index] (0 is actions, counting rightwards), from the bar's left edge. */
    fun actionCentre(index: Int, barWidth: Dp): Dp {
        val fit = barFit(barWidth)
        return fit.margin + GalleryWidth + fit.actionGap + fit.button / 2 + (fit.button + fit.actionGap) * index
    }

    // the gallery; light, near white with dark grey inks and a frosted white capsule
    val GalleryBackground: Color get() = pick(0xFF17141C, 0xFFF3F0E9)
    val GalleryTitle: Color get() = pick(0xFFF2EEE5, 0xFF3E3931)
    val GalleryText: Color get() = pick(0xFFA8A296, 0xFF5B564D)
    val GallerySize: Color get() = pick(0xFF5B5764, 0xFF6E6A60)
    val Capsule: Color get() = pick(0xFF201D27, 0xF0F8F6F0)
    val CapsuleEdge: Color get() = pick(0xFF322F3B, 0x0F26221F)
    val LightPanel = Color(0xFFD8D4CB)
    val LightText = Color(0xFF33302A)
    val LightRowStrong = Color(0xFFE4E0D7)
    val LightRow = Color(0xFFE0DCD3)
    val LightSeparator = Color(0xFFD0CCC3)
    val LightChip = Color(0xFFD9D5CC)

    // the side bar
    // the bar and the tracks translucent, each straight over what is behind; the thumbs opaque,
    // warm paper on both faces of the interface
    val SideBar: Color get() = pick(0x24F7CAC9, 0xCFF3E4DE)
    val SliderTrack: Color get() = pick(0xC9353140, 0xBFF2EFE7)
    // a lighter rim round each track, more of what is behind showing through it
    val SliderRim: Color get() = pick(0xB9565262, 0xFFEDEAE3)
    val SliderRimWidth = 0.8.dp
    val SliderThumb: Color get() = pick(0xFFB9B4A8, 0xFFFCFAF3)
    val SideGlyph: Color get() = pick(0xFF98928A, 0xFF5B564D)
    val SideGlyphDisabled: Color get() = pick(0x1AF5F1E8, 0x3326221F)
    val SideBarWidth = 51.dp
    val SideBarHeight = 519.dp
    val SideBarCorner = 20.dp
    /** Where the side bar's centre sits, as a fraction of the screen height. */
    const val SIDE_BAR_CENTRE = 0.5234f
    /** Left under the side bar in a window too short for its measured tracks, and how short they go. */
    private val ShortSideBarFoot = 17.dp
    private val ShortestTrack = 44.dp
    val SliderWidth = 40.dp
    val SliderHeight = 170.dp
    val SliderCorner = 13.dp
    val ThumbWidth = 32.dp
    val ThumbHeight = 18.dp
    val ThumbInset = 4.dp
    val ModifySize = 22.5.dp
    val ModifyStroke = 2.4.dp
    val ModifyCorner = 6.dp

    /**
     * The gaps between the tool buttons and between the action buttons for a bar [width] wide:
     * the room left after both margins picks one of five steps.
     */
    private fun barGaps(width: Dp): Pair<Dp, Dp> {
        val room = width - BarMargin * 2
        return when {
            room < 435.dp -> 2.dp to 4.dp
            room < 530.dp -> 8.dp to 14.dp
            room < 625.dp -> 26.dp to 14.dp
            room < 730.dp -> 8.dp to 14.dp
            else -> 26.dp to 14.dp
        }
    }

    /** The top bar's spacing: its margins, the gaps between its tool and action buttons, and how wide those are. */
    data class BarFit(val margin: Dp, val toolGap: Dp, val actionGap: Dp, val button: Dp) {
        /** The width it takes for the bar's two ends not to meet: Gallery and four actions, five tools. */
        val needs: Dp get() = margin * 2 + GalleryWidth + (button + actionGap) * 4 + button * 5 + toolGap * 4
    }

    /** Tighter and tighter, for windows too narrow for the measured steps. */
    private val NarrowBars = listOf(
        BarFit(BarMargin, 8.dp, 14.dp, BarButtonWidth),
        BarFit(BarMargin, 2.dp, 4.dp, BarButtonWidth),
        BarFit(16.dp, 2.dp, 4.dp, BarButtonWidth),
        BarFit(8.dp, 0.dp, 0.dp, BarButtonWidth),
        BarFit(8.dp, 0.dp, 0.dp, 36.dp),
        BarFit(4.dp, 0.dp, 0.dp, 32.dp),
    )

    /**
     * The spacing of a top bar [width] wide: the measured step for that width, or where its two
     * ends would run into each other (a narrow window, as in split screen) the first of
     * [NarrowBars] that keeps them apart.
     */
    fun barFit(width: Dp): BarFit {
        val (tool, action) = barGaps(width)
        val measured = BarFit(BarMargin, tool, action, BarButtonWidth)
        return if (measured.needs <= width) measured else NarrowBars.firstOrNull { it.needs <= width } ?: NarrowBars.last()
    }

    /** Where the side bar goes: its top, and how long each of its two tracks is. */
    data class SideBarFit(val top: Dp, val track: Dp)

    /**
     * The side bar in a window [height] tall: at [SIDE_BAR_CENTRE] with its measured tracks wherever
     * that clears the top bar; in a shorter window (split screen) just under the top bar, its tracks
     * giving up what it is short of, so that all of it can be reached.
     */
    fun sideBarFit(height: Dp): SideBarFit {
        val top = height * SIDE_BAR_CENTRE - SideBarHeight / 2
        if (top >= BarHeight) return SideBarFit(top, SliderHeight)
        val rest = SideBarHeight - SliderHeight * 2
        return SideBarFit(BarHeight, ((height - BarHeight - ShortSideBarFoot - rest) / 2).coerceIn(ShortestTrack, SliderHeight))
    }
}

/**
 * The Glass Horizon: InkFrame's world, in ProStudio. The exact colours, glass values and light of
 * the Glass Horizon visual contract — a luminous horizon over a deep world, paper canvas in a
 * rounded frame of frosted glass, and optical nodes that float over it, across all eight of
 * InkFrame's colour worlds.
 */
object Glass {
    /** One of InkFrame's eight colour worlds: its horizon stops, glass tint, accent and palette swatches. */
    data class World(
        val name: String,
        val accent: Color,
        val accentDeep: Color,
        val rose: Color,
        val blush: Color,
        val violet: Color,
        val stops: List<Color>,
        val swatches: List<Int>,
    )

    val worlds: List<World> = listOf(
        World(
            name = "Glass Horizon",
            accent = Color(0xFFBB0037), accentDeep = Color(0xFF880057), rose = Color(0xFFF7CAC9), blush = Color(0xFFFFF0F3), violet = Color(0xFF2A001A),
            stops = listOf(Color(0xFFFFD9E2), Color(0xFFF7CAC9), Color(0xFFD77FA0), Color(0xFFA52766), Color(0xFF4D0A33), Color(0xFF1A001A)),
            swatches = listOf(0xBB0037, 0x880057, 0xF7CAC9, 0xFFF0F3, 0x2A001A, 0xD77FA0, 0xA52766, 0x7A1746),
        ),
        World(
            name = "Aurora",
            accent = Color(0xFF1F9C6B), accentDeep = Color(0xFF0F5C45), rose = Color(0xFFBFE9D6), blush = Color(0xFFEAFFF6), violet = Color(0xFF04211A),
            stops = listOf(Color(0xFFD8FFF0), Color(0xFFBFE9D6), Color(0xFF5FC79B), Color(0xFF1F9C6B), Color(0xFF0C4A37), Color(0xFF04211A)),
            swatches = listOf(0x1F9C6B, 0x0F5C45, 0xBFE9D6, 0xEAFFF6, 0x04211A, 0x57C79B, 0x127A55, 0x0A3A2B),
        ),
        World(
            name = "Sapphire",
            accent = Color(0xFF2F6DF2), accentDeep = Color(0xFF14367A), rose = Color(0xFFBCD2FF), blush = Color(0xFFEEF4FF), violet = Color(0xFF050A24),
            stops = listOf(Color(0xFFDBE7FF), Color(0xFFBCD2FF), Color(0xFF6F9BF5), Color(0xFF2F6DF2), Color(0xFF15306E), Color(0xFF050A24)),
            swatches = listOf(0x2F6DF2, 0x14367A, 0xBCD2FF, 0xEEF4FF, 0x050A24, 0x5B8AF5, 0x1D4AA8, 0x0C1C4A),
        ),
        World(
            name = "Ember",
            accent = Color(0xFFE3552B), accentDeep = Color(0xFF8A2410), rose = Color(0xFFFFCDAB), blush = Color(0xFFFFF0E6), violet = Color(0xFF2A0A04),
            stops = listOf(Color(0xFFFFE2CF), Color(0xFFFFCDAB), Color(0xFFF2864F), Color(0xFFE3552B), Color(0xFF6E1C0A), Color(0xFF2A0A04)),
            swatches = listOf(0xE3552B, 0x8A2410, 0xFFCDAB, 0xFFF0E6, 0x2A0A04, 0xF2864F, 0xB53A17, 0x5C1606),
        ),
        World(
            name = "Amethyst",
            accent = Color(0xFF8E44C0), accentDeep = Color(0xFF4D1F70), rose = Color(0xFFE3C9F7), blush = Color(0xFFF7EEFF), violet = Color(0xFF1A0428),
            stops = listOf(Color(0xFFF0DDFF), Color(0xFFE3C9F7), Color(0xFFB06FE0), Color(0xFF8E44C0), Color(0xFF3C1758), Color(0xFF1A0428)),
            swatches = listOf(0x8E44C0, 0x4D1F70, 0xE3C9F7, 0xF7EEFF, 0x1A0428, 0xB06FE0, 0x6A2E96, 0x34114E),
        ),
        World(
            name = "Graphite",
            accent = Color(0xFF8A8F99), accentDeep = Color(0xFF4A4F57), rose = Color(0xFFCFD4DC), blush = Color(0xFFF2F4F7), violet = Color(0xFF15171C),
            stops = listOf(Color(0xFFE7EAEF), Color(0xFFCFD4DC), Color(0xFF9AA0AA), Color(0xFF6F757E), Color(0xFF2C2E34), Color(0xFF15171C)),
            swatches = listOf(0x8A8F99, 0x4A4F57, 0xCFD4DC, 0xF2F4F7, 0x15171C, 0xA8ADB6, 0x5E636B, 0x2A2D33),
        ),
        World(
            name = "Gold Dusk",
            accent = Color(0xFFD9A328), accentDeep = Color(0xFF8A6310), rose = Color(0xFFF3DCA0), blush = Color(0xFFFFF7E3), violet = Color(0xFF261A04),
            stops = listOf(Color(0xFFFFF1C9), Color(0xFFF3DCA0), Color(0xFFE0B94E), Color(0xFFD9A328), Color(0xFF6E4F0C), Color(0xFF261A04)),
            swatches = listOf(0xD9A328, 0x8A6310, 0xF3DCA0, 0xFFF7E3, 0x261A04, 0xE0B94E, 0xA87C18, 0x4E3908),
        ),
        World(
            name = "Mint Glass",
            accent = Color(0xFF1FB6A6), accentDeep = Color(0xFF0F6B62), rose = Color(0xFFB8EFE7), blush = Color(0xFFE8FFFB), violet = Color(0xFF042320),
            stops = listOf(Color(0xFFD6FFF8), Color(0xFFB8EFE7), Color(0xFF5FD6C7), Color(0xFF1FB6A6), Color(0xFF0C544C), Color(0xFF042320)),
            swatches = listOf(0x1FB6A6, 0x0F6B62, 0xB8EFE7, 0xE8FFFB, 0x042320, 0x5FD6C7, 0x138C80, 0x0A423C),
        ),
    )

    /** Which of the eight colour worlds is in play. */
    var worldIndex by mutableIntStateOf(0)

    /** Whether high-contrast readable label capsules (`Actions ▸ Text`) are on. */
    var readable by mutableStateOf(false)

    val current: World get() = worlds[worldIndex.coerceIn(0, worlds.lastIndex)]

    // the horizon's progression, bright at the top of the sky to deep at the far edge
    val Highlight: Color get() = current.stops[0]
    val RoseQuartz: Color get() = current.stops[1]
    val RoseDim: Color get() = current.stops[2]
    val HorizonDeep: Color get() = current.stops[3]
    val ShadowPlum: Color get() = current.stops[4]
    val Deep: Color get() = current.stops[5]
    /** The horizon's stops, top to bottom, as the contract specifies. */
    val HorizonStops: List<Color> get() = current.stops
    /** Shadows with the deep cast of the active world. */
    val ShadowMagenta: Color get() = current.accentDeep
    /** The interactive accent: what is chosen, what is possible. */
    val Accent: Color get() = current.accent
    /** The canvas paper, and the near-white of the rims. */
    val Paper: Color get() = current.blush
    /** rgba(20,0,14,.55) — the vignette's edge falloff. */
    val VignetteEdge: Color get() = current.violet.copy(alpha = 0.55f)
    /** rgba(10,0,10,.62) — a glass node's deep exterior shadow. */
    val NodeShadow = Color(0x9E0A000A)
    /** rgba(20,0,14,.55) — the frame glass's long drop shadow. */
    val FrameShadow: Color get() = current.violet.copy(alpha = 0.55f)

    // the frosted glass the contract specifies, in its two strengths
    /** The glass of the world at rest. */
    val Normal: Color get() = current.rose.copy(alpha = 0.12f)
    /** The glass over what is lit (rims, strong edges). */
    val Strong: Color get() = current.rose.copy(alpha = 0.20f)
    /** The glass of what is held or open. */
    val NormalHeld: Color get() = current.rose.copy(alpha = 0.30f)
    val StrongHeld: Color get() = current.rose.copy(alpha = 0.42f)
    /** The bright inner rim a glass edge catches. */
    val RimInner: Color get() = current.blush.copy(alpha = 0.35f)
    /** The rim of a node's optical centre. */
    val Rim: Color get() = current.blush.copy(alpha = 0.60f)
    /** The glass gradient's strong end. */
    val GradientStrong: Color get() = current.rose.copy(alpha = 0.18f)
    /** The glass gradient's weak end. */
    val GradientWeak: Color get() = current.rose.copy(alpha = 0.06f)
    /** The shared glass gradient (160 degrees: light from the upper-left). */
    val Gradient: Brush get() = Brush.linearGradient(listOf(GradientStrong, GradientWeak))
    /** The bar across the top, its frost catching the horizon above it. */
    val BarGradient: Brush get() = Brush.linearGradient(listOf(current.rose.copy(alpha = 0.24f), current.rose.copy(alpha = 0.12f)))
    /** The blush-to-accent gradient of what is current. */
    val AccentGradient: Brush get() = Brush.linearGradient(listOf(current.rose, Accent))
    /** The wordmark's vertical gradient: white through the rose 55% down to the accent. */
    val TitleGradient: Brush get() = Brush.verticalGradient(colorStops = arrayOf(0f to Color(0xFFFFFFFF), 0.55f to RoseQuartz, 1f to Accent))
    /** The deep radial optical centre a node's glyph sits on, for contrast. */
    val OpticalCenter: Brush get() = Brush.radialGradient(listOf(current.violet.copy(alpha = 0.70f), Color(0x00FFFFFF)))
}

/**
 * The open canvas's colour space: its colours (0xRRGGBB values) are Display P3 numbers or sRGB
 * ones. The window is in wide colour, so P3 colours show as they are.
 */
object CanvasSpace {
    var p3 by mutableStateOf(false)
        private set

    /**
     * How the canvas shows a colour of its own, when not as its numbers say: as a CMYK canvas's
     * press prints it, or an imported profile's colour in Display P3.
     */
    private var shown by mutableStateOf<((Int) -> Int)?>(null)
    private val seen = HashMap<Int, Int>()
    private val displayP3 by lazy { android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.DISPLAY_P3) }

    /** The canvas's colours are Display P3 numbers (else sRGB), shown through [shown] if given. */
    fun set(p3: Boolean, shown: ((Int) -> Int)? = null) {
        seen.clear()
        this.p3 = p3
        this.shown = shown
    }

    /** A canvas colour, in the canvas's space. */
    fun color(rgb: Int): Color {
        val f = shown
        val c = if (f == null) rgb else seen.getOrPut(rgb) { f(rgb) }
        return if (p3) {
            Color(((c shr 16) and 0xFF) / 255f, ((c shr 8) and 0xFF) / 255f, (c and 0xFF) / 255f, 1f, androidx.compose.ui.graphics.colorspace.ColorSpaces.DisplayP3)
        } else {
            Color(0xFF000000 or c.toLong())
        }
    }

    /** [bitmap], of the canvas's colours, marked with the canvas's space (on Android 9, a copy so marked). */
    fun tag(bitmap: android.graphics.Bitmap): android.graphics.Bitmap {
        if (!p3) return bitmap
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            bitmap.setColorSpace(displayP3)
            return bitmap
        }
        // a bitmap's space can't be changed before Android 10: its pixels go into one made in it
        val config = bitmap.config ?: android.graphics.Bitmap.Config.ARGB_8888
        val tagged = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, config, true, displayP3)
        val pixels = java.nio.ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(pixels)
        pixels.rewind()
        tagged.copyPixelsFromBuffer(pixels)
        return tagged
    }
}
