package io.github.antistereov.start.connector.spotify.exception

import io.github.antistereov.start.connector.shared.exception.ConnectorException

open class SpotifyException(message: String, cause: Throwable? = null) : ConnectorException(message, cause)