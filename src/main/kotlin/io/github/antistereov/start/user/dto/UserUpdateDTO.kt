package io.github.antistereov.start.user.dto

import io.github.antistereov.start.user.model.RoleModel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserUpdateDTO (
    @field:NotBlank(message = "Id is required.")
    val id: Long,

    val username: String? = null,

    @field:Email(message = "Email should be valid.")
    val email: String? = null,

    val name: String? = null,

    @field:Size(min = 8, message = "Password must have at least 8 characters.")
    @field:Pattern(
        regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}",
        message = "Password must have at least one digit, one lowercase letter, " +
                "one uppercase letter, one special character and no spaces."
    )
    val password: String? = null
)