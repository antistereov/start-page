package io.github.antistereov.start.widgets.auth.instagram.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.auth.instagram.service.InstagramAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/instagram")
class InstagramAuthController(
    private val tokenService: InstagramAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(InstagramAuthController::class.java)

    @GetMapping
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing login method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.getAuthorizationUrl(userId).map { url ->
                logger.info("Redirecting user $userId to Instagram authorization URL: $url.")

                "redirect:$url"
            }
        }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
        @RequestParam errorReason: String?,
        @RequestParam errorDescription: String?,
    ): Mono<String> {
        logger.info("Received callback with state: $state and error: $error.")

        return tokenService.authenticate(code, state, error, errorReason, errorDescription)
            .map {
                logger.info("Instagram authentication successful.")

                "Instagram authentication successful."
            }
    }

    @GetMapping("/refresh")
    fun refreshAccessToken(authentication: Authentication): Mono<String> {
        logger.info("Executing refreshAccessToken method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                tokenService.refreshAccessToken(userId)
                    .map {
                        logger.info("Successfully refreshed access token for user: $userId.")

                        "Successfully refreshed Instagram access token for user: $userId."
                    }
            }
    }

    @PostMapping
    fun saveAccessToken(
        authentication: Authentication,
        @RequestBody accessToken: String
    ): Mono<String> {
        logger.info("Executing saveAccessToken method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.saveAccessToken(userId, accessToken)
                .map {
                    logger.info("Successfully saved access token for user: $userId")

                    "Successfully saved access token for user: $userId"
                }
        }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.logout(userId).then(Mono.fromCallable { "Instagram user information deleted for user: $userId." })
        }
    }

}