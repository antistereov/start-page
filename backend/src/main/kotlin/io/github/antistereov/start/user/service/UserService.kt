package io.github.antistereov.start.user.service

import io.github.antistereov.start.global.model.exception.CannotSaveUserException
import io.github.antistereov.start.global.model.exception.ServiceException
import io.github.antistereov.start.global.model.exception.UserNotFoundException
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun findOrCreateUser(userId: String): Mono<User> {
        logger.debug("Finding or creating user: $userId")

        return userRepository.findById(userId)
            .switchIfEmpty(
                userRepository.save(User(userId))
                    .onErrorMap(DataAccessException::class.java) { ex ->
                        ServiceException("Error creating user: $userId", ex)
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
}