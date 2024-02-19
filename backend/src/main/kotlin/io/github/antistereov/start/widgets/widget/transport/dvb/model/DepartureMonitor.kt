package io.github.antistereov.start.widgets.widget.transport.dvb.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DepartureMonitor(
    @JsonProperty("Name") val name: String,
    @JsonProperty("Status") val status: Status,
    @JsonProperty("Place") val place: String,
    @JsonProperty("ExpirationTime") val expirationTime: String,
    @JsonProperty("Departures") val departures: List<Departure>,
)