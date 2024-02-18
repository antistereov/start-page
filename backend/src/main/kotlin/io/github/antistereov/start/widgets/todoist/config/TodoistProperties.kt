package io.github.antistereov.start.widgets.todoist.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "todoist")
data class TodoistProperties(
    val serviceName: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: String,
    val apiBaseUrl: String,
)
