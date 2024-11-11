package io.github.antistereov.start.user.exception

open class UserException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)