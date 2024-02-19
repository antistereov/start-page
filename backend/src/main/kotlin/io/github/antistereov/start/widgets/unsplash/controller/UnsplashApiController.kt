package io.github.antistereov.start.widgets.unsplash.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.unsplash.model.Photo
import io.github.antistereov.start.widgets.unsplash.service.UnsplashApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/unsplash")
class UnsplashApiController(
    private val apiService: UnsplashApiService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashApiController::class.java)

    @GetMapping("/photo")
    fun getRandomPhoto(authentication: Authentication, @RequestParam query: String? = null): Mono<Photo> {
        logger.info("Executing Unsplash getRandomPhoto with method query: ${query}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                apiService.getRandomPhoto(userId, query)
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
                apiService.getNewRandomPhotoUrlForScreen(userId, query, width, height, quality)
            }
    }

    @GetMapping("/photo/recent")
    fun getRecentPhotos(authentication: Authentication): Flux<Photo> {
        logger.info("Executing Unsplash getRecentPhotos method.")

        return principalExtractor.getUserId(authentication)
            .flatMapMany { userId ->
                apiService.getRecentPhotos(userId)
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
                apiService.getRecentPhotoUrlForScreen(userId, index, width, height, quality)
            }
    }

    @GetMapping("/photo/{id}")
    fun getPhoto(@PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash getPhoto method with id: ${id}.")

        return apiService.getPhoto(id)
    }

    @PostMapping("photo/{id}/like")
    fun likePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash likePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                apiService.likePhoto(userId, id)
            }
    }

    @DeleteMapping("photo/{id}/like")
    fun unlikePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash unlikePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { accessToken ->
                apiService.unlikePhoto(accessToken, id)
            }
    }
}