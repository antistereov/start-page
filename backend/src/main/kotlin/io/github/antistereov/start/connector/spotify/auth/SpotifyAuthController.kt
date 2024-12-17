package io.github.antistereov.start.connector.spotify.auth

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.spotify.model.SpotifyUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/auth/spotify")
class SpotifyAuthController(
    private val spotifyAuthService: SpotifyAuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun connect(exchange: ServerWebExchange): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Spotify login method." }

        val userId = principalService.getUserId(exchange)
        val url = spotifyAuthService.getAuthorizationUrl(userId)

        return ResponseEntity.ok(
            mapOf("url" to url)
        )
    }

    @GetMapping("/callback")
    suspend fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
    ): ResponseEntity<SpotifyUserProfile> {
        logger.info {"Received Spotify callback with state: $state and error: $error." }

        val userProfile = spotifyAuthService.authenticate(code, state, error)

        logger.info { "Spotify authentication successful." }

        return ResponseEntity.ok(
            userProfile
        )
    }

    @DeleteMapping
    suspend fun disconnect(exchange: ServerWebExchange): ResponseEntity<Any> {
        logger.info { "Executing Spotify logout method" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            spotifyAuthService.disconnect(userId)
        )
    }

    @GetMapping("/access-token")
    suspend fun getAccessToken(exchange: ServerWebExchange): ResponseEntity<Map<String, String>> {
        logger.info { "Fetching Spotify access token" }

        val userId = principalService.getUserId(exchange)

        val accessToken = spotifyAuthService.getAccessToken(userId)

        return ResponseEntity.ok(
            mapOf("accessToken" to accessToken)
        )
    }

    @GetMapping("/me")
    suspend fun getUserProfile(exchange: ServerWebExchange): ResponseEntity<SpotifyUserProfile> {
        logger.info { "Fetching Spotify user profile" }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            spotifyAuthService.getUserProfile(userId)
        )
    }
}