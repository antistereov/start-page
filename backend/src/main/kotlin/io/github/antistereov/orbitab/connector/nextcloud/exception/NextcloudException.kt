package io.github.antistereov.orbitab.connector.nextcloud.exception

import io.github.antistereov.orbitab.connector.shared.exception.ConnectorException

open class NextcloudException(message: String, cause: Throwable? = null) : ConnectorException(message, cause)