package io.github.antistereov.orbitab.connector.unsplash

import io.github.antistereov.orbitab.connector.unsplash.auth.UnsplashAuthService
import io.github.antistereov.orbitab.connector.unsplash.exception.UnsplashApiException
import io.github.antistereov.orbitab.connector.unsplash.exception.UnsplashRateLimitException
import io.github.antistereov.orbitab.connector.unsplash.model.UnsplashPhoto
import io.github.antistereov.orbitab.connector.unsplash.model.UnsplashPhotoApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

@Service
class UnsplashService(
    private val unsplashAuthService: UnsplashAuthService,
    private val properties: UnsplashProperties,
    private val webClient: WebClient,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashService::class.java)

    private suspend inline fun <reified T: Any> handleApiRequest(
        uri: String,
        userId: String,
        requestBuilder: WebClient.RequestHeadersSpec<*>,
    ): T {
        val accessToken = unsplashAuthService.getAccessToken(userId)

        return try {
            requestBuilder
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .onStatus({status -> status.isError}) { response ->
                    throw UnsplashApiException(
                        uri,
                        response.statusCode(),
                        "Request failed with status ${response.statusCode()}"
                    )
                }
                .awaitBody<T>()
        } catch (ex: WebClientResponseException.TooManyRequests) {
            throw UnsplashRateLimitException(
                uri,
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Please try again later."
            )
        } catch (ex: WebClientResponseException) {
            throw UnsplashApiException(
                uri,
                ex.statusCode,
                "Unexpected API error: ${ex.message}"
            )
        }
    }

    suspend fun getRandomPhoto(
        userId: String,
        screenWidth: Int,
        screenHeight: Int,
        quality: Int,
    ): UnsplashPhoto {
        logger.debug("Getting random photo for user $userId")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/random")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return handleApiRequest<UnsplashPhotoApiResponse>(
            uri,
            userId,
            webClient.get().uri(uri)
        ).toUnsplashPhoto(screenWidth, screenHeight, quality)
    }

    suspend fun getPhoto(
        userId: String,
        id: String,
        screenWidth: Int,
        screenHeight: Int,
        quality: Int,
    ): UnsplashPhoto {
        logger.debug("Getting photo with id $id")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/$id")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return handleApiRequest<UnsplashPhotoApiResponse>(
            uri,
            userId,
            webClient.get().uri(uri)
        ).toUnsplashPhoto(screenWidth, screenHeight, quality)
    }

    suspend fun likePhoto(userId: String, photoId: String) {
        logger.debug("Liking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"

        handleApiRequest<Any>(
            uri,
            userId,
            webClient.post().uri(uri)
        )
    }

    suspend fun unlikePhoto(userId: String, photoId: String) {
        logger.debug("Unliking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"

        handleApiRequest<Any>(
            uri,
            userId,
            webClient.delete().uri(uri)
        )
    }
}