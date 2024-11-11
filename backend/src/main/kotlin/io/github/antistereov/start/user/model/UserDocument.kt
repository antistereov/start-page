package io.github.antistereov.start.user.model

import io.github.antistereov.start.connector.shared.model.WidgetUserInformation
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class UserDocument(
    @Id val id: String? = null,
    @Indexed(unique = true) val username: String,
    val password: String,
    val roles: List<Role> = listOf(Role.USER),
    val widgets: WidgetUserInformation? = null,
)
