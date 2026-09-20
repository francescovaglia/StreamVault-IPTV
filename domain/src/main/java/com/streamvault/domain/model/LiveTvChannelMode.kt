package com.streamvault.domain.model

enum class LiveTvChannelMode {
    COMFORTABLE,
    COMPACT,
    PRO;

    companion object {
        fun fromStorage(value: String?): LiveTvChannelMode =
            // Default away from PRO: its first click starts a preview ExoPlayer next to the
            // one that will play fullscreen, which a weak TV and a one-connection line both
            // pay for. PRO stays available in settings.
            entries.firstOrNull { it.name == value } ?: COMFORTABLE
    }
}
