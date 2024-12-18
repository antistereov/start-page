package io.github.antistereov.orbitab.user.exception

open class UserException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)