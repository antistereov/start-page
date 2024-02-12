package io.github.antistereov.start.global.model

class SpotifyAPIException(message: String) : RuntimeException("Error from Spotify API: $message")