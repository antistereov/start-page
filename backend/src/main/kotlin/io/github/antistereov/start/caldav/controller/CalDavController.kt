package io.github.antistereov.start.caldav.controller

import io.github.antistereov.start.caldav.model.CalDavCredentials
import io.github.antistereov.start.caldav.service.CalDavService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/caldav")
class CalDavController {

    @Autowired
    private lateinit var calDavService: CalDavService

    @PostMapping("/auth")
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody calDavCredentials: CalDavCredentials
    ): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            calDavService.authentication(userId, calDavCredentials)
            ResponseEntity.ok("Credentials stored successfully.")
        } catch(e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to store credentials: $e")
        }
    }

    @GetMapping("/events/{calendarName}")
    fun getEvents(authentication: Authentication, @PathVariable calendarName: String): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val calDavCredentials = calDavService.getCredentials(userId)
             ResponseEntity.ok(calDavService.getEvents(calDavCredentials, calendarName))

        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }
}