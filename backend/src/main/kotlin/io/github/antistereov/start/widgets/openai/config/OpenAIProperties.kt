package io.github.antistereov.start.widgets.openai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "open-ai")
data class OpenAIProperties(
    val serviceName: String,
    val apiKey: String,
    val apiBaseUrl: String,
    val messageLimit: Int,
)
