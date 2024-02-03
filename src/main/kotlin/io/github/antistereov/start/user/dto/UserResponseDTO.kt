package io.github.antistereov.start.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val username: String,
    val email: String,
    val name: String,
)