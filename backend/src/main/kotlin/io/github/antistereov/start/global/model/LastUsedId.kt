package io.github.antistereov.start.global.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "lastUsedIdCollection")
data class LastUsedId(
    @Id val collection: String,
    var lastUsedId: Long = 0
)