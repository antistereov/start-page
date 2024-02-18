package io.github.antistereov.start.widgets.transport.controller

import io.github.antistereov.start.widgets.transport.service.LocationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/widgets/transport")
class LocationController(
    private val locationService: LocationService,
) {

    @GetMapping("/location")
    fun getLocation(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double
    ): Mono<String> {
        return locationService.getLocationAddress(lat, lon).map { address ->
            locationService.toAddressString(address)
        }
    }

    @GetMapping("/nearby")
    fun getNearbyPublicTransport(
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double,
        @RequestParam(required = false, defaultValue = "500") radius: String,
    ): Mono<List<String>> {
        return locationService.getNearbyPublicTransport(lat, lon, radius)
    }
}