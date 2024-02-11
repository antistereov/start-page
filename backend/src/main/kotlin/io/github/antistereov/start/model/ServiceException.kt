package io.github.antistereov.start.model

class ServiceException(message: String, cause: Throwable?): RuntimeException(message, cause)