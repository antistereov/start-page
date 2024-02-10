package io.github.antistereov.start.widgets.caldav.model

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class CalDavCredentials(
    @field:URL(message = "Invalid URL format")
    val url: String,

    @field:NotBlank(message = "Username must not be empty")
    val username: String,

    @field:NotBlank(message = "Password must not be empty")
    val password: String,
)