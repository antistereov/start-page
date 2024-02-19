package io.github.antistereov.start.widgets.widget.tasks.todoist.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.tasks.todoist.service.TodoistTasksService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tasks/todoist")
class TodoistTaskController(
    private val service: TodoistTasksService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(TodoistTaskController::class.java)

    @GetMapping
    fun getTasks(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist getTasks method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getTasks(userId)
            }
    }
}