package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.openai.model.Message

data class OpenAIDetails(
    var history: MutableMap<Int, Message> = mutableMapOf(),
    var totalTokens: Int = 0,
)
