package com.streamvault.domain.model

import com.google.common.truth.Truth.assertThat

import org.junit.Test

class GuideLookupKeyTest {
    @Test
    fun usesPositiveStreamIdBeforeTrimmedEpgId() {
        assertThat(Channel(1L, "One", epgChannelId = "  epg.one  ", streamId = 42L).guideLookupKey())
            .isEqualTo("42")
    }

    @Test
    fun fallsBackToPositiveStreamId() {
        assertThat(Channel(1L, "One", streamId = 42L).guideLookupKey()).isEqualTo("42")
    }

    @Test
    fun fallsBackToTrimmedEpgIdWhenStreamIdIsMissing() {
        assertThat(Channel(1L, "One", epgChannelId = "  epg.one  ").guideLookupKey())
            .isEqualTo("epg.one")
    }

    @Test
    fun returnsNullWhenBothIdentifiersAreMissing() {
        assertThat(Channel(1L, "One").guideLookupKey()).isNull()
    }
}
