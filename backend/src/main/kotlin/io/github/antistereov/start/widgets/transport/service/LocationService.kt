package io.github.antistereov.start.widgets.transport.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.antistereov.start.widgets.transport.model.LocationAddress
import io.github.antistereov.start.widgets.transport.model.NearbyStop
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class LocationService(
    private val webClient: WebClient,
) {

    fun getLocationAddress(latitude: Double, longitude: Double): Mono<LocationAddress> {
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
                val road = json.get("address").get("road").asText()
                val houseNumber = json.get("address").get("house_number").asText()
                val postcode = json.get("address").get("postcode").asText()
                val city = json.get("address").get("city").asText()
                val country = json.get("address").get("country").asText()
                LocationAddress(road, houseNumber, postcode, city, country)
            }
    }

    fun toAddressString(locationAddress: LocationAddress): String {
        return "${locationAddress.road} " +
                "${locationAddress.houseNumber}, " +
                "${locationAddress.postcode} ${locationAddress.city}, " +
                locationAddress.country
    }

    fun getNearbyPublicTransport(lat: Double, lon: Double, radius: String): Mono<List<NearbyStop>> {
        val query = """
            [out:json];
            (
              node["highway"="bus_stop"](around:${radius},${lat},${lon});
              node["railway"="tram_stop"](around:${radius},${lat},${lon});
              node["railway"="station"](around:${radius},${lat},${lon});
              node["railway"="subway_entrance"](around:${radius},${lat},${lon});
            );
            out body;
        """.trimIndent()

        val url = "https://overpass-api.de/api/interpreter"

        return WebClient.create(url)
            .post()
            .bodyValue(query)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMap { parsePublicTransportResponse(it, lat, lon) }
    }


    private fun parsePublicTransportResponse(
        response: String,
        lat: Double,
        lon: Double
    ): Mono<List<NearbyStop>> {
        return getLocationAddress(lat, lon).map { location ->
            val mapper = ObjectMapper()
            val json = mapper.readTree(response)
            val elements = json.get("elements")

            val stops = mutableListOf<NearbyStop>()
            for (element in elements) {
                val name = element.get("tags").get("name")?.asText()
                val stopLat = element.get("lat").asDouble()
                val stopLon = element.get("lon").asDouble()
                val distance = calculateDistance(lat, lon, stopLat, stopLon)
                val mot = listOfNotNull(
                    element.get("tags").get("highway")?.asText()?.replace("_stop", ""),
                    element.get("tags").get("railway")?.asText()?.replace("_stop", "")
                )

                if (name != null) {
                    stops.add(NearbyStop("$name, ${location.city}, ${location.country}", distance, mot))
                }
            }

            processStops(stops)
        }
    }

    fun processStops(stops: List<NearbyStop>): List<NearbyStop> {
        return stops.groupBy { it.name }
            .map { (name, group) ->
                val minDistance = group.minOf { it.distance }
                val mot = group.flatMap { it.mot }
                    .map { it.replace("_stop", "") }
                    .distinct()
                NearbyStop(name, minDistance, mot)
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun encode(value: String): Mono<String> {
        return Mono.fromCallable { URLEncoder.encode(value, "UTF-8") }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { Mono.just(it) }
    }
}