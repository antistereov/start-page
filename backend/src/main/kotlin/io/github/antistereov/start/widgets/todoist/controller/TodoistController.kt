package io.github.antistereov.start.widgets.todoist.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.todoist.service.TodoistService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/todoist")
class TodoistController(
    private val todoistService: TodoistService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
    ) {

    val logger: Logger = LoggerFactory.getLogger(TodoistController::class.java)

    @GetMapping("/login")
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist login method.")

        return principalExtractor.getUserId(authentication)
            .map {userId ->
                logger.info("Redirecting user $userId to Todoist authorization URL.")

                "redirect:${todoistService.getAuthorizationUrl(userId)}"
            }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam(required = true) code: String,
        @RequestParam(required = true) state: String
    ): Mono<String> {
        logger.info("Received Todoist callback with code: $code and state: $state")

        return todoistService.authenticate(code, state)
            .map {
                logger.info("Todoist authentication successful.")

                "Todoist authentication successful."
            }
    }

    @GetMapping("/tasks")
    fun getTasks(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist getTasks method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                todoistService.getAccessToken(userId)
                    .flatMap { accessToken ->
                        todoistService.getTasks(accessToken)
                    }
            }
    }

}