package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.exception.UserDoesNotExistException
import io.github.antistereov.start.user.model.UserDocument
import io.github.antistereov.start.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(userId: String): UserDocument {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId) ?: throw UserDoesNotExistException(userId)
    }

    suspend fun findByIdOrNull(userId: String): UserDocument? {
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