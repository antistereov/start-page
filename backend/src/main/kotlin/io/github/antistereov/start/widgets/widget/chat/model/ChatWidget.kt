package io.github.antistereov.start.widgets.widget.chat.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "chat")
data class ChatWidget(
    val id: Long,
    var chatHistory: ChatHistory = ChatHistory()
)