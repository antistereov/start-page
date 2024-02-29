package io.github.antistereov.start.widgets.widget.unsplash.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.unsplash.config.UnsplashProperties
import io.github.antistereov.start.widgets.auth.unsplash.service.UnsplashAuthService
import io.github.antistereov.start.widgets.widget.unsplash.model.Photo
import io.github.antistereov.start.widgets.widget.unsplash.model.UnsplashWidget
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashService(
    private val tokenService: UnsplashAuthService,
    private val baseService: BaseService,
    private val properties: UnsplashProperties,
    private val userService: UserService,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashService::class.java)

    fun getRandomPhoto(userId: String, query: String? = null): Mono<Photo> {
        logger.debug("Getting random photo for user $userId")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/random")
            .queryParam("client_id", properties.clientId)
            .queryParam("orientation", "landscape")

        if (query != null) uri.queryParam("query", query)

        return baseService.getMono(uri.toUriString()).flatMap { response ->
            saveRecentPicture(userId, response)
        }
    }

    fun getPhoto(id: String): Mono<String> {
        logger.debug("Getting photo with id $id")

        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/$id")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return baseService.getMono(uri)
    }

    fun likePhoto(userId: String, photoId: String): Mono<String> {
        logger.debug("Liking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.postMono(uri, accessToken)
        }
    }

    fun unlikePhoto(userId: String, photoId: String): Mono<String> {
        logger.debug("Unliking photo with id $photoId for user $userId")

        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.deleteMono(uri, accessToken)
        }
    }

    fun getRecentPhotos(userId: String): Mono<List<Photo>> {
        logger.debug("Getting recent photos for user $userId")

        return userService.findById(userId).map { user ->
            user.widgets.unsplash.recentPhotos
        }
    }

    fun getNewRandomPhotoUrlForScreen(
        userId: String,
        query: String? = null,
        screenWidth: Int,
        screenHeight: Int,
        quality: Int = 85
    ): Mono<String> {
        logger.debug("Getting new random photo for user $userId")

        return getRandomPhoto(userId, query).then(
            getRecentPhotoUrlForScreen(userId, 0, screenWidth, screenHeight, quality)
        )
    }

    fun getRecentPhotoUrlForScreen(
        userId: String,
        index: Int,
        screenWidth: Int,
        screenHeight: Int,
        quality: Int = 85
    ): Mono<String> {
        logger.debug("Getting recent photo for user $userId at index $index")

        return userService.findById(userId).map { user ->
            val photo = user.widgets.unsplash.recentPhotos.getOrElse(index) {
                throw IllegalArgumentException("No recent pictures found for user $userId at index $index")
            }
            val width = calculateMinimumPictureWidth(photo.width, photo.height, screenWidth, screenHeight)
            UriComponentsBuilder.fromHttpUrl(photo.imageUrl)
                .queryParam("w", width)
                .queryParam("q", quality)
                .toUriString()
        }
    }

    fun clearUnsplashWidget(userId: String): Mono<String> {
        logger.debug("Clearing Unsplash widget for user $userId")

        return userService.findById(userId).flatMap { user ->
            user.widgets.unsplash = UnsplashWidget()
            userService.save(user).thenReturn("Unsplash widget cleared.")
        }
    }

    private fun saveRecentPicture(userId: String, response: String): Mono<Photo> {
        logger.debug("Saving recent picture for user $userId")

        return userService.findById(userId).flatMap { user ->
            val photo = Photo(
                baseService.extractField(response, "id"),
                baseService.extractField(response, "urls", "raw"),
                baseService.extractField(response, "width").toInt(),
                baseService.extractField(response, "height").toInt()
            )
            if (user.widgets.unsplash.recentPhotos.size >= 30) {
                user.widgets.unsplash.recentPhotos.removeAt(user.widgets.unsplash.recentPhotos.size - 1)
            }
            user.widgets.unsplash.recentPhotos.add(0, photo)
            userService.save(user).thenReturn(photo)
        }
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

    // TODO check if portrait, square or landscape
}
