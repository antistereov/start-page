package io.github.antistereov.start.widgets.auth.instagram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class InstagramLongLivedTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long,
)