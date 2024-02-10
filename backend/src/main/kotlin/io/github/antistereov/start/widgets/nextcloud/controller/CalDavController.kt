package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.widgets.nextcloud.service.AuthService
import io.github.antistereov.start.widgets.nextcloud.service.CalDavService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/nextcloud/caldav")
class CalDavController(
    private val calDavService: CalDavService,
    private val authService: AuthService,
) {

    @GetMapping("/events/{calendarName}")
    fun getEvents(authentication: Authentication, @PathVariable calendarName: String): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val calDavCredentials = authService.getCredentials(userId)
             ResponseEntity.ok(calDavService.getEvents(calDavCredentials, calendarName))

        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }

    @GetMapping("/calendars")
    fun getCalendars(authentication: Authentication): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val calDavCredentials = authService.getCredentials(userId)
            ResponseEntity.ok(calDavService.getCalendars(calDavCredentials))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }
}