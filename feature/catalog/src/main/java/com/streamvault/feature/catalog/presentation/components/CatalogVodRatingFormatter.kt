package com.streamvault.feature.catalog.presentation.components


fun formatVodRatingLabel(rating: Float): String {
    val normalizedRating = rating.coerceAtLeast(0f)
    val scale = if (normalizedRating <= 5f) 5 else 10
    val valueText = formatVodRatingValue(normalizedRating)
    return "\u2605 $valueText/$scale"
}

private fun formatVodRatingValue(rating: Float): String {
    // String.format builds a Formatter and parses the pattern every time, and this runs on each
    // visible poster on each recomposition. A rating never needs more than one decimal.
    val tenths = Math.round(rating * 10f)
    val whole = tenths / 10
    val decimal = tenths % 10
    return if (decimal == 0) whole.toString() else "$whole.$decimal"
}

