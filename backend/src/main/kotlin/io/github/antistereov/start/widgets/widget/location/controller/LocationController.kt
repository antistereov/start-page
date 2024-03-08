package io.github.antistereov.start.widgets.widget.location.controller

import io.github.antistereov.start.widgets.widget.transport.company.vvo.model.NearbyStop
import io.github.antistereov.start.widgets.widget.location.service.LocationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/location")
class LocationController(
    private val locationService: LocationService,
) {

    private val logger: Logger = LoggerFactory.getLogger(LocationController::class.java)

    @GetMapping
    fun getLocation(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double
    ): Mono<String> {
        logger.info("Getting location for lat: $lat, lon: $lon")

        return locationService.getLocationAddress(lat, lon).map { address ->
            locationService.toAddressString(address)
        }
    }

    @GetMapping("/nearbyPublicTransport")
    fun getNearbyPublicTransport(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double,
        @RequestParam(required = false, defaultValue = "500") radius: Long,
    ): Flux<NearbyStop> {
        logger.info("Getting nearby public transport for lat: $lat, lon: $lon, radius: $radius")

        return locationService.getNearbyPublicTransport(lat, lon, radius)
    }
}