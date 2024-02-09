package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.CreateUserDto
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.dto.UpdateUserDTO
import io.github.antistereov.start.user.model.RoleModel
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
    // Initialize ADMIN and USER role at startup
    init {
        if (!roleRepository.existsByName("ADMIN")) {
            val adminRole = RoleModel(name = "ADMIN")
            roleRepository.save(adminRole)
        }
        if (!roleRepository.existsByName("USER")) {
            val adminRole = RoleModel(name = "USER")
            roleRepository.save(adminRole)
        }
    }

    private val userRole = roleRepository.findByName("USER")!!
    private val adminRole = roleRepository.findByName("ADMIN")!!

    fun createUser(createUserDto: CreateUserDto): UserModel {
        if (userRepository.existsByUsername(createUserDto.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(createUserDto.email)) {
            throw IllegalArgumentException("Email is already in use")
        }

        val newUser = toModel(createUserDto)
        try {
            userRepository.save(newUser)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Failed to create user")
        }
        return newUser
    }

    fun update(updateUserDTO: UpdateUserDTO): UserModel {
        val user = userRepository.findById(updateUserDTO.id).orElseThrow { IllegalArgumentException("User not found") }

        if(updateUserDTO.username == null &&
            updateUserDTO.email == null &&
            updateUserDTO.name == null &&
            updateUserDTO.password == null
            ) { throw IllegalArgumentException("No fields to update") }

        if (updateUserDTO.username != null) {
            if (userRepository.existsByUsername(updateUserDTO.username)) {
                throw IllegalArgumentException("Username is already taken")
            }
            user.username = updateUserDTO.username
        }
        if (updateUserDTO.email != null) {
            if(userRepository.existsByEmail(updateUserDTO.email)) {
                throw IllegalArgumentException("Email is already in use")
            }
            user.email = updateUserDTO.email
        }
        if (updateUserDTO.name != null) {
            user.name = updateUserDTO.name
        }
        if (updateUserDTO.password != null) {
            user.password = passwordEncoder.encode(updateUserDTO.password)
        }

        try {
            userRepository.save(user)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Updating user failed")
        }

        return user
    }

    fun findById(id: Long): UserResponseDTO? {
        return toResponseDTO(userRepository.findById(id).orElse(null))
    }

    fun deleteById(id: Long): Boolean {
        return if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    private fun toModel(createUserDto: CreateUserDto): UserModel {
        return UserModel(
            username = createUserDto.username,
            name = createUserDto.name,
            email = createUserDto.email,
            password = passwordEncoder.encode(createUserDto.password),
            roles = setOf(userRole)
        )
    }

    private fun toResponseDTO(userModel: UserModel?): UserResponseDTO? {
        return userModel?.let {
            UserResponseDTO(
                username = userModel.username,
                email = userModel.email,
                name = userModel.name,
            )
        }
    }
}