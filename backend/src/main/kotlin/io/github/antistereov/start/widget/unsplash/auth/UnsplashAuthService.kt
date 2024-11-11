package io.github.antistereov.start.widget.unsplash.auth

import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widget.shared.model.WidgetUserInformation
import io.github.antistereov.start.user.service.StateValidation
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widget.unsplash.UnsplashProperties
import io.github.antistereov.start.widget.unsplash.auth.model.UnsplashPublicUserProfile
import io.github.antistereov.start.widget.unsplash.auth.model.UnsplashTokenResponse
import io.github.antistereov.start.widget.unsplash.auth.model.UnsplashUserProfile
import io.github.antistereov.start.widget.unsplash.exception.UnsplashTokenException
import io.github.antistereov.start.widget.unsplash.exception.UnsplashException
import io.github.antistereov.start.widget.unsplash.exception.UnsplashInvalidCallbackException
import io.github.antistereov.start.widget.unsplash.model.UnsplashUserInformation
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

@Service
class UnsplashAuthService(
    private val webClient: WebClient,
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val stateValidation: StateValidation,
    private val properties: UnsplashProperties,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getAuthorizationUrl(userId: String): String {
        logger.debug { "Creating Unsplash authorization URL for user $userId." }

        val state = stateValidation.createState(userId)

        return UriComponentsBuilder.fromHttpUrl("https://unsplash.com/oauth/authorize")
                .queryParam("redirect_uri", properties.redirectUri)
                .queryParam("client_id", properties.clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", properties.scopes)
                .queryParam("state", state)
                .toUriString()
    }

    suspend fun authenticate(
        code: String?,
        state: String?,
    ): UnsplashPublicUserProfile {

        suspend fun handleTokenResponse(userId: String, code: String): UnsplashTokenResponse {
            logger.debug { "Getting Unsplash token response" }

            val uri = "https://unsplash.com/oauth/token"

            val response = webClient.post()
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
                .awaitBody<UnsplashTokenResponse>()

            val user = userService.findById(userId)

            val widgets = user.widgets ?: WidgetUserInformation()
            val unsplashWidget = widgets.unsplash ?: UnsplashUserInformation()
            val encryptedAccessToken = aesEncryption.encrypt(response.accessToken)

            val updatedUnsplashInfo = unsplashWidget.copy(accessToken = encryptedAccessToken)
            val widgetWithAccessToken = widgets.copy(unsplash = updatedUnsplashInfo)
            val updatedUser = user.copy(widgets = widgetWithAccessToken)

            userService.save(updatedUser)

            return response
        }

        suspend fun handleUser(userId: String): UnsplashPublicUserProfile {
            logger.debug { "Handling Unsplash user $userId" }

            val user = userService.findById(userId)

            val widgets = user.widgets ?: WidgetUserInformation()
            val unsplashWidget = widgets.unsplash ?: UnsplashUserInformation()

            val publicUserProfile = getPublicUserProfile(userId)

            val updatedUnsplashWidget = unsplashWidget.copy(
                username = publicUserProfile.username,
                profileImage = publicUserProfile.profileImage,
            )

            val updatedWidgets = widgets.copy(unsplash = updatedUnsplashWidget)

            val updatedUser = user.copy(widgets = updatedWidgets)

            userService.save(updatedUser)

            return publicUserProfile
        }

        logger.debug { "Received Unsplash callback with state: $state" }

        if (code == null || state == null) {
            throw UnsplashInvalidCallbackException()
        }

        val userId = stateValidation.getUserId(state)
        handleTokenResponse(userId, code)
        return handleUser(userId)
    }

    suspend fun logout(userId: String) {
        logger.debug { "Deleting Unsplash user information for user $userId." }

        val user = userService.findById(userId)

        val updatedWidgets = user.widgets?.copy(unsplash = null) ?: WidgetUserInformation()
        val updatedUser = user.copy(widgets = updatedWidgets)

        userService.save(updatedUser)
    }

    suspend fun getAccessToken(userId: String): String {
        logger.debug { "Getting Unsplash access token for user $userId." }

        val user = userService.findById(userId)
        val encryptedAccessToken = user.widgets?.unsplash?.accessToken
            ?: throw UnsplashTokenException("No Unsplash access token found for user $userId")

        return aesEncryption.decrypt(encryptedAccessToken)
    }

    suspend fun getUserProfile(userId: String): UnsplashUserProfile {
        logger.debug { "Fetching Unsplash user information from connected user $userId" }

        val accessToken = getAccessToken(userId)

        return webClient.get()
            .uri("${properties.apiBaseUrl}/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<UnsplashUserProfile>()
    }

    suspend fun getPublicUserProfile(userId: String): UnsplashPublicUserProfile {
        logger.debug { "Fetching public Unsplash user profile for user $userId" }
        val userProfile = getUserProfile(userId)

        val accessToken = getAccessToken(userId)

        return webClient.get()
            .uri("${properties.apiBaseUrl}/users/${userProfile.username}")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<UnsplashPublicUserProfile>()
    }

    suspend fun getSavedPublicUserProfile(userId: String): UnsplashPublicUserProfile {
        logger.debug { "Fetching saved public Unsplash user profile for user $userId from database" }

        val user = userService.findById(userId)

        val unsplash = user.widgets?.unsplash ?: throw UnsplashException("No Unsplash user information saved")

        if (unsplash.username == null) throw UnsplashException("No Unsplash user information saved")

        return UnsplashPublicUserProfile(
            username = unsplash.username,
            profileImage = unsplash.profileImage,
        )
    }
}