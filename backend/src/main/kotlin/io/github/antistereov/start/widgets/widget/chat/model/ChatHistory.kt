package io.github.antistereov.start.widgets.widget.chat.model

data class ChatHistory(
    var history: MutableMap<Int, Message> = mutableMapOf(),
    var totalTokens: Int = 0,
)
