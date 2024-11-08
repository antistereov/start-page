package io.github.antistereov.start.config

import io.github.antistereov.start.auth.properties.JwtProperties
import io.github.antistereov.start.config.properties.EncryptionProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    JwtProperties::class,
    EncryptionProperties::class,
)
class ApplicationConfig