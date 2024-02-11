package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UserRepository : ReactiveCrudRepository<User, String>
