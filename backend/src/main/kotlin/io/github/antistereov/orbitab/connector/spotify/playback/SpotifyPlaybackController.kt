package io.github.antistereov.orbitab.connector.spotify.playback

import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
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
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/currently_playing")
    suspend fun getCurrentSong(): ResponseEntity<String> {
        logger.info { "Executing Spotify getCurrentSong method." }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.getCurrentlyPlaying(userId)
        )
    }

    @PostMapping("/next")
    suspend fun skipToNext(
        @RequestParam("device_id") deviceId: String? = null
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify skipToNext method" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.skipToNext(userId, deviceId)
        )
    }

    @PostMapping("/previous")
    suspend fun skipToPrevious(
        @RequestParam("device_id") deviceId: String? = null
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify skipToPrevious method" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.skipToPrevious(userId, deviceId)
        )
    }

    @PutMapping("/seek")
    suspend fun seekToPosition(
        @RequestParam("position_ms") positionMs: Int,
        @RequestParam("device_id") deviceId: String? = null,
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify seekToPosition method" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.seekToPosition(userId, positionMs, deviceId)
        )
    }
}