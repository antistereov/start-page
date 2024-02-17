package io.github.antistereov.start.widgets.todoist.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.todoist.service.TodoistTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/todoist/auth")
class TodoistTokenController(
    private val tokenService: TodoistTokenService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
    ) {

    val logger: Logger = LoggerFactory.getLogger(TodoistTokenController::class.java)

    @GetMapping
    fun login(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist login method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.getAuthorizationUrl(userId).map { url ->
                logger.info("Redirecting user $userId to Todoist authorization URL: $url.")

                "redirect:$url"
            }
        }
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam code: String?,
        @RequestParam state: String?,
        @RequestParam error: String?,
    ): Mono<String> {
        logger.info("Received Todoist callback with code: $code, state: $state and error: $error")

        return tokenService.authenticate(code, state, error)
            .map {
                logger.info("Todoist authentication successful.")

                "Todoist authentication successful."
            }
    }
}
