package io.github.antistereov.start.connector.shared.model

import io.github.antistereov.start.connector.spotify.model.SpotifyUserInformation
import io.github.antistereov.start.connector.unsplash.model.UnsplashUserInformation
import io.github.antistereov.start.connector.nextcloud.auth.model.NextcloudUserInformation

data class ConnectorInformation(
    val unsplash: UnsplashUserInformation? = null,
    val spotify: SpotifyUserInformation? = null,
    val nextcloud: NextcloudUserInformation? = null,
)