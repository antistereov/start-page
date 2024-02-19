package io.github.antistereov.start.config

import io.github.antistereov.start.config.properties.Auth0Properties
import io.github.antistereov.start.config.properties.EncryptionProperties
import io.github.antistereov.start.widgets.auth.instagram.config.InstagramProperties
import io.github.antistereov.start.widgets.auth.nextcloud.config.NextcloudProperties
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.auth.spotify.config.SpotifyProperties
import io.github.antistereov.start.widgets.auth.todoist.config.TodoistProperties
import io.github.antistereov.start.widgets.unsplash.config.UnsplashProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    Auth0Properties::class,
    EncryptionProperties::class,
    InstagramProperties::class,
    NextcloudProperties::class,
    OpenAIProperties::class,
    SpotifyProperties::class,
    TodoistProperties::class,
    UnsplashProperties::class,
)
class ApplicationConfig