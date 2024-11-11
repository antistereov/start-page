package io.github.antistereov.start.connector.spotify

import io.github.antistereov.start.connector.spotify.auth.SpotifyAuthService
import io.github.antistereov.start.connector.spotify.auth.SpotifyProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class SpotifyService(
    private val tokenService: SpotifyAuthService,
    private val properties: SpotifyProperties,
    private val webClient: WebClient,
) {

    private val logger = LoggerFactory.getLogger(SpotifyService::class.java)

    suspend fun getCurrentlyPlaying(userId: String): String {
        logger.debug("Getting currently playing song for user: $userId.")

        val uri = "${properties.apiBaseUrl}/me/player/currently-playing"

        val accessToken = tokenService.getAccessToken(userId)

        return webClient.get()
            .uri(uri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }
}