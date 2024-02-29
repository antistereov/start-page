package io.github.antistereov.start.widgets.widget.weather.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.weather.model.WeatherLocation
import io.github.antistereov.start.widgets.widget.weather.model.WeatherWidget
import io.github.antistereov.start.widgets.widget.weather.service.WeatherService
import io.github.antistereov.start.widgets.widget.weather.service.WeatherWidgetService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/weather")
class WeatherController(
    private val weatherService: WeatherService,
    private val widgetService: WeatherWidgetService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    @GetMapping("/current")
    fun getCurrentWeather(
        authentication: Authentication,
        @RequestParam lat: Double,
        @RequestParam lon: Double,
    ): Mono<String> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.getCurrentWeather(userId, lat, lon)
        }
    }

    @GetMapping("/forecast")
    fun getWeatherForecast(
        authentication: Authentication,
        @RequestParam lat: Double,
        @RequestParam lon: Double,
    ): Mono<String> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.getWeatherForecast(userId, lat, lon)
        }
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

    @GetMapping
    fun getWeatherWidget(
        authentication: Authentication,
    ): Mono<WeatherWidget> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.getWeatherWidgetSettings(userId)
        }
    }

    @PutMapping("/primary-location")
    fun updatePrimaryLocation(
        authentication: Authentication,
        @RequestBody weatherLocation: WeatherLocation
    ): Mono<WeatherLocation> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.updatePrimaryLocation(userId, weatherLocation)
        }
    }

    @PostMapping("/location")
    fun saveWeatherLocation(
        authentication: Authentication,
        @RequestBody weatherLocation: WeatherLocation
    ): Mono<WeatherLocation> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.saveWeatherLocation(userId, weatherLocation)
        }
    }

    @DeleteMapping("/location")
    fun deleteWeatherLocation(
        authentication: Authentication,
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) lon: Double,
    ): Mono<String> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.deleteWeatherLocation(userId, lat, lon)
        }
    }

    @PutMapping("/units")
    fun updateUnits(
        authentication: Authentication,
        @RequestParam units: String
    ): Mono<String> {
        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.updateUnits(userId, units)
        }
    }
}