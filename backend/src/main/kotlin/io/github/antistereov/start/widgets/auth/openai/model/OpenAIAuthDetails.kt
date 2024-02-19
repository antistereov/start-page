package io.github.antistereov.start.widgets.auth.openai.model

import io.github.antistereov.start.user.model.ChatDetails

data class OpenAIAuthDetails(
    var apiKey: String? = null,
    var chatDetails: ChatDetails = ChatDetails()
)