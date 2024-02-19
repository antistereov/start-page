package io.github.antistereov.start.widgets.auth.instagram.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "instagram")
data class InstagramProperties(
    val serviceName: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val apiBaseUrl: String,
    val scopes: String,
)
