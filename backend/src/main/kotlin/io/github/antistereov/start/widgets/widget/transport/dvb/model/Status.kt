package io.github.antistereov.start.widgets.widget.transport.dvb.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Status(
    @JsonProperty("Code")
    val code: String
)