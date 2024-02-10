package io.github.antistereov.start.user.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    var id: String = "",

    @Column(name = "spotify_user_id")
    var spotifyUserId: String? = null,

    @Column(name = "spotify_access_token")
    var accessToken: String? = null,

    @Column(name = "spotify_refresh_token")
    var refreshToken: String? = null,

    @Column(name = "spotify_access_token_expiration_date")
    var accessTokenExpirationDate: java.time.LocalDateTime? = null,
)