package io.github.antistereov.start.widgets.widget.chat.service

import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.widget.chat.model.ChatWidgetData
import io.github.antistereov.start.widgets.widget.chat.repository.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ChatWidgetService(
    private val chatRepository: ChatRepository,
    private val userService: UserService,
    private val properties: OpenAIProperties,
) {

    private val logger = LoggerFactory.getLogger(ChatWidgetService::class.java)

    fun saveChatWidgetForUser(userId: String, widget: ChatWidgetData): Mono<ChatWidgetData> {
        logger.debug("Saving ChatWidget for user: $userId.")


        return userService.findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId

            if (chatId == null) {
                saveChatWidget(widget).flatMap { widget ->
                    user.widgets.chatId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                saveChatWidget(widget)
            }
        }
    }

    fun findChatWidgetById(widgetId: String?): Mono<ChatWidgetData> {
        logger.debug("Finding ChatWidget by ID: $widgetId.")

        if (widgetId == null) {
            return Mono.error(IllegalArgumentException("No ChatWidget ID provided."))
        }
        return chatRepository.findById(widgetId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("ChatWidget not found with ID: $widgetId")))
    }

    fun findChatWidgetByUserId(userId: String): Mono<ChatWidgetData> {
        logger.debug("Finding ChatWidget by user ID: $userId.")

        return userService.findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId
                ?: return@flatMap Mono.error(IllegalArgumentException("No ChatWidget ID found for user: $userId"))
            findChatWidgetById(chatId)
        }
    }

    fun fetchAndValidateChat(userId: String): Mono<ChatWidgetData> {
        logger.debug("Fetching and validating user: $userId.")

        return findOrCreateChatWidgetByUserId(userId).flatMap { chatWidget ->
            val chatHistory = chatWidget.chatHistory.history

            if (chatHistory.size >= properties.messageLimit) {
                Mono.error(io.github.antistereov.start.global.exception.MessageLimitExceededException())
            } else {
                Mono.just(chatWidget)
            }
        }
    }

    fun deleteChatWidget(userId: String): Mono<String> {
        logger.debug("Deleting ChatWidget for user: $userId.")

        return userService.deleteChatWidget(userId)
    }

    private fun findOrCreateChatWidgetByUserId(userId: String): Mono<ChatWidgetData> {
        return userService.findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId

            if (chatId == null) {
                val newWidget = ChatWidgetData()
                saveChatWidget(newWidget).flatMap { widget ->
                    user.widgets.chatId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                findChatWidgetById(chatId)
            }
        }
    }

    private fun saveChatWidget(chatWidgetData: ChatWidgetData): Mono<ChatWidgetData> {
        logger.debug("Saving ChatWidget.")

        return chatRepository.save(chatWidgetData)
            .onErrorMap { error ->
                logger.error("Error saving ChatWidget for user.", error)
                error
            }
    }
}