package io.github.antistereov.orbitab.user.repository

import io.github.antistereov.orbitab.user.model.StateParameter
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StateRepository : CoroutineCrudRepository<StateParameter, String>