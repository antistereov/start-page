package io.github.antistereov.start.widgets.auth.unsplash.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("created_at") val createdAt: Long,
)