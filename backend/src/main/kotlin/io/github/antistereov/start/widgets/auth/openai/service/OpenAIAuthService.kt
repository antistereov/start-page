package io.github.antistereov.start.widgets.auth.openai.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.openai.model.OpenAIAuthDetails
import io.github.antistereov.start.user.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val webClient: WebClient,
    private val properties: OpenAIProperties,
    private val aesEncryption: AESEncryption,
) {

    private val logger = LoggerFactory.getLogger(OpenAIAuthService::class.java)

    fun authentication(userId: String, apiKey: String): Mono<String> {
        logger.debug("Authenticating user: $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .map { user ->
                user.openAi.apiKey = aesEncryption.encrypt(apiKey)
                user
            }
            .flatMap { user ->
                verifyCredentials(apiKey)
                    .then(
                        userRepository.save(user)
                        .onErrorMap { throwable ->
                            CannotSaveUserException(throwable)
                        }
                    )
                    .map { "Credentials are correct, verified and have been saved." }
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

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.openAi = OpenAIAuthDetails()

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .then()
            }
    }
}