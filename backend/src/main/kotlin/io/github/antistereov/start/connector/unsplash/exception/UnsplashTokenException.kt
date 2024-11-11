package io.github.antistereov.start.connector.unsplash.exception

class UnsplashTokenException(
    message: String,
    cause: Throwable? = null,
) : UnsplashException(message, cause)