package io.github.antistereov.start.widgets.widget.chat.service

import io.github.antistereov.start.global.exception.MissingCredentialsException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.widget.chat.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val webClient: WebClient,
    private val properties: OpenAIProperties,
    private val userService: UserService,
    private val widgetService: ChatWidgetService,
    private val aesEncryption: AESEncryption,
) {

    //TODO: Add @ annotation like Copilot, e.g. @Summarize

    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    fun chat(userId: String, content: String): Mono<Message> {
        logger.debug("Chatting with user: $userId.")

        return widgetService.fetchAndValidateChat(userId).flatMap { widget ->
            createChatRequest(widget, content).flatMap { chatRequest ->
                sendChatRequestAndHandleResponse(userId, chatRequest).flatMap { response ->
                    updateChatHistory(userId, widget, response, chatRequest.messages.last())
                }
            }
        }
    }

    private fun createChatRequest(widget: ChatWidget, content: String): Mono<ChatRequest> {
        logger.debug("Creating chat request.")

        val newMessage = Message("user", content)
        val systemMessage = Message(
            "system",
            "You are ChatGPT, a large language model trained by OpenAI.\n" +
                    "Carefully heed the user's instructions. \n" +
                    "Please respond using Markdown."
        )
        val chatRequest = ChatRequest(
            mutableListOf(systemMessage)
                .apply {
                    addAll(decryptMessages(widget.chatHistory.history).values)
                    add(newMessage)
                },
        )

        return Mono.just(chatRequest)
    }

    private fun sendChatRequestAndHandleResponse(userId: String, chatRequest: ChatRequest): Mono<ChatResponse> {
        logger.debug("Sending chat request and handling response.")

        return userService.findById(userId).flatMap { user ->
            val encryptedApiKey = user.auth.openAi.apiKey
                ?: return@flatMap Mono.error<ChatResponse>(
                    io.github.antistereov.start.global.exception.MissingCredentialsException(
                        properties.serviceName,
                        "API key",
                        user.id
                    )
                )
            val uri = "${properties.apiBaseUrl}/chat/completions"
            val apiKey = aesEncryption.decrypt(encryptedApiKey)

            webClient
                .post()
                .uri(uri)
                .header("Authorization", "Bearer $apiKey")
                .bodyValue(chatRequest)
                .retrieve()
                .bodyToMono(ChatResponse::class.java)
        }
    }

    private fun updateChatHistory(
        userId: String,
        widget: ChatWidget,
        response: ChatResponse,
        newMessage: Message
    ): Mono<Message> {
        logger.debug("Updating chat history.")

        val chatHistory = widget.chatHistory.history
        val entryNumber = chatHistory.size
        chatHistory[entryNumber] = encryptMessage(newMessage)
        chatHistory[entryNumber + 1] = encryptMessage(response.choices.first().message)
        widget.chatHistory.totalTokens = response.usage.totalTokens



        return widgetService.saveChatWidgetForUser(userId, widget)
            .thenReturn(response.choices.first().message)
    }

    fun deleteHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        logger.debug("Deleting history entry.")

        return widgetService.findChatWidgetByUserId(userId).flatMap { widget ->
            val history = widget.chatHistory.history
            val entryToRemove = history.remove(entryNumber)
                ?: return@flatMap Mono.error(IndexOutOfBoundsException("No history entry at index $entryNumber"))

            val updatedHistory = mutableMapOf<Int, Message>()
            for ((key, value) in history) {
                if (key > entryNumber) {
                    updatedHistory[key - 1] = value
                } else {
                    updatedHistory[key] = value
                }
            }
            widgetService.saveChatWidgetForUser(userId, widget)
                .thenReturn(decryptMessage(entryToRemove))
        }
    }

    fun clearChatHistory(userId: String): Mono<String> {
        logger.debug("Clearing chat history.")

        return widgetService.deleteChatWidget(userId)
    }

    fun getHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        logger.debug("Getting history entry.")

        return widgetService.findChatWidgetByUserId(userId).flatMap { widget ->
            val history = widget.chatHistory.history
            val encryptedMessage = history[entryNumber]
                ?: return@flatMap Mono.error(IndexOutOfBoundsException("No history entry at index $entryNumber"))
            Mono.just(decryptMessage(encryptedMessage))
        }
    }

    fun getChatHistory(userId: String): Mono<ChatHistory> {
        logger.debug("Getting chat history.")

        return widgetService.findChatWidgetByUserId(userId).map { widget ->
            ChatHistory(
                widget.chatHistory.history.mapValues { (_, message) -> decryptMessage(message) }.toMutableMap(),
                widget.chatHistory.totalTokens
            )
        }
    }

    private fun encryptMessage(message: Message): Message = message.copy(content = aesEncryption.encrypt(message.content))

    private fun decryptMessage(message: Message): Message = message.copy(content = aesEncryption.decrypt(message.content))

    private fun decryptMessages(history: MutableMap<Int, Message>): MutableMap<Int, Message> {
        logger.debug("Decrypting messages.")

        return history.mapValues { (_, message) ->
            val decryptedContent = aesEncryption.decrypt(message.content)
            message.copy(content = decryptedContent)
        }.toMutableMap()
    }
}