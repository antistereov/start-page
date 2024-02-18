package io.github.antistereov.start.widgets.transport.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.antistereov.start.widgets.transport.model.LocationAddress
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder
import javax.xml.stream.Location

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

    fun getNearbyPublicTransport(latitude: Double, longitude: Double, radius: String): Mono<List<String>> {
        val query = """
            [out:json];
            (
              node["highway"="bus_stop"](around:${radius},${latitude},${longitude});
              node["railway"="tram_stop"](around:${radius},${latitude},${longitude});
            );
            out body;
        """.trimIndent()

        val url = "https://overpass-api.de/api/interpreter"

        return getLocationAddress(latitude, longitude)
            .flatMap { location ->
                WebClient.create(url)
                    .post()
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .flatMap { parsePublicTransportResponse(it, location) }
            }
    }

    private fun parsePublicTransportResponse(response: String, location: LocationAddress): Mono<List<String>> {
        return Mono.fromCallable {
            val mapper = ObjectMapper()
            val json = mapper.readTree(response)
            val elements = json.get("elements")

            val names = mutableListOf<String>()
            for (element in elements) {
                val name = element.get("tags").get("name")?.asText()
                if (name != null) {
                    names.add("$name, ${location.city}, ${location.country}")
                }
            }

            names.distinct()
        }
    }

    private fun encode(value: String): Mono<String> {
        return Mono.fromCallable { URLEncoder.encode(value, "UTF-8") }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { Mono.just(it) }
    }
}