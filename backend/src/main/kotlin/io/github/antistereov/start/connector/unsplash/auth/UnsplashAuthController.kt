package io.github.antistereov.start.connector.unsplash.auth

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.unsplash.auth.model.UnsplashPublicUserProfile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/auth/unsplash")
class UnsplashAuthController(
    private val service: UnsplashAuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @GetMapping
    suspend fun connect(exchange: ServerWebExchange): ResponseEntity<Map<String, String>> {
        logger.info { "Executing Unsplash connect method." }

        val userId = principalService.getUserId(exchange)
        val authorizationUrl = service.getAuthorizationUrl(userId)

        logger.info { "Redirect URL created: $authorizationUrl" }

        return ResponseEntity.ok(
            mapOf("url" to authorizationUrl)
        )
    }

    @GetMapping("/me")
    suspend fun getPublicUserProfile(
        exchange: ServerWebExchange,
        @RequestParam refresh: Boolean = false
    ): ResponseEntity<UnsplashPublicUserProfile> {
        logger.info { "Fetching public Unsplash user profile" }

        val userId = principalService.getUserId(exchange)

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
    suspend fun disconnect(exchange: ServerWebExchange): ResponseEntity<Any> {
        logger.info { "Executing Unsplash logout method." }

        val userId = principalService.getUserId(exchange)

        return ResponseEntity.ok(
            service.disconnect(userId)
        )
    }
}