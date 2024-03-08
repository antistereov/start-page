package io.github.antistereov.start.widgets.widget.transport.company.vvo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Diva(
    @JsonProperty("Number") val number: String?,
    @JsonProperty("Network") val network: String?,
)