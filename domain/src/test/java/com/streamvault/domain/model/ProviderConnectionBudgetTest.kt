package com.streamvault.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProviderConnectionBudgetTest {

    @Test
    fun `a single connection leaves nothing for a second stream`() {
        ProviderType.entries
            .filterNot { it == ProviderType.JELLYFIN }
            .forEach { type ->
                assertThat(providerAllowsExtraStream(type, maxConnections = 1)).isFalse()
            }
    }

    @Test
    fun `two connections allow one extra stream`() {
        ProviderType.entries.forEach { type ->
            assertThat(providerAllowsExtraStream(type, maxConnections = 2)).isTrue()
        }
    }

    @Test
    fun `jellyfin is the user's own server and is never held back`() {
        assertThat(providerAllowsExtraStream(ProviderType.JELLYFIN, maxConnections = 1)).isTrue()
    }

    @Test
    fun `an unknown provider gets no extra stream`() {
        assertThat(providerAllowsExtraStream(null, maxConnections = 99)).isFalse()
    }
}
