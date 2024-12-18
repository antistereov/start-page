package io.github.antistereov.orbitab.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    val baseUrl: String
)