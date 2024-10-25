package io.github.antistereov.start.auth.dto

data class LoginResponseDto(
    val accessToken: String,
    val expiresIn: Long,
)
