package io.github.antistereov.start.widgets.widget.transport.company.vvo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Departure(
    @JsonProperty("Id") val id: String,
    @JsonProperty("DlId") val dlId: String,
    @JsonProperty("LineName") val lineName: String,
    @JsonProperty("Direction") val direction: String,
    @JsonProperty("Platform") val platform: Platform?,
    @JsonProperty("Mot") val mot: String,
    @JsonProperty("RealTime") val realTime: String?,
    @JsonProperty("ScheduledTime") val scheduledTime: String,
    @JsonProperty("State") val state: String?,
    @JsonProperty("RouteChanges") val routeChanges: List<String>,
    @JsonProperty("Diva") val diva: Diva?,
    @JsonProperty("CancelReasons") val cancelReasons: List<String>,
    @JsonProperty("Occupancy") val occupancy: String
)
