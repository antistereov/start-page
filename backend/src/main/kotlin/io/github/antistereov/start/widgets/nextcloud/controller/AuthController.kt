package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.service.AuthService
import io.github.antistereov.start.widgets.spotify.controller.SpotifyController
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import kotlin.math.log

@RestController
@RequestMapping("/api/nextcloud")
class AuthController(
    private val authService: AuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/auth")
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody credentials: NextcloudCredentials
    ): Mono<String> {
        logger.info("Executing Nextcloud authentication method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                authService.authentication(userId, credentials)
            }
    }
}