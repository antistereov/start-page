package io.github.antistereov.start.global.respository

import io.github.antistereov.start.global.model.LastUsedId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface LastUsedIdRepository : ReactiveMongoRepository<LastUsedId, String>