package io.github.antistereov.start.user.model

data class InstagramAuthDetails(
    var userId: String? = null,
    var username: String? = null,
    var avatar: String? = null,
    var accessToken: String? = null,
    var expirationDate: java.time.LocalDateTime? = null,
)
