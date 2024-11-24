package io.github.antistereov.start.connector.spotify.playback

import io.github.antistereov.start.connector.spotify.auth.SpotifyAuthService
import io.github.antistereov.start.connector.spotify.auth.SpotifyProperties
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

@Service
class SpotifyPlaybackService(
    private val spotifyAuthService: SpotifyAuthService,
    private val properties: SpotifyProperties,
    private val webClient: WebClient,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getCurrentlyPlaying(userId: String): String {
        logger.debug { "Getting currently playing song for user: $userId." }

        val uri = "${properties.apiBaseUrl}/me/player/currently-playing"

        val accessToken = spotifyAuthService.getAccessToken(userId)

        return webClient.get()
            .uri(uri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }



    suspend fun startResumePlayback(
        userId: String,
        deviceId: String? = null,
        contextUri: String? = null,
        offset: Int? = null,
        positionMs: Int = 0,
    ): String {
        val accessToken = spotifyAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromUriString("${properties.apiBaseUrl}/me/player/play")

        // TODO: Add body

        return webClient.put()
            .uri(uri.toUriString())
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun skipToNext(userId: String, deviceId: String? = null): String {
        logger.debug { "Skipping track for user: $userId" }

        val accessToken = spotifyAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromUriString("${properties.apiBaseUrl}/me/player/next")

        if (deviceId != null) {
            uri.queryParam("device_id", deviceId)
        }

        return webClient.post()
            .uri(uri.toUriString())
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun skipToPrevious(userId: String, deviceId: String? = null): String {
        logger.debug { "Skipping to previous track for user: $userId" }

        val accessToken = spotifyAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromUriString("${properties.apiBaseUrl}/me/player/previous")

        if (deviceId != null) {
            uri.queryParam("device_id", deviceId)
        }

        return webClient.post()
            .uri(uri.toUriString())
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun seekToPosition(userId: String, positionMs: Int, deviceId: String? = null): String {
        logger.debug { "Seeking playback position $positionMs for user $userId" }

        val accessToken = spotifyAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromUriString("${properties.apiBaseUrl}/me/player/seek")
            .queryParam("position_ms", positionMs)

        if (deviceId != null) {
            uri.queryParam("device_id", deviceId)
        }

        return webClient.put()
            .uri(uri.toUriString())
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }
}