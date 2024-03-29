package io.github.antistereov.start.widgets.widget.instagram.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.instagram.service.InstagramService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/instagram")
class InstagramController(
    private val service: InstagramService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger: Logger = LoggerFactory.getLogger(InstagramController::class.java)

    @GetMapping("/{instagramUserId}")
    fun getUsername(
        authentication: Authentication,
        @PathVariable instagramUserId: String
    ): Mono<String> {
        logger.info("Executing getUsername method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getUsername(userId, instagramUserId)
            }
    }

    @GetMapping("/{instagramUserId}/media")
    fun getUserMedia(
        authentication: Authentication,
        @PathVariable instagramUserId: String,
        @RequestParam limit: Long = 25,
        @RequestParam before: String?,
        @RequestParam after: String?,
    ): Mono<String> {
        logger.info("Executing getUserMedia method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getUserMedia(userId, instagramUserId, limit, before, after)
            }
    }

    @GetMapping("/media/{mediaId}")
    fun getMedia(
        authentication: Authentication,
        @PathVariable mediaId: String
    ): Mono<String> {
        logger.info("Executing getMedia method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { userId ->
                service.getMedia(userId, mediaId)
            }
    }
}
