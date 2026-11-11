package io.github.artistso.prostudio.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChromeFitTest {
    private val sideBarClear = Ps.SideBarWidth + Ps.PopoverGap

    private fun widths(from: Int, to: Int) = (from..to).map { it.dp }

    @Test
    fun aTabletsTopBarKeepsItsMeasuredSteps() {
        for ((width, tool) in listOf(720.dp to 8.dp, 744.dp to 8.dp, 1133.dp to 26.dp, 1200.dp to 26.dp)) {
            assertEquals(Ps.BarFit(40.dp, tool, 14.dp, 44.dp), Ps.barFit(width))
        }
        // wherever the measured step fits, it is the one used
        for (width in widths(705, 2000)) {
            val fit = Ps.barFit(width)
            assertEquals(40.dp, fit.margin)
            assertEquals(44.dp, fit.button)
            assertEquals(14.dp, fit.actionGap)
        }
    }

    @Test
    fun theTopBarsEndsNeverMeet() {
        for (width in widths(354, 2000)) assertTrue("$width", Ps.barFit(width).needs <= width)
    }

    @Test
    fun panelsStillHangFromTheirButtons() {
        // the bar at 411: 8 margins and 36 buttons, nothing between them
        assertEquals(26.dp, Ps.toolCentreFromRight(0, 411.dp))
        assertEquals(170.dp, Ps.toolCentreFromRight(4, 411.dp))
        assertEquals(84.dp, Ps.actionCentre(0, 411.dp))
        // on a tablet, as ever
        assertEquals(62.dp, Ps.toolCentreFromRight(0, 1200.dp))
        assertEquals(342.dp, Ps.toolCentreFromRight(4, 1200.dp))
        assertEquals(134.dp, Ps.actionCentre(0, 1200.dp))
    }

    @Test
    fun aTabletsSideBarStaysWhereItWasMeasured() {
        // portrait top 333.5, landscape top 130
        assertEquals(333.5f, Ps.sideBarFit(1133.dp).top.value, 0.1f)
        assertEquals(130f, Ps.sideBarFit(744.dp).top.value, 0.1f)
        for (height in widths(580, 2000)) {
            val fit = Ps.sideBarFit(height)
            assertEquals(Ps.SliderHeight, fit.track)
            assertEquals(height * Ps.SIDE_BAR_CENTRE - Ps.SideBarHeight / 2, fit.top)
            assertTrue(fit.top >= Ps.BarHeight)
        }
    }

    @Test
    fun theSideBarFitsUnderTheTopBarOfAShortWindow() {
        for (height in widths(328, 579)) {
            val fit = Ps.sideBarFit(height)
            assertEquals(Ps.BarHeight, fit.top)
            val bar = Ps.SideBarHeight - (Ps.SliderHeight - fit.track) * 2
            assertTrue("$height", fit.top + bar <= height)
            assertTrue(fit.track <= Ps.SliderHeight)
        }
        // no jump where the two meet
        assertEquals(170f, Ps.sideBarFit(579.dp).track.value, 0.6f)
    }

    @Test
    fun aPanelInAWideEnoughWindowGoesWhereItAlwaysDid() {
        // as it was before narrow windows were thought of; it keeps the same fallback the real
        // popoverPlace uses, so narrow windows land where the fallback lands (wide ones are unchanged)
        fun old(width: Dp, tip: Dp, screen: Dp, minLeft: Dp, minRight: Dp): Dp {
            val furthest = screen - minRight - width
            return if (furthest >= minLeft) (tip + Ps.ArrowShift - width / 2).coerceIn(minLeft, furthest)
                   else ((screen.value - width.value) / 2f).coerceAtLeast(0f).dp
        }
        for (screen in widths(451, 2000)) {
            val tip = screen - Ps.toolCentreFromRight(0, screen)
            assertEquals(old(376.dp, tip, screen, sideBarClear, Ps.PopoverGap), popoverPlace(376.dp, tip, screen, sideBarClear).left)
            assertEquals(old(376.dp, tip, screen, Ps.PopoverGap, sideBarClear), popoverPlace(376.dp, tip, screen, minRight = sideBarClear).left)
        }
    }

    @Test
    fun aPanelStaysInsideANarrowWindow() {
        for (panel in listOf(330.dp, 376.dp, 384.dp, 390.dp, 440.dp)) {
            for (screen in widths(panel.value.toInt(), 700)) {
                val tip = screen - Ps.toolCentreFromRight(0, screen)
                for (place in listOf(popoverPlace(panel, tip, screen, sideBarClear), popoverPlace(panel, tip, screen, minRight = sideBarClear))) {
                    assertTrue("$panel in $screen", place.left >= 0.dp && place.left + panel <= screen)
                }
            }
        }
        // the colour panel in a window 411 wide, which ran 28 off its right edge
        assertEquals(17.5.dp, popoverPlace(376.dp, 411.dp - 26.dp, 411.dp, sideBarClear).left)
    }
}
