package io.github.antistereov.start.model

class SpotifyAPIException(message: String) : RuntimeException("Error from Spotify API: $message")