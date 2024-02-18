package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.antistereov.start.util.CustomLocalDateTimeDeserializer
import java.time.LocalDateTime

data class DepartureMonitor(
    @JsonProperty("Name") val name: String,
    @JsonProperty("Status") val status: Status,
    @JsonProperty("Place") val place: String,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("ExpirationTime") val expirationTime: LocalDateTime,
    @JsonProperty("Departures") val departures: List<Departure>,
)