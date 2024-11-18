package io.github.antistereov.start.connector.nextcloud.auth

import io.github.antistereov.start.connector.nextcloud.exception.NextcloudCredentialsException
import io.github.antistereov.start.connector.nextcloud.exception.NextcloudInvalidCredentialsException
import io.github.antistereov.start.connector.shared.model.ConnectorInformation
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.connector.nextcloud.auth.model.NextcloudUserInformation
import io.github.antistereov.start.global.component.AuthHandler
import io.github.antistereov.start.global.component.UrlHandler
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange

@Service
class NextcloudAuthService(
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
    private val webClient: WebClient,
    private val urlHandler: UrlHandler,
    private val authHandler: AuthHandler,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun getCredentials(userId: String): NextcloudUserInformation {
        logger.debug { "Getting credentials for user: $userId." }

        val user = userService.findById(userId)

        val nextcloudUserInfo = user.connectors?.nextcloud
            ?: throw NextcloudCredentialsException("No Nextcloud credentials found for user $userId")

        return nextcloudUserInfo.copy(
            host = aesEncryption.decrypt(nextcloudUserInfo.host),
            username = aesEncryption.decrypt(nextcloudUserInfo.username),
            password = aesEncryption.decrypt(nextcloudUserInfo.password),
        )
    }

    suspend fun authentication(userId: String, credentials: NextcloudUserInformation) {
        logger.debug { "Authenticating user: $userId." }

        val user = userService.findById(userId)
        val credentialsWithNormalizedUrl = credentials.copy(
            host = urlHandler.normalizeBaseUrl(credentials.host)
        )

        if (!credentialsAreValid(credentialsWithNormalizedUrl)) {
            throw NextcloudInvalidCredentialsException("Cannot authenticate user")
        }

        val userConnectorInformation = user.connectors ?: ConnectorInformation()

        val encryptedNextcloudInformation = credentialsWithNormalizedUrl.copy(
            host = aesEncryption.encrypt(credentialsWithNormalizedUrl.host),
            username = aesEncryption.encrypt(credentialsWithNormalizedUrl.username),
            password = aesEncryption.encrypt(credentialsWithNormalizedUrl.password),
        )

        val updatedUser = user.copy(
            connectors = userConnectorInformation.copy(
                nextcloud = encryptedNextcloudInformation
            )
        )

        userService.save(updatedUser)
    }


    suspend fun credentialsAreValid(credentials: NextcloudUserInformation): Boolean {
        logger.debug { "Verifying credentials." }

        val encodedCredentials = authHandler.createBasicAuthHeader(credentials.username, credentials.password)

        return webClient.get()
                .uri("${credentials.host}/remote.php/dav/files/${credentials.username}")
                .header("Authentication", "Basic $encodedCredentials")
                .awaitExchange { clientResponse ->
                    when {
                        clientResponse.statusCode().is2xxSuccessful -> true
                        clientResponse.statusCode() == HttpStatus.UNAUTHORIZED -> false
                        else -> throw NextcloudInvalidCredentialsException("Credentials are not valid, " +
                                "status code: ${clientResponse.statusCode()}")

                    }
                }
    }


    suspend fun logout(userId: String) {
        logger.debug { "Logging out user: $userId." }

        val user = userService.findById(userId)
        val updatedUser = user.copy(
            connectors = user.connectors?.copy(
                nextcloud = null
            )
        )

        userService.save(updatedUser)
    }
}