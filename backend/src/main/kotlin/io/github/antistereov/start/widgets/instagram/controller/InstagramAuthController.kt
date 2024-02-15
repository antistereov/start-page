package io.github.antistereov.start.widgets.instagram.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.instagram.model.InstagramLongLivedTokenResponse
import io.github.antistereov.start.widgets.instagram.service.InstagramAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

@Controller
@RequestMapping("/api/instagram")
class InstagramAuthController(
    private val instagramAuthService: InstagramAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(InstagramAuthController::class.java)

    @GetMapping("/login")
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Instagram login method.")

        return principalExtractor.getUserId(authentication)
            .map { userId ->
                val url = instagramAuthService.getAuthorizationUrl(userId)
                logger.info("Redirecting user $userId to Instagram authorization URL: $url.")

                "redirect:$url"
            }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam(required = true) code: String,
        @RequestParam(required = true) state: String
    ): Mono<String> {
        logger.info("Received Instagram callback with code: $code and state: $state")

        return instagramAuthService.authenticate(code, state)
            .map {
                logger.info("Instagram authentication successful.")

                "Instagram authentication successful."
            }
    }

    @GetMapping("/refresh")
    fun refreshAccessToken(authentication: Authentication): Mono<InstagramLongLivedTokenResponse> {
        logger.info("Executing Instagram refreshAccessToken method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                instagramAuthService.refreshToken(userId)
            }
    }

}