package com.streamvault.feature.playback.player

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerRebufferPolicyTest {

    @Test
    fun `third rebuffer inside a minute asks for another source`() {
        val stalls = ArrayDeque<Long>()
        assertThat(recordRebuffer(stalls, 0L)).isFalse()
        assertThat(recordRebuffer(stalls, 20_000L)).isFalse()
        assertThat(recordRebuffer(stalls, 50_000L)).isTrue()
    }

    @Test
    fun `rebuffers older than a minute do not count`() {
        val stalls = ArrayDeque<Long>()
        recordRebuffer(stalls, 0L)
        recordRebuffer(stalls, 10_000L)
        assertThat(recordRebuffer(stalls, 75_000L)).isFalse()
    }
}
