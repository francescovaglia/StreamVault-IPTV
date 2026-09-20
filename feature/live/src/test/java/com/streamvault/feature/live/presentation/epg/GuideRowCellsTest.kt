package com.streamvault.feature.live.presentation.epg

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Program
import org.junit.Test

class GuideRowCellsTest {
    private val hour = 60 * 60 * 1000L
    private val windowStart = 1_000 * hour
    private val windowEnd = windowStart + 7 * hour
    private val totalWidth = 1400.dp

    private fun program(startMinutes: Long, durationMinutes: Long) = Program(
        channelId = "1",
        title = "P$startMinutes",
        startTime = windowStart + startMinutes * 60_000L,
        endTime = windowStart + (startMinutes + durationMinutes) * 60_000L
    )

    @Test
    fun `a widened short programme never covers the next one`() {
        // 200 dp per hour, so a 5-minute programme is 16 dp of timeline and gets widened to 48.
        val cells = guideRowCells(
            programs = listOf(program(0, 5), program(5, 55), program(60, 60)),
            windowStart = windowStart,
            windowEnd = windowEnd,
            totalTimelineWidth = totalWidth,
            minimumItemWidth = 48.dp
        )
        assertThat(cells).hasSize(3)
        assertThat(cells[0].width).isEqualTo(48.dp)
        // The neighbour starts where the widened cell ends, not under it.
        assertThat(cells[1].startPadding).isEqualTo(0.dp)
        var cursor = 0.dp
        cells.forEach { cell ->
            assertThat(cell.startPadding.value).isAtLeast(0f)
            cursor += cell.startPadding + cell.width
        }
        assertThat(cursor.value).isAtMost(totalWidth.value + 48f)
    }

    @Test
    fun `a gap in the schedule stays a gap`() {
        val cells = guideRowCells(
            programs = listOf(program(0, 60), program(120, 60)),
            windowStart = windowStart,
            windowEnd = windowEnd,
            totalTimelineWidth = totalWidth,
            minimumItemWidth = 48.dp
        )
        assertThat(cells[1].startPadding).isEqualTo(200.dp)
    }

    @Test
    fun `programmes outside the window are dropped`() {
        val past = Program(
            channelId = "1",
            title = "yesterday",
            startTime = windowStart - 4 * hour,
            endTime = windowStart - 3 * hour
        )
        val cells = guideRowCells(
            programs = listOf(past, program(0, 60)),
            windowStart = windowStart,
            windowEnd = windowEnd,
            totalTimelineWidth = totalWidth,
            minimumItemWidth = 48.dp
        )
        assertThat(cells.map { it.program.title }).containsExactly("P0")
    }
}
