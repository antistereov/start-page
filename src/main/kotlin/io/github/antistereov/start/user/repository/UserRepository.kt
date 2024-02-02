package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.UserModel
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserModel, Long> {

    fun findByUsername(username: String): UserModel?
}