package io.github.antistereov.start.widget.shared.model

import io.github.antistereov.start.widget.spotify.model.SpotifyUserInformation
import io.github.antistereov.start.widget.unsplash.model.UnsplashUserInformation

data class WidgetUserInformation(
    val unsplash: UnsplashUserInformation? = null,
    val spotify: SpotifyUserInformation? = null
)