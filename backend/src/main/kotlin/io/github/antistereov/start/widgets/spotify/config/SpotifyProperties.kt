package io.github.antistereov.start.widgets.spotify.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spotify")
data class SpotifyProperties(
    val serviceName: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: String,
    val apiBaseUrl: String,
)
