package io.github.antistereov.start.widgets.spotify.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.spotify.service.SpotifyApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/spotify")
class SpotifyApiController(
    private val apiService: SpotifyApiService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(SpotifyApiController::class.java)

    @GetMapping("/me/player/currently_playing")
    fun getCurrentSong(authentication: Authentication): Mono<String> {
        logger.info("Executing Spotify getCurrentSong method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                apiService.getCurrentlyPlaying(userId)
            }
    }
}