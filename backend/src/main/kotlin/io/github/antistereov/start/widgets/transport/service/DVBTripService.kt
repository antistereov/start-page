package io.github.antistereov.start.widgets.transport.service

import io.github.antistereov.start.global.service.BaseService
import io.netty.handler.codec.DecoderException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class DVBTripService(
    private val webClient: WebClient,
    private val departureService: DVBDepartureService,
    private val locationService: LocationService,
    private val baseService: BaseService,
) {

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
        return departureService.pointFinder(originName, false).flatMap { originPoints ->
            departureService.pointFinder(destinationName, false).flatMap { destinationPoints ->
                getTrip(
                    originPoints.points[0].split("|")[0],
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
            .let { baseService.handleError(url, it) }
            .bodyToMono(String::class.java)
            .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(url))
            .onErrorResume(DecoderException::class.java, baseService.handleParsingError(url))
    }


}