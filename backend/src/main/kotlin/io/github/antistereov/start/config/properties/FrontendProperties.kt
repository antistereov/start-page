package io.github.antistereov.start.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    val baseUrl: String
)