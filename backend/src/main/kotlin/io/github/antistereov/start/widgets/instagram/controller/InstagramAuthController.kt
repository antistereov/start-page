package io.github.antistereov.start.widgets.instagram.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.instagram.service.InstagramAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/instagram")
class InstagramAuthController(
    private val instagramAuthService: InstagramAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(InstagramAuthController::class.java)

    @GetMapping("/login")
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram login method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            instagramAuthService.getAuthorizationUrl(userId).map { url ->
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
        logger.info("Received Instagram callback with code: $code and state: $state")

        return instagramAuthService.authenticate(code, state, error, errorReason, errorDescription)
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
                instagramAuthService.refreshAccessToken(userId)
                    .map { "Successfully refreshed Instagram access token for user: $userId." }
            }
    }

    @GetMapping("/user/update")
    fun updateUserInfo(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram updateUserInfo method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            instagramAuthService.updateUserInfo(userId)
                .map {
                    logger.info("Successfully updated Instagram user information for user: $userId")

                    "Successfully updated Instagram user information for user: $userId"
                }
        }
    }

}