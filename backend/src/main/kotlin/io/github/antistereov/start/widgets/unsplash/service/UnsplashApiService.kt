package io.github.antistereov.start.widgets.unsplash.service

import io.github.antistereov.start.global.service.BaseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class UnsplashApiService(
    private val tokenService: UnsplashTokenService,
    private val baseService: BaseService,
) {

    @Value("\${unsplash.clientId}")
    private lateinit var clientId: String
    @Value("\${unsplash.apiBaseUrl}")
    private lateinit var apiBaseUrl: String

    fun getRandomPhoto(query: String? = null): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("$apiBaseUrl/photos/random")
            .queryParam("client_id", clientId)
            .queryParam("orientation", "landscape")

        if (query != null) uri.queryParam("query", query)

        return baseService.makeGetRequest(uri.toUriString())
    }

    fun getPhoto(id: String): Mono<String> {
        val uri = UriComponentsBuilder.fromHttpUrl("$apiBaseUrl/photos/$id")
            .queryParam("client_id", clientId)
            .toUriString()

        return baseService.makeGetRequest(uri)
    }

    fun likePhoto(userId: String, photoId: String): Mono<String> {
        val uri = "$apiBaseUrl/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedGetRequest(uri, accessToken)
        }
    }

    fun unlikePhoto(userId: String, photoId: String): Mono<String> {
        val uri = "$apiBaseUrl/photos/$photoId/like"
        return tokenService.getAccessToken(userId).flatMap { accessToken ->
            baseService.makeAuthorizedDeleteRequest(uri, accessToken)
        }
    }
}
