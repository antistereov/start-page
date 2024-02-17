package io.github.antistereov.start.global.service

import io.github.antistereov.start.global.model.exception.*
import io.netty.handler.codec.DecoderException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class BaseService(
    private val webClient: WebClient,
) {

    fun makeGetRequest(uri: String): Mono<String> {
        return webClient
            .get()
            .uri(uri)
            .retrieve()
            .let { handleError(uri, it) }
            .bodyToMono(String::class.java)
            .let { handleUnexpectedError(uri, it) }
            .onErrorResume(WebClientResponseException::class.java, handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, handleParsingError(uri))
    }

    fun makeAuthorizedGetRequest(uri: String, accessToken: String): Mono<String> {
        return webClient
            .get()
            .uri(uri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .let { handleError(uri, it) }
            .bodyToMono(String::class.java)
            .let { handleUnexpectedError(uri, it) }
            .onErrorResume(WebClientResponseException::class.java, handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, handleParsingError(uri))
    }

    fun makeAuthorizedDeleteRequest(uri: String, accessToken: String): Mono<String> {
        return webClient
            .delete()
            .uri(uri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .let { handleError(uri, it) }
            .bodyToMono(String::class.java)
            .let { handleUnexpectedError(uri, it) }
            .onErrorResume(WebClientResponseException::class.java, handleNetworkError(uri))
            .onErrorResume(DecoderException::class.java, handleParsingError(uri))
    }

    fun handleError(uri: String, responseSpec: WebClient.ResponseSpec): WebClient.ResponseSpec {
        return responseSpec.onStatus({ status -> status.is4xxClientError || status.is5xxServerError }, {
            it.bodyToMono(String::class.java)
                .flatMap { errorMessage ->
                    Mono.error(ThirdPartyAPIException(uri, errorMessage))
                }
        })
    }

    fun <T> handleUnexpectedError(uri: String, mono: Mono<T>): Mono<T> {
        return mono.onErrorResume { exception ->
            Mono.error(
                UnexpectedErrorException(
                    "An unexpected error occurred calling uri: $uri.",
                    exception
                )
            )
        }
    }

    fun <T> handleNetworkError(uri: String): (WebClientResponseException) -> Mono<T> = { e ->
        if (e.statusCode == HttpStatus.REQUEST_TIMEOUT) {
            Mono.error(TimeoutException("Request to $uri timed out"))
        } else {
            Mono.error(NetworkErrorException("Network error occurred while calling $uri: ${e.message}"))
        }
    }

    fun <T> handleParsingError(uri: String): (DecoderException) -> Mono<T> = { e ->
        Mono.error(ParsingErrorException("Error parsing response from $uri: ${e.message}"))
    }
}
