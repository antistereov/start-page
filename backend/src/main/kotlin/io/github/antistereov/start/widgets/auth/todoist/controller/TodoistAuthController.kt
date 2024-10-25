package io.github.antistereov.start.widgets.auth.todoist.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widgets.auth.todoist.service.TodoistAuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/todoist")
class TodoistAuthController(
    private val tokenService: TodoistAuthService,
    private val principalExtractor: PrincipalService,
    ) {

    private val logger: Logger = LoggerFactory.getLogger(TodoistAuthController::class.java)

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
        logger.info("Received Todoist callback with state: $state and error: $error")

        return tokenService.authenticate(code, state, error)
            .map {
                logger.info("Todoist authentication successful.")

                "Todoist authentication successful."
            }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing Todoist logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            tokenService.logout(userId).then(Mono.fromCallable { "Todoist user information deleted for user: $userId." })
        }
    }
}
