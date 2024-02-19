package io.github.antistereov.start.widgets.widget.chat.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Message(
    @JsonProperty("role")
    val role: String,
    @JsonProperty("content")
    val content: String
)