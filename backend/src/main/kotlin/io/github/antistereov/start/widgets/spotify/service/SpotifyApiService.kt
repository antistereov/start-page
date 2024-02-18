package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.spotify.config.SpotifyProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SpotifyApiService(
    private val tokenService: SpotifyTokenService,
    private val baseService: BaseService,
    private val properties: SpotifyProperties,
) {

    fun getCurrentlyPlaying(userId: String): Mono<String> {
        val uri = "${properties.apiBaseUrl}/me/player/currently-playing"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }
}