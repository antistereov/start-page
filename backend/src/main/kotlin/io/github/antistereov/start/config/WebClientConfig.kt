package io.github.antistereov.start.config

import io.github.antistereov.start.global.model.exception.NetworkErrorException
import io.github.antistereov.start.global.model.exception.ParsingErrorException
import io.github.antistereov.start.global.model.exception.ThirdPartyAPIException
import io.github.antistereov.start.global.model.exception.TimeoutException
import io.netty.handler.codec.DecoderException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Configuration
class WebClientConfig {

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        val strategies = ExchangeStrategies.builder()
            .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) } // 2MB
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
            .filter { request, next ->
                next.exchange(request).flatMap { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono(String::class.java)
                            .flatMap { errorMessage ->
                                Mono.error(
                                    ThirdPartyAPIException(request.url(), clientResponse.statusCode(), errorMessage)
                                )
                            }
                    } else {
                        Mono.just(clientResponse)
                    }
                }
                .onErrorResume(WebClientResponseException::class.java) { e ->
                    if (e.statusCode == HttpStatus.REQUEST_TIMEOUT) {
                        Mono.error(
                            TimeoutException("Timeout occurred while calling ${request.url()}: ${e.message}")
                        )
                    } else {
                        Mono.error(
                            NetworkErrorException("Network error occurred while calling ${request.url()}: ${e.message}")
                        )
                    }
                }
                .onErrorResume(DecoderException::class.java) { e ->
                    Mono.error(
                        ParsingErrorException("Error parsing response from ${request.url()}: ${e.message}")
                    )
                }
            }
    }

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder.build()
    }
}