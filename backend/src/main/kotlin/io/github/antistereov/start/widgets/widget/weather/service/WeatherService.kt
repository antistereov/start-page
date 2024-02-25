package io.github.antistereov.start.widgets.widget.weather.service

import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.widgets.widget.weather.config.OpenWeatherMapProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class WeatherService(
    private val baseService: BaseService,
    private val properties: OpenWeatherMapProperties
) {

    private val logger = LoggerFactory.getLogger(WeatherService::class.java)


    fun getCurrentWeather(lat: Double, lon: Double, unit: String): Mono<String> {
        logger.debug("Getting current weather for location: lat=$lat, lon=$lon")

        val url = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/weather")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .queryParam("units", unit)
            .queryParam("appid", properties.apiKey)
            .toUriString()

        return baseService.getMono(url)
    }

    fun getWeatherForecast(lat: Double, lon: Double, unit: String): Mono<String> {
        logger.debug("Getting weather forecast for location: lat=$lat, lon=$lon")

        val url = UriComponentsBuilder.fromHttpUrl("${properties.apiBaseUrl}/forecast")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .queryParam("units", unit)
            .queryParam("appid", properties.apiKey)
            .toUriString()

        return baseService.getMono(url)
    }

    fun getCoordinatesByLocationName(cityName: String, stateCode: String?, countryCode: String?, limit: Int = 5): Mono<String> {
        logger.debug("Getting coordinates for location: $cityName${if (stateCode != null) ", $stateCode" else ""}${if (countryCode != null) ", $countryCode" else ""}")

        val url = UriComponentsBuilder.fromHttpUrl("http://api.openweathermap.org/geo/1.0/direct")
            .queryParam("q", "$cityName${if (stateCode != null) ",$stateCode" else ""}${if (countryCode != null) ",$countryCode" else ""}")
            .queryParam("limit", limit)
            .queryParam("appid", properties.apiKey)
            .toUriString()

        return baseService.getMono(url)
    }
}