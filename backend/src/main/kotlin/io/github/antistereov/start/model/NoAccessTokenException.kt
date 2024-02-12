package io.github.antistereov.start.model

class NoAccessTokenException(service: String, userId: String) : RuntimeException("No access token found for service: $service and user: $userId")