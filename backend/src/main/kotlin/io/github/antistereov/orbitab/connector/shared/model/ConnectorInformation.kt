package io.github.antistereov.orbitab.connector.shared.model

import io.github.antistereov.orbitab.connector.spotify.model.SpotifyUserInformation
import io.github.antistereov.orbitab.connector.unsplash.model.UnsplashUserInformation
import io.github.antistereov.orbitab.connector.nextcloud.auth.model.NextcloudUserInformation

data class ConnectorInformation(
    val unsplash: UnsplashUserInformation? = null,
    val spotify: SpotifyUserInformation? = null,
    val nextcloud: NextcloudUserInformation? = null,
)