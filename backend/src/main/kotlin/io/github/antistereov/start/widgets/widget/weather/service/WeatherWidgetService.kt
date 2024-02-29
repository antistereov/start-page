package io.github.antistereov.start.widgets.widget.weather.service

import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.widget.weather.model.WeatherLocation
import io.github.antistereov.start.widgets.widget.weather.model.WeatherWidget
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class WeatherWidgetService(
    private val userService: UserService,
    private val weatherService: WeatherService,
) {

    private val logger = LoggerFactory.getLogger(WeatherWidgetService::class.java)

    fun getCurrentWeather(userId: String, lat: Double?, lon: Double?): Mono<String> {
        logger.debug("Get current weather for user: $userId")

        return userService.findById(userId).flatMap { user ->
            if (lat == null || lon == null) {
                val primaryLocation = user.widgets.weather.primaryLocation
                    ?: return@flatMap Mono.error(IllegalArgumentException("Primary location not set."))

                val primaryLat = primaryLocation.lat
                val primaryLon = primaryLocation.lon

                weatherService.getCurrentWeather(primaryLat, primaryLon, user.widgets.weather.units)
            } else {
                weatherService.getCurrentWeather(lat, lon, user.widgets.weather.units)
            }
        }
    }

    fun getWeatherForecast(userId: String, lat: Double?, lon: Double?): Mono<String> {
        logger.debug("Get weather forecast for user: $userId")

        return userService.findById(userId).flatMap { user ->
            if (lat == null || lon == null) {
                val primaryLocation = user.widgets.weather.primaryLocation
                    ?: return@flatMap Mono.error(IllegalArgumentException("Primary location not set."))

                val primaryLat = primaryLocation.lat
                val primaryLon = primaryLocation.lon

                weatherService.getWeatherForecast(primaryLat, primaryLon, user.widgets.weather.units)
            } else {
                weatherService.getWeatherForecast(lat, lon, user.widgets.weather.units)
            }
        }
    }

    fun updatePrimaryLocation(userId: String, weatherLocation: WeatherLocation): Mono<WeatherLocation> {
        logger.debug("Update primary location: ${weatherLocation.cityName} for user: $userId")

        return userService.findById(userId).flatMap { user ->
            if (!user.widgets.weather.locations.contains(weatherLocation)) {
                saveWeatherLocation(userId, weatherLocation).flatMap { location ->
                    user.widgets.weather.primaryLocation = location
                    userService.save(user).thenReturn(weatherLocation)
                }
            } else {
                user.widgets.weather.primaryLocation = weatherLocation
                userService.save(user).thenReturn(weatherLocation)
            }
        }
    }

    fun saveWeatherLocation(userId: String, weatherLocation: WeatherLocation): Mono<WeatherLocation> {
        logger.debug("Save location: ${weatherLocation.cityName} for user: $userId")

        return userService.findById(userId).flatMap { user ->
            val locations = user.widgets.weather.locations

            if (locations.size >= 4) {
                return@flatMap Mono.error(IllegalArgumentException("Maximum of 4 locations allowed."))
            }

            if (locations.any { it.lat == weatherLocation.lat && it.lon == weatherLocation.lon }) {
                return@flatMap Mono.error(
                    IllegalArgumentException("Location: ${weatherLocation.cityName} already exists.")
                )
            }

            user.widgets.weather.locations.add(weatherLocation)

            if (locations.isEmpty()) {
                user.widgets.weather.primaryLocation = weatherLocation
            }
            userService.save(user).thenReturn(weatherLocation)
        }
    }

    fun getWeatherWidgetSettings(userId: String): Mono<WeatherWidget> {
        logger.debug("Get weather widget settings for user: $userId")

        return userService.findById(userId).map { user ->
            user.widgets.weather
        }
    }

    fun deleteWeatherLocation(userId: String, lat: Double, lon: Double): Mono<String> {
        logger.debug("Delete location lat: $lat, lon: $lon for user: $userId")

        return userService.findById(userId).flatMap { user ->
            val weatherLocation = user.widgets.weather.locations.find { it.lat == lat && it.lon == lon }
                ?: return@flatMap Mono.error(IllegalArgumentException("Location not found."))

            if (user.widgets.weather.primaryLocation == weatherLocation) {
                user.widgets.weather.primaryLocation = user.widgets.weather.locations.firstOrNull()
                    ?: return@flatMap Mono.error(IllegalArgumentException("Primary location cannot be deleted."))
            }

            user.widgets.weather.locations.remove(weatherLocation)
            userService.save(user).thenReturn("Location: ${weatherLocation.cityName} deleted for user: $userId.")
        }
    }

    fun updateUnits(userId: String, units: String): Mono<String> {
        logger.debug("Update units: $units for user: $userId")

        if (units != "metric" && units != "imperial") {
            return Mono.error(IllegalArgumentException("Invalid units: $units. Must be 'metric' or 'imperial'."))
        }

        return userService.findById(userId).flatMap { user ->
            user.widgets.weather.units = units
            userService.save(user).thenReturn("Units updated to: $units for user: $userId.")
        }
    }
}