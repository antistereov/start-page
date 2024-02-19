package io.github.antistereov.start.widgets.auth.instagram.service

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.global.component.StateValidation
import io.github.antistereov.start.global.model.exception.*
import io.github.antistereov.start.widgets.auth.instagram.model.InstagramAuthDetails
import io.github.antistereov.start.widgets.auth.instagram.config.InstagramProperties
import io.github.antistereov.start.widgets.auth.instagram.model.InstagramLongLivedTokenResponse
import io.github.antistereov.start.widgets.auth.instagram.model.InstagramShortLivedTokenResponse
import io.github.antistereov.start.widgets.auth.instagram.model.InstagramUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class InstagramAuthService(
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: InstagramProperties,
) {

    private val logger = LoggerFactory.getLogger(InstagramAuthService::class.java)

    fun getAuthorizationUrl(userId: String): Mono<String> {
        logger.debug("Getting authorization URL for user $userId")

        return stateValidation.createState(userId).map { state ->
            UriComponentsBuilder.fromHttpUrl("https://api.instagram.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId)
                .queryParam("scope", properties.scopes)
                .queryParam("redirect_uri", properties.redirectUri)
                .queryParam("state", state)
                .toUriString()
        }
    }

    fun authenticate(
        code: String?,
        state: String?,
        error: String?,
        errorCode: String?,
        errorReason: String?,
    ): Mono<InstagramLongLivedTokenResponse> {
        logger.debug("Authenticating with state: $state, error: $error, errorCode: $errorCode, errorReason: $errorReason")

        if (code != null && state != null) {
            return handleShortLivedToken(code, state).flatMap { user ->
                val userId = user.id
                val accessToken = user.auth.instagram.accessToken
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "access token", userId))

                handleLongLivedToken(userId, accessToken)
            }
        }

        if (error != null && errorCode != null && errorReason != null) {
            return Mono.error(ThirdPartyAuthorizationCanceledException(properties.serviceName, errorCode, errorReason))
        }

        return Mono.error(InvalidCallbackException(properties.serviceName, "Invalid request parameters."))
    }

    fun getAccessToken(userId: String): Mono<String> {
        logger.debug("Getting access token for user $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val accessToken = user.auth.instagram.accessToken
                if (accessToken != null) {
                    Mono.just(aesEncryption.decrypt(accessToken))
                } else {
                    Mono.error(MissingCredentialsException(properties.serviceName, "access token", userId))
            }
        }
    }

    fun refreshAccessToken(userId: String): Mono<User> {
        logger.debug("Refreshing access token for user $userId")

        val currentTime = LocalDateTime.now()

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                val expirationDate = user.auth.instagram.expirationDate
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "expirationDate", userId))
                if (currentTime.isAfter(expirationDate)) {
                    return@flatMap Mono.error(ExpiredTokenException(properties.serviceName, userId))
                }

                val encryptedAccessToken = user.auth.instagram.accessToken
                    ?: return@flatMap Mono.error(MissingCredentialsException(properties.serviceName, "access token", userId))
                val accessToken = aesEncryption.decrypt(encryptedAccessToken)

                val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/refresh_access_token")
                    .queryParam("grant_type", "ig_refresh_token")
                    .queryParam("access_token", accessToken)
                    .toUriString()

                webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(InstagramLongLivedTokenResponse::class.java)
                    .flatMap { token ->
                        updateAuthDetails(userId, accessToken = token.accessToken, expiresIn = token.expiresIn)
                    }
            }
    }

    fun saveAccessToken(userId: String, accessToken: String): Mono<User> {
        logger.debug("Saving access token for user $userId")

        return updateAuthDetails(userId, accessToken = accessToken, expiresIn = 90*24*60*60)
    }

    fun updateUserInfo(userId: String): Mono<User> {
        logger.debug("Updating user info for user $userId")

        return getAccessToken(userId).flatMap { accessToken ->
            val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/me")
                .queryParam("access_token", accessToken)
                .queryParam("fields", "username")
                .toUriString()

            webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(InstagramUser::class.java)
                .flatMap { instagramUser ->
                    if (instagramUser.username != null && instagramUser.id != null) {
                        updateAuthDetails(
                            userId,
                            instagramUserId = instagramUser.id,
                            instagramUsername = instagramUser.username
                        )
                    } else {
                        Mono.error(InvalidThirdPartyAPIResponseException(properties.serviceName, "No username in response found."))
                    }
                }
        }
    }

    fun logout(userId: String): Mono<Void> {
        logger.debug("Logging out user $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                user.auth.instagram = InstagramAuthDetails()
                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .then()
            }
    }

    private fun getShortLivedToken(code: String): Mono<InstagramShortLivedTokenResponse> {
        logger.debug("Getting short lived token.")

        return webClient
            .post()
            .uri("https://api.instagram.com/oauth/access_token")
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", properties.clientId)
                    .with("client_secret", properties.clientSecret)
                    .with("code", code)
                    .with("redirect_uri", properties.redirectUri)
            )
            .retrieve()
            .bodyToMono(InstagramShortLivedTokenResponse::class.java)
    }

    private fun handleShortLivedToken(
        code: String,
        state: String,
        expiresIn: Long? = 3600L
    ): Mono<User> {
        logger.debug("Handling short lived token.")

        return stateValidation.getUserId(state).flatMap { userId ->
            getShortLivedToken(code).flatMap { response ->
                updateAuthDetails(
                    userId = userId,
                    accessToken = response.accessToken,
                    expiresIn = expiresIn,
                    instagramUserId = response.userId
                )
            }
        }
    }

    private fun getLongLivedToken(accessToken: String): Mono<InstagramLongLivedTokenResponse> {
        logger.debug("Getting long lived token.")

        return webClient
            .post()
            .uri("https://api.instagram.com/oauth/access_token")
            .body(
                BodyInserters.fromFormData("grant_type", "ig_exchange_token")
                    .with("client_secret", properties.clientSecret)
                    .with("access_token", accessToken)
            )
            .retrieve()
            .bodyToMono(InstagramLongLivedTokenResponse::class.java)
    }

    private fun handleLongLivedToken(userId: String, accessToken: String): Mono<InstagramLongLivedTokenResponse> {
        logger.debug("Handling long lived token for user $userId")

        return getLongLivedToken(accessToken).flatMap { response ->
            updateAuthDetails(
                userId = userId,
                accessToken = response.accessToken,
                expiresIn = response.expiresIn,
            )
                .thenReturn(response)
        }
    }

    private fun updateAuthDetails(
        userId: String,
        instagramUserId: String? = null,
        instagramUsername: String? = null,
        accessToken: String? = null,
        expiresIn: Long? = null,
    ): Mono<User> {
        logger.debug("Updating auth details for user $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
            .flatMap { user ->
                instagramUserId?.let { user.auth.instagram.userId = aesEncryption.encrypt(it) }
                instagramUsername?.let {user.auth.instagram.username = aesEncryption.encrypt(it)}
                accessToken?.let { user.auth.instagram.accessToken = aesEncryption.encrypt(it) }
                expiresIn?.let { user.auth.instagram.expirationDate = LocalDateTime.now().plusSeconds(it) }

                userRepository.save(user)
                    .onErrorMap { throwable ->
                        CannotSaveUserException(throwable)
                    }
                    .thenReturn(user)
            }
    }
}
