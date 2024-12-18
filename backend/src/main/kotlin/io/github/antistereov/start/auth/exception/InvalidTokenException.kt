package io.github.antistereov.start.auth.exception

class InvalidTokenException(message: String, cause: Throwable? = null) : AuthException(message, cause)
