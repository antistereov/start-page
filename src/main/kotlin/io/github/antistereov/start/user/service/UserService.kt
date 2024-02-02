package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.UserRequestDTO
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.repository.RoleRepository
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun save(userRequestDTO: UserRequestDTO) {
        val user = toModel(userRequestDTO)
        userRepository.save(user)
    }

    fun findById(id: Long): UserResponseDTO? {
        return toResponseDTO(userRepository.findById(id).orElse(null))
    }

    fun deleteById(id: Long) {
        userRepository.deleteById(id)
    }

    val userRole = roleRepository.findByName("USER")!!

    private fun toModel(userRequestDTO: UserRequestDTO): UserModel {
        return UserModel(
            username = userRequestDTO.username,
            name = userRequestDTO.name,
            email = userRequestDTO.email,
            password = passwordEncoder.encode(userRequestDTO.password),
            roles = setOf(userRole)
        )
    }

    private fun toResponseDTO(userModel: UserModel?): UserResponseDTO? {
        return userModel?.let {
            UserResponseDTO(
                id = userModel.id,
                username = userModel.username,
                email = userModel.email,
                name = userModel.name,
            )
        }
    }
}