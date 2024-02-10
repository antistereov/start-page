package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String>
