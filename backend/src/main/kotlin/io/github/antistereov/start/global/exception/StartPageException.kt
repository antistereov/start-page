package io.github.antistereov.start.global.exception

open class StartPageException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)