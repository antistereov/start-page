package io.github.antistereov.start.user.service

import io.github.antistereov.start.config.properties.Auth0Properties
import io.github.antistereov.start.global.exception.CannotDeleteDocumentException
import io.github.antistereov.start.global.exception.CannotSaveDocumentException
import io.github.antistereov.start.global.exception.DocumentNotFoundException
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.widget.caldav.service.CalDavResourceService
import io.github.antistereov.start.widgets.widget.chat.repository.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val calDavResourceService: CalDavResourceService,
    private val webClient: WebClient,
    private val auth0properties: Auth0Properties,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun findOrCreateUser(userId: String): Mono<UserDocument> {
        logger.debug("Finding or creating user: $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(
                userRepository.save(UserDocument(userId))
                    .onErrorMap(DataAccessException::class.java) { ex ->
                        io.github.antistereov.start.global.exception.ServiceException(
                            "Error creating user: $userId",
                            ex
                        )
                    }
            )
    }

    fun findById(userId: String): Mono<UserDocument> {
        logger.debug("Finding user by ID: $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(DocumentNotFoundException(userId, UserDocument::class.java)))
    }

    fun save(user: UserDocument): Mono<UserDocument> {
        logger.debug("Saving user: {}", user.id)

        return userRepository.save(user)
            .onErrorMap { ex ->
                CannotSaveDocumentException(user.id, UserDocument::class.java, ex)
            }
    }

    fun delete(userId: String): Mono<String> {
        logger.debug("Deleting user: {}", userId)

        return findById(userId).flatMap { user ->
            deleteCalDavWidget(userId)
                .then(deleteChatWidget(userId))
                .then(Mono.defer { userRepository.delete(user) })
                .then(Mono.just("User deleted."))
                .onErrorMap(DataAccessException::class.java) { ex ->
                    CannotDeleteDocumentException(user.id, UserDocument::class.java, ex)
                }
        }
    }

    fun deleteAuth0User(userId: String): Mono<String> {
        return webClient.post()
            .uri("https://${auth0properties.domain}/oauth/token")
            .bodyValue(mapOf(
                "client_id" to auth0properties.clientId,
                "client_secret" to auth0properties.clientSecret,
                "audience" to "https://${auth0properties.domain}/api/v2/",
                "grant_type" to "client_credentials"
            ))
            .retrieve()
            .bodyToMono<Map<String, String>>()
            .flatMap { tokenResponse ->
                val token = tokenResponse["access_token"]
                webClient.delete()
                    .uri("https://${auth0properties.domain}/api/v2/users/$userId")
                    .header("Authorization", "Bearer $token")
                    .retrieve()
                    .bodyToMono(Void::class.java)
            }
            .then(delete(userId))
            .map { "User globally deleted." }
    }

    private fun deleteCalDavWidget(userId: String): Mono<Void> {
        logger.debug("Deleting CalDav widget for user {}", userId)

        return findById(userId).flatMap { user ->
            val resourceIds = user.widgets.calDav.resources

            Flux.fromIterable(resourceIds).flatMap { resourceId ->
                calDavResourceService.deleteCalDavResourceById(resourceId)
            }.then()
        }
    }

    fun deleteChatWidget(userId: String): Mono<String> {
        logger.debug("Deleting Chat widget for user: {}", userId)

        return findById(userId).flatMap { user ->
            val chatId = user.widgets.chatId
                ?: return@flatMap Mono.just("Chat widget cleared.")

            chatRepository.findById(chatId).flatMap { widget ->
                chatRepository.delete(widget)
                    .doOnError { error ->
                        logger.error("Error deleting Chat widget: $chatId", error)
                    }
                    .then(Mono.just("Chat widget cleared."))
                    .flatMap {
                        user.widgets.chatId = null
                        save(user).thenReturn("Chat widget cleared.")
                    }
            }
        }
    }
}