package io.github.antistereov.start.connector.spotify.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifyRefreshTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("expires_in") val expiresIn: Long,
)
