package io.github.antistereov.orbitab.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "backend")
data class BackendProperties(
    val baseUrl: String,
    val secure: Boolean,
)
