package io.github.antistereov.start.widgets.transport.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Status(
    @JsonProperty("Code")
    val code: String
)