package io.github.antistereov.start.widgets.auth.instagram.model

data class InstagramAuthDetails(
    var userId: String? = null,
    var username: String? = null,
    var accessToken: String? = null,
    var expirationDate: java.time.LocalDateTime? = null,
)
