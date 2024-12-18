package io.github.antistereov.orbitab.connector.unsplash.exception

import io.github.antistereov.orbitab.connector.shared.exception.ConnectorException

open class UnsplashException(message: String?, cause: Throwable? = null) : ConnectorException(message, cause)