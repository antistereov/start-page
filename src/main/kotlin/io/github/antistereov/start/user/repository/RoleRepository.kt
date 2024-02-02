package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.RoleModel
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<RoleModel, Long> {
    fun findByName(roleName: String): RoleModel?

    fun existsByName(roleName: String): Boolean
}