package io.github.antistereov.start.widgets.widget.transport.company.vvo.controller

import io.github.antistereov.start.widgets.widget.transport.company.vvo.model.DepartureMonitor
import io.github.antistereov.start.widgets.widget.transport.company.vvo.model.PointFinder
import io.github.antistereov.start.widgets.widget.transport.company.vvo.service.VVODepartureService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/transport/vvo")
class VVODepartureController(
    private val vvoDepartureService: VVODepartureService
) {

    private val logger: Logger = LoggerFactory.getLogger(VVODepartureController::class.java)

    @GetMapping("/departures/nearby")
    fun getNearbyDepartures(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double,
        @RequestParam(required = false, defaultValue = "500") radius: Long,
        @RequestParam(required = false, defaultValue = "10") limit: Long,
    ): Flux<DepartureMonitor> {
        logger.info("Getting nearby departures for lat: $lat, lon: $lon, radius: $radius, limit: $limit")

        return vvoDepartureService.getNearbyDepartures(lat, lon, radius, limit)
    }

    @GetMapping("/departures/stopName")
    fun getDeparturesByStopName(
        @RequestParam(required = true) name: String,
        @RequestParam(required = false, defaultValue = "10") limit: Long,
    ): Flux<DepartureMonitor> {
        logger.info("Getting departures for stop name: $name, limit: $limit")

        return vvoDepartureService.getDeparturesByStopName(name, limit)
    }

    @GetMapping("/departures/{stopId}")
    fun pointFinder(
        @PathVariable stopId: String,
        @RequestParam(required = false, defaultValue = "10") limit: Long,
    ): Flux<DepartureMonitor> {
        logger.info("Getting departures for stop id: $stopId, limit: $limit")

        return vvoDepartureService.getDeparturesByStopId(stopId, limit)
    }

    @GetMapping("/pointIdFinder")
    fun bestPointIdFinder(
        @RequestParam(required = true) query: String,
        @RequestParam(required = false, defaultValue = "false") stopsOnly: Boolean
    ): Mono<String> {
        logger.info("Getting best point id finder for query: $query, stopsOnly: $stopsOnly")

        return vvoDepartureService.bestPointIdFinder(query, stopsOnly)
    }

    @GetMapping("/pointFinder")
    fun pointFinder(
        @RequestParam(required = true) query: String,
        @RequestParam(required = false, defaultValue = "false") stopsOnly: Boolean
    ): Mono<PointFinder> {
        logger.info("Getting point finder for query: $query, stopsOnly: $stopsOnly")

        return vvoDepartureService.pointFinder(query, stopsOnly)
    }
}