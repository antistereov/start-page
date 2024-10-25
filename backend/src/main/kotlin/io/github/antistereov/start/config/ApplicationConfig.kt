package io.github.antistereov.start.config

import io.github.antistereov.start.auth.properties.JwtProperties
import io.github.antistereov.start.config.properties.EncryptionProperties
import io.github.antistereov.start.widgets.auth.instagram.config.InstagramProperties
import io.github.antistereov.start.widgets.auth.nextcloud.config.NextcloudProperties
import io.github.antistereov.start.widgets.auth.openai.config.OpenAIProperties
import io.github.antistereov.start.widgets.auth.spotify.config.SpotifyProperties
import io.github.antistereov.start.widgets.auth.todoist.config.TodoistProperties
import io.github.antistereov.start.widgets.auth.unsplash.config.UnsplashProperties
import io.github.antistereov.start.widgets.widget.weather.config.OpenWeatherMapProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    JwtProperties::class,
    EncryptionProperties::class,
    InstagramProperties::class,
    NextcloudProperties::class,
    OpenAIProperties::class,
    OpenWeatherMapProperties::class,
    SpotifyProperties::class,
    TodoistProperties::class,
    UnsplashProperties::class,
)
class ApplicationConfig