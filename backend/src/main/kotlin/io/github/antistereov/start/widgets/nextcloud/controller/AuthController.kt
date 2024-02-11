package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/nextcloud")
class AuthController(
    private val authService: AuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    @PostMapping("/auth")
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody credentials: NextcloudCredentials
    ): Mono<String> {
        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                authService.authentication(userId, credentials)
            }
    }
}