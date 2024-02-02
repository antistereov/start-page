package io.github.antistereov.start.user.dto

data class UserRequestDTO (
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val password: String,
)