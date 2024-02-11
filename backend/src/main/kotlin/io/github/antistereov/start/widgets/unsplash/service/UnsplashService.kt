package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.spotify.model.SpotifyTokenResponse
import io.github.antistereov.start.widgets.unsplash.model.UnsplashTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashService(
    private val webClientBuilder: WebClient.Builder,
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption
) {

    @Value("\${unsplash.clientId}")
    private lateinit var clientId: String

    @Value("\${unsplash.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${unsplash.redirectUri}")
    private lateinit var redirectUri: String

    private val unsplashApiUrl = "https://api.unsplash.com"
    private val scopes = "write_likes+public"

    fun getAuthorizationUrl(userId: String): String {
        val encryptedUserId = aesEncryption.encrypt(userId)

        return UriComponentsBuilder.fromHttpUrl("https://unsplash.com/oauth/authorize")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", scopes)
            .queryParam("state", encryptedUserId)
            .toUriString()
    }

    fun authenticate(code: String, encryptedUserId: String): UnsplashTokenResponse {
        val response = webClient.post()
            .uri("https://unsplash.com/oauth/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .bodyToMono(UnsplashTokenResponse::class.java)
            .block() ?: throw RuntimeException("Request to Unsplash API failed or timed out.")

        response.let {
            val userId = aesEncryption.decrypt(encryptedUserId)
            val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId") }

            user.unsplashAccessToken = aesEncryption.encrypt(it.accessToken)

            userRepository.save(user)
            return it
        }
    }

    fun getAccessToken(userId: String): String {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found: $userId")}
        val encryptedUnsplashAccessToken = user.unsplashAccessToken
            ?: throw RuntimeException("No Todoist Access token found.")

        return aesEncryption.decrypt(encryptedUnsplashAccessToken)
    }

    fun getRandomPhoto(query: String? = null): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("$unsplashApiUrl/photos/random")
            .queryParam("client_id", clientId)
            .queryParam("orientation", "landscape")

        if (query != null) uri.queryParam("query", query)

        return webClientBuilder.build().get()
            .uri(uri.toUriString())
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Unsplash API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
    }

    fun getPhoto(id: String): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("$unsplashApiUrl/photos/$id")
            .queryParam("client_id", clientId)
            .toUriString()

        return webClientBuilder.build().get()
            .uri(uri)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Unsplash API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
    }

    fun likePhoto(accessToken: String, photoId: String): Mono<String> {
        return webClientBuilder.build().post()
            .uri("$unsplashApiUrl/photos/$photoId/like")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Unsplash API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
    }

    fun unlikePhoto(accessToken: String, photoId: String): Mono<String> {
        return webClientBuilder.build().delete()
            .uri("$unsplashApiUrl/photos/$photoId/like")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .flatMap { errorMessage ->
                        Mono.error(RuntimeException("Error from Unsplash API: $errorMessage"))
                    }
            })
            .bodyToMono(String::class.java)
    }

}