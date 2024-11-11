package io.github.antistereov.start.connector.spotify.model

data class SpotifyUserInformation(
    val accessToken: String? = null,
    val expirationDate: java.time.LocalDateTime? = null,
    val refreshToken: String? = null,
)
