package io.github.antistereov.start.global.model.exception

class UnexpectedErrorException(message: String, cause: Throwable): RuntimeException(message, cause)