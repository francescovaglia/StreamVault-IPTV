package com.streamvault.feature.playback.player

import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.guideLookupKey
import com.streamvault.domain.model.Result
import com.streamvault.domain.repository.EpgRepository
import com.streamvault.domain.repository.ProviderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EPG_REFRESH_INTERVAL_MS = 30_000L

/** Owns the refresh and fallback lifecycle for the player guide timeline. */
class PlayerEpgCoordinator @Inject constructor(
    private val epgRepository: EpgRepository,
    private val providerRepository: ProviderRepository
) {
    private data class RequestIdentity(
        val sessionId: Long,
        val requestKey: EpgRequestKey,
        val fallbackKeys: List<EpgRequestKey>
    )

    private var activeRequest: RequestIdentity? = null
    private var refreshJob: Job? = null

    internal fun request(
        scope: CoroutineScope?,
        sessionId: Long,
        requestKey: EpgRequestKey,
        onPrograms: (List<Program>, Long) -> Unit,
        onClear: () -> Unit,
        fallbackKeys: List<EpgRequestKey> = emptyList()
    ) {
        val requestIdentity = RequestIdentity(sessionId = sessionId, requestKey = requestKey, fallbackKeys = fallbackKeys)
        if (requestIdentity == activeRequest && refreshJob?.isActive == true) return
        refreshJob?.cancel()
        activeRequest = requestIdentity
        refreshJob = scope?.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val programs = resolvePrograms(requestKey, now).ifEmpty {
                    fallbackKeys.firstNotNullOfOrNull { key -> localPrograms(key, now).takeIf { it.isNotEmpty() } }.orEmpty()
                }
                if (activeRequest != requestIdentity) return@launch
                if (programs.isEmpty()) {
                    onClear()
                } else {
                    onPrograms(programs, now)
                }
                delay(EPG_REFRESH_INTERVAL_MS)
            }
        }
    }

    internal fun clear(onClear: () -> Unit) {
        refreshJob?.cancel()
        refreshJob = null
        activeRequest = null
        onClear()
    }

    internal fun cancel() {
        refreshJob?.cancel()
        refreshJob = null
        activeRequest = null
    }

    /** The programme on air right now for each channel, keyed by guideLookupKey. Local guide only. */
    internal suspend fun nowPlaying(channels: List<Channel>, now: Long): Map<String, Program> = buildMap {
        channels.groupBy(Channel::providerId).forEach { (providerId, providerChannels) ->
            epgRepository.getResolvedProgramsForChannels(
                providerId = providerId,
                channelIds = providerChannels.map(Channel::id),
                startTime = now - (60L * 60L * 1000L),
                endTime = now + (2L * 60L * 60L * 1000L)
            ).forEach { (lookupKey, programs) ->
                programs.firstOrNull { it.startTime <= now && it.endTime > now }?.let { put(lookupKey, it) }
            }
        }
    }

    private suspend fun localPrograms(requestKey: EpgRequestKey, now: Long): List<Program> =
        epgRepository.getResolvedProgramsForPlaybackChannel(
            providerId = requestKey.providerId,
            internalChannelId = requestKey.internalChannelId,
            epgChannelId = requestKey.epgChannelId,
            streamId = requestKey.streamId,
            startTime = now - (24 * 60 * 60 * 1000L),
            endTime = now + (6 * 60 * 60 * 1000L)
        )

    private suspend fun resolvePrograms(requestKey: EpgRequestKey, now: Long): List<Program> {
        val localPrograms = localPrograms(requestKey, now)
        if (localPrograms.isNotEmpty()) return localPrograms
        if (requestKey.streamId <= 0L) return emptyList()

        return when (
            val result = providerRepository.getProgramsForLiveStream(
                providerId = requestKey.providerId,
                streamId = requestKey.streamId,
                epgChannelId = requestKey.epgChannelId,
                limit = 12
            )
        ) {
            is Result.Success -> result.data.sortedBy { it.startTime }
            else -> emptyList()
        }
    }
}
