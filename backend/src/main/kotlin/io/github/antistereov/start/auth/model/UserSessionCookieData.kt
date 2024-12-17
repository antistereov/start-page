package io.github.antistereov.start.auth.model

data class UserSessionCookieData(
    val accessToken: String,
    val expiresIn: Long,
)
