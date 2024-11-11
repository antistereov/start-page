package io.github.antistereov.start.widget.spotify.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifyUserProfile(
    @JsonProperty("display_name")
    val displayName: String,
    val images: List<Image>,
    val product: String,
    val uri: String,
) {
    data class Image(
        val url: String,
        val height: Int,
        val width: Int,
    )
}
