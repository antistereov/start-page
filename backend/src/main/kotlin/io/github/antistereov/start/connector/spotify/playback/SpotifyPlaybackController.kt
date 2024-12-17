package io.github.antistereov.start.connector.spotify.playback

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/spotify/me/player")
class SpotifyPlaybackController(
    private val service: SpotifyPlaybackService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping("/currently_playing")
    suspend fun getCurrentSong(exchange: ServerWebExchange): ResponseEntity<String> {
        logger.info { "Executing Spotify getCurrentSong method." }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.getCurrentlyPlaying(userId)
        )
    }

    @PostMapping("/next")
    suspend fun skipToNext(
        exchange: ServerWebExchange,
        @RequestParam("device_id") deviceId: String? = null
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify skipToNext method" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.skipToNext(userId, deviceId)
        )
    }

    @PostMapping("/previous")
    suspend fun skipToPrevious(
        exchange: ServerWebExchange,
        @RequestParam("device_id") deviceId: String? = null
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify skipToPrevious method" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.skipToPrevious(userId, deviceId)
        )
    }

    @PutMapping("/seek")
    suspend fun seekToPosition(
        exchange: ServerWebExchange,
        @RequestParam("position_ms") positionMs: Int,
        @RequestParam("device_id") deviceId: String? = null,
    ): ResponseEntity<String> {
        logger.info { "Executing Spotify seekToPosition method" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.seekToPosition(userId, positionMs, deviceId)
        )
    }
}