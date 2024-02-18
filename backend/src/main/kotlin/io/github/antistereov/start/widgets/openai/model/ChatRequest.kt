package io.github.antistereov.start.widgets.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatRequest(
    @JsonProperty("messages")
    val messages: MutableList<Message>,

    @JsonProperty("model")
    val model: String = "gpt-3.5-turbo-0125",

    @JsonProperty("frequency_penalty")
    val frequencyPenalty: Double? = null,

    @JsonProperty("logit_bias")
    val logitBias: Map<String, Double>? = null,

    @JsonProperty("logprobs")
    val logprobs: Boolean? = null,

    @JsonProperty("top_logprobs")
    val topLogprobs: Int? = null,

    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,

    @JsonProperty("n")
    val n: Int? = null,

    @JsonProperty("presence_penalty")
    val presencePenalty: Double? = null,

    @JsonProperty("response_format")
    val responseFormat: Map<String, String>? = null,

    @JsonProperty("seed")
    val seed: Int? = null,

    @JsonProperty("stop")
    val stop: List<String>? = null,

    @JsonProperty("stream")
    val stream: Boolean? = null,

    @JsonProperty("temperature")
    val temperature: Double? = null,

    @JsonProperty("top_p")
    val topP: Double? = null,

    @JsonProperty("tools")
    val tools: List<String>? = null,

    @JsonProperty("tool_choice")
    val toolChoice: Any? = null,

    @JsonProperty("user")
    val user: String? = null,
)