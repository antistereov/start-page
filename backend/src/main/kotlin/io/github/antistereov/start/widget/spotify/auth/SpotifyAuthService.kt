package io.github.antistereov.start.widget.spotify.auth

import io.github.antistereov.start.global.exception.InvalidCallbackException
import io.github.antistereov.start.global.exception.ThirdPartyAuthorizationCanceledException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.StateValidation
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widget.shared.model.WidgetUserInformation
import io.github.antistereov.start.widget.spotify.auth.model.SpotifyTokenResponse
import io.github.antistereov.start.widget.spotify.exception.SpotifyAccessTokenNotFoundException
import io.github.antistereov.start.widget.spotify.exception.SpotifyException
import io.github.antistereov.start.widget.spotify.model.SpotifyUserProfile
import io.github.antistereov.start.widget.spotify.model.SpotifyUserInformation
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
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

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getAuthorizationUrl(userId: String): String {
        logger.debug { "Getting authorization URL for user: $userId." }

        val state = stateValidation.createState(userId)

        return UriComponentsBuilder
            .fromHttpUrl("https://accounts.spotify.com/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId)
            .queryParam("scope", properties.scopes)
            .queryParam("redirect_uri", properties.redirectUri)
            .queryParam("state", state)
            .toUriString()
    }

    suspend fun authenticate(code: String?, state: String?, error: String?): SpotifyUserProfile {

        suspend fun getSpotifyTokenResponse(code: String): SpotifyTokenResponse {
            logger.debug { "Handling authentication." }

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
                .awaitBody<SpotifyTokenResponse>()
        }

        suspend fun handleUser(userId: String, response: SpotifyTokenResponse) {
            logger.debug { "Handling user." }

            val user = userService.findById(userId)

            val refreshToken = response.refreshToken
            val expirationDate = LocalDateTime.now().plusSeconds(response.expiresIn)

            val widgetInformation = user.widgets ?: WidgetUserInformation()

            val spotifyInfo = widgetInformation.spotify ?: SpotifyUserInformation()

            val updatedSpotifyInfo = spotifyInfo.copy(
                accessToken = aesEncryption.encrypt(response.accessToken),
                refreshToken = aesEncryption.encrypt(refreshToken),
                expirationDate = expirationDate
            )

            val updatedWidgetInformation = widgetInformation.copy(spotify = updatedSpotifyInfo)

            val updatedUser = user.copy(widgets = updatedWidgetInformation)

            userService.save(updatedUser)
        }

        logger.debug { "Authenticating user." }

        if (code != null && state != null) {
            val spotifyTokenResponse = getSpotifyTokenResponse(code)
            val userId = stateValidation.getUserId(state)
            handleUser(userId, spotifyTokenResponse)

            getUserProfile(userId)
        }

        if (error != null) {
            throw ThirdPartyAuthorizationCanceledException(
                properties.serviceName,
                error,
                error
            )
        }

        throw InvalidCallbackException(
            properties.serviceName,
            "Invalid request parameters."
        )

    }

    suspend fun logout(userId: String) {
        logger.debug { "Logging out user: $userId." }

        val user = userService.findById(userId)

        val widgetsInfo = user.widgets ?: WidgetUserInformation()
        val updatedWidgetInfo = widgetsInfo.copy(spotify = null)

        val updatedUser = user.copy(widgets = updatedWidgetInfo)

        userService.save(updatedUser)
    }

    suspend fun getUserProfile(userId: String): SpotifyUserProfile {
        logger.debug { "Fetching Spotify user profile for user $userId" }
        val accessToken = getAccessToken(userId)

        return webClient.get()
            .uri("${properties.apiBaseUrl}/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<SpotifyUserProfile>()
    }

    suspend fun getAccessToken(userId: String): String {
        logger.debug { "Getting access token for user: $userId." }

        val currentTime = LocalDateTime.now()

        val user = userService.findById(userId)

        val expirationDate = user.widgets?.spotify?.expirationDate
            ?: throw SpotifyException("No expiration date for Spotify access token saved for user $userId")

        return if (currentTime.isAfter(expirationDate)) {
            this.refreshToken(userId).accessToken
        } else {
            val encryptedSpotifyAccessToken = user.widgets.spotify.accessToken
                ?: throw SpotifyAccessTokenNotFoundException(userId)

            aesEncryption.decrypt(encryptedSpotifyAccessToken)
        }
    }

    private suspend fun refreshToken(userId: String): SpotifyTokenResponse {
        logger.debug { "Refreshing token for user: $userId." }

        val uri = "https://accounts.spotify.com/api/token"

        val user = userService.findById(userId)

        val encryptedRefreshToken = user.widgets?.spotify?.refreshToken
            ?: throw SpotifyAccessTokenNotFoundException(userId)

        val refreshToken = aesEncryption.decrypt(encryptedRefreshToken)

        return webClient.post()
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
            .awaitBody<SpotifyTokenResponse>()
    }
}