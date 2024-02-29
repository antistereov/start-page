package io.github.antistereov.start.user.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Id val id: String,
    var auth: Auth = Auth(),
    var widgets: Widgets = Widgets(),
)
