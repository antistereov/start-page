package io.github.antistereov.start.widgets.auth.spotify.model

data class SpotifyAuthDetails(
    var userId: String? = null,
    var username: String? = null,
    var accessToken: String? = null,
    var expirationDate: java.time.LocalDateTime? = null,
    var refreshToken: String? = null,
)
