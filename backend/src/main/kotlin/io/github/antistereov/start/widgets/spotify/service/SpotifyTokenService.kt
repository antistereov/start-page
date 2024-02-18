package io.github.antistereov.start.widgets.spotify.service

import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.spotify.model.SpotifyTokenResponse
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.spotify.config.SpotifyProperties
import io.netty.handler.codec.DecoderException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class SpotifyTokenService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val baseService: BaseService,
    private val properties: SpotifyProperties,
) {

    fun getAuthorizationUrl(userId: String): Mono<String> {
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
        if (code != null && state != null) {
            return handleAuthentication(code, state)
        }

        if (error != null) {
            return Mono.error(ThirdPartyAuthorizationCanceledException(properties.serviceName, error, error))
        }

        return Mono.error(InvalidCallbackException(properties.serviceName, "Invalid request parameters."))
    }

    private fun handleAuthentication(code: String, state: String): Mono<SpotifyTokenResponse> {
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
            .let { baseService.handleError(uri, it) }
            .bodyToMono(SpotifyTokenResponse::class.java)
            .flatMap { response ->
                stateValidation.getUserId(state).flatMap { userId ->
                    handleUser(userId, response)
                }
            }
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))

    }

    private fun handleUser(userId: String, response: SpotifyTokenResponse): Mono<SpotifyTokenResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val refreshToken = response.refreshToken
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "refresh token", userId))
                val expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

                user.spotify.accessToken = aesEncryption.encrypt(response.accessToken)
                user.spotify.refreshToken = aesEncryption.encrypt(refreshToken)
                user.spotify.expirationDate = expirationDate

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(response)
            }
    }

    fun refreshToken(userId: String): Mono<SpotifyTokenResponse> {
        val uri = "https://accounts.spotify.com/api/token"

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val encryptedRefreshToken = user.spotify.refreshToken
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "refresh token", userId))
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
                    .let { baseService.handleError(uri, it) }
                    .bodyToMono(SpotifyTokenResponse::class.java)
                    .flatMap { response ->
                        user.spotify.accessToken = aesEncryption.encrypt(response.accessToken)
                        user.spotify.expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

                        userRepository.save(user)
                            .onErrorMap { throwable ->
                                CannotSaveUserException(throwable)
                            }
                            .thenReturn(response)
                    }
                    .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
                    .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))
            }
    }

    fun getAccessToken(userId: String): Mono<String> {
        val currentTime = LocalDateTime.now()

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val expirationDate = user.spotify.expirationDate
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "expiration date", userId))

                if (currentTime.isAfter(expirationDate)) {
                    this.refreshToken(userId).map { it.accessToken }
                } else {
                    val encryptedSpotifyAccessToken = user.spotify.accessToken
                        ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "access token", userId))
                    Mono.just(aesEncryption.decrypt(encryptedSpotifyAccessToken))
                }
            }
    }
}
