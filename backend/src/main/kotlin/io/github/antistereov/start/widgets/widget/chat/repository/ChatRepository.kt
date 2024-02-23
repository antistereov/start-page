package io.github.antistereov.start.widgets.widget.chat.repository

import io.github.antistereov.start.widgets.widget.chat.model.ChatWidget
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ChatRepository : ReactiveMongoRepository<ChatWidget, Long>