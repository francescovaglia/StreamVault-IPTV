package com.streamvault.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppTopLevelDestinationTest {

    @Test
    fun `favorites ships in the default navigation bar`() {
        assertThat(AppTopLevelDestination.defaultOrder)
            .contains(AppTopLevelDestination.FAVORITES)
    }

    @Test
    fun `favorites round-trips through storage`() {
        assertThat(AppTopLevelDestination.fromStorage("favorites"))
            .isEqualTo(AppTopLevelDestination.FAVORITES)
    }

    @Test
    fun `every destination is reachable from its stored value`() {
        AppTopLevelDestination.entries.forEach { destination ->
            assertThat(AppTopLevelDestination.fromStorage(destination.storageValue))
                .isEqualTo(destination)
        }
    }

    @Test
    fun `favorites is not offered as a landing destination`() {
        // It has no landingDestination, so it must not appear in the startup picker.
        assertThat(AppTopLevelDestination.FAVORITES.landingDestination).isNull()
        assertThat(
            AppTopLevelDestination.availableLandingDestinations(AppTopLevelDestination.defaultOrder)
        ).doesNotContain(null)
    }
}
