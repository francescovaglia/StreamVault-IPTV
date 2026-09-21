package com.streamvault.feature.playback.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import org.junit.Test

class PlayerChannelIndexResolutionTest {

    private val list = listOf(
        Channel(id = 10, name = "Rai 1", streamUrl = "http://a/1", logicalGroupId = "1_rai1"),
        Channel(id = 11, name = "Sky Uno", streamUrl = "http://a/2", logicalGroupId = "1_skyuno")
    )

    @Test
    fun `a variant from another playlist is found through its channel group`() {
        val resolved = resolveCurrentChannelIndex(
            channels = list,
            targetId = 999,
            currentStreamUrl = "http://harmony/skyuno-hd",
            playingGroupId = "1_skyuno",
            previousIndex = -1
        )
        assertThat(resolved).isEqualTo(ResolvedChannelIndex(1, matchedByGroup = true))
    }

    @Test
    fun `the listed channel itself is found by id`() {
        val resolved = resolveCurrentChannelIndex(list, 10, "", "1_rai1", previousIndex = 1)
        assertThat(resolved).isEqualTo(ResolvedChannelIndex(0, matchedByGroup = false))
    }
}
