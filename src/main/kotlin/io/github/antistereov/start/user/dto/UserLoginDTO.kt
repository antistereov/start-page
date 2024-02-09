package io.github.antistereov.start.user.dto

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDTO(
    @field:NotBlank(message = "Username or e-mail is required.")
    val username: String,

    @field:NotBlank(message = "Password is required.")
    val password: String,
)