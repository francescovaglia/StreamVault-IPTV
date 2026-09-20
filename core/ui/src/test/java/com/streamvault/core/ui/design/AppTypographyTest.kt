package com.streamvault.core.ui.design

import androidx.compose.ui.text.font.FontFamily
import com.google.common.truth.Truth.assertThat
import java.util.Locale
import org.junit.Test

class AppTypographyTest {

    @Test
    fun `latin greek and cyrillic locales keep the bundled font`() {
        val covered = listOf("it", "en", "es", "pl", "ro", "cs", "tr", "el", "ru", "uk", "vi")
        covered.forEach { language ->
            assertThat(appFontFamilyFor(Locale(language))).isEqualTo(InterFamily)
        }
    }

    @Test
    fun `scripts inter has no glyphs for fall back to the system font`() {
        val uncovered = listOf("ar", "iw", "he", "ja", "ko", "zh")
        uncovered.forEach { language ->
            assertThat(appFontFamilyFor(Locale(language))).isEqualTo(FontFamily.SansSerif)
        }
    }
}
