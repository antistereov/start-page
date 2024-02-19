package io.github.antistereov.start.widgets.widget.transport.dvb.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Platform(
    @JsonProperty("Name") val name: String,
    @JsonProperty("Type") val type: String
)