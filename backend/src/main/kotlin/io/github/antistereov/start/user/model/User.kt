package io.github.antistereov.start.user.model

import jakarta.persistence.*
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "users")
data class User(
    @Id
    var id: String = "",
    var spotifyAccessToken: String? = null,
    var spotifyRefreshToken: String? = null,
    var spotifyAccessTokenExpirationDate: java.time.LocalDateTime? = null,
    var todoistAccessToken: String? = null,
    var unsplashAccessToken: String? = null,
    var nextcloudHost: String? = null,
    var nextcloudUsername: String? = null,
    var nextcloudPassword: String? = null,
)