package io.github.antistereov.start.widgets.unsplash.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.unsplash.service.UnsplashService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/unsplash")
class UnsplashController(
    private val unsplashService: UnsplashService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(UnsplashController::class.java)

    @GetMapping("/login")
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Unsplash login method.")

        return principalExtractor.getUserId(authentication)
            .map { userId ->
                logger.info("Redirecting user $userId to Unsplash authorization URL.")

                "redirect:${unsplashService.getAuthorizationUrl(userId)}"
            }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam(required = true) code: String,
        @RequestParam(required = true) state: String
    ): Mono<String> {
        logger.info("Received Unsplash callback with code: $code and state: $state")

        return unsplashService.authenticate(code, state)
            .map {
                logger.info("Unsplash authentication successful.")

                "Unsplash authentication successful."
            }
    }

    @GetMapping("/photo")
    fun getRandomPhoto(@RequestParam query: String? = null): Mono<String> {
        logger.info("Executing Unsplash getRandomPhoto with method query: ${query}.")

        return unsplashService.getRandomPhoto(query)
    }

    @GetMapping("/photo/{id}")
    fun getPhoto(@PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash getPhoto method with id: ${id}.")

        return unsplashService.getPhoto(id)
    }

    @PostMapping("photo/{id}/like")
    fun likePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash likePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                unsplashService.getAccessToken(userId)
                    .flatMap { accessToken ->
                        unsplashService.likePhoto(accessToken, id)
                    }
            }
    }

    @DeleteMapping("photo/{id}/like")
    fun unlikePhoto(authentication: Authentication, @PathVariable id: String): Mono<String> {
        logger.info("Executing Unsplash unlikePhoto method with id: ${id}.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                unsplashService.getAccessToken(userId)
                    .flatMap { accessToken ->
                        unsplashService.unlikePhoto(accessToken, id)
                    }
            }
    }
}