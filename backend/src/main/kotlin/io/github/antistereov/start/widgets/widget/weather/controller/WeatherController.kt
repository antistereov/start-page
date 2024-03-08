package io.github.antistereov.start.widgets.widget.weather.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.weather.model.WeatherLocation
import io.github.antistereov.start.widgets.widget.weather.model.WeatherWidgetData
import io.github.antistereov.start.widgets.widget.weather.service.OpenWeatherMapService
import io.github.antistereov.start.widgets.widget.weather.service.WeatherWidgetService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/weather")
class WeatherController(
    private val openWeatherMapService: OpenWeatherMapService,
    private val widgetService: WeatherWidgetService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    private val logger = LoggerFactory.getLogger(WeatherController::class.java)

    @GetMapping("/current")
    fun getCurrentWeather(
        authentication: Authentication,
        @RequestParam lat: Double,
        @RequestParam lon: Double,
    ): Mono<String> {
        logger.info("Getting current weather for location: lat=$lat, lon=$lon")

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
        logger.info("Getting weather forecast for location: lat=$lat, lon=$lon")

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
        logger.info("Getting coordinates for location: $cityName" +
                if (stateCode != null) ", $stateCode" else "" +
                if (countryCode != null) ", $countryCode" else ""
        )

        return openWeatherMapService.getCoordinatesByLocationName(cityName, stateCode, countryCode, limit)
    }

    @GetMapping
    fun getWeatherWidgetData(
        authentication: Authentication,
    ): Mono<WeatherWidgetData> {
        logger.info("Getting weather widget data.")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.getWeatherWidgetData(userId)
        }
    }

    @PutMapping("/primary-location")
    fun updatePrimaryLocation(
        authentication: Authentication,
        @RequestBody weatherLocation: WeatherLocation
    ): Mono<WeatherLocation> {
        logger.info("Update primary location: ${weatherLocation.cityName}")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.updatePrimaryLocation(userId, weatherLocation)
        }
    }

    @PostMapping("/location")
    fun saveWeatherLocation(
        authentication: Authentication,
        @RequestBody weatherLocation: WeatherLocation
    ): Mono<WeatherLocation> {
        logger.info("Save location: ${weatherLocation.cityName}")

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
        logger.info("Delete location: lat=$lat, lon=$lon")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.deleteWeatherLocation(userId, lat, lon)
        }
    }

    @PutMapping("/units")
    fun updateUnits(
        authentication: Authentication,
        @RequestParam units: String
    ): Mono<String> {
        logger.info("Update units: $units")

        return principalExtractor.getUserId(authentication).flatMap { userId ->
            widgetService.updateUnits(userId, units)
        }
    }
}