package io.github.antistereov.start.widgets.widget.calendar.caldav.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.widget.calendar.caldav.service.CalDavCalenderService
import io.github.antistereov.start.widgets.widget.calendar.nextcloud.service.NextcloudEventService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/calendar/caldav")
class CalDavCalendarController(
    private val apiService: CalDavCalenderService,
    private val eventService: NextcloudEventService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalDavCalendarController::class.java)

    @GetMapping
    fun getUserCalendars(authentication: Authentication): Flux<OnlineCalendar> {
        logger.info("Getting user calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.getUserCalendars(userId).flatMapIterable { it }
        }
    }

    @PostMapping
    fun updateCalendars(
        authentication: Authentication,
        @RequestBody calendars: MutableList<OnlineCalendar>,
    ): Flux<OnlineCalendar> {
        logger.info("Updating calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.addCalendars(userId, calendars)
        }
    }

    @DeleteMapping
    fun deleteCalendars(
        authentication: Authentication,
        @RequestBody icsLinks: List<String>?,
    ): Flux<OnlineCalendar> {
        logger.info("Deleting calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.deleteCalendars(userId, icsLinks)
        }
    }

    @GetMapping("/refresh")
    fun refreshCalendarEvents(authentication: Authentication): Flux<OnlineCalendar> {
        logger.info("Refreshing calendar events.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            eventService.refreshCalendarEvents(userId)
        }
    }
}
