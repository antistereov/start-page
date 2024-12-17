package io.github.antistereov.start.connector.nextcloud.auth

import io.github.antistereov.start.auth.service.PrincipalService
import io.github.antistereov.start.connector.nextcloud.auth.model.NextcloudUserInformation
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/auth/nextcloud")
class NextcloudAuthController(
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalService: PrincipalService,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    @PostMapping
    suspend fun connect(
        exchange: ServerWebExchange,
        @RequestBody credentials: NextcloudUserInformation
    ) {
        logger.info { "Executing authentication method." }

        val userId = principalService.getUserId(exchange)

        return nextcloudAuthService.authentication(userId, credentials)
    }

    @DeleteMapping
    suspend fun disconnect(exchange: ServerWebExchange) {
        logger.info { "Executing logout method." }

        val userId = principalService.getUserId(exchange)
        nextcloudAuthService.logout(userId)
    }
}
