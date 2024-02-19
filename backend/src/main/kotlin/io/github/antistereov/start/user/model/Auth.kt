package io.github.antistereov.start.user.model

import io.github.antistereov.start.widgets.auth.instagram.model.InstagramAuthDetails
import io.github.antistereov.start.widgets.auth.nextcloud.model.NextcloudAuthCredentials
import io.github.antistereov.start.widgets.auth.openai.model.OpenAIAuthDetails
import io.github.antistereov.start.widgets.auth.spotify.model.SpotifyAuthDetails
import io.github.antistereov.start.widgets.auth.todoist.model.TodoistAuthDetails
import io.github.antistereov.start.widgets.auth.unsplash.model.UnsplashAuthDetails

data class Auth(
    var spotify: SpotifyAuthDetails = SpotifyAuthDetails(),
    var todoist: TodoistAuthDetails = TodoistAuthDetails(),
    var unsplash: UnsplashAuthDetails = UnsplashAuthDetails(),
    var instagram: InstagramAuthDetails = InstagramAuthDetails(),
    var nextcloud: NextcloudAuthCredentials = NextcloudAuthCredentials(),
    var openAi: OpenAIAuthDetails = OpenAIAuthDetails(),
)