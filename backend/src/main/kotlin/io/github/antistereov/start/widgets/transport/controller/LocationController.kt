package io.github.antistereov.start.widgets.transport.controller

import io.github.antistereov.start.widgets.transport.model.NearbyStop
import io.github.antistereov.start.widgets.transport.service.LocationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/transport")
class LocationController(
    private val locationService: LocationService,
) {

    val logger: Logger = LoggerFactory.getLogger(LocationController::class.java)

    @GetMapping("/location")
    fun getLocation(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double
    ): Mono<String> {
        logger.info("Getting location for lat: $lat, lon: $lon")

        return locationService.getLocationAddress(lat, lon).map { address ->
            locationService.toAddressString(address)
        }
    }

    @GetMapping("/nearby")
    fun getNearbyPublicTransport(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double,
        @RequestParam(required = false, defaultValue = "500") radius: Long,
    ): Flux<NearbyStop> {
        logger.info("Getting nearby public transport for lat: $lat, lon: $lon, radius: $radius")

        return locationService.getNearbyPublicTransport(lat, lon, radius)
    }
}