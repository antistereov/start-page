package io.github.antistereov.start.connector.unsplash.auth

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.unsplash.auth.model.UnsplashPublicUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/unsplash")
class UnsplashAuthController(
    private val service: UnsplashAuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun connect(authentication: Authentication): String {
        logger.info { "Executing Unsplash connect method." }

        val userId = principalService.getUserId(authentication)
        val authorizationUrl = service.getAuthorizationUrl(userId)

        logger.info { "Redirect URL created: $authorizationUrl" }

        return "{ \"url\": \"$authorizationUrl\" }"
    }

    @GetMapping("/me")
    suspend fun getPublicUserProfile(authentication: Authentication, @RequestParam refresh: Boolean = false): UnsplashPublicUserProfile {
        logger.info { "Fetching public Unsplash user profile" }

        val userId = principalService.getUserId(authentication)

        return if (refresh) {
            service.getPublicUserProfile(userId)
        } else {
            service.getSavedPublicUserProfile(userId)
        }
    }

    @GetMapping("/callback")
    suspend fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
    ): UnsplashPublicUserProfile {
        logger.info { "Received Unsplash callback with code: $code and state: $state" }

        val publicUserProfile = service.authenticate(code, state)

        logger.info { "Unsplash authentication successful." }

        return publicUserProfile
    }

    @DeleteMapping
    suspend fun disconnect(authentication: Authentication) {
        logger.info { "Executing Unsplash logout method." }

        val userId = principalService.getUserId(authentication)

        service.disconnect(userId)
    }
}