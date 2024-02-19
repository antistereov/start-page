package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Trip(
    @JsonProperty("SessionId") val sessionId: String,
    @JsonProperty("Status") val status: Status,
    @JsonProperty("Routes") val routes: List<Route>,
)