package io.github.antistereov.start.user.exception

open class UserServiceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)