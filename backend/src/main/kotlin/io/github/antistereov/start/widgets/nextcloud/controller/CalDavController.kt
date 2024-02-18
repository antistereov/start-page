package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.nextcloud.service.CalDavService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/widgets/nextcloud/caldav")
class CalDavController(
    private val calDavService: CalDavService,
    private val nextcloudAuthService: NextcloudAuthService,
) {

    @GetMapping("/events/{calendarName}")
    fun getEvents(authentication: Authentication, @PathVariable calendarName: String): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val nextcloudCredentials = nextcloudAuthService.getCredentials(userId).block()!!
             ResponseEntity.ok(calDavService.getFutureEvents(nextcloudCredentials, calendarName))

        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }

    @GetMapping("/calendars")
    fun getCalendars(authentication: Authentication): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val nextcloudCredentials = nextcloudAuthService.getCredentials(userId).block()!!
            ResponseEntity.ok(Json.encodeToString(calDavService.getCalendars(nextcloudCredentials)))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }
}