package io.github.antistereov.start.widgets.instagram.service

import io.github.antistereov.start.global.model.CannotSaveUserException
import io.github.antistereov.start.global.model.ExpiredTokenException
import io.github.antistereov.start.global.model.NoAccessTokenException
import io.github.antistereov.start.global.model.UserNotFoundException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.util.StateValidation
import io.github.antistereov.start.widgets.instagram.model.InstagramLongLivedTokenResponse
import io.github.antistereov.start.widgets.instagram.model.InstagramShortLivedTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class InstagramAuthService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
) {

    @Value("\${instagram.clientId}")
    private lateinit var clientId: String

    @Value("\${instagram.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${instagram.redirectUri}")
    private lateinit var redirectUri: String

    private val scopes = "user_profile,user_media"
    private val serviceName = "Instagram"

    fun getAuthorizationUrl(userId: String): String {
        val state = stateValidation.createState(userId)

        return UriComponentsBuilder.fromHttpUrl("https://api.instagram.com/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", clientId)
            .queryParam("scope", scopes)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .toUriString()
    }

    fun authenticate(code: String, state: String): Mono<InstagramLongLivedTokenResponse> {
        return handleShortLivedToken(code, state)
            .flatMap { user ->
                val userId = user.id
                val accessToken = user.instagram.accessToken
                    ?: return@flatMap Mono.error(NoAccessTokenException(serviceName, userId))

                handleLongLivedToken(userId, accessToken)
            }
    }

    fun getAccessToken(userId: String): Mono<String> {
        return refreshToken(userId)
            .map { response ->
                response.accessToken
            }
    }

    fun refreshToken(userId: String): Mono<InstagramLongLivedTokenResponse> {
        val currentTime = LocalDateTime.now()

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                if (currentTime.isAfter(user.instagram.expirationDate)) {
                    return@flatMap Mono.error(ExpiredTokenException(serviceName, userId))
                }

                val encryptedAccessToken = user.instagram.accessToken
                    ?: return@flatMap Mono.error(NoAccessTokenException(serviceName, userId))
                val accessToken = aesEncryption.decrypt(encryptedAccessToken)

                val uri = UriComponentsBuilder.fromHttpUrl("https://graph.instagram.com/refresh_access_token")
                    .queryParam("grant_type", "ig_refresh_token")
                    .queryParam("access_token", accessToken)
                    .toUriString()

                webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono<InstagramLongLivedTokenResponse>()
            }
    }

    private fun getShortLivedToken(code: String): Mono<InstagramShortLivedTokenResponse> {
        return webClient
            .post()
            .uri("api.instagram.com/oauth/access_token")
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", code)
                    .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .bodyToMono(InstagramShortLivedTokenResponse::class.java)
    }

    private fun handleShortLivedToken(
        code: String,
        state: String,
        expiresIn: Long? = 3600L
    ): Mono<User> {
        return stateValidation.getUserId(state)
            .flatMap { userId ->
                getShortLivedToken(code)
                    .flatMap { response ->
                        updateAuthDetails(
                            userId,
                            response.accessToken,
                            expiresIn,
                            response.userId
                        )
                    }
            }
    }

    private fun getLongLivedToken(accessToken: String): Mono<InstagramLongLivedTokenResponse> {
        return webClient
            .post()
            .uri("api.instagram.com/oauth/access_token")
            .body(
                BodyInserters.fromFormData("grant_type", "ig_exchange_token")
                    .with("client_secret", clientSecret)
                    .with("access_token", accessToken)
            )
            .retrieve()
            .bodyToMono(InstagramLongLivedTokenResponse::class.java)
    }

    private fun handleLongLivedToken(userId: String, accessToken: String): Mono<InstagramLongLivedTokenResponse> {
        return getLongLivedToken(accessToken)
            .flatMap { response ->
                updateAuthDetails(
                    userId,
                    response.accessToken,
                    response.expiresIn,
                )
                    .thenReturn(response)
            }
    }

    private fun updateAuthDetails(
        userId: String,
        accessToken: String? = null,
        expiresIn: Long? = null,
        instagramUserId: String? = null,
    ): Mono<User> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                instagramUserId?.let { user.instagram.userId = aesEncryption.encrypt(it) }
                accessToken?.let { user.instagram.accessToken = aesEncryption.encrypt(it) }
                expiresIn?.let { user.instagram.expirationDate = LocalDateTime.now().plusSeconds(expiresIn) }

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(user)
            }
    }


}