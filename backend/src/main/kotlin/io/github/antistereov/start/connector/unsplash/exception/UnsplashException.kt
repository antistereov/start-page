package io.github.antistereov.start.connector.unsplash.exception

import io.github.antistereov.start.connector.shared.exception.ConnectorException

open class UnsplashException(message: String?, cause: Throwable? = null) : ConnectorException(message, cause)