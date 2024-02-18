package io.github.antistereov.start.widgets.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.nextcloud.model.NextcloudCredentials
import io.github.antistereov.start.widgets.nextcloud.service.NextcloudAuthService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/nextcloud")
class NextcloudAuthController(
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(NextcloudAuthController::class.java)

    @PostMapping("/auth")
    fun auth(
        authentication: Authentication,
        @Valid @RequestBody credentials: NextcloudCredentials
    ): Mono<String> {
        logger.info("Executing Nextcloud authentication method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                nextcloudAuthService.authentication(userId, credentials)
            }
    }

    @DeleteMapping("/auth")
    fun logout(authentication: Authentication): Mono<String> {
        logger.info("Executing Nextcloud logout method.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            nextcloudAuthService.logout(userId).then(Mono.fromCallable { "Nextcloud user information deleted for user: $userId." })
        }
    }
}
