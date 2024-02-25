package io.github.antistereov.start.widgets.widget.weather.controller

import io.github.antistereov.start.widgets.widget.weather.service.WeatherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/weather")
class WeatherController(private val weatherService: WeatherService) {

    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "metric") unit: String
    ): Mono<String> {
        return weatherService.getCurrentWeather(lat, lon, unit)
    }

    @GetMapping("/forecast")
    fun getWeatherForecast(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "metric") unit: String
    ): Mono<String> {
        return weatherService.getWeatherForecast(lat, lon, unit)
    }

    @GetMapping("/location")
    fun getCoordinatesByLocationName(
        @RequestParam cityName: String,
        @RequestParam(required = false) stateCode: String?,
        @RequestParam(required = false) countryCode: String?,
        @RequestParam(defaultValue = "5") limit: Int
    ): Mono<String> {
        return weatherService.getCoordinatesByLocationName(cityName, stateCode, countryCode, limit)
    }
}