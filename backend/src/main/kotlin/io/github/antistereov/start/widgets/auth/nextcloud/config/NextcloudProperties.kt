package io.github.antistereov.start.widgets.auth.nextcloud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nextcloud")
data class NextcloudProperties(
    val serviceName: String,
)