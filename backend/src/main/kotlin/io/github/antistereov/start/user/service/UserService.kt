package io.github.antistereov.start.user.service

import io.github.antistereov.start.auth.properties.JwtProperties
import io.github.antistereov.start.auth.service.HashService
import io.github.antistereov.start.global.exception.CannotDeleteDocumentException
import io.github.antistereov.start.global.exception.CannotSaveDocumentException
import io.github.antistereov.start.global.exception.DocumentNotFoundException
import io.github.antistereov.start.user.dto.RegisterUserDto
import io.github.antistereov.start.user.exception.UsernameAlreadyExistsException
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.repository.UserRepository
import io.github.antistereov.start.widgets.widget.caldav.service.CalDavResourceService
import io.github.antistereov.start.widgets.widget.chat.repository.ChatRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.catalina.User
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(userId: String): UserDocument? {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId)
    }

    suspend fun findByUsername(username: String): UserDocument? {
        logger.debug { "Fetching user with username $username" }

        return userRepository.findByUsername(username)
    }

    suspend fun existsByUsername(username: String): Boolean {
        logger.debug { "Checking if username $username already exists" }

        return userRepository.existsByUsername(username)
    }

    suspend fun save(user: UserDocument): UserDocument {
        logger.debug { "Saving user: ${user.id}" }

        return userRepository.save(user)
    }

    suspend fun delete(userId: String) {
        logger.debug { "Deleting user $userId" }

        userRepository.deleteById(userId)
    }
}