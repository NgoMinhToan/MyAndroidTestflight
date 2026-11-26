package dev.minhtoan.mytestflight.feature.applist

import dev.minhtoan.mytestflight.feature.applist.ui.settleCollapseTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class CollapseMotionTest {

    @Test
    fun `upward momentum collapses from below the midpoint`() {
        assertEquals(1f, settleCollapseTarget(progress = 0.3f, progressVelocity = 4f))
    }

    @Test
    fun `downward momentum expands from above the midpoint`() {
        assertEquals(0f, settleCollapseTarget(progress = 0.7f, progressVelocity = -4f))
    }

    @Test
    fun `a release without momentum falls back to the midpoint`() {
        assertEquals(1f, settleCollapseTarget(progress = 0.6f, progressVelocity = 0f))
        assertEquals(0f, settleCollapseTarget(progress = 0.4f, progressVelocity = 0f))
    }

    @Test
    fun `projection stays inside the collapse range`() {
        assertEquals(1f, settleCollapseTarget(progress = 1f, progressVelocity = 100f))
        assertEquals(0f, settleCollapseTarget(progress = 0f, progressVelocity = -100f))
    }
}
