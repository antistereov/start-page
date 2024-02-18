package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.antistereov.start.util.CustomLocalDateTimeDeserializer
import java.time.LocalDateTime

data class PointFinder(
    @JsonProperty("PointStatus")
    val pointStatus: String,
    @JsonProperty("Status")
    val status: Status,
    @JsonProperty("Points")
    val points: List<String>,
    @JsonProperty("ExpirationTime")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer::class)
    val expirationTime: LocalDateTime,
)
