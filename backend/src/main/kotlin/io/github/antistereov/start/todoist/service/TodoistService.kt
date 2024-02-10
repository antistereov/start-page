package io.github.antistereov.start.todoist.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.todoist.model.TodoistTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class TodoistService(
    private val webClientBuilder: WebClient.Builder,
    private val webClient: WebClient,
) {

    @Value("\${todoist.clientId}")
    private lateinit var clientId: String

    @Value("\${todoist.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${todoist.redirectUri}")
    private lateinit var redirectUri: String

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var aesEncryption: AESEncryption

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

    fun authenticate(code: String, encryptedUserId: String): TodoistTokenResponse {
        val response = webClient.post()
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
            .block() ?: throw RuntimeException("Request to Todoist API failed or timed out")

        response.let {
            val userId = aesEncryption.decrypt(encryptedUserId)
            val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId") }

            user.todoistAccessToken = aesEncryption.encrypt(it.accessToken)

            userRepository.save(user)

            return it
        }
    }

    fun getAccessToken(userId: String): String {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId")}
        val encryptedTodoistAccessToken = user.todoistAccessToken
            ?: throw RuntimeException("No Todoist Access token found.")

        return aesEncryption.decrypt(encryptedTodoistAccessToken)
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

    }
}