package io.github.antistereov.start.connector.unsplash.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UnsplashPhoto(
    val id: String,
    val url: String,
    val link: String,
    val user: User,
    val color: String,
    @JsonProperty("liked_by_user")
    val likedByUser: Boolean,
) {
    data class User(
        val name: String,
        val link: String,
    )
}
