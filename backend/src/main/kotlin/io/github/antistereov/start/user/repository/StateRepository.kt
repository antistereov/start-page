package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.StateParameter
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface StateRepository : ReactiveMongoRepository<StateParameter, String>