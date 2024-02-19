package io.github.antistereov.start.widgets.auth.todoist.service

import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.auth.todoist.model.TodoistAuthDetails
import io.github.antistereov.start.widgets.auth.todoist.model.TodoistTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.auth.todoist.config.TodoistProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class TodoistAuthService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: TodoistProperties,
) {

    private val logger = LoggerFactory.getLogger(TodoistAuthService::class.java)

    fun getAuthorizationUrl(userId: String): Mono<String> {
        logger.debug("Creating Todoist authorization URL for user $userId.")

        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://todoist.com/oauth/authorize")
                .queryParam("client_id", properties.clientId)
                .queryParam("scope", properties.scopes)
                .queryParam("state", state)
                .toUriString()
        }
    }

    fun authenticate(code: String?, state: String?, error: String?): Mono<TodoistTokenResponse> {
        logger.debug("Received Todoist callback with state: $state and error: $error.")

        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null) {
            return Mono.error(ThirdPartyAuthorizationCanceledException(properties.serviceName, error, error))
        }

        return Mono.error(InvalidCallbackException(properties.serviceName, "Invalid request parameters."))
    }

    fun logout(userId: String): Mono<Void> {
        logger.debug("Deleting Todoist user information for user $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.auth.todoist = TodoistAuthDetails()

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .then()
            }
    }

    private fun handleAuthentication(code: String, state: String): Mono<TodoistTokenResponse> {
        logger.debug("Handling Todoist authentication with state: $state.")

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
            .bodyToMono(TodoistTokenResponse::class.java)
            .flatMap { response ->
                val userId = aesEncryption.decrypt(state)
                handleUser(userId, response)
            }
    }

    private fun handleUser(userId: String, response: TodoistTokenResponse): Mono<TodoistTokenResponse> {
        logger.debug("Handling Todoist user {} with response: {}.", userId, response)

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.auth.todoist.accessToken = aesEncryption.encrypt(response.accessToken)

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(response)
            }
    }

    fun getAccessToken(userId: String): Mono<String> {
        logger.debug("Getting Todoist access token for user $userId.")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val encryptedAccessToken = user.auth.todoist.accessToken
                    ?: return@flatMap Mono.error(MissingCredentialsException("Todoist", "access token", userId))
                Mono.just(aesEncryption.decrypt(encryptedAccessToken))
            }
    }
}
