package io.github.antistereov.start.auth.model

data class SessionCookieData(
    val accessToken: String,
    val expiresIn: Long,
)
