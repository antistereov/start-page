package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserRepository : ReactiveMongoRepository<User, String>
