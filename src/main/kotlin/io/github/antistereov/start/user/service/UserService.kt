package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.UserRequestDTO
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.dto.UserUpdateDTO
import io.github.antistereov.start.user.model.UserModel
import io.github.antistereov.start.user.repository.RoleRepository
import io.github.antistereov.start.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun create(userRequestDTO: UserRequestDTO) {
        if (userRepository.existsByUsername(userRequestDTO.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(userRequestDTO.email)) {
            throw IllegalArgumentException("Email is already in use")
        }

        val newUser = toModel(userRequestDTO)
        try {
            userRepository.save(newUser)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Failed to create user")
        }
    }

    fun update(userUpdateDTO: UserUpdateDTO) {
        val user = userRepository.findById(userUpdateDTO.id).orElseThrow { IllegalArgumentException("User not found") }

        if (userRepository.existsByUsername(userUpdateDTO.username)) {
            throw IllegalArgumentException("Username is already taken")
        }

        if(userRepository.existsByEmail(userUpdateDTO.email)) {
            throw IllegalArgumentException("Email is already in use")
        }

        user.name = userUpdateDTO.name
        user.email = userUpdateDTO.email
        user.name = userUpdateDTO.name
        user.password = passwordEncoder.encode(userUpdateDTO.password)

        try {
            userRepository.save(user)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Updating user failed")
        }
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