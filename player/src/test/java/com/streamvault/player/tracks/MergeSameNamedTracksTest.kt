package com.streamvault.player.tracks

import com.google.common.truth.Truth.assertThat
import com.streamvault.player.PlayerTrack
import com.streamvault.player.TrackType
import org.junit.Test

class MergeSameNamedTracksTest {
    private fun text(id: String, name: String, selected: Boolean = false) =
        PlayerTrack(id = id, name = name, language = null, type = TrackType.TEXT, isSelected = selected)

    @Test
    fun `same named tracks collapse to one, keeping the selected copy`() {
        val merged = mergeSameNamedTracks(
            listOf(text("a", "Italiano"), text("b", "Italiano", selected = true), text("c", "English"))
        )
        assertThat(merged.map(PlayerTrack::id)).containsExactly("b", "c").inOrder()
    }
}
