package io.github.antistereov.orbitab.auth.exception

class AccessTokenExpiredException(message: String, cause: Throwable? = null) : AuthException(message, cause)