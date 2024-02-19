package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.spotify.config.SpotifyProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SpotifyApiService(
    private val tokenService: SpotifyTokenService,
    private val baseService: BaseService,
    private val properties: SpotifyProperties,
) {

    private val logger = LoggerFactory.getLogger(SpotifyApiService::class.java)

    fun getCurrentlyPlaying(userId: String): Mono<String> {
        logger.debug("Getting currently playing song for user: $userId.")

        val uri = "${properties.apiBaseUrl}/me/player/currently-playing"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }
}