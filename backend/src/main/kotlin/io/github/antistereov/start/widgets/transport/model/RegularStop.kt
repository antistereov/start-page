package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RegularStop(
    @JsonProperty("ArrivalTime") val arrivalTime: String,
    @JsonProperty("DepartureTime") val departureTime: String,
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