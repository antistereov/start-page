package io.github.antistereov.start.widgets.widget.caldav.base.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.base.service.CalDavService
import io.github.antistereov.start.widgets.widget.caldav.calendar.model.CalDavCalendar
import io.github.antistereov.start.widgets.widget.caldav.calendar.service.CalendarService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/caldav")
class CalDavController(
    private val calDavService: CalDavService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalDavController::class.java)

    @GetMapping
    fun getUserResources(authentication: Authentication): Mono<List<CalDavResource>> {
        logger.info("Getting user resources.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.getUserResources(userId)
        }
    }

    @PostMapping
    fun addResources(
        authentication: Authentication,
        @RequestBody calendars: MutableList<CalDavCalendar>,
    ): Mono<List<CalDavResource>> {
        logger.info("Updating resources.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.addResources(userId, calendars)
        }
    }

    @DeleteMapping
    fun deleteResources(
        authentication: Authentication,
        @RequestBody icsLinks: List<String> = emptyList(),
    ): Mono<List<CalDavResource>> {
        logger.info("Deleting resources.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            calDavService.deleteResources(userId, icsLinks)
        }
    }

    @PostMapping("/update")
    fun updateResourceEntities(
        authentication: Authentication,
        @RequestBody icsLinks: List<String> = emptyList()
    ): Flux<CalDavResource> {
        logger.info("Updating resource entities.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            calDavService.updateResourceEntities(userId, icsLinks)
        }
    }
}