package io.github.antistereov.start.widgets.auth.unsplash.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.auth.unsplash.service.UnsplashAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/unsplash")
class UnsplashAuthController(
    private val tokenService: UnsplashAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(UnsplashAuthController::class.java)

    @GetMapping
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Unsplash login method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.getAuthorizationUrl(userId).map { url ->
                logger.info("Redirecting user $userId to Unsplash authorization URL.")

                "redirect:$url"
            }
        }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
        @RequestParam(name = "error_description") errorDescription: String?,
    ): Mono<String> {
        logger.info("Received Unsplash callback with code: $code, state: $state and error: $error.")

        return tokenService.authenticate(code, state, error, errorDescription)
            .map {
                logger.info("Unsplash authentication successful.")

                "Unsplash authentication successful."
            }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing Unsplash logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.logout(userId).then(Mono.fromCallable { "Unsplash user information deleted for user: $userId." })
        }
    }
}