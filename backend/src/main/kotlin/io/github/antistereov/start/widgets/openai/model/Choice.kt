package io.github.antistereov.start.widgets.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Choice(
    val index: Int,
    val message: Message,
    @JsonProperty("logprobs") val logProbs: Any?,
    @JsonProperty("finish_reason") val finishReason: String,
)