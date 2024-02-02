package io.github.antistereov.start.user.dto

import io.github.antistereov.start.user.model.RoleModel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserUpdateDTO (
    @field:NotBlank(message = "Id is required.")
    val id: Long,

    val username: String?,

    @field:Email(message = "Email should be valid.")
    val email: String?,

    val name: String?,

    val password: String?
)