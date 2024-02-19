package io.github.antistereov.start.widgets.widget.chat.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatResponse(
    val id: String,
    @JsonProperty("object") val objectValue: String,
    val created: Long,
    val model: String,
    @JsonProperty("system_fingerprint") val systemFingerprint: String,
    val choices: List<Choice>,
    val usage: Usage,
)