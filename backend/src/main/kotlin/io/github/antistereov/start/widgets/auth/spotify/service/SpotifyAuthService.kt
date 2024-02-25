package io.github.antistereov.start.widgets.auth.spotify.service

import io.github.antistereov.start.global.exception.InvalidCallbackException
import io.github.antistereov.start.global.exception.MissingCredentialsException
import io.github.antistereov.start.global.exception.ThirdPartyAuthorizationCanceledException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.StateValidation
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.spotify.config.SpotifyProperties
import io.github.antistereov.start.widgets.auth.spotify.model.SpotifyAuthDetails
import io.github.antistereov.start.widgets.auth.spotify.model.SpotifyTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class SpotifyAuthService(
    private val webClient: WebClient,
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: SpotifyProperties,
) {

    private val logger = LoggerFactory.getLogger(SpotifyAuthService::class.java)

    fun getAuthorizationUrl(userId: String): Mono<String> {
        logger.debug("Getting authorization URL for user: $userId.")

        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder
                .fromHttpUrl("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId)
                .queryParam("scope", properties.scopes)
                .queryParam("redirect_uri", properties.redirectUri)
                .queryParam("state", state)
                .toUriString()
        }
    }

    fun authenticate(code: String?, state: String?, error: String?): Mono<SpotifyTokenResponse> {
        logger.debug("Authenticating user.")

        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null) {
            return Mono.error(
                ThirdPartyAuthorizationCanceledException(
                    properties.serviceName,
                    error,
                    error
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
        logger.debug("Logging out user: $userId.")

        return userService.findById(userId).flatMap { user ->
            user.auth.spotify = SpotifyAuthDetails()

            userService.save(user).then()
        }
    }

    private fun handleAuthentication(code: String, state: String): Mono<SpotifyTokenResponse> {
        logger.debug("Handling authentication.")

        val auth = "${properties.clientId}:${properties.clientSecret}"
        val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
        val uri = "https://accounts.spotify.com/api/token"

        return webClient
            .post()
            .uri(uri)
            .header(
                "Content-Type",
                "application/x-www-form-urlencoded")
            .header(
                "Authorization",
                "Basic $encodedAuth")
            .body(
                BodyInserters
                    .fromFormData("grant_type", "authorization_code")
                    .with("code", code)
                    .with("redirect_uri", properties.redirectUri)
            )
            .retrieve()
            .bodyToMono(SpotifyTokenResponse::class.java)
            .flatMap { response ->
                stateValidation.getUserId(state).flatMap { userId ->
                    handleUser(userId, response)
                }
            }
    }

    private fun handleUser(userId: String, response: SpotifyTokenResponse): Mono<SpotifyTokenResponse> {
        logger.debug("Handling user.")

        return userService  .findById(userId).flatMap { user ->
            val refreshToken = response.refreshToken
                ?: return@flatMap Mono.error(
                    MissingCredentialsException(
                        properties.serviceName,
                        "refresh token",
                        userId
                    )
                )
            val expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

            user.auth.spotify.accessToken = aesEncryption.encrypt(response.accessToken)
            user.auth.spotify.refreshToken = aesEncryption.encrypt(refreshToken)
            user.auth.spotify.expirationDate = expirationDate

            userService.save(user).thenReturn(response)
        }
    }

    fun refreshToken(userId: String): Mono<SpotifyTokenResponse> {
        logger.debug("Refreshing token for user: $userId.")

        val uri = "https://accounts.spotify.com/api/token"

        return userService.findById(userId).flatMap { user ->
            val encryptedRefreshToken = user.auth.spotify.refreshToken
                ?: return@flatMap Mono.error(
                    MissingCredentialsException(
                        properties.serviceName,
                        "refresh token",
                        userId
                    )
                )
            val refreshToken = aesEncryption.decrypt(encryptedRefreshToken)

            webClient.post()
                .uri(uri)
                .header(
                    "Content-Type",
                    "application/x-www-form-urlencoded")
                .body(
                    BodyInserters
                        .fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken)
                        .with("client_id", properties.clientId)
                        .with("client_secret", properties.clientSecret)
                )
                .retrieve()
                .bodyToMono(SpotifyTokenResponse::class.java)
                .flatMap { response ->
                    user.auth.spotify.accessToken = aesEncryption.encrypt(response.accessToken)
                    user.auth.spotify.expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

                    userService.save(user).thenReturn(response)
                }
        }
    }

    fun getAccessToken(userId: String): Mono<String> {
        logger.debug("Getting access token for user: $userId.")

        val currentTime = LocalDateTime.now()

        return userService.findById(userId).flatMap { user ->
            val expirationDate = user.auth.spotify.expirationDate
                ?: return@flatMap Mono.error(
                    MissingCredentialsException(
                        properties.serviceName,
                        "expiration date",
                        userId
                    )
                )

            if (currentTime.isAfter(expirationDate)) {
                this.refreshToken(userId).map { it.accessToken }
            } else {
                val encryptedSpotifyAccessToken = user.auth.spotify.accessToken
                    ?: return@flatMap Mono.error(
                        MissingCredentialsException(
                            properties.serviceName,
                            "access token",
                            userId
                        )
                    )
                Mono.just(aesEncryption.decrypt(encryptedSpotifyAccessToken))
            }
        }
    }
}
