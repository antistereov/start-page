package io.github.antistereov.start.user.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    var id: String = "",
    val spotify: SpotifyAuthDetails = SpotifyAuthDetails(),
    val todoist: TodoistAuthDetails = TodoistAuthDetails(),
    val unsplash: UnsplashAuthDetails = UnsplashAuthDetails(),
    val instagram: InstagramAuthDetails = InstagramAuthDetails(),
    val nextcloud: NextCloudAuthDetails = NextCloudAuthDetails(),
)
