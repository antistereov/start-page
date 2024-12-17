package io.github.antistereov.start.connector.unsplash.auth

import io.github.antistereov.start.auth.service.AuthenticationService
import io.github.antistereov.start.connector.unsplash.auth.model.UnsplashPublicUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/unsplash")
class UnsplashAuthController(
    private val service: UnsplashAuthService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun connect(): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash connect method." }

        val userId = authenticationService.getCurrentUserId()
        val authorizationUrl = service.getAuthorizationUrl(userId)

        logger.info { "Redirect URL created: $authorizationUrl" }

        return ResponseEntity.ok(
            mapOf("url" to authorizationUrl)
        )
    }

    @GetMapping("/me")
    suspend fun getPublicUserProfile(
        @RequestParam refresh: Boolean = false
    ): ResponseEntity<UnsplashPublicUserProfile> {
        logger.info { "Fetching public Unsplash user profile" }

        val userId = authenticationService.getCurrentUserId()

        return if (refresh) {
            ResponseEntity.ok(
                service.getPublicUserProfile(userId)
            )
        } else {
            ResponseEntity.ok(
                service.getSavedPublicUserProfile(userId)
            )
        }
    }

    @GetMapping("/callback")
    suspend fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
    ): ResponseEntity<UnsplashPublicUserProfile> {
        logger.info { "Received Unsplash callback with code: $code and state: $state" }

        val publicUserProfile = service.authenticate(code, state)

        logger.info { "Unsplash authentication successful." }

        return ResponseEntity.ok(
            publicUserProfile
        )
    }

    @DeleteMapping
    suspend fun disconnect(): ResponseEntity<Any> {
        logger.info { "Executing Unsplash logout method." }

        val userId = authenticationService.getCurrentUserId()

        return ResponseEntity.ok(
            service.disconnect(userId)
        )
    }
}