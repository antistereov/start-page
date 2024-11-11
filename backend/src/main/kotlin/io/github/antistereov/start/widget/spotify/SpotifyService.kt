package io.github.antistereov.start.widget.spotify

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widget.spotify.auth.SpotifyAuthService
import io.github.antistereov.start.widget.spotify.auth.SpotifyProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SpotifyService(
    private val tokenService: SpotifyAuthService,
    private val baseService: BaseService,
    private val properties: SpotifyProperties,
) {

    private val logger = LoggerFactory.getLogger(SpotifyService::class.java)

    fun getCurrentlyPlaying(userId: String): Mono<String> {
        logger.debug("Getting currently playing song for user: $userId.")

        val uri = "${properties.apiBaseUrl}/me/player/currently-playing"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }
}