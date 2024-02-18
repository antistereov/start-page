package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCalendar
import io.github.antistereov.start.widgets.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.nextcloud.service.CalDavService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

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
    fun getRemoteCalendars(authentication: Authentication): Flux<NextcloudCalendar> {
        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                calDavService.getRemoteCalendars(credentials)
            }
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
        @RequestBody icsLinks: List<String>?,
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
