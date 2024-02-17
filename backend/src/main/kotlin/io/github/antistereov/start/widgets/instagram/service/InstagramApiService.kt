package io.github.antistereov.start.widgets.instagram.service

import io.github.antistereov.start.global.service.BaseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class InstagramApiService(
    private val tokenService: InstagramTokenService,
    private val baseService: BaseService,
) {

    @Value("\${instagram.apiBaseUrl}")
    private lateinit var apiBaseUrl: String


    fun getUsername(userId: String, instagramUserId: String): Mono<String> {
        val uri = "$apiBaseUrl/$instagramUserId?fields=id,username"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }

    fun getUserMedia(
        userId: String,
        instagramUserId: String,
        limit: Long = 25,
        before: String? = null,
        after: String? = null,
    ): Mono<String> {
        val uri = UriComponentsBuilder
            .fromHttpUrl("$apiBaseUrl/$instagramUserId/media")
            .queryParam("limit", limit)
            .queryParam("before", before)
            .queryParam("after", after)
            .toUriString()
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }

    fun getMedia(userId: String, mediaId: String): Mono<String> {
        val uri = "$apiBaseUrl/$mediaId?fields=id,media_type,media_url,username,timestamp,caption,permalink"

        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }
}
