package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.antistereov.start.util.CustomLocalDateTimeDeserializer
import java.time.LocalDateTime

data class RegularStop(
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("ArrivalTime") val arrivalTime: LocalDateTime,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("DepartureTime") val departureTime: LocalDateTime,
    @JsonProperty("Place") val place: String,
    @JsonProperty("Name") val name: String,
    @JsonProperty("Type") val type: String,
    @JsonProperty("DataId") val dataId: String,
    @JsonProperty("DhId") val dhId: String,
    @JsonProperty("Latitude") val latitude: Int,
    @JsonProperty("Longitude") val longitude: Int,
    @JsonProperty("CancelReasons") val cancelReasons: List<Any>,
    @JsonProperty("ParkAndRail") val parkAndRail: List<Any>,
    @JsonProperty("Occupancy") val occupancy: String,
    @JsonProperty("Platform") val platform: Platform?
)