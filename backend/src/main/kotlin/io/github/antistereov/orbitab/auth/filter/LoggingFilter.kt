package io.github.antistereov.orbitab.auth.filter

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class LoggingFilter : WebFilter {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val method = request.method
        val path = request.uri.path

        logger.debug { "Incoming request  - $method $path" }

        return chain.filter(exchange)
            .doOnSuccess {

                val status = exchange.response.statusCode
                logger.debug { "Outgoing response - $method $path - Status: $status" }
            }
            .onErrorResume { e ->
                val status = exchange.response.statusCode
                logger.warn { "Request failed    - $method $path - Status: $status - Error: ${e.message}" }
                throw e
            }
    }
}