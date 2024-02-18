package io.github.antistereov.start.widgets.openai.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.openai.model.ChatRequest
import io.github.antistereov.start.widgets.openai.model.ChatResponse
import io.github.antistereov.start.widgets.openai.model.Message
import io.netty.handler.codec.DecoderException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class OpenAIAuthService(
    private val userRepository: UserRepository,
    private val webClient: WebClient,
    private val properties: OpenAIProperties,
    private val baseService: BaseService,
    private val aesEncryption: AESEncryption,
) {

    fun authentication(userId: String, apiKey: String): Mono<String> {
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
        val uri = "${properties.apiBaseUrl}/chat/completions"

        return webClient
            .post()
            .uri(uri)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(ChatRequest(mutableListOf(Message("user", "test"))))
            .retrieve()
            .let { baseService.handleError(uri, it) }
            .bodyToMono(ChatResponse::class.java)
            .map { "Credentials are correct." }
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))
    }
}