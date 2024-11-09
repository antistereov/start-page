package io.github.antistereov.start.widget.unsplash.exception

class UnsplashAccessTokenNotFoundException(
    userId: String
) : UnsplashException(message = "No Unsplash access token found for user $userId")