package com.streamvault.feature.live.presentation.epg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.streamvault.domain.model.Channel
import com.streamvault.core.ui.theme.Primary
import com.streamvault.domain.model.Program
import com.streamvault.feature.live.epg.EpgViewModel
import com.streamvault.domain.model.guideLookupKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

private const val GUIDE_SCROLL_SNAP_MS = 30 * 60 * 1000L

/** Left edge of the visible timeline: the anchor floored to the half hour, like TiviMate. */
fun liveGuideScrollTargetTime(anchorTime: Long, windowStart: Long, windowEnd: Long): Long =
    (anchorTime - anchorTime.mod(GUIDE_SCROLL_SNAP_MS)).coerceIn(windowStart, windowEnd)

fun liveGuideInitialFocusIndex(
    channels: List<Channel>,
    initialFocusedChannelId: Long?
): Int {
    val resolvedInitialChannelId = initialFocusedChannelId ?: channels.firstOrNull()?.id
    return resolvedInitialChannelId
        ?.let { channelId -> channels.indexOfFirst { it.id == channelId } }
        ?.takeIf { it >= 0 }
        ?: 0
}

@Composable
fun LiveGuideGrid(
    modifier: Modifier = Modifier,
    channels: List<Channel>,
    favoriteChannelIds: Set<Long>,
    programsByChannel: Map<String, List<Program>>,
    guideWindowStart: Long,
    guideWindowEnd: Long,
    density: GuideDensity,
    labels: LiveGuideGridLabels,
    transparentOverlay: Boolean = false,
    initialFocusedChannelId: Long? = null,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: ((Channel, Program?) -> Unit)? = null,
    onProgramClick: (Channel, Program) -> Unit,
    onChannelFocused: (Channel, Program?, Boolean) -> Unit,
    onProgramFocused: (Channel, Program, Boolean) -> Unit,
    onRequestMoreChannels: () -> Unit = {},
    // The view models build the window as anchor - LOOKBACK .. anchor + LOOKAHEAD.
    anchorTime: Long = guideWindowStart + EpgViewModel.LOOKBACK_MS
) {
    val channelRailWidth = 180.dp
    val timelineGap = 4.dp
    val rowHeight = when (density) {
        GuideDensity.COMPACT -> 38.dp
        GuideDensity.COMFORTABLE -> 44.dp
        GuideDensity.CINEMATIC -> 52.dp
    }
    val horizontalScrollState = rememberScrollState()
    val verticalListState = rememberLazyListState()
    val resolvedInitialChannelId = initialFocusedChannelId ?: channels.firstOrNull()?.id
    val initialFocusRequester = remember(resolvedInitialChannelId) { FocusRequester() }
    val initialFocusIndex = remember(channels, resolvedInitialChannelId) {
        liveGuideInitialFocusIndex(channels, resolvedInitialChannelId)
    }

    LaunchedEffect(channels.size, resolvedInitialChannelId) {
        if (channels.isEmpty()) return@LaunchedEffect
        verticalListState.scrollToItem((initialFocusIndex - 2).coerceAtLeast(0))
        delay(140)
        initialFocusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        val timelineViewportWidth = (maxWidth - channelRailWidth - timelineGap).coerceAtLeast(640.dp)
        val totalDuration = (guideWindowEnd - guideWindowStart).coerceAtLeast(1L)
        val visibleDurationMs = 3 * 60 * 60 * 1000L
        val calculatedTimelineWidth = timelineViewportWidth * (totalDuration.toFloat() / visibleDurationMs.toFloat())
        val totalTimelineWidth = if (calculatedTimelineWidth > timelineViewportWidth) {
            calculatedTimelineWidth
        } else {
            timelineViewportWidth
        }
        val markerStepMs = LIVE_GUIDE_MARKER_STEP_MS
        val localDensity = LocalDensity.current
        val totalTimelineWidthPx = with(localDensity) { totalTimelineWidth.toPx() }
        val timelineStartPx = with(localDensity) { (channelRailWidth + timelineGap).toPx() }
        val timelineViewportPx = with(localDensity) { timelineViewportWidth.toPx() }
        val nowLineWidthPx = with(localDensity) { 2.dp.toPx() }
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val now = currentLiveGuideNow()

        // Every time the window moves (open, "Now", paging, day jumps) the grid follows the anchor.
        // Without this the horizontal scroll stayed at its old pixel offset, so the guide opened
        // an hour in the past and "Now" only moved the window, not the view.
        LaunchedEffect(guideWindowStart, guideWindowEnd, anchorTime, totalTimelineWidthPx) {
            val target = liveGuideScrollTargetTime(anchorTime, guideWindowStart, guideWindowEnd)
            val targetPx = totalTimelineWidthPx * (target - guideWindowStart) / totalDuration
            snapshotFlow { horizontalScrollState.maxValue }.first { it in 1 until Int.MAX_VALUE }
            horizontalScrollState.scrollTo(targetPx.roundToInt())
        }

        Column(modifier = Modifier.fillMaxSize()) {
            LiveGuideTimelineHeader(
                windowStart = guideWindowStart,
                windowEnd = guideWindowEnd,
                channelRailWidth = channelRailWidth,
                timelineGap = timelineGap,
                timelineViewportWidth = timelineViewportWidth,
                totalTimelineWidth = totalTimelineWidth,
                markerStepMs = markerStepMs,
                scrollState = horizontalScrollState
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                state = verticalListState,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (now !in guideWindowStart..guideWindowEnd) return@drawWithContent
                        // Scroll is read at draw time only, so scrolling redraws without recomposing rows.
                        val offset = totalTimelineWidthPx * (now - guideWindowStart) / totalDuration -
                            horizontalScrollState.value
                        if (offset < 0f || offset > timelineViewportPx) return@drawWithContent
                        val x = if (isRtl) {
                            size.width - timelineStartPx - offset - nowLineWidthPx
                        } else {
                            timelineStartPx + offset
                        }
                        // A rectangle, not drawLine: a stroke is centred on its x, so the line
                        // sat half a pixel left of the 2 dp marker the hour bar draws as a box.
                        drawRect(Primary, Offset(x, 0f), Size(nowLineWidthPx, size.height))
                    },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(
                    items = channels,
                    key = { index, channel -> liveEpgChannelKey(channel, index) },
                    contentType = { _, _ -> "epg_channel" }
                ) { index, channel ->
                    if (index >= channels.size - 15) {
                        LaunchedEffect(channels.size) { onRequestMoreChannels() }
                    }
                    val programs = channel.guideLookupKey()?.let { lookupKey ->
                        programsByChannel[lookupKey].orEmpty()
                    }.orEmpty()
                    val isFirstRow = index == 0
                    LiveGuideGridRow(
                        channel = channel,
                        isFavorite = channel.id in favoriteChannelIds,
                        programs = programs,
                        windowStart = guideWindowStart,
                        windowEnd = guideWindowEnd,
                        channelRailWidth = channelRailWidth,
                        timelineGap = timelineGap,
                        timelineViewportWidth = timelineViewportWidth,
                        totalTimelineWidth = totalTimelineWidth,
                        density = density,
                        transparentOverlay = transparentOverlay,
                        rowHeight = rowHeight,
                        markerStepMs = markerStepMs,
                        scrollState = horizontalScrollState,
                        labels = labels,
                        focusRequester = if (channel.id == resolvedInitialChannelId) initialFocusRequester else null,
                        onChannelClick = { onChannelClick(channel) },
                        onChannelLongClick = onChannelLongClick?.let { cb -> { prog -> cb(channel, prog) } },
                        onChannelFocused = { onChannelFocused(channel, it, isFirstRow) },
                        onProgramClick = { program -> onProgramClick(channel, program) },
                        onProgramFocused = { program -> onProgramFocused(channel, program, isFirstRow) }
                    )
                }
            }
        }
    }
}
