package io.github.antistereov.start.connector.nextcloud.exception

import io.github.antistereov.start.connector.shared.exception.ConnectorException

open class NextcloudException(message: String, cause: Throwable? = null) : ConnectorException(message, cause)