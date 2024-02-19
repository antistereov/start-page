package io.github.antistereov.start.widgets.transport.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class DVBTripService(
    private val webClient: WebClient,
    private val departureService: DVBDepartureService,
    private val locationService: LocationService,
) {

    private val logger: Logger = LoggerFactory.getLogger(DVBTripService::class.java)

    fun getTripsFromAddressToAddress(
        originName: String,
        destinationName: String,
        time: LocalDateTime,
        isArrivalTime: Boolean = false,
        shortTermChanges: Boolean = true,
        mobilityRestriction: String = "None",
        maxChanges: String = "5",
        walkingSpeed: String = "normal",
        footpathToStop: Int = 10,
        mot: List<String> = listOf("Tram", "CityBus", "IntercityBus", "SuburbanRailway", "Train", "Cableway", "Ferry", "HailedSharedTaxi"),
        includeAlternativeStops: Boolean = true,
    ): Mono<String> {
        logger.debug("Getting trip from $originName to $destinationName")

        return departureService.bestPointIdFinder(originName, false).flatMap { originPoint ->
            departureService.bestPointIdFinder(destinationName, false).flatMap { destinationPoint ->
                getTrip(
                    originPoint,
                    destinationPoint,
                    time,
                    isArrivalTime,
                    shortTermChanges,
                    mobilityRestriction,
                    maxChanges,
                    walkingSpeed,
                    footpathToStop,
                    mot,
                    includeAlternativeStops,
                )
            }
        }
    }

    fun getTripsFromLocationToAddress(
        originLat: Double,
        originLon: Double,
        destinationName: String,
        time: LocalDateTime,
        isArrivalTime: Boolean = false,
        shortTermChanges: Boolean = true,
        mobilityRestriction: String = "None",
        maxChanges: String = "5",
        walkingSpeed: String = "normal",
        footpathToStop: Int = 10,
        mot: List<String> = listOf("Tram", "CityBus", "IntercityBus", "SuburbanRailway", "Train", "Cableway", "Ferry", "HailedSharedTaxi"),
        includeAlternativeStops: Boolean = true,
    ): Mono<String> {
        logger.debug("Getting trip from $originLat, $originLon to $destinationName")

        return locationService.getLocationAddress(originLat, originLon).flatMap { originAddress ->
            departureService.pointFinder(destinationName, false).flatMap { destinationPoints ->
                getTrip(
                    locationService.toAddressString(originAddress),
                    destinationPoints.points[0].split("|")[0],
                    time,
                    isArrivalTime,
                    shortTermChanges,
                    mobilityRestriction,
                    maxChanges,
                    walkingSpeed,
                    footpathToStop,
                    mot,
                    includeAlternativeStops,
                )
            }
        }
    }
    fun getTripsFromAddressToLocation(
        originName: String,
        destinationLat: Double,
        destinationLon: Double,
        time: LocalDateTime,
        isArrivalTime: Boolean = false,
        shortTermChanges: Boolean = true,
        mobilityRestriction: String = "None",
        maxChanges: String = "5",
        walkingSpeed: String = "normal",
        footpathToStop: Int = 10,
        mot: List<String> = listOf("Tram", "CityBus", "IntercityBus", "SuburbanRailway", "Train", "Cableway", "Ferry", "HailedSharedTaxi"),
        includeAlternativeStops: Boolean = true,
    ): Mono<String> {
        logger.debug("Getting trip from $originName to $destinationLat, $destinationLon")

        return locationService.getLocationAddress(destinationLat, destinationLon).flatMap { destinationAddress ->
            departureService.pointFinder(originName, false).flatMap { originPoints ->
                getTrip(
                    originPoints.points[0].split("|")[0],
                    locationService.toAddressString(destinationAddress),
                    time,
                    isArrivalTime,
                    shortTermChanges,
                    mobilityRestriction,
                    maxChanges,
                    walkingSpeed,
                    footpathToStop,
                    mot,
                    includeAlternativeStops,
                )
            }
        }
    }

    fun getTrip(
        origin: String,
        destination: String,
        time: LocalDateTime,
        isArrivalTime: Boolean = false,
        shortTermChanges: Boolean = true,
        mobilityRestriction: String = "None",
        maxChanges: String = "5",
        walkingSpeed: String = "normal",
        footpathToStop: Int = 10,
        mot: List<String> = listOf("Tram", "CityBus", "IntercityBus", "SuburbanRailway", "Train", "Cableway", "Ferry", "HailedSharedTaxi"),
        includeAlternativeStops: Boolean = true,
    ): Mono<String> {
        logger.debug("Getting trip from $origin to $destination")

        val url = UriComponentsBuilder.fromHttpUrl("http://webapi.vvo-online.de/tr/trips")
            .queryParam("format", "json")
            .queryParam("origin", origin)
            .queryParam("destination", destination)
            .queryParam("time", time)
            .queryParam("isarrivaltime", isArrivalTime)
            .queryParam("shorttermchanges", shortTermChanges)
            .queryParam("mobilityrestriction", mobilityRestriction)
            .queryParam("maxchanges", maxChanges)
            .queryParam("walkingspeed", walkingSpeed)
            .queryParam("footpathtostop", footpathToStop)
            .queryParam("mot", mot.joinToString(","))
            .queryParam("includealternativestops", includeAlternativeStops)
            .build()
            .toUriString()

        return webClient.post()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
    }


}