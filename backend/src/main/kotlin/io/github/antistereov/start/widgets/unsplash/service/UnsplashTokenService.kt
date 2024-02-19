package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.UnsplashAuthDetails
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.unsplash.config.UnsplashProperties
import io.github.antistereov.start.widgets.unsplash.model.UnsplashTokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashTokenService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: UnsplashProperties,
) {

    fun getAuthorizationUrl(userId: String): Mono<String> {
        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://unsplash.com/oauth/authorize")
                .queryParam("redirect_uri", properties.redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", properties.scopes)
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
            return Mono.error(ThirdPartyAuthorizationCanceledException(properties.serviceName, error, errorDescription))
        }

        return Mono.error(InvalidCallbackException(properties.serviceName, "Invalid request parameters."))
    }

    fun logout(userId: String): Mono<Void> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.unsplash = UnsplashAuthDetails()

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .then()
            }
    }

    private fun handleAuthentication(code: String, state: String): Mono<UnsplashTokenResponse> {
        val uri = "https://unsplash.com/oauth/token"

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", properties.clientId)
                    .with("client_secret", properties.clientSecret)
                    .with("code", code)
                    .with("redirect_uri", properties.redirectUri)
            )
            .retrieve()
            .bodyToMono(UnsplashTokenResponse::class.java)
            .flatMap { response ->
                stateValidation.getUserId(state).flatMap { userId ->
                    handleUser(userId, response)
                }
            }
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
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "access token", userId))
                Mono.just(aesEncryption.decrypt(encryptedAccessToken))
            }
    }
}
