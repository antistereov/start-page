package io.github.antistereov.start.widgets.widget.chat.service

import io.github.antistereov.start.global.model.exception.MessageLimitExceededException
import io.github.antistereov.start.global.model.exception.MissingCredentialsException
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.widgets.widget.chat.model.ChatHistory
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.service.UserService
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.widget.chat.model.ChatRequest
import io.github.antistereov.start.widgets.widget.chat.model.ChatResponse
import io.github.antistereov.start.widgets.widget.chat.model.Message
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val webClient: WebClient,
    private val properties: OpenAIProperties,
    private val userService: UserService,
    private val aesEncryption: AESEncryption,
) {

    //TODO: Add @ annotation like Copilot, e.g. @Summarize

    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    fun chat(userId: String, content: String): Mono<ChatResponse> {
        logger.debug("Chatting with user: $userId.")

        return fetchAndValidateUser(userId).flatMap { user ->
            createChatRequest(user, content).flatMap { chatRequest ->
                sendChatRequestAndHandleResponse(user, chatRequest).flatMap { response ->
                    updateChatHistory(user, response, chatRequest.messages.last())
                }
            }
        }
    }

    private fun fetchAndValidateUser(userId: String): Mono<User> {
        logger.debug("Fetching and validating user: $userId.")

        return userService.findById(userId).flatMap { user ->
            val chatHistory = decryptMessages(user.widgets.chat.chatHistory.history)

            if (chatHistory.size >= properties.messageLimit) {
                return@flatMap Mono.error(MessageLimitExceededException())
            }

            Mono.just(user)
        }
    }

    private fun createChatRequest(user: User, content: String): Mono<ChatRequest> {
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
                    addAll(decryptMessages(user.widgets.chat.chatHistory.history).values)
                    add(newMessage)
                },
            user = user.id,
        )

        return Mono.just(chatRequest)
    }

    private fun sendChatRequestAndHandleResponse(user: User, chatRequest: ChatRequest): Mono<ChatResponse> {
        logger.debug("Sending chat request and handling response.")

        val encryptedApiKey = user.auth.openAi.apiKey
            ?: return Mono.error(MissingCredentialsException(properties.serviceName, "API key", user.id))
        val uri = "${properties.apiBaseUrl}/chat/completions"
        val apiKey = aesEncryption.decrypt(encryptedApiKey)

        return webClient
            .post()
            .uri(uri)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(chatRequest)
            .retrieve()
            .bodyToMono(ChatResponse::class.java)
    }

    private fun updateChatHistory(user: User, response: ChatResponse, newMessage: Message): Mono<ChatResponse> {
        logger.debug("Updating chat history.")

        val chatHistory = decryptMessages(user.widgets.chat.chatHistory.history)
        val entryNumber = chatHistory.size
        chatHistory[entryNumber] = newMessage
        chatHistory[entryNumber + 1] = response.choices.first().message
        user.widgets.chat.chatHistory.history = encryptMessages(chatHistory)
        user.widgets.chat.chatHistory.totalTokens = response.usage.totalTokens

        return userService.save(user).thenReturn(response)
    }

    fun deleteHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        logger.debug("Deleting history entry.")

        return userService.findById(userId).flatMap { user ->
            val history = user.widgets.chat.chatHistory.history
            if (history.size <= entryNumber) {
                return@flatMap Mono.error(IndexOutOfBoundsException("No history entry at index $entryNumber"))
            }
            val entryToRemove = history.remove(entryNumber)

            val updatedHistory = mutableMapOf<Int, Message>()
            for ((key, value) in history) {
                if (key > entryNumber) {
                    updatedHistory[key - 1] = value
                } else {
                    updatedHistory[key] = value
                }
            }
            user.widgets.chat.chatHistory.history = updatedHistory
            userService.save(user).thenReturn(entryToRemove!!)
        }
    }

    fun clearChatHistory(userId: String): Mono<ChatHistory> {
        logger.debug("Clearing chat history.")

        return userService.findById(userId).flatMap { user ->
            user.widgets.chat.chatHistory.history.clear()
            user.widgets.chat.chatHistory.totalTokens = 0
            userService.save(user).thenReturn(user.widgets.chat.chatHistory)
        }
    }

    fun getHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        logger.debug("Getting history entry.")

        return userService.findById(userId).handle { user, sink ->
            val history = user.widgets.chat.chatHistory.history
            if (history.size <= entryNumber) {
                sink.error(IndexOutOfBoundsException("No history entry at index $entryNumber"))
                return@handle
            }
            val originalMessage = history.values.elementAt(entryNumber)
            val decryptedContent = aesEncryption.decrypt(originalMessage.content)
            val decryptedMessage = originalMessage.copy(content = decryptedContent)
            sink.next(decryptedMessage)
        }
    }

    fun getChatHistory(userId: String): Mono<ChatHistory> {
        logger.debug("Getting chat history.")

        return userService.findById(userId).map { user ->
            ChatHistory(
                decryptMessages(user.widgets.chat.chatHistory.history),
                user.widgets.chat.chatHistory.totalTokens
            )
        }
    }

    private fun encryptMessages(history: MutableMap<Int, Message>): MutableMap<Int, Message> {
        logger.debug("Encrypting messages.")

        return history.mapValues { (_, message) ->
            val encryptedContent = aesEncryption.encrypt(message.content)
            message.copy(content = encryptedContent)
        }.toMutableMap()
    }

    private fun decryptMessages(history: MutableMap<Int, Message>): MutableMap<Int, Message> {
        logger.debug("Decrypting messages.")

        return history.mapValues { (_, message) ->
            val decryptedContent = aesEncryption.decrypt(message.content)
            message.copy(content = decryptedContent)
        }.toMutableMap()
    }
}