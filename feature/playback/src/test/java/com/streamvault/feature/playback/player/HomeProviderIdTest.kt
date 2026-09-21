package com.streamvault.feature.playback.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.LiveChannelVariant
import org.junit.Test

class HomeProviderIdTest {
    private fun variant(id: Long, providerId: Long) = LiveChannelVariant(
        rawChannelId = id,
        logicalGroupId = "1_sky uno",
        providerId = providerId,
        originalName = "Sky Uno",
        canonicalName = "Sky Uno",
        streamUrl = "http://example/$id"
    )

    @Test
    fun `a switch to another playlist keeps the remembered choice on the home playlist`() {
        val channel = Channel(
            id = 10L,
            name = "Sky Uno",
            providerId = 1L,
            logicalGroupId = "1_sky uno",
            variants = listOf(variant(10L, 1L), variant(20L, 2L))
        )
        val switched = channel.withSelectedVariant(20L)!!

        assertThat(switched.providerId).isEqualTo(2L)
        assertThat(switched.homeProviderId()).isEqualTo(1L)
    }
}
