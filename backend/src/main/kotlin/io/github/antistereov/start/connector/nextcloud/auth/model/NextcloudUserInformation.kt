package io.github.antistereov.start.connector.nextcloud.auth.model


data class NextcloudUserInformation(
    val host: String,
    val username: String,
    val password: String,
)