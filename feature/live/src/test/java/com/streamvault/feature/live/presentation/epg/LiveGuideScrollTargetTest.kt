package com.streamvault.feature.live.presentation.epg

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiveGuideScrollTargetTest {
    private val halfHour = 30 * 60 * 1000L
    private val hour = 2 * halfHour

    @Test
    fun anchorIsFlooredToHalfHour() {
        val windowStart = 100 * hour
        val anchor = windowStart + hour + 17 * 60 * 1000L
        assertThat(liveGuideScrollTargetTime(anchor, windowStart, windowStart + 7 * hour))
            .isEqualTo(windowStart + hour)
    }

    @Test
    fun targetStaysInsideWindow() {
        assertThat(liveGuideScrollTargetTime(anchorTime = 5L, windowStart = hour, windowEnd = 2 * hour))
            .isEqualTo(hour)
    }
}
