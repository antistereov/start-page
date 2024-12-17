package io.github.antistereov.start.auth.controller

import io.github.antistereov.start.auth.service.AuthService
import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.user.dto.LoginUserDto
import io.github.antistereov.start.user.dto.RegisterUserDto
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @PostMapping("/login")
    suspend fun login(@RequestBody payload: LoginUserDto): ResponseEntity<Map<String, String>> {
        logger.info { "Executing login" }

        val sessionCookieData = authService.login(payload)

        val cookie = ResponseCookie.from("auth", sessionCookieData.accessToken)
            .httpOnly(true)
            // TODO: Add this in production for HTTPS
            // .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(sessionCookieData.expiresIn)
            .build()

        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(mapOf("message" to "Login successful"))
    }

    @PostMapping("/register")
    suspend fun register(@RequestBody payload: RegisterUserDto): ResponseEntity<Map<String, String>> {
        logger.info { "Executing register" }

        val sessionCookieData = authService.register(payload)

        val cookie = ResponseCookie.from("auth", sessionCookieData.accessToken)
            .httpOnly(true)
            // TODO: Add this in production for HTTPS
            // .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(sessionCookieData.expiresIn)
            .build()

        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(mapOf("message" to "Successfully registered"))
    }

    @PostMapping("/logout")
    suspend fun logout(): ResponseEntity<Map<String, String>> {
        logger.info { "Executing logout" }

        val clearCookie = ResponseCookie.from("auth", "")
            .httpOnly(true)
            // TODO: Add this in production for HTTPS
            // .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()

        return ResponseEntity.ok()
            .header("Set-Cookie", clearCookie.toString())
            .body(mapOf("message" to "Logout successful"))
    }

    @GetMapping("/check")
    suspend fun checkAuthentication(exchange: ServerWebExchange): ResponseEntity<Map<String, String>> {
        logger.info { "Checking authentication" }

        return try {
            val userId = principalService.getUserId(exchange)
            ResponseEntity.ok(mapOf("status" to "authenticated", "userId" to userId))
        } catch (ex: Exception) {
            ResponseEntity.status(401).body(mapOf("status" to "unauthenticated"))
        }
    }
}