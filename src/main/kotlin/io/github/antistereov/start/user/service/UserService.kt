package io.github.antistereov.start.user.service

import io.github.antistereov.start.user.dto.UserCreateDTO
import io.github.antistereov.start.user.dto.UserResponseDTO
import io.github.antistereov.start.user.dto.UserUpdateDTO
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

    fun createUser(userCreateDTO: UserCreateDTO): UserModel {
        if (userRepository.existsByUsername(userCreateDTO.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(userCreateDTO.email)) {
            throw IllegalArgumentException("Email is already in use")
        }

        val newUser = toModel(userCreateDTO)
        try {
            userRepository.save(newUser)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Failed to create user")
        }
        return newUser
    }

    fun createAdmin(userCreateDTO: UserCreateDTO): UserModel {
        if (userRepository.existsByUsername(userCreateDTO.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        if (userRepository.existsByEmail(userCreateDTO.email)) {
            throw IllegalArgumentException("Email is already in use")
        }

        val newUser = toModel(userCreateDTO)
        newUser.roles = setOf(adminRole, userRole)

        try {
            userRepository.save(newUser)
        } catch(e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Failed to create admin user")
            }

        return newUser
    }

    fun update(userUpdateDTO: UserUpdateDTO): UserModel {
        val user = userRepository.findById(userUpdateDTO.id).orElseThrow { IllegalArgumentException("User not found") }

        if(userUpdateDTO.username == null &&
            userUpdateDTO.email == null &&
            userUpdateDTO.name == null &&
            userUpdateDTO.password == null
            ) { throw IllegalArgumentException("No fields to update") }

        if (userUpdateDTO.username != null) {
            if (userRepository.existsByUsername(userUpdateDTO.username)) {
                throw IllegalArgumentException("Username is already taken")
            }
            user.username = userUpdateDTO.username
        }
        if (userUpdateDTO.email != null) {
            if(userRepository.existsByEmail(userUpdateDTO.email)) {
                throw IllegalArgumentException("Email is already in use")
            }
            user.email = userUpdateDTO.email
        }
        if (userUpdateDTO.name != null) {
            user.name = userUpdateDTO.name
        }
        if (userUpdateDTO.password != null) {
            user.password = passwordEncoder.encode(userUpdateDTO.password)
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

    private fun toModel(userCreateDTO: UserCreateDTO): UserModel {
        return UserModel(
            username = userCreateDTO.username,
            name = userCreateDTO.name,
            email = userCreateDTO.email,
            password = passwordEncoder.encode(userCreateDTO.password),
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