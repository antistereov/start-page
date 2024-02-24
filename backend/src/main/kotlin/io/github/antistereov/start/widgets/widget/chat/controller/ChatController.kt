package io.github.antistereov.start.widgets.widget.chat.controller

import io.github.antistereov.start.security.AuthenticationPrincipalExtractor
import io.github.antistereov.start.widgets.widget.chat.model.ChatHistory
import io.github.antistereov.start.widgets.widget.chat.model.Message
import io.github.antistereov.start.widgets.widget.chat.service.ChatService
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

    private val logger: Logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping
    fun chat(
        authentication: Authentication,
        @RequestBody message: String
    ): Mono<Message> {
        logger.info("Executing ChatController chat method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.chat(it, message) }
    }

    @GetMapping("/history/{id}")
    fun getHistoryEntry(
        authentication: Authentication,
        @PathVariable id: Int,
    ): Mono<Message> {
        logger.info("Executing ChatController getHistoryEntry method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.getHistoryEntry(it, id) }
    }

    @GetMapping("/history")
    fun getHistory(
        authentication: Authentication
    ): Mono<ChatHistory> {
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
    ): Mono<String> {
        logger.info("Executing ChatController deleteHistory method.")

        return principalExtractor.getUserId(authentication)
            .flatMap { chatService.clearChatHistory(it) }
    }
}