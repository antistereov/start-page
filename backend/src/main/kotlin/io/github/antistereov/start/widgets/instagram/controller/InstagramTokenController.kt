package io.github.antistereov.start.widgets.instagram.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.instagram.service.InstagramTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/instagram/auth")
class InstagramTokenController(
    private val tokenService: InstagramTokenService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(InstagramTokenController::class.java)

    @GetMapping
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram login method.")

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
        logger.info("Received Instagram callback with code: $code, state: $state and error: $error.")

        return tokenService.authenticate(code, state, error, errorReason, errorDescription)
            .map {
                logger.info("Instagram authentication successful.")

                "Instagram authentication successful."
            }
    }

    @GetMapping("/refresh")
    fun refreshAccessToken(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram refreshAccessToken method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                tokenService.refreshAccessToken(userId)
                    .map {
                        logger.info("Successfully refreshed Instagram access token for user: $userId.")

                        "Successfully refreshed Instagram access token for user: $userId."
                    }
            }
    }

    @GetMapping("/user/update")
    fun updateUserInfo(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram updateUserInfo method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.updateUserInfo(userId)
                .map {
                    logger.info("Successfully updated Instagram user information for user: $userId")

                    "Successfully updated Instagram user information for user: $userId"
                }
        }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.logout(userId).then(Mono.fromCallable { "Instagram user information deleted for user: $userId." })
        }
    }

}