package io.github.antistereov.start.widget.unsplash

import io.github.antistereov.start.widget.unsplash.auth.UnsplashAuthService
import io.github.antistereov.start.widget.unsplash.model.UnsplashPhoto
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

    suspend fun getRandomPhoto(userId: String, query: String? = null): UnsplashPhoto {
        logger.debug("Getting random photo for user $userId")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/random")
            .queryParam("client_id", properties.clientId)
            .queryParam("orientation", "landscape")

        if (query != null) uri.queryParam("query", query)

        return webClient.get()
            .uri(uri.toUriString())
            .retrieve()
            .awaitBody<UnsplashPhoto>()
    }

    suspend fun getPhoto(id: String): UnsplashPhoto {
        logger.debug("Getting photo with id $id")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/$id")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return webClient.get()
            .uri(uri)
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

    suspend fun getNewRandomPhotoUrlForScreen(
        userId: String,
        query: String? = null,
        screenWidth: Int,
        screenHeight: Int,
        quality: Int = 85
    ): String {
        logger.debug("Getting new random photo for user $userId")

        val randomPhoto = getRandomPhoto(userId, query)

        val width = calculateMinimumPictureWidth(randomPhoto.width, randomPhoto.height, screenWidth, screenHeight)

        return UriComponentsBuilder.fromHttpUrl(randomPhoto.imageUrl)
            .queryParam("w", width)
            .queryParam("q", quality)
            .toUriString()
    }

    private fun calculateMinimumPictureWidth(pictureWidth: Int, pictureHeight: Int, screenWidth: Int, screenHeight: Int): Int {
        logger.debug("Calculating minimum picture width")

        val screenAspectRatio = screenWidth.toDouble() / screenHeight
        val pictureAspectRatio = pictureWidth.toDouble() / pictureHeight

        return if (screenAspectRatio > pictureAspectRatio) {
            screenWidth
        } else {
            (screenHeight * pictureAspectRatio).toInt()
        }
    }
}