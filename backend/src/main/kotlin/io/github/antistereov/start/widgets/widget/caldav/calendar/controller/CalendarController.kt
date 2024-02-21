package io.github.antistereov.start.widgets.widget.caldav.calendar.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.calendar.service.CalendarService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/caldav/calendar")
class CalendarController(
    private val calendarService: CalendarService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalendarController::class.java)

    @GetMapping
    fun getUserCalendars(authentication: Authentication): Mono<List<CalDavResource>> {
        logger.info("Getting user calendars.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calendarService.getUserCalendars(userId)
        }
    }
}
