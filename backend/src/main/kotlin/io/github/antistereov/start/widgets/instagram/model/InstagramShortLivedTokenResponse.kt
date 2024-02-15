package io.github.antistereov.start.widgets.instagram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class InstagramShortLivedTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("user_id") val userId: String,
)