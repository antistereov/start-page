package io.github.antistereov.start.config

import io.github.antistereov.start.global.exception.NetworkErrorException
import io.github.antistereov.start.global.exception.ParsingErrorException
import io.github.antistereov.start.global.exception.ThirdPartyAPIException
import io.github.antistereov.start.global.exception.TimeoutException
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
    fun webClient(): WebClient {
        return WebClient.builder()
            .build()
    }
}