package io.github.antistereov.start.auth.exception

open class AuthServiceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
)