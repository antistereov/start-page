package io.github.antistereov.start.widgets.auth.spotify.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widgets.auth.spotify.service.SpotifyAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/spotify")
class SpotifyAuthController(
    private val tokenService: SpotifyAuthService,
    private val principalExtractor: PrincipalService,
) {

    private val logger: Logger = LoggerFactory.getLogger(SpotifyAuthController::class.java)

    @GetMapping
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify login method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.getAuthorizationUrl(userId).map { url ->
                logger.info("Redirecting user $userId to Spotify authorization URL: $url.")

                "redirect:$url"
            }
        }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
    ): Mono<String> {
        logger.info("Received Spotify callback with state: $state and error: $error.")

        return tokenService.authenticate(code, state, error)
            .map {
                logger.info("Spotify authentication successful.")

                "Spotify authentication successful."
            }
    }

    @GetMapping("/refresh")
    fun refreshAccessToken(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify refreshAccessToken method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                tokenService.refreshToken(userId). map {
                    logger.info("Successfully refreshed Instagram access token for user: $userId.")

                    "Successfully refreshed Spotify access token for user: $userId."
                }
            }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.logout(userId).then(Mono.fromCallable { "Spotify user information deleted for user: $userId." })
        }
    }
}
