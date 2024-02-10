package io.github.antistereov.start.user.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    var id: String = "",

    @Column(name = "spotify_access_token", length = 512)
    var spotifyAccessToken: String? = null,

    @Column(name = "spotify_refresh_token", length = 512)
    var spotifyRefreshToken: String? = null,

    @Column(name = "spotify_access_token_expiration_date")
    var spotifyAccessTokenExpirationDate: java.time.LocalDateTime? = null,

    @Column(name = "todist_access_token")
    var todoistAccessToken: String? = null,

    @Column(name = "nextcloud_url")
    var nextcloudHost: String? = null,

    @Column(name = "nextcloud_username")
    var nextcloudUsername: String? = null,

    @Column(name = "nextcloud_password")
    var nextcloudPassword: String? = null,
)