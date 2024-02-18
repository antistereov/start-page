package io.github.antistereov.start.user.model

data class OpenAIAuthDetails(
    var apiKey: String? = null,
    var chatDetails: ChatDetails = ChatDetails()
)