package com.streamvault.feature.catalog.presentation.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CatalogVodRatingFormatterTest {

    @Test
    fun `whole ratings drop the decimal`() {
        assertThat(formatVodRatingLabel(8f)).isEqualTo("★ 8/10")
        assertThat(formatVodRatingLabel(0f)).isEqualTo("★ 0/5")
    }

    @Test
    fun `one decimal is kept and rounded half up`() {
        assertThat(formatVodRatingLabel(7.25f)).isEqualTo("★ 7.3/10")
        assertThat(formatVodRatingLabel(6.84f)).isEqualTo("★ 6.8/10")
    }

    @Test
    fun `ratings up to five use the five point scale`() {
        assertThat(formatVodRatingLabel(4.5f)).isEqualTo("★ 4.5/5")
        assertThat(formatVodRatingLabel(5f)).isEqualTo("★ 5/5")
    }

    @Test
    fun `negative ratings are clamped`() {
        assertThat(formatVodRatingLabel(-3f)).isEqualTo("★ 0/5")
    }
}
