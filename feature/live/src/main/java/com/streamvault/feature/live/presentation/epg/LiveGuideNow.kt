package com.streamvault.feature.live.presentation.epg

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.streamvault.core.ui.time.rememberCurrentTimeMillis

// Not static: a static local recomposes the whole guide every tick instead of only its readers.
private val LocalLiveGuideNow = compositionLocalOf { 0L }

@Composable
fun rememberLiveGuideNow(): Long = rememberCurrentTimeMillis()

@Composable
fun LiveGuideNowProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLiveGuideNow provides rememberLiveGuideNow()) {
        content()
    }
}

@Composable
fun currentLiveGuideNow(): Long = LocalLiveGuideNow.current
