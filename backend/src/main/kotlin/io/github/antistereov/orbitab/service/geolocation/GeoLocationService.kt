package io.github.antistereov.orbitab.service.geolocation

import io.github.antistereov.orbitab.service.geolocation.model.GeoLocationResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class GeoLocationService(
    private val webClient: WebClient,
) {

    // TODO: Error Handling

    suspend fun getLocation(ipAddress: String): GeoLocationResponse {
        val uri = "https://freeipapi.com/api/json/$ipAddress"

        return webClient.get()
            .uri(uri)
            .retrieve()
            .awaitBody<GeoLocationResponse>()
    }
}