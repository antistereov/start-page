package io.github.antistereov.start.widgets.nextcloud.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/nextcloud/caldav")
class CalDavController(
    private val calDavService: CalDavService,
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    @GetMapping("/calendars")
    fun getUserCalendars(authentication: Authentication): Flux<NextcloudCalendar> {
        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.getUserCalendars(userId).flatMapIterable { it }
        }
    }

    @GetMapping("/calendars/remote")
    fun getRemoteCalendars(authentication: Authentication): ResponseEntity<String> {
        val principal = authentication.principal as Jwt
        val userId = principal.claims["sub"].toString()

        return try {
            val nextcloudCredentials = nextcloudAuthService.getCredentials(userId).block()!!
            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(calDavService.getRemoteCalendars(nextcloudCredentials))
            ResponseEntity.ok(json)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to claim credentials: $e")
        }
    }

    @PostMapping("/calendars")
    fun updateCalendars(
        authentication: Authentication,
        @RequestBody calendars: MutableList<NextcloudCalendar>,
    ): Flux<NextcloudCalendar> {
        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.addCalendars(userId, calendars)
        }
    }

    @DeleteMapping("/calendars")
    fun deleteCalendars(
        authentication: Authentication,
        @RequestBody icsLinks: List<String>,
    ): Flux<NextcloudCalendar> {
        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.deleteCalendars(userId, icsLinks)
        }
    }

    @GetMapping("/calendars/refresh")
    fun refreshCalendarEvents(authentication: Authentication): Flux<NextcloudCalendar> {
        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.refreshCalendarEvents(userId)
        }
    }
}
