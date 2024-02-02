package io.github.antistereov.start.user.dto

import io.github.antistereov.start.user.model.RoleModel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserUpdateDTO (
    @field:NotBlank(message = "Id is required.")
    val id: Long,

    val username: String? = null,

    @field:Email(message = "Email should be valid.")
    val email: String? = null,

    val name: String? = null,

    val password: String? = null
)