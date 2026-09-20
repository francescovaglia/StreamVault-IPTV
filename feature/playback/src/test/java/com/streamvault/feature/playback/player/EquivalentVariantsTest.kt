package com.streamvault.feature.playback.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.LiveChannelVariant
import org.junit.Test

class EquivalentVariantsTest {
    private fun variant(id: Long, providerId: Long) = LiveChannelVariant(
        rawChannelId = id,
        logicalGroupId = "${providerId}_rai1",
        providerId = providerId,
        originalName = "Rai 1",
        canonicalName = "Rai 1",
        streamUrl = "http://p$providerId/$id"
    )

    @Test
    fun rawChannelGainsItselfAndOtherPlaylists() {
        val channel = Channel(id = 1, name = "Rai 1", providerId = 1, logicalGroupId = "1_rai1")
        val merged = channel.withEquivalentVariants(listOf(variant(7, 2), variant(1, 1)))
        assertThat(merged.variants.map { it.rawChannelId }).containsExactly(1L, 7L).inOrder()
    }

    @Test
    fun noNewVariantsKeepsSameInstance() {
        val channel = Channel(id = 1, name = "Rai 1", providerId = 1, variants = listOf(variant(1, 1)))
        assertThat(channel.withEquivalentVariants(listOf(variant(1, 1)))).isSameInstanceAs(channel)
    }

    @Test
    fun selectingForeignVariantSwitchesProvider() {
        val channel = Channel(id = 1, name = "Rai 1", providerId = 1)
            .withEquivalentVariants(listOf(variant(1, 1), variant(7, 2)))
        val switched = channel.withSelectedVariant(7)!!
        assertThat(switched.providerId).isEqualTo(2L)
        assertThat(switched.streamUrl).isEqualTo("http://p2/7")
    }
}
