package io.github.antistereov.start.global.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "states")
data class StateParameter(
    @Id val id: String,
    val userId: String,
    val timestamp: Long
)