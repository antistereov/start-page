package io.github.antistereov.start.connector.shared.model

import io.github.antistereov.start.connector.spotify.model.SpotifyUserInformation
import io.github.antistereov.start.connector.unsplash.model.UnsplashUserInformation

data class WidgetUserInformation(
    val unsplash: UnsplashUserInformation? = null,
    val spotify: SpotifyUserInformation? = null
)