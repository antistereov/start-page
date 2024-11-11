package io.github.antistereov.start.widget.spotify.exception

class SpotifyAccessTokenNotFoundException(userId: String) : SpotifyException(
    message = "No Spotify access token found for user $userId"
)