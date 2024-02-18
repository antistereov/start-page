package io.github.antistereov.start.widgets.openai.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.MessageLimitExceededException
import io.github.antistereov.start.global.model.exception.MissingCredentialsException
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.security.AESEncryption
import io.github.antistereov.start.user.model.ChatDetails
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.openai.model.ChatRequest
import io.github.antistereov.start.widgets.openai.model.ChatResponse
import io.github.antistereov.start.widgets.openai.model.Message
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val webClient: WebClient,
    private val baseService: BaseService,
    private val properties: OpenAIProperties,
    private val userRepository: UserRepository,
    private val aesEncryption: AESEncryption,
) {

    //TODO: Add @ annotation like Copilot, e.g. @Summarize

    fun chat(userId: String, content: String): Mono<ChatResponse> {
        return fetchAndValidateUser(userId).flatMap { user ->
            createChatRequest(user, content).flatMap { chatRequest ->
                sendChatRequestAndHandleResponse(user, chatRequest).flatMap { response ->
                    updateChatHistory(user, response, chatRequest.messages.last())
                }
            }
        }
    }

    private fun fetchAndValidateUser(userId: String): Mono<User> {
        return userRepository.findById(userId).flatMap { user ->
            val chatHistory = decryptMessages(user.openAi.chatDetails.history)

            if (chatHistory.size >= properties.messageLimit) {
                return@flatMap Mono.error(MessageLimitExceededException())
            }

            Mono.just(user)
        }
    }

    private fun createChatRequest(user: User, content: String): Mono<ChatRequest> {
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
                    addAll(decryptMessages(user.openAi.chatDetails.history).values)
                    add(newMessage)
                },
            user = user.id,
        )

        return Mono.just(chatRequest)
    }

    private fun sendChatRequestAndHandleResponse(user: User, chatRequest: ChatRequest): Mono<ChatResponse> {
        val encryptedApiKey = user.openAi.apiKey
            ?: return Mono.error(MissingCredentialsException(properties.serviceName, "API key", user.id))
        val uri = "${properties.apiBaseUrl}/chat/completions"
        val apiKey = aesEncryption.decrypt(encryptedApiKey)

        return webClient
            .post()
            .uri(uri)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(chatRequest)
            .retrieve()
            .let { baseService.handleError(uri, it) }
            .bodyToMono(ChatResponse::class.java)
    }

    private fun updateChatHistory(user: User, response: ChatResponse, newMessage: Message): Mono<ChatResponse> {
        val chatHistory = decryptMessages(user.openAi.chatDetails.history)
        val entryNumber = chatHistory.size
        chatHistory[entryNumber] = newMessage
        chatHistory[entryNumber + 1] = response.choices.first().message
        user.openAi.chatDetails.history = encryptMessages(chatHistory)
        user.openAi.chatDetails.totalTokens = response.usage.totalTokens

        return userRepository.save(user)
            .onErrorMap { throwable ->
                CannotSaveUserException(throwable)
            }
            .thenReturn(response)
    }

    fun deleteHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        return userRepository.findById(userId).flatMap { user ->
            val history = user.openAi.chatDetails.history
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
            user.openAi.chatDetails.history = updatedHistory
            userRepository.save(user)
                .onErrorMap { throwable ->
                    CannotSaveUserException(throwable)
                }
                .thenReturn(entryToRemove!!)
        }
    }

    fun clearChatHistory(userId: String): Mono<ChatDetails> {
        return userRepository.findById(userId).flatMap { user ->
            user.openAi.chatDetails.history.clear()
            user.openAi.chatDetails.totalTokens = 0
            userRepository.save(user)
                .onErrorMap { throwable ->
                    CannotSaveUserException(throwable)
                }
                .thenReturn(user.openAi.chatDetails)
        }
    }

    fun getHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        return userRepository.findById(userId).handle { user, sink ->
            val history = user.openAi.chatDetails.history
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

    fun getChatHistory(userId: String): Mono<ChatDetails> {
        return userRepository.findById(userId).map { user ->
            ChatDetails(
                decryptMessages(user.openAi.chatDetails.history),
                user.openAi.chatDetails.totalTokens
            )
        }
    }

    private fun encryptMessages(history: MutableMap<Int, Message>): MutableMap<Int, Message> {
        return history.mapValues { (_, message) ->
            val encryptedContent = aesEncryption.encrypt(message.content)
            message.copy(content = encryptedContent)
        }.toMutableMap()
    }

    private fun decryptMessages(history: MutableMap<Int, Message>): MutableMap<Int, Message> {
        return history.mapValues { (_, message) ->
            val decryptedContent = aesEncryption.decrypt(message.content)
            message.copy(content = decryptedContent)
        }.toMutableMap()
    }
}