package io.github.artistso.prostudio.ui

import androidx.compose.runtime.getValue
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
    val RowSelected: Color get() = pick(0xFFBB0037, 0xFFA00030)
    val AccentRose = Color(0xFFBB0037)
    val DeepPlum = Color(0xFF1A001A)
    val Accent: Color get() = pick(0xFFBB0037, 0xFFA00030)
    /** The brand blend: blush into accent rose into deep plum. For the rare stroke that says where you are. */
    val Brand: Brush get() = Brush.linearGradient(listOf(Color(0xFFFFD9E2), Color(0xFFBB0037), Color(0xFF4D0A33)))
    val Text: Color get() = pick(0xFFF5F1E8, 0xFF26221F)
    val TextDim: Color get() = pick(0x99F5F1E8, 0x9926221F)
    val Disabled: Color get() = pick(0x4DF5F1E8, 0x4D26221F)
    val Divider: Color get() = pick(0x26F5F1E8, 0x1F26221F)
    val Surround = Color(0xFF1A001A)

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
    val PopoverFill: Color get() = pick(0xFF37333F, 0xFFECE9E2)
    /** A panel's own material over the canvas: what is behind it shows through, blurred. */
    val PopoverFrost: Color get() = pick(0xDE242129, 0xFFECE9E2)
    /**
     * The gallery's popovers: their glass as it shows over the gallery's dark (nothing blurs what
     * is behind them there, and unblurred it shows through too sharply).
     */
    val GalleryPopover: Color get() = pick(0xFF201D26, 0xFFECE9E2)
    val PopoverShadow: Color get() = pick(0x38000000, 0x24000000)
    val PopoverShadowRadius = 51.dp
    val PopoverCorner = 35.dp
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
 * the Glass Horizon visual contract — a luminous rose horizon over a deep plum world, paper canvas
 * in a rounded frame of frosted glass, and optical nodes that float over it. Dark only: the world
 * does not dim with the lamps on; with the light interface the same glass reads over warm blush.
 */
object Glass {
    // the horizon's progression, bright rose at the top of the sky to plum-black at the far edge
    val Highlight = Color(0xFFFFD9E2)
    val RoseQuartz = Color(0xFFF7CAC9)
    val RoseDim = Color(0xFFD77FA0)
    val HorizonDeep = Color(0xFFA52766)
    val ShadowPlum = Color(0xFF4D0A33)
    val Deep = Color(0xFF1A001A)
    /** The horizon's stops, top to bottom, as the contract specifies. */
    val HorizonStops = listOf(Highlight, RoseQuartz, RoseDim, HorizonDeep, ShadowPlum, Deep)
    /** #880057 — shadows with the dark magenta cast of the reference. */
    val ShadowMagenta = Color(0xFF880057)
    /** #BB0037 — the interactive accent: what is chosen, what is possible. */
    val Accent = Color(0xFFBB0037)
    /** #FFF0F3 — the canvas paper, and the near-white of the rims. */
    val Paper = Color(0xFFFFF0F3)
    /** rgba(20,0,14,.55) — the vignette's edge falloff. */
    val VignetteEdge = Color(0x8C14000E)
    /** rgba(10,0,10,.62) — a glass node's deep exterior shadow. */
    val NodeShadow = Color(0x9E0A000A)
    /** rgba(20,0,14,.55) — the frame glass's long drop shadow. */
    val FrameShadow = Color(0x8C14000E)

    // the frosted glass the contract specifies, in its two strengths
    /** rgba(247,202,201,.12) — the glass of the world at rest. */
    val Normal = Color(0x1FF7CAC9)
    /** rgba(247,202,201,.20) — the glass over what is lit (rims, strong edges). */
    val Strong = Color(0x33F7CAC9)
    /** rgba(247,202,201,.30) / rgba(247,202,201,.42) — the glass of what is held or open. */
    val NormalHeld = Color(0x4DF7CAC9)
    val StrongHeld = Color(0x6BF7CAC9)
    /** rgba(255,240,243,.35) — the bright inner rim a glass edge catches. */
    val RimInner = Color(0x59FFF0F3)
    /** rgba(255,240,243,.6) — the rim of a node's optical centre. */
    val Rim = Color(0x99FFF0F3)
    /** rgba(247,202,201,.18) — the glass gradient's strong end. */
    val GradientStrong = Color(0x2EF7CAC9)
    /** rgba(247,202,201,.06) — the glass gradient's weak end. */
    val GradientWeak = Color(0x0FF7CAC9)
    /** The shared glass gradient (160 degrees: light from the upper-left). */
    val Gradient: Brush = Brush.linearGradient(listOf(GradientStrong, GradientWeak))
    /** The bar across the top, its frost catching the horizon above it. */
    val BarGradient: Brush = Brush.linearGradient(listOf(Color(0x3DF7CAC9), Color(0x1FF7CAC9)))
    /** The blush-to-accent gradient of what is current. */
    val AccentGradient: Brush = Brush.linearGradient(listOf(Color(0xFFF7CAC9), Accent))
    /** The wordmark's vertical gradient: white through rose to accent red. */
    val TitleGradient: Brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Highlight, RoseQuartz, RoseDim, Accent))
    /** The deep radial optical centre a node's glyph sits on, for contrast. */
    val OpticalCenter: Brush = Brush.radialGradient(listOf(Color(0xB30F020F), Color(0x00FFFFFF)))
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

    /** [bitmap], of the canvas's colours, marked with the canvas's space. */
    fun tag(bitmap: android.graphics.Bitmap): android.graphics.Bitmap {
        if (p3) bitmap.setColorSpace(displayP3)
        return bitmap
    }
}
