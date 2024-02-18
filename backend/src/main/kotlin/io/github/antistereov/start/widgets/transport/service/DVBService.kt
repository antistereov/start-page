package io.github.antistereov.start.widgets.transport.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder

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

    fun getStopInfo(stopId: String): Flux<String> {
        TODO()
    }

    fun getConnection(fromStopId: String, toStopId: String): Flux<String> {
        TODO()
    }

    fun getLocationAddress(latitude: Double, longitude: Double): Mono<String> {
        return encode(latitude.toString())
            .zipWith(encode(longitude.toString()))
            .flatMap { tuple ->
                val encodedLatitude = tuple.t1
                val encodedLongitude = tuple.t2
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&" +
                        "lat=$encodedLatitude&" +
                        "lon=$encodedLongitude"
                webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String::class.java)
            }
            .map { response ->
                val mapper = ObjectMapper()
                val json = mapper.readTree(response)
                json.get("display_name").asText()
            }
    }

    private fun encode(value: String): Mono<String> {
        return Mono.fromCallable { URLEncoder.encode(value, "UTF-8") }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { Mono.just(it) }
    }
}

