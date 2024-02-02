package io.github.antistereov.start.service

import io.github.antistereov.start.model.UserEntity
import io.github.antistereov.start.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(user: UserEntity) = userRepository.save(user)
}