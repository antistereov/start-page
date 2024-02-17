package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.unsplash.model.UnsplashTokenResponse
import io.netty.handler.codec.DecoderException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashTokenService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val baseService: BaseService,
) {

    val serviceName = "Unsplash"

    @Value("\${unsplash.clientId}")
    private lateinit var clientId: String
    @Value("\${unsplash.clientSecret}")
    private lateinit var clientSecret: String
    @Value("\${unsplash.redirectUri}")
    private lateinit var redirectUri: String
    @Value("\${unsplash.scopes}")
    private lateinit var scopes: String

    fun getAuthorizationUrl(userId: String): Mono<String> {
        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://unsplash.com/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("state", state)
                .toUriString()
        }
    }

    fun authenticate(
        code: String?,
        state: String?,
        error: String?,
        errorDescription: String?
    ): Mono<UnsplashTokenResponse> {
        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null && errorDescription != null) {
            return Mono.error(ThirdPartyAuthorizationCanceledException(serviceName, error, errorDescription))
        }

        return Mono.error(InvalidCallbackException(serviceName, "Invalid request parameters."))
    }

    private fun handleAuthentication(code: String, state: String): Mono<UnsplashTokenResponse> {
        val uri = "https://unsplash.com/oauth/token"

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .let { baseService.handleError(uri, it) }
            .bodyToMono(UnsplashTokenResponse::class.java)
            .flatMap { response ->
                stateValidation.getUserId(state).flatMap { userId ->
                    handleUser(userId, response)
                }
            }
            .let { baseService.handleUnexpectedError(uri, it) }
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))
    }

    private fun handleUser(userId: String, response: UnsplashTokenResponse): Mono<UnsplashTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.unsplash.accessToken = aesEncryption.encrypt(response.accessToken)

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
                val encryptedAccessToken = user.unsplash.accessToken
                    ?: return@flatMap Mono.error(MissingCredentialsException(serviceName, "access token", userId))
                Mono.just(aesEncryption.decrypt(encryptedAccessToken))
            }
    }
}
