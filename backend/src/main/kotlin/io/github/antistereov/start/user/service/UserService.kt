package io.github.antistereov.start.user.service

import io.github.antistereov.start.global.model.exception.ServiceException
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
}