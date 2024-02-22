package io.github.antistereov.start.widgets.widget.caldav.tasks.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.caldav.base.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.tasks.service.CalDavTaskListService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/caldav/tasks")
class CalDavTaskListController(
    private val taskListService: CalDavTaskListService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(CalDavTaskListController::class.java)

    @GetMapping
    fun getUserTaskLists(authentication: Authentication): Mono<List<CalDavResource>> {
        logger.info("Getting user task lists.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            taskListService.getUserTaskLists(userId)
        }
    }
}