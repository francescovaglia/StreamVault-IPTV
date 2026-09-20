package com.streamvault.domain.playback

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import org.junit.Test

class ArchivePlaybackOffersReplayTest {
    private fun xtreamChannel(catchUpSupported: Boolean, catchUpDays: Int) = Channel(
        id = 1,
        name = "Rai 1",
        streamUrl = "xtream://1/live/55",
        streamId = 55,
        providerId = 1,
        catchUpSupported = catchUpSupported,
        catchUpDays = catchUpDays
    )

    @Test
    fun xtreamChannelWithoutProviderArchiveDoesNotOfferReplay() {
        val capability = xtreamChannel(catchUpSupported = false, catchUpDays = 0).archivePlaybackCapability()
        assertThat(capability.canBuildReplayCandidate).isTrue()
        assertThat(capability.offersReplay).isFalse()
    }

    @Test
    fun providerArchiveOrWindowOffersReplay() {
        assertThat(xtreamChannel(true, 0).archivePlaybackCapability().offersReplay).isTrue()
        assertThat(xtreamChannel(false, 7).archivePlaybackCapability().offersReplay).isTrue()
    }
}
