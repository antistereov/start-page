package io.github.antistereov.start.widget.spotify.auth

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widget.spotify.model.SpotifyUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/spotify")
class SpotifyAuthController(
    private val tokenService: SpotifyAuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun login(authentication: Authentication): String {
        logger.info { "Executing Spotify login method." }

        val userId = principalService.getUserId(authentication)
        val url = tokenService.getAuthorizationUrl(userId)
        logger.info { "Redirecting user $userId to Spotify authorization URL: $url." }

        return "{ \"url\": $url }"
    }

    @GetMapping("/callback")
    suspend fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
    ): SpotifyUserProfile {
        logger.info {"Received Spotify callback with state: $state and error: $error." }

        val userProfile = tokenService.authenticate(code, state, error)

        logger.info { "Spotify authentication successful." }

        return userProfile
    }

    @DeleteMapping
    suspend fun logout(authentication: Authentication) {
        logger.info { "Executing Spotify logout method" }

        val userId = principalService.getUserId(authentication)

        tokenService.logout(userId)
    }
}