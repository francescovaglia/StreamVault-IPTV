package com.streamvault.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** The ranking rule behind "reserve only": curated playlists first, reserves last. */
class FallbackVariantRankTest {
    private fun rank(providerId: Long, currentProviderId: Long, fallbackOnly: Set<Long>): Int = when {
        providerId == currentProviderId -> 0
        providerId !in fallbackOnly -> 1
        else -> 2
    }

    @Test
    fun reserveRanksAfterEveryBrowsablePlaylist() {
        val fallbackOnly = setOf(9L)
        assertThat(rank(1L, currentProviderId = 1L, fallbackOnly = fallbackOnly)).isEqualTo(0)
        assertThat(rank(2L, currentProviderId = 1L, fallbackOnly = fallbackOnly)).isEqualTo(1)
        assertThat(rank(9L, currentProviderId = 1L, fallbackOnly = fallbackOnly)).isEqualTo(2)
    }
}
