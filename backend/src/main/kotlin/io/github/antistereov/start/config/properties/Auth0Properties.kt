package io.github.antistereov.start.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth0")
data class Auth0Properties(
    val domain: String,
    val audience: String,
    val clientId: String,
    val clientSecret: String,
)
