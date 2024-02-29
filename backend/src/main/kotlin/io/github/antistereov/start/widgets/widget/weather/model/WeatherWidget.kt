package io.github.antistereov.start.widgets.widget.weather.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "weather")
data class WeatherWidget(
    @Id var id: String? = null,
    var units: String = "metric",
    var locations: MutableList<WeatherLocation> = mutableListOf(),
    var primaryLocation: WeatherLocation? = null,
)