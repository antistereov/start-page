package io.github.antistereov.start.widgets.auth.openai.model

import io.github.antistereov.start.widgets.widget.chat.model.ChatHistory

data class OpenAIAuthDetails(
    var apiKey: String? = null,
    var chatHistory: ChatHistory = ChatHistory()
)