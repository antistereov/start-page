package io.github.antistereov.start.connector.shared.exception

import io.github.antistereov.start.global.exception.StartPageException

open class ConnectorException(message: String? = null, cause: Throwable? = null) : StartPageException(message, cause)