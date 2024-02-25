package io.github.antistereov.start.widgets.auth.unsplash.service

import io.github.antistereov.start.global.exception.InvalidCallbackException
import io.github.antistereov.start.global.exception.MissingCredentialsException
import io.github.antistereov.start.global.exception.ThirdPartyAuthorizationCanceledException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.StateValidation
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.unsplash.config.UnsplashProperties
import io.github.antistereov.start.widgets.auth.unsplash.model.UnsplashAuthDetails
import io.github.antistereov.start.widgets.auth.unsplash.model.UnsplashTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashAuthService(
    private val webClient: WebClient,
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: UnsplashProperties,
) {

    private val logger = LoggerFactory.getLogger(UnsplashAuthService::class.java)

    fun getAuthorizationUrl(userId: String): Mono<String> {
        logger.debug("Creating Unsplash authorization URL for user $userId.")

        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://unsplash.com/oauth/authorize")
                .queryParam("redirect_uri", properties.redirectUri)
                .queryParam("client_id", properties.clientId)
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
        logger.debug("Received Unsplash callback with state: $state and error: $error.")

        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null && errorDescription != null) {
            return Mono.error(
                ThirdPartyAuthorizationCanceledException(
                    properties.serviceName,
                    error,
                    errorDescription
                )
            )
        }

        return Mono.error(
            InvalidCallbackException(
                properties.serviceName,
                "Invalid request parameters."
            )
        )
    }

    fun logout(userId: String): Mono<Void> {
        logger.debug("Deleting Unsplash user information for user $userId.")

        return userService.findById(userId).flatMap { user ->
            user.auth.unsplash = UnsplashAuthDetails()

            userService.save(user).then()
        }
    }

    private fun handleAuthentication(code: String, state: String): Mono<UnsplashTokenResponse> {
        logger.debug("Handling Unsplash authentication with state: $state.")

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
        logger.debug("Handling Unsplash user $userId")

        return userService.findById(userId).flatMap { user ->
            user.auth.unsplash.accessToken = aesEncryption.encrypt(response.accessToken)

            userService.save(user).thenReturn(response)
        }
    }

    fun getAccessToken(userId: String): Mono<String> {
        logger.debug("Getting Unsplash access token for user $userId.")

        return userService.findById(userId).flatMap { user ->
            val encryptedAccessToken = user.auth.unsplash.accessToken
                ?: return@flatMap Mono.error(
                    MissingCredentialsException(
                        properties.serviceName,
                        "access token",
                        userId
                    )
                )
            Mono.just(aesEncryption.decrypt(encryptedAccessToken))
        }
    }
}
