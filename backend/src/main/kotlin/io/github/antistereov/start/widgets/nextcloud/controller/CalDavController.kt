package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.service.CalDavApiService
import io.github.antistereov.start.widgets.nextcloud.service.CalDavEventService
import io.github.antistereov.start.widgets.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.nextcloud.service.CalDavRemoteService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/widgets/nextcloud/caldav")
class CalDavController(
    private val remoteService: CalDavRemoteService,
    private val apiService: CalDavApiService,
    private val eventService: CalDavEventService,
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalDavController::class.java)

    @GetMapping("/calendars")
    fun getUserCalendars(authentication: Authentication): Flux<NextcloudCalendar> {
        logger.info("Getting user calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.getUserCalendars(userId).flatMapIterable { it }
        }
    }

    @GetMapping("/calendars/remote")
    fun getRemoteCalendars(authentication: Authentication): Flux<NextcloudCalendar> {
        logger.info("Getting remote calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                remoteService.getRemoteCalendars(credentials)
            }
        }
    }

    @PostMapping("/calendars")
    fun updateCalendars(
        authentication: Authentication,
        @RequestBody calendars: MutableList<NextcloudCalendar>,
    ): Flux<NextcloudCalendar> {
        logger.info("Updating calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.addCalendars(userId, calendars)
        }
    }

    @DeleteMapping("/calendars")
    fun deleteCalendars(
        authentication: Authentication,
        @RequestBody icsLinks: List<String>?,
    ): Flux<NextcloudCalendar> {
        logger.info("Deleting calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            apiService.deleteCalendars(userId, icsLinks)
        }
    }

    @GetMapping("/calendars/refresh")
    fun refreshCalendarEvents(authentication: Authentication): Flux<NextcloudCalendar> {
        logger.info("Refreshing calendar events.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            eventService.refreshCalendarEvents(userId)
        }
    }
}
