package io.github.antistereov.start.widgets.nextcloud.controller

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

@RestController
@RequestMapping("/api/nextcloud")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/auth")
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody nextcloudCredentials: NextcloudCredentials
    ): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            authService.authentication(userId, nextcloudCredentials)
            ResponseEntity.ok("Credentials stored successfully.")
        } catch(e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to store credentials: $e")
        }
    }
}