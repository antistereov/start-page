package io.github.antistereov.start.widgets.widget.weather.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "open-weather-map")
data class OpenWeatherMapProperties(
    val serviceName: String,
    val apiKey: String,
    val apiBaseUrl: String,
)