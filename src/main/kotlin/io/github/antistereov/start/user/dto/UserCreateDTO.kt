package io.github.antistereov.start.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserCreateDTO (
    @field:NotBlank(message = "Username is required.")
    val username: String,

    @field:NotBlank(message = "Email is required.")
    @field:Email(message = "Email should be valid.")
    val email: String,

    @field:NotBlank(message = "Name is required.")
    val password: String,

    @field:NotBlank(message = "Password is required.")
    val name: String,
)