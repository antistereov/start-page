package io.github.antistereov.start.widgets.widget.instagram.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.auth.instagram.config.InstagramProperties
import io.github.antistereov.start.widgets.auth.instagram.service.InstagramAuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class InstagramService(
    private val tokenService: InstagramAuthService,
    private val baseService: BaseService,
    private val properties: InstagramProperties,
) {

    private val logger = LoggerFactory.getLogger(InstagramService::class.java)

    fun getUsername(userId: String, instagramUserId: String): Mono<String> {
        logger.debug("Getting username for Instagram user: $instagramUserId.")

        val uri = "${properties.apiBaseUrl}/$instagramUserId?fields=id,username"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }

    fun getUserMedia(
        userId: String,
        instagramUserId: String,
        limit: Long = 25,
        before: String? = null,
        after: String? = null,
    ): Mono<String> {
        logger.debug("Getting media for Instagram user: $instagramUserId.")

        val uri = UriComponentsBuilder
            .fromHttpUrl("${properties.apiBaseUrl}/$instagramUserId/media")
            .queryParam("limit", limit)
            .queryParam("before", before)
            .queryParam("after", after)
            .toUriString()
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }

    fun getMedia(userId: String, mediaId: String): Mono<String> {
        logger.debug("Getting media with ID: $mediaId.")

        val uri = "${properties.apiBaseUrl}/$mediaId?fields=id,media_type,media_url,username,timestamp,caption,permalink"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.getMono(uri, accessToken)
        }
    }
}
