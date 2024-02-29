package io.github.antistereov.start.user.service

import io.github.antistereov.start.config.properties.Auth0Properties
import io.github.antistereov.start.global.exception.CannotSaveUserException
import io.github.antistereov.start.global.exception.UserNotFoundException
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.widget.caldav.repository.CalDavRepository
import io.github.antistereov.start.widgets.widget.chat.repository.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val calDavRepository: CalDavRepository,
    private val webClient: WebClient,
    private val auth0properties: Auth0Properties,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun findOrCreateUser(userId: String): Mono<User> {
        logger.debug("Finding or creating user: $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(
                userRepository.save(User(userId))
                    .onErrorMap(DataAccessException::class.java) { ex ->
                        io.github.antistereov.start.global.exception.ServiceException(
                            "Error creating user: $userId",
                            ex
                        )
                    }
            )
    }

    fun findById(userId: String): Mono<User> {
        logger.debug("Finding user by ID: $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException(userId)))
    }

    fun save(user: User): Mono<User> {
        logger.debug("Saving user: {}", user.id)

        return userRepository.save(user)
            .onErrorMap(DataAccessException::class.java) { ex ->
                CannotSaveUserException(ex)
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
                    RuntimeException("Error deleting user: $userId, ${ex.message}", ex)
                }
        }
    }

    fun deleteAuth0User(userId: String): Mono<Void> {
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
    }

    fun deleteCalDavWidget(userId: String): Mono<String> {
        logger.debug("Deleting CalDav widget for user: {}", userId)

        return findById(userId).flatMap { user ->
            val calDavId = user.widgets.calDavId
                ?: return@flatMap Mono.just("CalDav widget cleared.")

            calDavRepository.findById(calDavId).flatMap { widget ->
                calDavRepository.delete(widget)
                    .doOnError { error ->
                        logger.error("Error deleting CalDav widget: $calDavId", error)
                    }
                    .then(Mono.just("CalDav widget cleared."))
                    .flatMap {
                        user.widgets.calDavId = null
                        save(user).thenReturn("CalDav widget cleared.")
                    }
            }
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