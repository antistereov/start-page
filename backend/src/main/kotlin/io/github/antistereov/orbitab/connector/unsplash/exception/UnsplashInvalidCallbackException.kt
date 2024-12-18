package io.github.antistereov.orbitab.connector.unsplash.exception

class UnsplashInvalidCallbackException : UnsplashException(
    message = "Invalid callback from Unsplash: no code or no state passed to callback"
)