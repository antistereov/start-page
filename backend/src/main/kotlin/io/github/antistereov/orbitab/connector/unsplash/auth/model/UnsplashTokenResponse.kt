package io.github.antistereov.orbitab.connector.unsplash.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("created_at") val createdAt: Long,
)