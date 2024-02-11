package io.github.antistereov.start.model

class NoRefreshTokenException(service: String, userId: String) : RuntimeException("No refresh token found for service: $service and user: $userId")