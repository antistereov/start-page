package io.github.antistereov.start.widgets.widget.weather.model

data class WeatherLocation(
    val cityName: String,
    val stateCode: String,
    val countryCode: String,
    val lat: Double,
    val lon: Double
)