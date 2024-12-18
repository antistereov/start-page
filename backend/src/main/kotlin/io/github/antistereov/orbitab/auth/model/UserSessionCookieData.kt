package io.github.antistereov.orbitab.auth.model

data class UserSessionCookieData(
    val accessToken: String,
    val expiresIn: Long,
)
