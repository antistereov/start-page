package io.github.antistereov.start.widgets.widget.transport.dvb.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PointFinder(
    @JsonProperty("PointStatus")
    val pointStatus: String,
    @JsonProperty("Status")
    val status: Status,
    @JsonProperty("Points")
    val points: List<String>,
    @JsonProperty("ExpirationTime")
    val expirationTime: String,
)
