package io.github.antistereov.start.global.exception

class ExpiredTokenException(
    service: String,
    userId: String,
    type: String = "access token"
) : RuntimeException(
    "Token of type: $type for service: $service and user: $userId expired."
)