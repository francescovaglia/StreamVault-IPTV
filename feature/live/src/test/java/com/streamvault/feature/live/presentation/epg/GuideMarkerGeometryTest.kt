package com.streamvault.feature.live.presentation.epg

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GuideMarkerGeometryTest {
    private val halfHour = 30 * 60 * 1000L
    private val window = 7 * 2 * halfHour
    private val totalPx = 2800f

    @Test
    fun stepMatchesTheShareOfTheWindow() {
        val geometry = guideMarkerGeometry(totalPx, windowStart = 0L, markerStepMs = halfHour, window, 1f)
        assertThat(geometry.stepPx).isEqualTo(totalPx / 14f)
    }

    @Test
    fun firstMarkerSitsBeforeAWindowThatDoesNotStartOnTheHalfHour() {
        // Window starting 10 minutes past the half hour: the grid line belongs 10 minutes earlier,
        // otherwise every label in the header drifts against the cells.
        val tenMinutes = 10 * 60 * 1000L
        val geometry = guideMarkerGeometry(totalPx, 100 * halfHour + tenMinutes, halfHour, window, 1f)
        assertThat(geometry.firstOffsetPx).isWithin(0.01f).of(-totalPx * tenMinutes / window)
    }
}
