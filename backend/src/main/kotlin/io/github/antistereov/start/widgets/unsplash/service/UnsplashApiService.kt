package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.unsplash.config.UnsplashProperties
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashApiService(
    private val tokenService: UnsplashTokenService,
    private val baseService: BaseService,
    private val properties: UnsplashProperties,
) {

    fun getRandomPhoto(query: String? = null): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/random")
            .queryParam("client_id", properties.clientId)
            .queryParam("orientation", "landscape")

        if (query != null) uri.queryParam("query", query)

        return baseService.makeGetRequest(uri.toUriString())
    }

    fun getPhoto(id: String): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/photos/$id")
            .queryParam("client_id", properties.clientId)
            .toUriString()

        return baseService.makeGetRequest(uri)
    }

    fun likePhoto(userId: String, photoId: String): Mono<String> {
        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }

    fun unlikePhoto(userId: String, photoId: String): Mono<String> {
        val uri = "${properties.apiBaseUrl}/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedDeleteRequest(uri, accessToken)
        }
    }
}
