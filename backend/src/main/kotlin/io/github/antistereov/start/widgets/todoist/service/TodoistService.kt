package io.github.antistereov.start.widgets.todoist.service

import io.github.antistereov.start.global.model.CannotSaveUserException
import io.github.antistereov.start.global.model.NoAccessTokenException
import io.github.antistereov.start.global.model.UnexpectedErrorException
import io.github.antistereov.start.global.model.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.todoist.model.TodoistTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class TodoistService(
    private val webClientBuilder: WebClient.Builder,
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    @Value("\${todoist.clientId}")
    private lateinit var clientId: String

    @Value("\${todoist.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${todoist.redirectUri}")
    private lateinit var redirectUri: String


    private val todoistApiUrl = "https://api.todoist.com/rest/v2/"
    private val scopes = "data:read"

    fun getAuthorizationUrl(userId: String): String {
        val encryptedUserId = aesEncryption.encrypt(userId)

        return UriComponentsBuilder.fromHttpUrl("https://todoist.com/oauth/authorize")
            .queryParam("client_id", clientId)
            .queryParam("scope", scopes)
            .queryParam("state", encryptedUserId)
            .toUriString()
    }

    fun authenticate(code: String, encryptedUserId: String): Mono<TodoistTokenResponse> {
        return webClient.post()
            .uri("https://todoist.com/oauth/access_token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(
                BodyInserters.fromFormData("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .bodyToMono(TodoistTokenResponse::class.java)
            .flatMap { response ->
                val userId = aesEncryption.decrypt(encryptedUserId)
                handleUser(userId, response)
            }
    }

    private fun handleUser(userId: String, response: TodoistTokenResponse): Mono<TodoistTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.todoistAccessToken = aesEncryption.encrypt(response.accessToken)

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
                val encryptedAccessToken = user.todoistAccessToken
                    ?: return@flatMap Mono.error(NoAccessTokenException("Todoist", userId))
                Mono.just(aesEncryption.decrypt(encryptedAccessToken))
            }
    }

    fun getTasks(accessToken: String): Mono<String> {
        return webClientBuilder.build().get()
            .uri("$todoistApiUrl/tasks")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Spotify API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
            .onErrorResume { exception ->
                Mono.error(UnexpectedErrorException("An unexpected error occurred during the Todoist getTasks method.", exception))
            }
    }
}
