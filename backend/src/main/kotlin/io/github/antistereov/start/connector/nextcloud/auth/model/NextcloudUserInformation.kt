package io.github.antistereov.start.connector.nextcloud.auth.model

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class NextcloudUserInformation(
    @field:URL(message = "Invalid URL format")
    val host: String,

    @field:NotBlank(message = "Username must not be empty")
    val username: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,
)