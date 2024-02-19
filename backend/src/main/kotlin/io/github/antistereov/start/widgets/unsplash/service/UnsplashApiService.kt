package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.unsplash.config.UnsplashProperties
import io.github.antistereov.start.widgets.unsplash.model.Photo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UnsplashApiService(
    private val tokenService: UnsplashTokenService,
    private val baseService: BaseService,
    private val properties: UnsplashProperties,
    private val userRepository: UserRepository,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashApiService::class.java)

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

    fun getRecentPhotos(userId: String): Flux<Photo> {
        logger.debug("Getting recent photos for user $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMapMany { user ->
            Flux.fromIterable(user.unsplash.recentPhotos)
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

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                val photo = user.unsplash.recentPhotos.getOrElse(index) {
                    throw IllegalArgumentException("No recent pictures found for user $userId at index $index")
                }
                val width = calculateMinimumPictureWidth(photo.width, photo.height, screenWidth, screenHeight)
                UriComponentsBuilder.fromHttpUrl(photo.imageUrl)
                    .queryParam("w", width)
                    .queryParam("q", quality)
                    .toUriString()
            }
    }

    private fun saveRecentPicture(userId: String, response: String): Mono<Photo> {
        logger.debug("Saving recent picture for user $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val photo = Photo(
                    baseService.extractField(response, "id"),
                    baseService.extractField(response, "urls", "raw"),
                    baseService.extractField(response, "width").toInt(),
                    baseService.extractField(response, "height").toInt()
                )
                if (user.unsplash.recentPhotos.size >= 30) {
                    user.unsplash.recentPhotos.removeAt(user.unsplash.recentPhotos.size - 1)
                }
                user.unsplash.recentPhotos.add(0, photo)
                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(photo)
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
