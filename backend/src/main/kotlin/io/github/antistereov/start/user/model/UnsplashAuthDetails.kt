package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.unsplash.model.Photo


data class UnsplashAuthDetails(
    var userId: String? = null,
    var username: String? = null,
    var accessToken: String? = null,
    var recentPhotos: MutableList<Photo> = mutableListOf()
)
