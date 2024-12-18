package io.github.antistereov.orbitab.connector.unsplash.exception

class UnsplashTokenException(
    message: String,
    cause: Throwable? = null,
) : UnsplashException(message, cause)