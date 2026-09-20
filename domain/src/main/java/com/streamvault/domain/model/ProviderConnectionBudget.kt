package com.streamvault.domain.model

/**
 * Whether a provider can serve a stream *on top of* the one being watched: preloading the next
 * episode, multi-view, picture-in-picture.
 *
 * The rule is deliberately the same for every remote provider. Only Xtream and Stalker report a
 * real figure, so an M3U playlist sits at the conservative default of one, and guessing
 * generously there is exactly how a single-connection subscription ends up refusing the stream
 * the user is actually watching. Jellyfin is the user's own server, where a second connection
 * costs nothing.
 */
fun providerAllowsExtraStream(type: ProviderType?, maxConnections: Int): Boolean = when (type) {
    ProviderType.JELLYFIN -> true
    null -> false
    else -> maxConnections >= 2
}
