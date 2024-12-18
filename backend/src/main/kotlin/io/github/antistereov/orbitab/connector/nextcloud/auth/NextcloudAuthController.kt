package io.github.antistereov.orbitab.connector.nextcloud.auth

import io.github.antistereov.orbitab.auth.service.AuthenticationService
import io.github.antistereov.orbitab.connector.nextcloud.auth.model.NextcloudUserInformation
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/nextcloud")
class NextcloudAuthController(
    private val nextcloudAuthService: NextcloudAuthService,
    private val authenticationService: AuthenticationService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @PostMapping
    suspend fun connect(
        @RequestBody credentials: NextcloudUserInformation
    ) {
        logger.info { "Executing authentication method." }

        val userId = authenticationService.getCurrentUserId()

        return nextcloudAuthService.authentication(userId, credentials)
    }

    @DeleteMapping
    suspend fun disconnect() {
        logger.info { "Executing logout method." }

        val userId = authenticationService.getCurrentUserId()
        nextcloudAuthService.logout(userId)
    }
}
