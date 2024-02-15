package io.github.antistereov.start.global.model.exception

class NoAccessTokenException(
    service: String,
    userId: String
) : RuntimeException(
    "No access token found for service: $service and user: $userId"
)