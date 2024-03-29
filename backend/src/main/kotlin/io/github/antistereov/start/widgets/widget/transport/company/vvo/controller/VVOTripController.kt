package io.github.antistereov.start.widgets.widget.transport.company.vvo.controller

import io.github.antistereov.start.widgets.widget.transport.company.vvo.service.VVOTripService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RestController
@RequestMapping("/transport/vvo")
class VVOTripController(
    private val vvoTripService: VVOTripService,
) {

    private val logger: Logger = LoggerFactory.getLogger(VVOTripController::class.java)

    @GetMapping("/trips")
    fun getTrip(
        @RequestParam(required = true) origin: String,
        @RequestParam(required = true) destination: String,
        @RequestParam(required = true) time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "false") isArrivalTime: Boolean,
        @RequestParam(required = false, defaultValue = "true") shortTermChanges: Boolean,
        @RequestParam(required = false, defaultValue = "None") mobilityRestriction: String,
        @RequestParam(required = false, defaultValue = "5") maxChanges: String,
        @RequestParam(required = false, defaultValue = "normal") walkingSpeed: String,
        @RequestParam(required = false, defaultValue = "10") footpathToStop: Int,
        @RequestParam(
            required = false,
            defaultValue = "Tram,CityBus,IntercityBus,SuburbanRailway,Train,Cableway,Ferry,HailedSharedTaxi"
        ) mot: List<String>,
        @RequestParam(required = false, defaultValue = "true") includeAlternativeStops: Boolean,

        ): Mono<String> {
        logger.info("Getting trip for originId: $origin, destinationId: $destination")

        return vvoTripService.getTrip(
            origin,
            destination,
            time,
            isArrivalTime,
            shortTermChanges,
            mobilityRestriction,
            maxChanges,
            walkingSpeed,
            footpathToStop,
            mot,
            includeAlternativeStops
        )
    }

    @GetMapping("/trips/fromAddressToAddress")
    fun getTripFromAddressToAddress(
        @RequestParam(required = true) origin: String,
        @RequestParam(required = true) destination: String,
        @RequestParam(required = true) time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "false") isArrivalTime: Boolean,
        @RequestParam(required = false, defaultValue = "true") shortTermChanges: Boolean,
        @RequestParam(required = false, defaultValue = "None") mobilityRestriction: String,
        @RequestParam(required = false, defaultValue = "5") maxChanges: String,
        @RequestParam(required = false, defaultValue = "normal") walkingSpeed: String,
        @RequestParam(required = false, defaultValue = "10") footpathToStop: Int,
        @RequestParam(
            required = false,
            defaultValue = "Tram,CityBus,IntercityBus,SuburbanRailway,Train,Cableway,Ferry,HailedSharedTaxi"
        ) mot: List<String>,
        @RequestParam(required = false, defaultValue = "true") includeAlternativeStops: Boolean,

        ): Mono<String> {
        logger.info("Getting trip for originAddress: $origin, destinationAddress: $destination")

        return vvoTripService.getTripsFromAddressToAddress(
            origin,
            destination,
            time,
            isArrivalTime,
            shortTermChanges,
            mobilityRestriction,
            maxChanges,
            walkingSpeed,
            footpathToStop,
            mot,
            includeAlternativeStops
        )
    }

    @GetMapping("/trips/fromLocation")
    fun getTripsFromLocationToAddress(
        @RequestParam(required = true) originLat: Double,
        @RequestParam(required = true) originLon: Double,
        @RequestParam(required = true) destination: String,
        @RequestParam(required = true) time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "false") isArrivalTime: Boolean,
        @RequestParam(required = false, defaultValue = "true") shortTermChanges: Boolean,
        @RequestParam(required = false, defaultValue = "None") mobilityRestriction: String,
        @RequestParam(required = false, defaultValue = "5") maxChanges: String,
        @RequestParam(required = false, defaultValue = "normal") walkingSpeed: String,
        @RequestParam(required = false, defaultValue = "10") footpathToStop: Int,
        @RequestParam(
            required = false,
            defaultValue = "Tram,CityBus,IntercityBus,SuburbanRailway,Train,Cableway,Ferry,HailedSharedTaxi"
        ) mot: List<String>,
        @RequestParam(required = false, defaultValue = "true") includeAlternativeStops: Boolean,

        ): Mono<String> {
        logger.info("Getting trip for originLat: $originLat, originLon: $originLon, destinationName: $destination")

        return vvoTripService.getTripsFromLocationToAddress(
            originLat,
            originLon,
            destination,
            time,
            isArrivalTime,
            shortTermChanges,
            mobilityRestriction,
            maxChanges,
            walkingSpeed,
            footpathToStop,
            mot,
            includeAlternativeStops
        )
    }

    @GetMapping("/trips/toLocation")
    fun getTripsFromAddressToLocation(
        @RequestParam(required = true) origin: String,
        @RequestParam(required = true) destinationLat: Double,
        @RequestParam(required = true) destinationLon: Double,
        @RequestParam(required = true) time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "false") isArrivalTime: Boolean,
        @RequestParam(required = false, defaultValue = "true") shortTermChanges: Boolean,
        @RequestParam(required = false, defaultValue = "None") mobilityRestriction: String,
        @RequestParam(required = false, defaultValue = "5") maxChanges: String,
        @RequestParam(required = false, defaultValue = "normal") walkingSpeed: String,
        @RequestParam(required = false, defaultValue = "10") footpathToStop: Int,
        @RequestParam(
            required = false,
            defaultValue = "Tram,CityBus,IntercityBus,SuburbanRailway,Train,Cableway,Ferry,HailedSharedTaxi"
        ) mot: List<String>,
        @RequestParam(required = false, defaultValue = "true") includeAlternativeStops: Boolean,

        ): Mono<String> {
        logger.info("Getting trip for originName: $origin, destinationLat: $destinationLat, destinationLon: $destinationLon")

        return vvoTripService.getTripsFromAddressToLocation(
            origin,
            destinationLat,
            destinationLon,
            time,
            isArrivalTime,
            shortTermChanges,
            mobilityRestriction,
            maxChanges,
            walkingSpeed,
            footpathToStop,
            mot,
            includeAlternativeStops
        )
    }
}