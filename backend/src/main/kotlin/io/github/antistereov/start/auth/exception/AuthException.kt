package io.github.antistereov.start.auth.exception

import io.github.antistereov.start.global.exception.StartPageException

open class AuthException(
    message: String,
    cause: Throwable? = null
) : StartPageException(
    message,
    cause
)