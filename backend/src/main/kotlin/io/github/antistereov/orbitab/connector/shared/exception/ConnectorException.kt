package io.github.antistereov.orbitab.connector.shared.exception

import io.github.antistereov.orbitab.global.exception.StartPageException

open class ConnectorException(message: String? = null, cause: Throwable? = null) : StartPageException(message, cause)