package io.github.antistereov.start.connector.nextcloud.auth.model

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank


data class NextcloudUserInformation(
    val host: String,
    val username: String,
    val password: String,
)