package io.github.antistereov.start.widgets.widget.chat.service

import io.github.antistereov.start.global.exception.MessageLimitExceededException
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.widget.chat.model.ChatWidget
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

    fun saveChatWidgetForUser(userId: String, widget: ChatWidget): Mono<ChatWidget> {
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

    fun findChatWidgetById(widgetId: String?): Mono<ChatWidget> {
        logger.debug("Finding ChatWidget by ID: $widgetId.")

        if (widgetId == null) {
            return Mono.error(IllegalArgumentException("No ChatWidget ID provided."))
        }
        return chatRepository.findById(widgetId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("ChatWidget not found with ID: $widgetId")))
    }

    fun findChatWidgetByUserId(userId: String): Mono<ChatWidget> {
        logger.debug("Finding ChatWidget by user ID: $userId.")

        return userService.findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId
                ?: return@flatMap Mono.error(IllegalArgumentException("No ChatWidget ID found for user: $userId"))
            findChatWidgetById(chatId)
        }
    }

    fun fetchAndValidateChat(userId: String): Mono<ChatWidget> {
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

        return userService.findById(userId).flatMap { user ->
            if (user.widgets.chatId == null) {
                return@flatMap Mono.just("Chat history cleared for user: $userId.")
            }
            findChatWidgetById(user.widgets.chatId).flatMap { widget ->
                chatRepository.delete(widget)
                    .doOnError { error ->
                        logger.error("Error deleting ChatWidget for user: $userId.", error)
                    }
                    .then(userService.save(user.apply { this.widgets.chatId = null }))
                    .map {"Chat history cleared for user: $userId." }
            }
        }
    }

    private fun findOrCreateChatWidgetByUserId(userId: String): Mono<ChatWidget> {
        return userService.findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId

            if (chatId == null) {
                val newWidget = ChatWidget()
                saveChatWidget(newWidget).flatMap { widget ->
                    user.widgets.chatId = widget.id
                    userService.save(user).thenReturn(widget)
                }
            } else {
                findChatWidgetById(chatId)
            }
        }
    }

    private fun saveChatWidget(chatWidget: ChatWidget): Mono<ChatWidget> {
        logger.debug("Saving ChatWidget.")

        return chatRepository.save(chatWidget)
            .onErrorMap { error ->
                logger.error("Error saving ChatWidget for user.", error)
                error
            }
    }
}