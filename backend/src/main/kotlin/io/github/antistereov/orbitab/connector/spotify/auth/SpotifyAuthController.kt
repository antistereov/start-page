package io.github.antistereov.orbitab.connector.spotify.auth

import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.connector.spotify.model.SpotifyUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/spotify")
class SpotifyAuthController(
    private val spotifyAuthService: SpotifyAuthService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun connect(): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Spotify login method." }

        val userId = authenticationService.getCurrentUserId()
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
    suspend fun disconnect(): ResponseEntity<Any> {
        logger.info { "Executing Spotify logout method" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            spotifyAuthService.disconnect(userId)
        )
    }

    @GetMapping("/access-token")
    suspend fun getAccessToken(): ResponseEntity<Map<String, String>> {
        logger.info { "Fetching Spotify access token" }

        val userId = authenticationService.getCurrentUserId()

        val accessToken = spotifyAuthService.getAccessToken(userId)

        return ResponseEntity.ok(
            mapOf("accessToken" to accessToken)
        )
    }

    @GetMapping("/me")
    suspend fun getUserProfile(): ResponseEntity<SpotifyUserProfile?> {
        logger.info { "Fetching Spotify user profile" }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            spotifyAuthService.getUserProfile(userId)
        )
    }
}