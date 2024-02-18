package io.github.antistereov.start.widgets.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Message(
    @JsonProperty("role")
    val role: String,
    @JsonProperty("content")
    val content: String
)