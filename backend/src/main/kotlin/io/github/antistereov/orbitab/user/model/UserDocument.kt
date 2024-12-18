package io.github.antistereov.orbitab.user.model

import io.github.antistereov.orbitab.connector.shared.model.ConnectorInformation
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class UserDocument(
    @Id val id: String? = null,
    @Indexed(unique = true) val username: String,
    val password: String,
    val roles: List<Role> = listOf(Role.USER),
    val connectors: ConnectorInformation? = null,
    val devices: List<DeviceInfo> = listOf(),
)
