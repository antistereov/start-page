package io.github.antistereov.start.connector.unsplash

import io.github.antistereov.start.connector.unsplash.auth.UnsplashAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

@Service
class UnsplashService(
    private val unsplashAuthService: UnsplashAuthService,
    private val properties: UnsplashProperties,
    private val webClient: WebClient,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashService::class.java)

    suspend fun getRandomPhoto(
        userId: String,
        params: Map<String, Any?> = emptyMap(),
    ): String {
        logger.debug("Getting random photo for user $userId")

        val accessToken = unsplashAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/random")
            .queryParam("client_id", properties.clientId)

        params.forEach { (key, value) ->
            if (value != null) uri.queryParam(key, value)
        }

        return webClient.get()
            .uri(uri.toUriString())
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun getPhoto(userId: String, id: String): String {
        logger.debug("Getting photo with id $id")

        val accessToken = unsplashAuthService.getAccessToken(userId)

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/$id")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return webClient.get()
            .uri(uri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun likePhoto(userId: String, photoId: String): String {
        logger.debug("Liking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        val accessToken = unsplashAuthService.getAccessToken(userId)

        return webClient.post()
            .uri(uri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }

    suspend fun unlikePhoto(userId: String, photoId: String): String {
        logger.debug("Unliking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        val accessToken = unsplashAuthService.getAccessToken(userId)

        return webClient.delete()
            .uri(uri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody()
    }
}