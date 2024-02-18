package io.github.antistereov.start.widgets.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Usage(
    @JsonProperty("prompt_tokens")
    val promptTokens: Int,
    @JsonProperty("completion_tokens")
    val completionTokens: Int,
    @JsonProperty("total_tokens")
    val totalTokens: Int
)