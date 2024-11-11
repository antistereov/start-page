package io.github.antistereov.start.widget.spotify

import io.github.antistereov.start.auth.service.PrincipalService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/spotify")
class SpotifyController(
    private val service: SpotifyService,
    private val principalService: PrincipalService,
) {

    private val logger: Logger = LoggerFactory.getLogger(SpotifyController::class.java)

    @GetMapping("/me/player/currently_playing")
    suspend fun getCurrentSong(authentication: Authentication): String {
        logger.info("Executing Spotify getCurrentSong method.")

        val userId = principalService.getUserId(authentication)

        return service.getCurrentlyPlaying(userId)
    }
}