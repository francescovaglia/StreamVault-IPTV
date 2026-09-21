@file:Suppress("DEPRECATION")

package com.streamvault.feature.catalog.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged

/**
 * Prevents a parent scrollable (e.g. LazyColumn) from micro-scrolling vertically
 * when D-pad focus moves between children of an inner horizontal row (e.g. LazyRow).
 *
 * Works by replacing the per-card BringIntoView rect with the full height of this
 * container, so the parent only scrolls to ensure the whole row section is visible.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.suppressParentVerticalScroll(): Modifier = composed {
    var height by remember { mutableFloatStateOf(0f) }

    this
        .onSizeChanged { height = it.height.toFloat() }
        .bringIntoViewResponder(object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect =
                Rect(localRect.left, 0f, localRect.right, height)

            override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                // No-op: horizontal scrolling is handled by the inner LazyRow.
            }
        })
}

/**
 * Horizontal rows scroll only when the focused card would leave the screen, and then just enough
 * to show it with [edgeMarginPx] to spare. The TV default keeps the focused card pinned at 30% of
 * the row, so every press slid the whole row under a still focus, and the second press moved it by
 * only the content padding: a small twitch followed by full-card slides.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class EdgeRevealBringIntoViewSpec(private val edgeMarginPx: Float) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        val start = offset - edgeMarginPx
        val end = offset + size + edgeMarginPx
        return when {
            start >= 0f && end <= containerSize -> 0f
            size + 2 * edgeMarginPx > containerSize -> start
            end > containerSize -> end - containerSize
            else -> start
        }
    }
}
