package com.streamvault.domain.model

/**
 * The key an EPG programme is stored under for this channel. Lives in the domain because more
 * than one feature has to resolve "what is on now": duplicating the rule is how two surfaces
 * end up disagreeing about which schedule belongs to which channel.
 */
fun Channel.guideLookupKey(): String? {
    return streamId.takeIf { it > 0L }?.toString()
        ?: epgChannelId?.trim()?.takeIf { it.isNotEmpty() }
}
