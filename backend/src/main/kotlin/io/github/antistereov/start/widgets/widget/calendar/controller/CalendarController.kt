package io.github.antistereov.start.widgets.widget.calendar.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.widget.calendar.service.CalenderService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/calendar")
class CalendarController(
    private val calenderService: CalenderService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalendarController::class.java)

    @GetMapping
    fun getUserCalendars(authentication: Authentication): Mono<List<OnlineCalendar>> {
        logger.info("Getting user calendars.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calenderService.getUserCalendars(userId)
        }
    }

    @PostMapping
    fun updateCalendars(
        authentication: Authentication,
        @RequestBody calendars: MutableList<OnlineCalendar>,
    ): Mono<List<OnlineCalendar>> {
        logger.info("Updating calendars.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calenderService.addCalendars(userId, calendars)
        }
    }

    @DeleteMapping
    fun deleteCalendars(
        authentication: Authentication,
        @RequestBody icsLinks: List<String> = emptyList(),
    ): Mono<List<OnlineCalendar>> {
        logger.info("Deleting calendars.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calenderService.deleteCalendars(userId, icsLinks)
        }
    }

    @PostMapping("/update")
    fun updateCalendarEvents(
        authentication: Authentication,
        @RequestBody icsLinks: List<String> = emptyList()
    ): Flux<OnlineCalendar> {
        logger.info("Getting calendar events.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calenderService.updateCalendarEvents(userId, icsLinks)
        }
    }
}
