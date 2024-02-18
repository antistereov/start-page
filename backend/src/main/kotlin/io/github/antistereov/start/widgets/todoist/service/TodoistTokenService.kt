package io.github.antistereov.start.widgets.todoist.service

import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.todoist.model.TodoistTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.todoist.config.TodoistProperties
import io.netty.handler.codec.DecoderException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class TodoistTokenService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val baseService: BaseService,
    private val properties: TodoistProperties,
) {

    fun getAuthorizationUrl(userId: String): Mono<String> {
        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://todoist.com/oauth/authorize")
                .queryParam("client_id", properties.clientId)
                .queryParam("scope", properties.scopes)
                .queryParam("state", state)
                .toUriString()
        }
    }

    fun authenticate(code: String?, state: String?, error: String?): Mono<TodoistTokenResponse> {
        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null) {
            return Mono.error(ThirdPartyAuthorizationCanceledException(properties.serviceName, error, error))
        }

        return Mono.error(InvalidCallbackException(properties.serviceName, "Invalid request parameters."))
    }

    private fun handleAuthentication(code: String, state: String): Mono<TodoistTokenResponse> {
        val uri = "https://todoist.com/oauth/access_token"

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(
                BodyInserters.fromFormData("client_id", properties.clientId)
                    .with("client_secret", properties.clientSecret)
                    .with("code", code)
                    .with("redirect_uri", properties.redirectUri)
            )
            .retrieve()
            .let { baseService.handleError(uri, it) }
            .bodyToMono(TodoistTokenResponse::class.java)
            .flatMap { response ->
                val userId = aesEncryption.decrypt(state)
                handleUser(userId, response)
            }
            .let { baseService.handleUnexpectedError(uri, it) }
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))
    }

    private fun handleUser(userId: String, response: TodoistTokenResponse): Mono<TodoistTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.todoist.accessToken = aesEncryption.encrypt(response.accessToken)

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(response)
            }
    }

    fun getAccessToken(userId: String): Mono<String> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val encryptedAccessToken = user.todoist.accessToken
                    ?: return@flatMap Mono.error(MissingCredentialsException("Todoist", "access token", userId))
                Mono.just(aesEncryption.decrypt(encryptedAccessToken))
            }
    }
}
