package com.streamvault.data.util

import java.time.Year

/**
 * Year.now() reads the system clock and allocates a LocalDate, and the deduplication comparators
 * ask for the current year inside a key selector, so it ran on both sides of every comparison.
 * Cached per calendar day rather than per process: a TV box is left on across New Year.
 */
internal object CurrentYearCache {
    @Volatile private var day = Long.MIN_VALUE
    @Volatile private var year = 0

    fun value(): Int {
        val today = System.currentTimeMillis() / 86_400_000L
        if (today != day) {
            year = Year.now().value
            day = today
        }
        return year
    }
}
