package io.github.antistereov.start.connector.unsplash.exception

class UnsplashInvalidCallbackException : UnsplashException(
    message = "Invalid callback from Unsplash: no code or no state passed to callback"
)