package io.github.antistereov.orbitab.auth.exception

import io.github.antistereov.orbitab.global.exception.StartPageException

open class AuthException(
    message: String,
    cause: Throwable? = null
) : StartPageException(
    message,
    cause
)