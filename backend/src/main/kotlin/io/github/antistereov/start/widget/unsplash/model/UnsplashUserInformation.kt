package io.github.antistereov.start.widget.unsplash.model

import io.github.antistereov.start.widget.unsplash.auth.model.UnsplashProfileImage

data class UnsplashUserInformation(
    val username: String? = null,
    val accessToken: String? = null,
    val profileImage: UnsplashProfileImage? = null
)