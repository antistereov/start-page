package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.antistereov.start.util.CustomLocalDateTimeDeserializer
import java.time.LocalDateTime

data class PartialRoute(
    @JsonProperty("Duration") val duration: Int,
    @JsonProperty("Mot") val mot: Mot,
    @JsonProperty("MapDataIndex") val mapDataIndex: Int,
    @JsonProperty("Shift") val shift: String,
    @JsonProperty("RegularStops") val regularStops: List<RegularStop>,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("NextDepartureTimes") val nextDepartureTimes: List<LocalDateTime>,
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    @JsonProperty("PreviousDepartureTimes") val previousDepartureTimes: List<LocalDateTime>
)