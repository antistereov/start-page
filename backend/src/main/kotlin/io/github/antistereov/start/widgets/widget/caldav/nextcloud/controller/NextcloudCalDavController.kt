package io.github.antistereov.start.widgets.widget.caldav.nextcloud.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.auth.nextcloud.service.NextcloudAuthService
import io.github.antistereov.start.widgets.widget.caldav.model.CalDavResource
import io.github.antistereov.start.widgets.widget.caldav.nextcloud.service.NextcloudCalDavService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/caldav/nextcloud")
class NextcloudCalDavController(
    private val remoteService: NextcloudCalDavService,
    private val nextcloudAuthService: NextcloudAuthService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(NextcloudCalDavController::class.java)

    @GetMapping
    fun getRemoteCalendars(authentication: Authentication): Flux<CalDavResource> {
        logger.info("Getting remote calendars.")

        return principalExtractor.getUserId(authentication).flatMapMany { userId ->
            nextcloudAuthService.getCredentials(userId).flatMapMany { credentials ->
                remoteService.getRemoteResources(credentials)
            }
        }
    }

    @GetMapping("/raw")
    fun getRemoteCalendarsRaw(authentication: Authentication): Mono<MutableMap<String, String>> {
        logger.info("Getting remote calendars.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            nextcloudAuthService.getCredentials(userId).flatMap { credentials ->
                remoteService.getRemoteCalendarsRaw(credentials)
            }
        }
    }
}