package io.github.antistereov.orbitab.config

import io.github.antistereov.orbitab.auth.properties.JwtProperties
import io.github.antistereov.orbitab.config.properties.EncryptionProperties
import io.github.antistereov.orbitab.config.properties.FrontendProperties
import io.github.antistereov.orbitab.connector.spotify.auth.SpotifyProperties
import io.github.antistereov.orbitab.connector.unsplash.UnsplashProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    JwtProperties::class,
    EncryptionProperties::class,
    FrontendProperties::class,
    UnsplashProperties::class,
    SpotifyProperties::class,
)
class ApplicationConfig