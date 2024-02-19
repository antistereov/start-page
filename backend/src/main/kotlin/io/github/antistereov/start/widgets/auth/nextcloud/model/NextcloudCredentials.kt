package io.github.antistereov.start.widgets.auth.nextcloud.model

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class NextcloudCredentials(
    @field:URL(message = "Invalid URL format")
    var host: String? = null,

    @field:NotBlank(message = "Username must not be empty")
    var username: String? = null,

    @field:NotBlank(message = "Password must not be empty")
    var password: String? = null,
)