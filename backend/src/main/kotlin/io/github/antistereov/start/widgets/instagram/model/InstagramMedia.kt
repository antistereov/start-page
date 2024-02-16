package io.github.antistereov.start.widgets.instagram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class InstagramMedia(
    @JsonProperty
    val fields: Map<String, String>
)
