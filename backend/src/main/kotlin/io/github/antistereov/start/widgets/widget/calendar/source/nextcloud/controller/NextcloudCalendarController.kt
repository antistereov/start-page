package io.github.antistereov.start.widgets.widget.calendar.source.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.calendar.controller.CalendarController
import io.github.antistereov.start.widgets.widget.calendar.model.OnlineCalendar
import io.github.antistereov.start.widgets.widget.calendar.source.nextcloud.service.NextcloudCalendarService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/calendar/nextcloud")
class NextcloudCalendarController(
    private val remoteService: NextcloudCalendarService,
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalendarController::class.java)

    @GetMapping
    fun getRemoteCalendars(authentication: Authentication): Flux<OnlineCalendar> {
        logger.info("Getting remote calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                remoteService.getRemoteCalendars(credentials)
            }
        }
    }
}