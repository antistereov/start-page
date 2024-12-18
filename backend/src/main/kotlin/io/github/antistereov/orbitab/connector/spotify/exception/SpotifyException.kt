package io.github.antistereov.orbitab.connector.spotify.exception

import io.github.antistereov.orbitab.connector.shared.exception.ConnectorException

open class SpotifyException(message: String, cause: Throwable? = null) : ConnectorException(message, cause)