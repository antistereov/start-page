package io.github.antistereov.start.widgets.unsplash.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.unsplash.service.UnsplashApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/unsplash")
class UnsplashApiController(
    private val apiService: UnsplashApiService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(UnsplashApiController::class.java)

    @GetMapping("/photo")
    fun getRandomPhoto(@RequestParam query: String? = null): Mono<String> {
        logger.info("Executing Unsplash getRandomPhoto with method query: ${query}.")

        return apiService.getRandomPhoto(query)
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