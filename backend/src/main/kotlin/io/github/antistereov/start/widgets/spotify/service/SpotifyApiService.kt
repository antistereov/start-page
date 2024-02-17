package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.global.service.BaseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SpotifyApiService(
    private val tokenService: SpotifyTokenService,
    private val baseService: BaseService,
) {

    @Value("\${spotify.apiBaseUrl}")
    private lateinit var apiBaseUrl: String

    fun getCurrentlyPlaying(userId: String): Mono<String> {
        val uri = "$apiBaseUrl/me/player/currently-playing"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }
}