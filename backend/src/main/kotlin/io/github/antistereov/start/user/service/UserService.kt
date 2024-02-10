package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.CreateUserDto
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.dto.UpdateUserDTO
import io.github.antistereov.start.user.model.User
import io.github.antistereov.start.user.repository.UserRepository
import org.hibernate.service.spi.ServiceException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun findOrCreateUser(userId: String): User {
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            return user.get()
        }

        try {
            return userRepository.save(User(userId))
        } catch (exception: DataAccessException) {
            throw ServiceException("Error creating user:  $userId", exception)
        }
    }
}