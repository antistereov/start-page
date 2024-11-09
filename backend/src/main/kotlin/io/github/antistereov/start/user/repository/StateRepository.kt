package io.github.antistereov.start.user.repository

import io.github.antistereov.start.user.model.StateParameter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StateRepository : CoroutineCrudRepository<StateParameter, String>