package io.github.antistereov.start.widgets.todoist.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.todoist.service.TodoistApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/todoist")
class TodoistApiController(
    private val apiService: TodoistApiService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(TodoistApiController::class.java)

    @GetMapping("/tasks")
    fun getTasks(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist getTasks method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                apiService.getTasks(userId)
            }
    }
}