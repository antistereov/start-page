package io.github.antistereov.start.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "encryption")
data class EncryptionProperties(
    val secretKey: String,
)
