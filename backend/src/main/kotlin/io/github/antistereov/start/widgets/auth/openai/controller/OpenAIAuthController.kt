package io.github.antistereov.start.widgets.auth.openai.controller

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.widgets.auth.openai.service.OpenAIAuthService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth/openai")
class OpenAIAuthController(
    private val authService: OpenAIAuthService,
    private val principalExtractor: PrincipalService,
) {

    private val logger: Logger = LoggerFactory.getLogger(OpenAIAuthService::class.java)

    @PostMapping
    fun login(
        authentication: Authentication,
        @Valid @RequestBody apiKey: String
    ): Mono<String> {
        logger.info("Executing OpenAI authentication method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                authService.authentication(userId, apiKey)
            }
    }

    @DeleteMapping
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing OpenAI logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            authService.logout(userId).then(Mono.fromCallable { "OpenAI user information deleted for user: $userId." })
        }
    }
}
