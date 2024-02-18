package io.github.antistereov.start.widgets.openai.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.openai.service.OpenAIAuthService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/openai/auth")
class OpenAIAuthController(
    private val authService: OpenAIAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(OpenAIAuthService::class.java)

    @PostMapping
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody apiKey: String
    ): Mono<String> {
        logger.info("Executing OpenAI authentication method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                authService.authentication(userId, apiKey)
            }
    }
}
