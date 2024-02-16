package io.github.antistereov.start.widgets.instagram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class InstagramUser(
    @JsonProperty("account_type")
    val accountType: String?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("media_count")
    val mediaCount: Int?,
    @JsonProperty("username")
    val username: String?,
    @JsonProperty("media")
    val media: List<InstagramMedia>?
)