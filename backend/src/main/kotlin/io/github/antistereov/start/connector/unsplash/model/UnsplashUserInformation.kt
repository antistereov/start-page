package io.github.antistereov.start.connector.unsplash.model

import io.github.antistereov.start.connector.unsplash.auth.model.UnsplashProfileImage

data class UnsplashUserInformation(
    val username: String? = null,
    val accessToken: String? = null,
    val profileImage: UnsplashProfileImage? = null
)