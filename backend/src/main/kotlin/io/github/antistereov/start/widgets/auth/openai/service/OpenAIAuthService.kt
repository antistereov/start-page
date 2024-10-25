package io.github.antistereov.start.widgets.auth.openai.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.openai.model.OpenAIAuthDetails
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.widget.chat.model.ChatRequest
import io.github.antistereov.start.widgets.widget.chat.model.ChatResponse
import io.github.antistereov.start.widgets.widget.chat.model.Message
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class OpenAIAuthService(
    private val userService: UserService,
    private val webClient: WebClient,
    private val properties: OpenAIProperties,
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(OpenAIAuthService::class.java)

    fun authentication(userId: String, apiKey: String): Mono<String> {
        logger.debug("Authenticating user: $userId.")

        return userService.findById(userId).map { user ->
            user.widgetAuthenticationDetails.openAi.apiKey = aesEncryption.encrypt(apiKey)
            user
        }
        .flatMap { user ->
            verifyCredentials(apiKey)
                .then(userService.save(user))
                .map { "OpenAI API key is correct and have been saved for user: $userId" }
        }
    }

    fun verifyCredentials(apiKey: String): Mono<String> {
        logger.debug("Verifying credentials.")

        val uri = "${properties.apiBaseUrl}/chat/completions"

        return webClient
            .post()
            .uri(uri)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(ChatRequest(mutableListOf(Message("user", "test"))))
            .retrieve()
            .bodyToMono(ChatResponse::class.java)
            .map { "Credentials are correct." }
    }

    fun logout(userId: String): Mono<Void> {
        logger.debug("Logging out user: $userId.")

        return userService.findById(userId).flatMap { user ->
            user.widgetAuthenticationDetails.openAi = OpenAIAuthDetails()

            userService.save(user).then()
        }
    }
}