package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.antistereov.start.util.CustomLocalDateTimeDeserializer
import java.time.LocalDateTime

data class Departure(
    @JsonProperty("Id") val id: String,
    @JsonProperty("DlId") val dlId: String,
    @JsonProperty("LineName") val lineName: String,
    @JsonProperty("Direction") val direction: String,
    @JsonProperty("Platform") val platform: Platform?,
    @JsonProperty("Mot") val mot: String,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("RealTime") val realTime: LocalDateTime?,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("ScheduledTime") val scheduledTime: LocalDateTime,
    @JsonProperty("State") val state: String?,
    @JsonProperty("RouteChanges") val routeChanges: List<String>,
    @JsonProperty("Diva") val diva: Diva?,
    @JsonProperty("CancelReasons") val cancelReasons: List<String>,
    @JsonProperty("Occupancy") val occupancy: String
)
