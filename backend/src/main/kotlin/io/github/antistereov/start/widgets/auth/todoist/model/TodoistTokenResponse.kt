package io.github.antistereov.start.widgets.auth.todoist.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TodoistTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
)