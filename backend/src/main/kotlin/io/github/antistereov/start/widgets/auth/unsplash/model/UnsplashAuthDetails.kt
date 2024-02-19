package io.github.antistereov.start.widgets.auth.unsplash.model

import io.github.antistereov.start.widgets.widget.unsplash.model.Photo


data class UnsplashAuthDetails(
    var userId: String? = null,
    var username: String? = null,
    var accessToken: String? = null,
    var recentPhotos: MutableList<Photo> = mutableListOf()
)
