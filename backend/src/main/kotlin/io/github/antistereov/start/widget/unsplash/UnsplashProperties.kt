package io.github.antistereov.start.widget.unsplash

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "unsplash")
data class UnsplashProperties(
    val serviceName: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: String,
    val apiBaseUrl: String,
)