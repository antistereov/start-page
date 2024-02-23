package io.github.antistereov.start.widgets.widget.unsplash.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.unsplash.model.Photo
import io.github.antistereov.start.widgets.widget.unsplash.service.UnsplashService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/unsplash")
class UnsplashController(
    private val service: UnsplashService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashController::class.java)

    @GetMapping("/photo")
    fun getRandomPhoto(authentication: Authentication, @RequestParam query: String? = null): Mono<Photo> {
        logger.info("Executing Unsplash getRandomPhoto with method query: ${query}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getRandomPhoto(userId, query)
            }
    }

    @GetMapping("/photo/url")
    fun getRandomPhotoUrlForScreen(
        authentication: Authentication,
        @RequestParam(required = false) query: String? = null,
        @RequestParam(required = true) width: Int,
        @RequestParam(required = true) height: Int,
        @RequestParam(required = false) quality: Int = 85,
    ): Mono<String> {
        logger.info("Executing Unsplash getRandomPhotoUrlForScreen method with query: ${query}, width: ${width}, height: ${height}, quality: ${quality}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getNewRandomPhotoUrlForScreen(userId, query, width, height, quality)
            }
    }

    @GetMapping("/photo/recent")
    fun getRecentPhotos(authentication: Authentication): Mono<List<Photo>> {
        logger.info("Executing Unsplash getRecentPhotos method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getRecentPhotos(userId)
            }
    }

    @GetMapping("/photo/recent/url")
    fun getRecentPhotoUrlForScreen(
        authentication: Authentication,
        @RequestParam(required = true) index: Int,
        @RequestParam(required = true) width: Int,
        @RequestParam(required = true) height: Int,
        @RequestParam(required = false) quality: Int = 85,
    ): Mono<String> {
        logger.info("Executing Unsplash getRecentPhotoUrlForScreen method with index: ${index}, width: ${width}, height: ${height}, quality: ${quality}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getRecentPhotoUrlForScreen(userId, index, width, height, quality)
            }
    }

    @GetMapping("/photo/{id}")
    fun getPhoto(@PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash getPhoto method with id: ${id}.")

        return service.getPhoto(id)
    }

    @PostMapping("photo/{id}/like")
    fun likePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash likePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.likePhoto(userId, id)
            }
    }

    @DeleteMapping("photo/{id}/like")
    fun unlikePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash unlikePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { accessToken ->
                service.unlikePhoto(accessToken, id)
            }
    }
}