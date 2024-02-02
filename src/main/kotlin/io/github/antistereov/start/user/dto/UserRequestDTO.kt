package io.github.antistereov.start.user.dto

data class UserRequestDTO (
    val username: String,
    val email: String,
    val name: String,
    val password: String,
)