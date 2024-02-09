package io.github.antistereov.start.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDto (
    @field:NotBlank(message = "Username is required.")
    val username: String,

    @field:NotBlank(message = "Email is required.")
    @field:Email(message = "Email should be valid.")
    val email: String,

    @field:NotBlank(message = "Password is required.")
    @field:Size(min = 8, message = "Password must have at least 8 characters.")
    @field:Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}",
        message = "Password must have at least one digit, " +
                "one lowercase letter, one uppercase letter, " +
                "one special character and no spaces.")
    val password: String,

    @field:NotBlank(message = "Name is required.")
    val name: String,
)