package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.UserDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserRepository : ReactiveMongoRepository<UserDocument, String>
