package io.github.antistereov.start.widgets.spotify.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.spotify.model.SpotifyTokenResponse
import io.github.antistereov.start.widgets.spotify.service.SpotifyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/spotify")
class SpotifyController(
    private val spotifyService: SpotifyService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(SpotifyController::class.java)

    @GetMapping("/login")
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify login method.")

        return principalExtractor.getUserId(authentication)
            .map { userId ->
                logger.info("Redirecting user $userId to authorization URL.")

                "redirect:${spotifyService.getAuthorizationUrl(userId)}"
            }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam(required = true) code: String,
        @RequestParam(required = true) state: String
    ): Mono<String> {
        logger.info("Received Spotify callback with code: $code and state: $state")

        return spotifyService.authenticate(code, state)
            .map {
                logger.info("Spotify authentication successful.")

                "Authentication successful."
            }
    }

    @GetMapping("/refresh")
    fun refreshAccessToken(authentication: Authentication): Mono<SpotifyTokenResponse> {
        logger.info("Executing Spotify refreshAccessToken method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                spotifyService.refreshToken(userId)
            }
    }

    @GetMapping("/current-song")
    fun getCurrentSong(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify getCurrentSong method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                spotifyService.getAccessToken(userId)
                    .flatMap {  accessToken ->
                        spotifyService.getCurrentSong(accessToken) }
            }
    }
}