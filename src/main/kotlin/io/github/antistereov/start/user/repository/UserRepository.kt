package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.UserModel
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<UserModel, Long> {

    fun findByUsername(username: String): UserModel?

    fun findByEmail(email: String): UserModel?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findBySpotifyUserId(spotifyUserId: String): UserModel?
}