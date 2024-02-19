package io.github.antistereov.start.widgets.transport.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.transport.model.DepartureMonitor
import io.github.antistereov.start.widgets.transport.model.Point
import io.github.antistereov.start.widgets.transport.model.PointFinder
import io.netty.handler.codec.DecoderException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DVBDepartureService(
    private val webClient: WebClient,
    private val locationService: LocationService,
    private val baseService: BaseService,
) {

    val logger: Logger = LoggerFactory.getLogger(DVBDepartureService::class.java)

    fun getNearbyDepartures(lat: Double, lon: Double, radius: Long, limit: Long): Flux<DepartureMonitor> {
        logger.debug("Getting nearby departures for $lat, $lon")
        return locationService.getNearbyPublicTransport(lat, lon, radius).map { nearbyStops ->
            nearbyStops
        }.flatMap { departures ->
            getDeparturesByStopName(departures.name, limit)
        }
    }

    fun getDeparturesByStopId(stopId: String, limit: Long): Flux<DepartureMonitor> {
        logger.debug("Getting departures for $stopId")
        val url = "http://webapi.vvo-online.de/dm?format=json"
        val requestBody = mapOf(
            "stopid" to stopId,
            "time" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            "isarrival" to false,
            "limit" to limit,
            "shorttermchanges" to true,
            "mot" to listOf("Tram", "CityBus", "IntercityBus", "SuburbanRailway", "Train", "HailedSharedTaxi")
        )

        return webClient.post()
            .uri(url)
            .bodyValue(requestBody)
            .retrieve()
            .let { baseService.handleError(url, it) }
            .bodyToFlux(DepartureMonitor::class.java)
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(url))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(url))
    }

    fun getDeparturesByStopName(name: String, limit: Long): Flux<DepartureMonitor> {
        logger.debug("Getting departures for $name")
        return getStopIdByName(name).flatMap { points ->
            getDeparturesByStopId(points.id, limit)
        }
    }

    private fun getStopIdByName(name: String): Flux<Point> {
        logger.debug("Getting stop ID for $name")
        return pointFinder(name, true).flatMapMany { pointFinder ->
            if (pointFinder.points.isEmpty()) {
                return@flatMapMany Flux.error(IllegalArgumentException("No stop with name $name found"))
            }
            parsePoints(pointFinder.points)
        }
    }

    fun pointFinder(query: String, stopsOnly: Boolean): Mono<PointFinder> {
        logger.debug("Finding points for $query")
        val url = UriComponentsBuilder.fromHttpUrl("https://webapi.vvo-online.de/tr/pointfinder?format=json")
            .queryParam("query", query)
            .queryParam("stopsOnly", stopsOnly)
            .queryParam("dvb", true)
            .build()
            .toUriString()

        return webClient.post()
            .uri(url)
            .retrieve()
            .let { baseService.handleError(url, it) }
            .bodyToMono(PointFinder::class.java)
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(url))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(url))
    }

    fun bestPointIdFinder(query: String, stopsOnly: Boolean): Mono<String> {
        return pointFinder(query, stopsOnly).handle { pointFinder, sink ->
            if (pointFinder.points.isEmpty()) {
                sink.error(IllegalArgumentException("No point found for $query"))
                return@handle
            }
            sink.next(pointFinder.points[0].split("|")[0])
        }
    }

    fun parsePoints(points: List<String>): Flux<Point> {
        logger.debug("Parsing points")
        return Flux.fromIterable(points).flatMap { pointString ->
            val fields = pointString.split("|")
            if (fields.getOrNull(0) == null || fields.getOrNull(3) == null) {
                return@flatMap Flux.error<Point>(IllegalArgumentException("Point ID is missing"))
            }
            Flux.just(
                Point(
                    id = fields[0],
                    type = fields.getOrNull(1),
                    city = fields.getOrNull(2),
                    name = fields[3],
                )
            )
        }
    }
}
