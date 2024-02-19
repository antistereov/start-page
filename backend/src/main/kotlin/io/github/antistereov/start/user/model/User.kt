package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.auth.instagram.model.InstagramAuthDetails
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudAuthDetails
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    var id: String = "",
    var spotify: SpotifyAuthDetails = SpotifyAuthDetails(),
    var todoist: TodoistAuthDetails = TodoistAuthDetails(),
    var unsplash: UnsplashAuthDetails = UnsplashAuthDetails(),
    var instagram: InstagramAuthDetails = InstagramAuthDetails(),
    var nextcloud: NextcloudAuthDetails = NextcloudAuthDetails(),
    var openAi: OpenAIAuthDetails = OpenAIAuthDetails(),
)
