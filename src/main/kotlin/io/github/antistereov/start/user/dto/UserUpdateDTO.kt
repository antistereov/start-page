package io.github.antistereov.start.user.dto

import io.github.antistereov.start.user.model.RoleModel

data class UserUpdateDTO (
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val password: String
)