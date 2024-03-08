package io.github.antistereov.start.widgets.widget.transport.company.vvo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Status(
    @JsonProperty("Code")
    val code: String
)