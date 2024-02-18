package io.github.antistereov.start.widgets.openai.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.user.model.OpenAIDetails
import io.github.antistereov.start.widgets.openai.model.ChatResponse
import io.github.antistereov.start.widgets.openai.model.Message
import io.github.antistereov.start.widgets.openai.service.ChatService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
    private val principalExtractor: AuthenticationPrincipalExtractor,
) {

    val logger: Logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping
    fun chat(
        authentication: Authentication,
        @RequestBody message: String
    ): Mono<ChatResponse> {
        logger.info("Executing ChatController chat method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.chat(it, message) }
    }

    @GetMapping("/history/{id}")
    fun getHistoryEntry(
        authentication: Authentication,
        @PathVariable id: Int,
    ): Mono<MutableMap.MutableEntry<Int, Message>> {
        logger.info("Executing ChatController getHistoryEntry method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.getHistoryEntry(it, id) }
    }

    @GetMapping("/history")
    fun getHistory(
        authentication: Authentication
    ): Mono<OpenAIDetails> {
        logger.info("Executing ChatController getHistory method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.getChatHistory(it) }
    }

    @DeleteMapping("/history/{id}")
    fun deleteHistoryEntry(
        authentication: Authentication,
        @PathVariable id: Int,
    ): Mono<Message> {
        logger.info("Executing ChatController deleteHistoryEntry method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.deleteHistoryEntry(it, id) }
    }

    @DeleteMapping("/history")
    fun deleteHistory(
        authentication: Authentication,
    ): Mono<OpenAIDetails> {
        logger.info("Executing ChatController deleteHistory method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.clearChatHistory(it) }
    }
}