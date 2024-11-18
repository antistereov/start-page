package io.github.antistereov.start.auth.exception

class AccessTokenExpiredException(message: String, cause: Throwable? = null) : AuthException(message, cause)