package io.github.antistereov.start.widgets.widget.chat.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "chat")
data class ChatWidget(
    @Id var id: String? = null,
    var chatHistory: ChatHistory = ChatHistory()
)