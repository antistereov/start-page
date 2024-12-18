package io.github.antistereov.orbitab.user.repository

import io.github.antistereov.orbitab.user.model.UserDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<UserDocument, String> {

    suspend fun existsByUsername(username: String): Boolean

    suspend fun findByUsername(username: String): UserDocument?
}
