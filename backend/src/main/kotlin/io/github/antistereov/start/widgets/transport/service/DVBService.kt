package io.github.antistereov.start.widgets.transport.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Service
class DVBService(
    private val webClient: WebClient
) {

    fun getDepartures(stopId: String): Flux<String> {
        TODO()
    }

    fun getNearbyStops(lat: Double, lon: Double): Flux<String> {
        TODO()
    }

    fun getConnection(fromStopId: String, toStopId: String): Flux<String> {
        TODO()
    }
}
