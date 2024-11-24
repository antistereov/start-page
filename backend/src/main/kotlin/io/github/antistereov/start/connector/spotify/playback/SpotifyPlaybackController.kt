package io.github.antistereov.start.connector.spotify.playback

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/spotify/me/player")
class SpotifyPlaybackController(
    private val service: SpotifyPlaybackService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/currently_playing")
    suspend fun getCurrentSong(authentication: Authentication): String {
        logger.info { "Executing Spotify getCurrentSong method." }

        val userId = principalService.getUserId(authentication)

        return service.getCurrentlyPlaying(userId)
    }

    @PostMapping("/next")
    suspend fun skipToNext(authentication: Authentication, @RequestParam("device_id") deviceId: String? = null): String {
        logger.info { "Executing Spotify skipToNext method" }

        val userId = principalService.getUserId(authentication)

        return service.skipToNext(userId, deviceId)
    }

    @PostMapping("/previous")
    suspend fun skipToPrevious(authentication: Authentication, @RequestParam("device_id") deviceId: String? = null): String {
        logger.info { "Executing Spotify skipToPrevious method" }

        val userId = principalService.getUserId(authentication)

        return service.skipToPrevious(userId, deviceId)
    }

    @PutMapping("/seek")
    suspend fun seekToPosition(
        authentication: Authentication,
        @RequestParam("position_ms") positionMs: Int,
        @RequestParam("device_id") deviceId: String? = null,
    ): String {
        logger.info { "Executing Spotify seekToPosition method" }

        val userId = principalService.getUserId(authentication)

        return service.seekToPosition(userId, positionMs, deviceId)
    }
}