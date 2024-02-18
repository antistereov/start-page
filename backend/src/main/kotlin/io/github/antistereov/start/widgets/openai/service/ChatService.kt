package io.github.antistereov.start.widgets.openai.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.MessageLimitExceededException
import io.github.antistereov.start.global.service.BaseService
import io.github.antistereov.start.user.model.OpenAIDetails
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.openai.model.ChatRequest
import io.github.antistereov.start.widgets.openai.model.ChatResponse
import io.github.antistereov.start.widgets.openai.model.Message
import io.netty.handler.codec.DecoderException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val webClient: WebClient,
    private val baseService: BaseService,
    private val properties: OpenAIProperties,
    private val userRepository: UserRepository,
) {

    fun chat(userId: String, content: String): Mono<ChatResponse> {
        val uri = "${properties.apiBaseUrl}/chat/completions"

        return userRepository.findById(userId).flatMap { user ->
            val chatHistory = user.openAi.history.values
            if (chatHistory.size >= properties.messageLimit) {
                return@flatMap Mono.error(MessageLimitExceededException())
            }

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
                        addAll(chatHistory)
                        add(newMessage)
                    },
                user = userId,
           )

            webClient
                .post()
                .uri(uri)
                .header("Authorization", "Bearer ${properties.apiKey}")
                .bodyValue(chatRequest)
                .retrieve()
                .let { baseService.handleError(uri, it) }
                .bodyToMono(ChatResponse::class.java)
                .flatMap { response ->
                    val entryNumber = user.openAi.history.size
                    user.openAi.history[entryNumber] = newMessage
                    user.openAi.history[entryNumber + 1] = response.choices.first().message
                    user.openAi.totalTokens = response.usage.totalTokens
                    userRepository.save(user)
                        .onErrorMap { throwable ->
                            CannotSaveUserException(throwable)
                        }
                        .thenReturn(response)
                }
                .onErrorResume(WebClientResponseException::class.java, baseService.handleNetworkError(uri))
                .onErrorResume(DecoderException::class.java, baseService.handleParsingError(uri))
        }
    }

    fun deleteHistoryEntry(userId: String, entryNumber: Int): Mono<Message> {
        return userRepository.findById(userId).flatMap { user ->
            val history = user.openAi.history
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
            user.openAi.history = updatedHistory
            userRepository.save(user)
                .onErrorMap { throwable ->
                    CannotSaveUserException(throwable)
                }
                .thenReturn(entryToRemove!!)
        }
    }

    fun clearChatHistory(userId: String): Mono<OpenAIDetails> {
        return userRepository.findById(userId).flatMap { user ->
            user.openAi.history.clear()
            user.openAi.totalTokens = 0
            userRepository.save(user)
                .onErrorMap { throwable ->
                    CannotSaveUserException(throwable)
                }
                .thenReturn(user.openAi)
        }
    }

    fun getHistoryEntry(userId: String, entryNumber: Int): Mono<MutableMap.MutableEntry<Int, Message>> {
        return userRepository.findById(userId).handle { user, sink ->
            val history = user.openAi.history
            if (history.size <= entryNumber) {
                sink.error(IndexOutOfBoundsException("No history entry at index $entryNumber"))
                return@handle
            }
            sink.next(history.entries.elementAt(entryNumber))
        }
    }

    fun getChatHistory(userId: String): Mono<OpenAIDetails> {
        return userRepository.findById(userId).map { user ->
            user.openAi
        }
    }
}