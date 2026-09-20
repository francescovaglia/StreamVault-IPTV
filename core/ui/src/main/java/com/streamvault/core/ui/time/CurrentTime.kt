package com.streamvault.core.ui.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

const val DEFAULT_CLOCK_INTERVAL_MS = 30_000L

/**
 * A wall clock that actually recomposes its readers. Anything drawing "how far into the programme
 * are we" needs this: reading System.currentTimeMillis() straight from a composable freezes the
 * value until something unrelated recomposes the tree.
 */
@Composable
fun rememberCurrentTimeMillis(intervalMs: Long = DEFAULT_CLOCK_INTERVAL_MS): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(intervalMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(intervalMs)
        }
    }
    return now
}
