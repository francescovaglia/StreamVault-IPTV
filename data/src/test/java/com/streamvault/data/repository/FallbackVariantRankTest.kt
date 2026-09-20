package com.streamvault.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** The ranking rule behind "reserve only": curated playlists first, reserves last. */
class FallbackVariantRankTest {
    @Test
    fun reserveRanksAfterEveryBrowsablePlaylist() {
        val fallbackOnly = setOf(9L)
        assertThat(variantSourceRank(1L, currentProviderId = 1L, fallbackOnlyProviderIds = fallbackOnly)).isEqualTo(0)
        assertThat(variantSourceRank(2L, currentProviderId = 1L, fallbackOnlyProviderIds = fallbackOnly)).isEqualTo(1)
        assertThat(variantSourceRank(9L, currentProviderId = 1L, fallbackOnlyProviderIds = fallbackOnly)).isEqualTo(2)
    }
}
