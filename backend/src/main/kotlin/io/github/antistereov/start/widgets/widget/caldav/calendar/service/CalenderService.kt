package io.github.antistereov.start.widgets.widget.caldav.calendar.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResourceType
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CalendarService(
    userService: UserService,
    aesEncryption: AESEncryption,
    eventService: EventService,
) : CalDavService(userService, aesEncryption, eventService) {

    private val logger = LoggerFactory.getLogger(CalendarService::class.java)

    fun getUserCalendars(userId: String): Mono<List<CalDavResource>> {
        logger.debug("Getting user calendars for user: $userId.")

        return super.getUserResources(userId)
            .map { resources ->
                resources.filter { it.type == CalDavResourceType.Calendar }
            }
    }
}