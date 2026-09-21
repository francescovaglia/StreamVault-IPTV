package com.streamvault.feature.catalog.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class)
class EdgeRevealBringIntoViewSpecTest {
    private val spec = EdgeRevealBringIntoViewSpec(edgeMarginPx = 20f)

    @Test
    fun `a card already on screen does not move the row`() {
        assertThat(spec.calculateScrollDistance(offset = 300f, size = 136f, containerSize = 960f)).isEqualTo(0f)
    }

    @Test
    fun `a card past the right edge scrolls just enough to show it with the margin`() {
        assertThat(spec.calculateScrollDistance(offset = 900f, size = 136f, containerSize = 960f)).isEqualTo(96f)
    }

    @Test
    fun `a card past the left edge scrolls back to the margin`() {
        assertThat(spec.calculateScrollDistance(offset = -50f, size = 136f, containerSize = 960f)).isEqualTo(-70f)
    }
}
